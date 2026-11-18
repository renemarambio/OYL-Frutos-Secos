package com.example.oylfrutossecos.data.remote.api

import com.example.oylfrutossecos.data.remote.dto.OrderDto
import com.example.oylfrutossecos.data.remote.dto.OrderRequestDto
import com.example.oylfrutossecos.data.remote.dto.StatusRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApiService {

    @GET("api/orders")
    suspend fun getOrders(
        @Query("customerEmail") customerEmail: String? = null
    ): Response<List<OrderDto>>

    @GET("api/orders/{id}")
    suspend fun getOrderById(
        @Path("id") id: Long
    ): Response<OrderDto>

    @POST("api/orders")
    suspend fun createOrder(
        @Body request: OrderRequestDto
    ): Response<OrderDto>

    @PATCH("api/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: Long,
        @Body request: StatusRequestDto
    ): Response<OrderDto>

    @DELETE("api/orders/{id}")
    suspend fun deleteOrder(
        @Path("id") id: Long
    ): Response<Unit>
}
