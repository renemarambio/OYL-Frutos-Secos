package com.example.oylfrutossecos.data.remote.api

import com.example.oylfrutossecos.data.remote.dto.IndicatorDto
import retrofit2.Response
import retrofit2.http.GET

interface IndicatorApiService {

    @GET("api/dolar")
    suspend fun getDolar(): Response<IndicatorDto>

    @GET("api/uf")
    suspend fun getUf(): Response<IndicatorDto>
}
