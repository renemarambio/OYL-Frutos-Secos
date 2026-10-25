package com.example.oylfrutossecos.data.repository

import android.content.Context
import com.example.oylfrutossecos.data.local.AppDatabaseHelper
import com.example.oylfrutossecos.data.model.CartItem
import com.example.oylfrutossecos.data.model.Sale
import com.example.oylfrutossecos.data.model.SaleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class SalesRepository(context: Context) {

    private val dbHelper = AppDatabaseHelper.getInstance(context)

    suspend fun getAllSales(): List<Sale> = withContext(Dispatchers.IO) {
        dbHelper.getAllSales()
    }

    suspend fun getSalesByCustomer(email: String): List<Sale> = withContext(Dispatchers.IO) {
        dbHelper.getSalesByCustomerEmail(email)
    }

    suspend fun createSale(
        cartItems: List<CartItem>,
        customerName: String,
        customerEmail: String,
        customerAddress: String,
        deliveryZone: String,
        deliveryFee: Double,
        paymentMethod: String,
        latitude: Double = -33.42628,
        longitude: Double = -70.61254
    ): Result<Sale> = withContext(Dispatchers.IO) {
        try {
            if (cartItems.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("El carrito de compras está vacío"))
            }

            val timestamp = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateFormatted = dateFormat.format(Date(timestamp))
            val orderNumber = "ORD-2026-${Random.nextInt(1000, 9999)}"

            val subtotal = cartItems.sumOf { it.subtotal }
            val total = subtotal + deliveryFee

            val sale = Sale(
                orderNumber = orderNumber,
                timestamp = timestamp,
                dateFormatted = dateFormatted,
                customerName = customerName,
                customerEmail = customerEmail,
                customerAddress = customerAddress,
                deliveryZone = deliveryZone,
                paymentMethod = paymentMethod,
                subtotal = subtotal,
                deliveryFee = deliveryFee,
                total = total,
                status = "Confirmado",
                latitude = latitude,
                longitude = longitude
            )

            val saleItems = cartItems.map { item ->
                SaleItem(
                    productId = item.product.id,
                    productName = item.product.name,
                    unitPrice = item.product.price,
                    quantity = item.quantity,
                    subtotal = item.subtotal
                )
            }

            val saleId = dbHelper.insertSaleWithItems(sale, saleItems)
            val completedSale = sale.copy(id = saleId, items = saleItems)

            // Vaciar carrito
            CartRepository.getInstance().clearCart()

            Result.success(completedSale)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
