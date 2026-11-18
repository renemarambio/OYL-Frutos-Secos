package com.example.oylfrutossecos.data.remote.api

import com.example.oylfrutossecos.data.remote.dto.CategoryDto
import com.example.oylfrutossecos.data.remote.dto.ProductDto
import com.example.oylfrutossecos.data.remote.dto.ProductRequestDto
import com.example.oylfrutossecos.data.remote.dto.StockRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApiService {

    @GET("api/products")
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("q") query: String? = null
    ): Response<List<ProductDto>>

    @GET("api/products/{id}")
    suspend fun getProductById(
        @Path("id") id: Long
    ): Response<ProductDto>

    @GET("api/products/barcode/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): Response<ProductDto>

    @GET("api/products/categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @POST("api/products")
    suspend fun createProduct(
        @Body request: ProductRequestDto
    ): Response<ProductDto>

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Long,
        @Body request: ProductRequestDto
    ): Response<ProductDto>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: Long
    ): Response<Unit>

    @PATCH("api/products/{id}/stock")
    suspend fun updateStock(
        @Path("id") id: Long,
        @Body request: StockRequestDto
    ): Response<ProductDto>
}
