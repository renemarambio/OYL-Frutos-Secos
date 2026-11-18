package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.remote.RetrofitClient
import com.example.oylfrutossecos.data.remote.api.IndicatorApiService
import com.example.oylfrutossecos.data.remote.dto.IndicatorDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para la API externa de mindicador.cl.
 * Admite inyección por constructor para pruebas unitarias con MockK.
 */
class IndicatorRepository(
    val api: IndicatorApiService = RetrofitClient.indicatorApiService
) {

    suspend fun getDolar(): Result<IndicatorDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDolar()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener valor del dólar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUf(): Result<IndicatorDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUf()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener valor de la UF: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
