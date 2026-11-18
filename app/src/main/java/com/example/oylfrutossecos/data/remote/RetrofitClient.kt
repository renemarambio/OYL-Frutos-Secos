package com.example.oylfrutossecos.data.remote

import com.example.oylfrutossecos.BuildConfig
import com.example.oylfrutossecos.data.remote.api.IndicatorApiService
import com.example.oylfrutossecos.data.remote.api.OrderApiService
import com.example.oylfrutossecos.data.remote.api.ProductApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val TIMEOUT_SECONDS = 15L

    val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val productsRetrofit: Retrofit by lazy {
        createRetrofit(BuildConfig.PRODUCTS_BASE_URL)
    }

    val ordersRetrofit: Retrofit by lazy {
        createRetrofit(BuildConfig.ORDERS_BASE_URL)
    }

    val indicatorsRetrofit: Retrofit by lazy {
        createRetrofit(BuildConfig.INDICATORS_BASE_URL)
    }

    val productApiService: ProductApiService by lazy {
        productsRetrofit.create(ProductApiService::class.java)
    }

    val orderApiService: OrderApiService by lazy {
        ordersRetrofit.create(OrderApiService::class.java)
    }

    val indicatorApiService: IndicatorApiService by lazy {
        indicatorsRetrofit.create(IndicatorApiService::class.java)
    }
}
