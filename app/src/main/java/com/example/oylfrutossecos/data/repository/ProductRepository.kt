package com.example.oylfrutossecos.data.repository

import android.content.Context
import com.example.oylfrutossecos.data.local.AppDatabaseHelper
import com.example.oylfrutossecos.data.local.MockDataProvider
import com.example.oylfrutossecos.data.model.Category
import com.example.oylfrutossecos.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository(context: Context) {

    private val dbHelper = AppDatabaseHelper.getInstance(context)

    suspend fun getAllProducts(): List<Product> = withContext(Dispatchers.IO) {
        dbHelper.getAllProducts()
    }

    suspend fun getProductById(id: Long): Product? = withContext(Dispatchers.IO) {
        dbHelper.getProductById(id)
    }

    suspend fun getProductByBarcode(barcode: String): Product? = withContext(Dispatchers.IO) {
        dbHelper.getProductByBarcode(barcode)
    }

    suspend fun getProductByCode(code: String): Product? = withContext(Dispatchers.IO) {
        dbHelper.getProductByCodeOrSku(code)
    }

    suspend fun searchProducts(query: String): List<Product> = withContext(Dispatchers.IO) {
        if (query.isBlank()) dbHelper.getAllProducts() else dbHelper.searchProducts(query)
    }

    suspend fun getProductsByCategory(category: String): List<Product> = withContext(Dispatchers.IO) {
        if (category.equals("Todos", ignoreCase = true)) {
            dbHelper.getAllProducts()
        } else {
            dbHelper.getProductsByCategory(category)
        }
    }

    suspend fun saveProduct(product: Product): Long = withContext(Dispatchers.IO) {
        if (product.id > 0) {
            dbHelper.updateProduct(product)
            product.id
        } else {
            dbHelper.insertProduct(product)
        }
    }

    suspend fun deleteProduct(id: Long): Boolean = withContext(Dispatchers.IO) {
        dbHelper.deleteProduct(id) > 0
    }

    suspend fun getCategories(): List<Category> = withContext(Dispatchers.IO) {
        val products = dbHelper.getAllProducts()
        MockDataProvider.categories.map { category ->
            val count = products.count { it.category.equals(category.name, ignoreCase = true) }
            category.copy(productCount = count)
        }
    }

    suspend fun resetDefaults() = withContext(Dispatchers.IO) {
        dbHelper.resetDatabaseToDefaults()
    }
}
