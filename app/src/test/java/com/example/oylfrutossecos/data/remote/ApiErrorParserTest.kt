package com.example.oylfrutossecos.data.remote

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class ApiErrorParserTest {

    @Test
    fun parse_conMapaDeErroresPorCampo_retornaErroresConcatenados() {
        val json = "{\"status\":400,\"message\":\"Validation failed\",\"errors\":{\"name\":\"El nombre es obligatorio\",\"price\":\"El precio debe ser positivo\"}}"
        val body = json.toResponseBody("application/json".toMediaTypeOrNull())
        val response = Response.error<Unit>(400, body)

        val parsed = ApiErrorParser.parse(response)

        assertEquals("El nombre es obligatorio. El precio debe ser positivo", parsed)
    }

    @Test
    fun parse_conMensajeSimple_retornaMensaje() {
        val json = "{\"status\":409,\"message\":\"Stock insuficiente para el producto\"}"
        val body = json.toResponseBody("application/json".toMediaTypeOrNull())
        val response = Response.error<Unit>(409, body)

        val parsed = ApiErrorParser.parse(response)

        assertEquals("Stock insuficiente para el producto", parsed)
    }

    @Test
    fun parse_sinBody_retornaMensajePorDefectoSegunCodigoHttp() {
        val response = Response.error<Unit>(404, "".toResponseBody("text/plain".toMediaTypeOrNull()))

        val parsed = ApiErrorParser.parse(response)

        assertEquals("Recurso no encontrado", parsed)
    }

    @Test
    fun defaultMessageForCode_cubreTodosLosCasos() {
        assertEquals("Datos inválidos", ApiErrorParser.defaultMessageForCode(400))
        assertEquals("Recurso no encontrado", ApiErrorParser.defaultMessageForCode(404))
        assertEquals("Conflicto con las reglas de negocio", ApiErrorParser.defaultMessageForCode(409))
        assertEquals("Servidor de productos no disponible (503)", ApiErrorParser.defaultMessageForCode(503))
        assertEquals("Error en el servidor (500)", ApiErrorParser.defaultMessageForCode(500))
    }
}
