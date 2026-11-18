package com.example.oylfrutossecos.data.local

import com.example.oylfrutossecos.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Fuente de datos local para la caché y operaciones offline de productos.
 */
interface ProductLocalDataSource {
    suspend fun getAllProducts(): List<Product>
    suspend fun getProductById(id: Long): Product?
    suspend fun getProductByBarcode(barcode: String): Product?
    suspend fun getProductByCodeOrSku(code: String): Product?
    suspend fun getProductsByCategory(category: String): List<Product>
    suspend fun searchProducts(query: String): List<Product>
    suspend fun saveProduct(product: Product): Long
    suspend fun deleteProduct(id: Long): Boolean
    suspend fun updateStock(id: Long, newStock: Int): Boolean
    suspend fun updatePrice(id: Long, newPrice: Double): Boolean
    suspend fun replaceCache(products: List<Product>)
    suspend fun resetDefaults()
}

/**
 * Implementación de [ProductLocalDataSource] delegada en [AppDatabaseHelper].
 */
class SqliteProductLocalDataSource(
    private val dbHelper: AppDatabaseHelper
) : ProductLocalDataSource {

    override suspend fun getAllProducts(): List<Product> = withContext(Dispatchers.IO) {
        dbHelper.getAllProducts()
    }

    override suspend fun getProductById(id: Long): Product? = withContext(Dispatchers.IO) {
        dbHelper.getProductById(id)
    }

    override suspend fun getProductByBarcode(barcode: String): Product? = withContext(Dispatchers.IO) {
        dbHelper.getProductByBarcode(barcode)
    }

    override suspend fun getProductByCodeOrSku(code: String): Product? = withContext(Dispatchers.IO) {
        dbHelper.getProductByCodeOrSku(code)
    }

    override suspend fun getProductsByCategory(category: String): List<Product> = withContext(Dispatchers.IO) {
        if (category.equals("Todos", ignoreCase = true)) {
            dbHelper.getAllProducts()
        } else {
            dbHelper.getProductsByCategory(category)
        }
    }

    override suspend fun searchProducts(query: String): List<Product> = withContext(Dispatchers.IO) {
        if (query.isBlank()) dbHelper.getAllProducts() else dbHelper.searchProducts(query)
    }

    override suspend fun saveProduct(product: Product): Long = withContext(Dispatchers.IO) {
        dbHelper.insertOrUpdateProduct(product)
    }

    override suspend fun deleteProduct(id: Long): Boolean = withContext(Dispatchers.IO) {
        dbHelper.deleteProduct(id) > 0
    }

    override suspend fun updateStock(id: Long, newStock: Int): Boolean = withContext(Dispatchers.IO) {
        dbHelper.updateProductStock(id, newStock) > 0
    }

    override suspend fun updatePrice(id: Long, newPrice: Double): Boolean = withContext(Dispatchers.IO) {
        dbHelper.updateProductPrice(id, newPrice) > 0
    }

    override suspend fun replaceCache(products: List<Product>): Unit = withContext(Dispatchers.IO) {
        dbHelper.replaceProductsCache(products)
    }

    override suspend fun resetDefaults(): Unit = withContext(Dispatchers.IO) {
        dbHelper.resetDatabaseToDefaults()
    }
}
