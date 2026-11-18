package com.example.oylfrutossecos.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.oylfrutossecos.data.ServiceLocator
import com.example.oylfrutossecos.data.local.ImageStorage
import com.example.oylfrutossecos.data.model.Category
import com.example.oylfrutossecos.data.model.Product
import com.example.oylfrutossecos.data.repository.ProductRepository
import com.example.oylfrutossecos.domain.validation.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProductFormState(
    val id: Long = 0,
    val name: String = "",
    val category: String = "Frutos Secos",
    val price: String = "",
    val stock: String = "",
    val unit: String = "kg",
    val description: String = "",
    val ingredients: String = "",
    val origin: String = "Chile",
    val barcode: String = "",
    val imageUrl: String = "",
    val nameError: String? = null,
    val priceError: String? = null,
    val stockError: String? = null,
    val barcodeError: String? = null,
    val isSaving: Boolean = false
)

class ProductViewModel(
    application: Application,
    private val repository: ProductRepository = ServiceLocator.provideProductRepository(application)
) : AndroidViewModel(application) {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todos")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _offlineMessage = MutableStateFlow<String?>(null)
    val offlineMessage: StateFlow<String?> = _offlineMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _formState = MutableStateFlow(ProductFormState())
    val formState: StateFlow<ProductFormState> = _formState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _products.value = repository.getAllProducts()
            _categories.value = repository.getCategories()
            updateOfflineStatus()
            _isLoading.value = false
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        filterProducts()
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
        filterProducts()
    }

    private fun filterProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            val currentCategory = _selectedCategory.value
            val currentQuery = _searchQuery.value

            val baseList = if (currentCategory == "Todos") {
                repository.getAllProducts()
            } else {
                repository.getProductsByCategory(currentCategory)
            }

            _products.value = if (currentQuery.isBlank()) {
                baseList
            } else {
                baseList.filter {
                    it.name.contains(currentQuery, ignoreCase = true) ||
                            it.ingredients.contains(currentQuery, ignoreCase = true) ||
                            it.barcode.contains(currentQuery, ignoreCase = true)
                }
            }
            updateOfflineStatus()
            _isLoading.value = false
        }
    }

    private fun updateOfflineStatus() {
        _isOffline.value = repository.isOffline
        _offlineMessage.value = if (repository.isOffline) {
            "Sin conexión con el servidor — mostrando datos guardados"
        } else {
            null
        }
    }

    fun loadProductById(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val prod = repository.getProductById(id)
            _selectedProduct.value = prod
            updateOfflineStatus()
            if (prod != null) {
                _formState.value = ProductFormState(
                    id = prod.id,
                    name = prod.name,
                    category = prod.category,
                    price = prod.price.toInt().toString(),
                    stock = prod.stock.toString(),
                    unit = prod.unit,
                    description = prod.description,
                    ingredients = prod.ingredients,
                    origin = prod.origin,
                    barcode = prod.barcode,
                    imageUrl = prod.imageUrl
                )
            } else {
                resetForm()
            }
            _isLoading.value = false
        }
    }

    fun resetForm() {
        _formState.value = ProductFormState()
    }

    fun onFormFieldChanged(
        name: String? = null,
        category: String? = null,
        price: String? = null,
        stock: String? = null,
        unit: String? = null,
        description: String? = null,
        ingredients: String? = null,
        origin: String? = null,
        barcode: String? = null,
        imageUrl: String? = null
    ) {
        _formState.update { current ->
            current.copy(
                name = name ?: current.name,
                category = category ?: current.category,
                price = price ?: current.price,
                stock = stock ?: current.stock,
                unit = unit ?: current.unit,
                description = description ?: current.description,
                ingredients = ingredients ?: current.ingredients,
                origin = origin ?: current.origin,
                barcode = barcode ?: current.barcode,
                imageUrl = imageUrl ?: current.imageUrl,
                nameError = if (name != null) null else current.nameError,
                priceError = if (price != null) null else current.priceError,
                stockError = if (stock != null) null else current.stockError,
                barcodeError = if (barcode != null) null else current.barcodeError
            )
        }
    }

    fun onPhotoCaptured(bitmap: Bitmap, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            val path = withContext(Dispatchers.IO) {
                ImageStorage.saveBitmap(getApplication(), bitmap)
            }
            _formState.update { it.copy(imageUrl = path) }
            onSaved()
        }
    }

    fun saveProduct(
        onSuccess: (Long) -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        val state = _formState.value
        val nameErr = Validators.validateProductName(state.name)
        val priceErr = Validators.validatePrice(state.price)
        val stockErr = Validators.validateStock(state.stock)
        val barcodeErr = Validators.validateBarcode(state.barcode)

        if (listOf(nameErr, priceErr, stockErr, barcodeErr).any { it != null }) {
            _formState.update {
                it.copy(nameError = nameErr, priceError = priceErr, stockError = stockErr, barcodeError = barcodeErr)
            }
            return
        }

        if (_isOffline.value) {
            val msg = "Sin conexión con el servidor — mostrando datos guardados. No se puede crear ni editar productos."
            _errorMessage.value = msg
            onError?.invoke(msg)
            return
        }

        val priceVal = state.price.trim().toDouble()
        val stockVal = state.stock.trim().toInt()

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            val productToSave = Product(
                id = state.id,
                name = state.name.trim(),
                category = state.category,
                price = priceVal,
                stock = stockVal,
                unit = state.unit.ifBlank { "kg" },
                description = state.description.trim(),
                ingredients = state.ingredients.trim(),
                origin = state.origin.trim().ifBlank { "Chile" },
                barcode = state.barcode.trim().ifBlank { "780${System.currentTimeMillis().toString().takeLast(9)}" },
                imageUrl = state.imageUrl
            )

            val result = repository.saveProduct(productToSave)
            result.onSuccess { savedId ->
                loadData()
                _formState.update { it.copy(isSaving = false) }
                onSuccess(savedId)
            }.onFailure { ex ->
                _formState.update { it.copy(isSaving = false) }
                val msg = ex.message ?: "Error al guardar el producto"
                _errorMessage.value = msg
                onError?.invoke(msg)
            }
        }
    }

    fun deleteProduct(
        id: Long,
        onSuccess: () -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        if (_isOffline.value) {
            val msg = "Sin conexión con el servidor — mostrando datos guardados. No se puede eliminar en modo sin conexión."
            _errorMessage.value = msg
            onError?.invoke(msg)
            return
        }

        viewModelScope.launch {
            val imagePath = _formState.value.imageUrl
            val result = repository.deleteProduct(id)
            result.onSuccess {
                withContext(Dispatchers.IO) { ImageStorage.deleteImage(imagePath) }
                loadData()
                onSuccess()
            }.onFailure { ex ->
                val msg = ex.message ?: "Error al eliminar producto"
                _errorMessage.value = msg
                onError?.invoke(msg)
            }
        }
    }

    fun resetToDefaults(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.resetDefaults()
            loadData()
            onComplete()
        }
    }

    fun findProductByCode(code: String, onResult: (Product?) -> Unit) {
        viewModelScope.launch {
            val product = repository.getProductByCode(code)
            updateOfflineStatus()
            onResult(product)
        }
    }

    fun adjustStock(productId: Long, delta: Int, onResult: (Boolean) -> Unit = {}) {
        if (_isOffline.value) {
            onResult(false)
            return
        }
        viewModelScope.launch {
            val currentList = _products.value
            val target = currentList.find { it.id == productId } ?: return@launch
            val newStock = (target.stock + delta).coerceAtLeast(0)
            if (newStock == target.stock) return@launch

            _products.update { list ->
                list.map { if (it.id == productId) it.copy(stock = newStock) else it }
            }

            val result = repository.updateStock(productId, newStock)
            result.onSuccess {
                onResult(true)
            }.onFailure {
                _products.update { list ->
                    list.map { if (it.id == productId) it.copy(stock = target.stock) else it }
                }
                onResult(false)
            }
        }
    }

    fun updateStockDirect(productId: Long, newStock: Int, onResult: (Boolean) -> Unit = {}) {
        if (_isOffline.value) {
            onResult(false)
            return
        }
        viewModelScope.launch {
            val validStock = newStock.coerceAtLeast(0)
            val currentList = _products.value
            val target = currentList.find { it.id == productId } ?: return@launch
            if (validStock == target.stock) {
                onResult(true)
                return@launch
            }

            _products.update { list ->
                list.map { if (it.id == productId) it.copy(stock = validStock) else it }
            }

            val result = repository.updateStock(productId, validStock)
            result.onSuccess {
                onResult(true)
            }.onFailure {
                _products.update { list ->
                    list.map { if (it.id == productId) it.copy(stock = target.stock) else it }
                }
                onResult(false)
            }
        }
    }

    fun updatePriceDirect(productId: Long, newPrice: Double, onResult: (Boolean) -> Unit = {}) {
        if (_isOffline.value) {
            onResult(false)
            return
        }
        viewModelScope.launch {
            if (newPrice <= 0) return@launch
            val currentList = _products.value
            val target = currentList.find { it.id == productId } ?: return@launch
            if (newPrice == target.price) {
                onResult(true)
                return@launch
            }

            _products.update { list ->
                list.map { if (it.id == productId) it.copy(price = newPrice) else it }
            }

            val result = repository.updatePrice(productId, newPrice)
            result.onSuccess {
                onResult(true)
            }.onFailure {
                _products.update { list ->
                    list.map { if (it.id == productId) it.copy(price = target.price) else it }
                }
                onResult(false)
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
