package com.example.oylfrutossecos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.oylfrutossecos.data.model.Category
import com.example.oylfrutossecos.data.model.Product
import com.example.oylfrutossecos.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val isSaving: Boolean = false
)

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProductRepository(application)

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

    private val _formState = MutableStateFlow(ProductFormState())
    val formState: StateFlow<ProductFormState> = _formState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _products.value = repository.getAllProducts()
            _categories.value = repository.getCategories()
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
            _isLoading.value = false
        }
    }

    fun loadProductById(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val prod = repository.getProductById(id)
            _selectedProduct.value = prod
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
                stockError = if (stock != null) null else current.stockError
            )
        }
    }

    fun saveProduct(onSuccess: (Long) -> Unit) {
        val state = _formState.value
        var hasError = false
        var nameErr: String? = null
        var priceErr: String? = null
        var stockErr: String? = null

        if (state.name.isBlank()) {
            nameErr = "El nombre del producto es obligatorio"
            hasError = true
        }

        val priceVal = state.price.toDoubleOrNull()
        if (priceVal == null || priceVal <= 0) {
            priceErr = "Ingresa un precio válido mayor a 0"
            hasError = true
        }

        val stockVal = state.stock.toIntOrNull()
        if (stockVal == null || stockVal < 0) {
            stockErr = "Ingresa una cantidad de stock válida"
            hasError = true
        }

        if (hasError) {
            _formState.update { it.copy(nameError = nameErr, priceError = priceErr, stockError = stockErr) }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            val productToSave = Product(
                id = state.id,
                name = state.name.trim(),
                category = state.category,
                price = priceVal ?: 0.0,
                stock = stockVal ?: 0,
                unit = state.unit.ifBlank { "kg" },
                description = state.description.trim(),
                ingredients = state.ingredients.trim(),
                origin = state.origin.trim().ifBlank { "Chile" },
                barcode = state.barcode.trim().ifBlank { "780${System.currentTimeMillis().toString().takeLast(9)}" },
                imageUrl = state.imageUrl
            )

            val savedId = repository.saveProduct(productToSave)
            loadData()
            _formState.update { it.copy(isSaving = false) }
            onSuccess(savedId)
        }
    }

    fun deleteProduct(id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteProduct(id)
            loadData()
            onSuccess()
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
            onResult(product)
        }
    }
}
