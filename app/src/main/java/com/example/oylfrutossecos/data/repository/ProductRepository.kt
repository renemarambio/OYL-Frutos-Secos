package com.example.oylfrutossecos.data.repository

import android.content.Context
import com.example.oylfrutossecos.data.local.AppDatabaseHelper
import com.example.oylfrutossecos.data.local.MockDataProvider
import com.example.oylfrutossecos.data.local.ProductLocalDataSource
import com.example.oylfrutossecos.data.local.SqliteProductLocalDataSource
import com.example.oylfrutossecos.data.model.Category
import com.example.oylfrutossecos.data.model.Product
import com.example.oylfrutossecos.data.remote.ApiErrorParser
import com.example.oylfrutossecos.data.remote.RetrofitClient
import com.example.oylfrutossecos.data.remote.api.ProductApiService
import com.example.oylfrutossecos.data.remote.dto.StockRequestDto
import com.example.oylfrutossecos.data.remote.mapper.toDomain
import com.example.oylfrutossecos.data.remote.mapper.toRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio de Productos conectado a ms-productos con SQLite como caché local.
 * Admite inyección de dependencias por constructor para pruebas unitarias con MockK.
 */
class ProductRepository(
    val api: ProductApiService,
    val local: ProductLocalDataSource
) {

    /** Constructor secundario para compatibilidad y uso general con Context */
    constructor(context: Context) : this(
        api = RetrofitClient.productApiService,
        local = SqliteProductLocalDataSource(AppDatabaseHelper.getInstance(context))
    )

    /** Indica si la última consulta falló por falta de conexión o 503 */
    var isOffline: Boolean = false
        private set

    /** Último mensaje de error devuelto por la API o red */
    var lastError: String? = null
        private set

    suspend fun getAllProducts(): List<Product> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProducts()
            if (response.isSuccessful && response.body() != null) {
                val products = response.body()!!.map { it.toDomain() }
                local.replaceCache(products)
                isOffline = false
                lastError = null
                products
            } else {
                isOffline = true
                lastError = ApiErrorParser.parse(response)
                local.getAllProducts()
            }
        } catch (e: Exception) {
            isOffline = true
            lastError = e.message ?: "Sin conexión"
            local.getAllProducts()
        }
    }

    suspend fun getProductById(id: Long): Product? = withContext(Dispatchers.IO) {
        try {
            val response = api.getProductById(id)
            if (response.isSuccessful && response.body() != null) {
                val prod = response.body()!!.toDomain()
                local.saveProduct(prod)
                isOffline = false
                lastError = null
                prod
            } else if (response.code() == 404) {
                null
            } else {
                isOffline = true
                lastError = ApiErrorParser.parse(response)
                local.getProductById(id)
            }
        } catch (e: Exception) {
            isOffline = true
            lastError = e.message ?: "Sin conexión"
            local.getProductById(id)
        }
    }

    suspend fun getProductByBarcode(barcode: String): Product? = withContext(Dispatchers.IO) {
        try {
            val response = api.getProductByBarcode(barcode)
            if (response.isSuccessful && response.body() != null) {
                val prod = response.body()!!.toDomain()
                local.saveProduct(prod)
                isOffline = false
                lastError = null
                prod
            } else if (response.code() == 404) {
                null
            } else {
                isOffline = true
                lastError = ApiErrorParser.parse(response)
                local.getProductByBarcode(barcode)
            }
        } catch (e: Exception) {
            isOffline = true
            lastError = e.message ?: "Sin conexión"
            local.getProductByBarcode(barcode)
        }
    }

    suspend fun getProductByCode(code: String): Product? = withContext(Dispatchers.IO) {
        val cleanCode = code.trim()
        val byBarcode = getProductByBarcode(cleanCode)
        if (byBarcode != null) return@withContext byBarcode

        val numericId = cleanCode.filter { it.isDigit() }.toLongOrNull()
        if (numericId != null) {
            val byId = getProductById(numericId)
            if (byId != null) return@withContext byId
        }

        val results = searchProducts(cleanCode)
        results.firstOrNull() ?: local.getProductByCodeOrSku(cleanCode)
    }

    suspend fun searchProducts(query: String): List<Product> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext getAllProducts()
        try {
            val response = api.getProducts(query = query)
            if (response.isSuccessful && response.body() != null) {
                isOffline = false
                lastError = null
                response.body()!!.map { it.toDomain() }
            } else {
                isOffline = true
                lastError = ApiErrorParser.parse(response)
                local.searchProducts(query)
            }
        } catch (e: Exception) {
            isOffline = true
            lastError = e.message ?: "Sin conexión"
            local.searchProducts(query)
        }
    }

    suspend fun getProductsByCategory(category: String): List<Product> = withContext(Dispatchers.IO) {
        if (category.equals("Todos", ignoreCase = true)) return@withContext getAllProducts()
        try {
            val response = api.getProducts(category = category)
            if (response.isSuccessful && response.body() != null) {
                isOffline = false
                lastError = null
                response.body()!!.map { it.toDomain() }
            } else {
                isOffline = true
                lastError = ApiErrorParser.parse(response)
                local.getProductsByCategory(category)
            }
        } catch (e: Exception) {
            isOffline = true
            lastError = e.message ?: "Sin conexión"
            local.getProductsByCategory(category)
        }
    }

    suspend fun getCategories(): List<Category> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCategories()
            if (response.isSuccessful && response.body() != null) {
                isOffline = false
                lastError = null
                val apiCategories = response.body()!!
                MockDataProvider.categories.map { cat ->
                    val found = apiCategories.find { it.name.equals(cat.name, ignoreCase = true) }
                    if (found != null) {
                        cat.copy(productCount = found.productCount.toInt())
                    } else {
                        cat.copy(productCount = 0)
                    }
                }
            } else {
                isOffline = true
                lastError = ApiErrorParser.parse(response)
                val localProducts = local.getAllProducts()
                MockDataProvider.categories.map { category ->
                    val count = localProducts.count { it.category.equals(category.name, ignoreCase = true) }
                    category.copy(productCount = count)
                }
            }
        } catch (e: Exception) {
            isOffline = true
            lastError = e.message ?: "Sin conexión"
            val localProducts = local.getAllProducts()
            MockDataProvider.categories.map { category ->
                val count = localProducts.count { it.category.equals(category.name, ignoreCase = true) }
                category.copy(productCount = count)
            }
        }
    }

    suspend fun saveProduct(product: Product): Result<Long> = withContext(Dispatchers.IO) {
        if (isOffline) {
            return@withContext Result.failure(
                IllegalStateException("Sin conexión con el servidor — mostrando datos guardados. No se puede crear ni editar.")
            )
        }
        try {
            val request = product.toRequestDto()
            val response = if (product.id > 0) {
                api.updateProduct(product.id, request)
            } else {
                api.createProduct(request)
            }
            if (response.isSuccessful && response.body() != null) {
                val saved = response.body()!!.toDomain()
                local.saveProduct(saved)
                Result.success(saved.id)
            } else {
                val msg = ApiErrorParser.parse(response)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            isOffline = true
            Result.failure(Exception(e.message ?: "Sin conexión con el servidor al guardar producto"))
        }
    }

    suspend fun deleteProduct(id: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        if (isOffline) {
            return@withContext Result.failure(
                IllegalStateException("Sin conexión con el servidor — mostrando datos guardados. No se puede eliminar.")
            )
        }
        try {
            val response = api.deleteProduct(id)
            if (response.isSuccessful) {
                local.deleteProduct(id)
                Result.success(true)
            } else {
                val msg = ApiErrorParser.parse(response)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            isOffline = true
            Result.failure(Exception(e.message ?: "Sin conexión con el servidor al eliminar producto"))
        }
    }

    suspend fun updateStock(id: Long, newStock: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        if (isOffline) {
            return@withContext Result.failure(
                IllegalStateException("Sin conexión con el servidor — mostrando datos guardados. No se puede actualizar stock.")
            )
        }
        try {
            val response = api.updateStock(id, StockRequestDto(stock = newStock))
            if (response.isSuccessful && response.body() != null) {
                val updated = response.body()!!.toDomain()
                local.saveProduct(updated)
                Result.success(true)
            } else {
                val msg = ApiErrorParser.parse(response)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            isOffline = true
            Result.failure(Exception(e.message ?: "Sin conexión con el servidor al actualizar stock"))
        }
    }

    suspend fun updatePrice(id: Long, newPrice: Double): Result<Boolean> = withContext(Dispatchers.IO) {
        val prod = getProductById(id) ?: return@withContext Result.failure(Exception("Producto no encontrado"))
        saveProduct(prod.copy(price = newPrice)).map { true }
    }

    suspend fun resetDefaults() = withContext(Dispatchers.IO) {
        local.resetDefaults()
    }
}
