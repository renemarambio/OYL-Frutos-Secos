package com.example.oylfrutossecos.data.repository

import android.content.Context
import com.example.oylfrutossecos.data.model.CartItem
import com.example.oylfrutossecos.data.model.Sale
import com.example.oylfrutossecos.data.remote.ApiErrorParser
import com.example.oylfrutossecos.data.remote.RetrofitClient
import com.example.oylfrutossecos.data.remote.api.OrderApiService
import com.example.oylfrutossecos.data.remote.dto.OrderItemRequestDto
import com.example.oylfrutossecos.data.remote.dto.OrderRequestDto
import com.example.oylfrutossecos.data.remote.dto.StatusRequestDto
import com.example.oylfrutossecos.data.remote.mapper.mapReadableStatusToBackend
import com.example.oylfrutossecos.data.remote.mapper.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio de Ventas y Pedidos conectado directamente a ms-pedidos.
 * Admite inyección por constructor para pruebas unitarias con MockK.
 */
class SalesRepository(
    val api: OrderApiService = RetrofitClient.orderApiService
) {

    /** Constructor secundario para compatibilidad */
    constructor(context: Context) : this(RetrofitClient.orderApiService)

    var lastError: String? = null
        private set

    suspend fun getAllSales(): List<Sale> = withContext(Dispatchers.IO) {
        try {
            val response = api.getOrders()
            if (response.isSuccessful && response.body() != null) {
                lastError = null
                response.body()!!.map { it.toDomain() }
            } else {
                lastError = ApiErrorParser.parse(response)
                emptyList()
            }
        } catch (e: Exception) {
            lastError = e.message ?: "Sin conexión con el servicio de pedidos"
            emptyList()
        }
    }

    suspend fun getSalesByCustomer(email: String): List<Sale> = withContext(Dispatchers.IO) {
        if (email.isBlank()) return@withContext emptyList()
        try {
            val response = api.getOrders(customerEmail = email.trim().lowercase())
            if (response.isSuccessful && response.body() != null) {
                lastError = null
                response.body()!!.map { it.toDomain() }
            } else {
                lastError = ApiErrorParser.parse(response)
                emptyList()
            }
        } catch (e: Exception) {
            lastError = e.message ?: "Sin conexión con el servicio de pedidos"
            emptyList()
        }
    }

    suspend fun getSaleById(id: Long): Sale? = withContext(Dispatchers.IO) {
        try {
            val response = api.getOrderById(id)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
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

            val itemsDto = cartItems.map { item ->
                OrderItemRequestDto(
                    productId = item.product.id,
                    quantity = item.quantity
                )
            }

            val requestDto = OrderRequestDto(
                customerName = customerName,
                customerEmail = customerEmail,
                customerAddress = customerAddress,
                deliveryZone = deliveryZone,
                deliveryFee = deliveryFee,
                paymentMethod = paymentMethod,
                latitude = latitude,
                longitude = longitude,
                items = itemsDto
            )

            val response = api.createOrder(requestDto)
            if (response.isSuccessful && response.body() != null) {
                val completedSale = response.body()!!.toDomain()
                // Vaciar carrito tras compra exitosa
                CartRepository.getInstance().clearCart()
                Result.success(completedSale)
            } else {
                val errorMsg = ApiErrorParser.parse(response)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Sin conexión con el servicio de pedidos. Intenta nuevamente."))
        }
    }

    suspend fun updateStatus(id: Long, readableStatus: String): Result<Sale> = withContext(Dispatchers.IO) {
        try {
            val backendStatus = mapReadableStatusToBackend(readableStatus)
            val response = api.updateOrderStatus(id, StatusRequestDto(status = backendStatus))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                val errorMsg = ApiErrorParser.parse(response)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Sin conexión con el servicio de pedidos"))
        }
    }

    suspend fun deleteSale(id: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteOrder(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val errorMsg = ApiErrorParser.parse(response)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Sin conexión con el servicio de pedidos"))
        }
    }
}
