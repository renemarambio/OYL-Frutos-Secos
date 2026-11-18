package com.example.oylfrutossecos.data.remote

import com.example.oylfrutossecos.data.remote.dto.ApiErrorResponse
import com.google.gson.Gson
import retrofit2.Response

object ApiErrorParser {
    private val gson = Gson()

    fun parse(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                val error = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                if (!error.errors.isNullOrEmpty()) {
                    return error.errors.values.joinToString(". ")
                }
                if (!error.message.isNullOrBlank()) {
                    return error.message
                }
            }
            defaultMessageForCode(response.code())
        } catch (_: Exception) {
            defaultMessageForCode(response.code())
        }
    }

    fun defaultMessageForCode(code: Int): String {
        return when (code) {
            400 -> "Datos inválidos"
            404 -> "Recurso no encontrado"
            409 -> "Conflicto con las reglas de negocio"
            503 -> "Servidor de productos no disponible (503)"
            else -> "Error en el servidor ($code)"
        }
    }
}
