package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.remote.api.IndicatorApiService
import com.example.oylfrutossecos.data.remote.dto.IndicatorDto
import com.example.oylfrutossecos.data.remote.dto.IndicatorSerieDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class IndicatorRepositoryTest {

    private val api: IndicatorApiService = mockk()
    private lateinit var repository: IndicatorRepository

    private val sampleDolarDto = IndicatorDto(
        version = "1.7.0",
        autor = "mindicador.cl",
        codigo = "dolar",
        nombre = "Dólar observado",
        unidadMedida = "Pesos",
        serie = listOf(
            IndicatorSerieDto(fecha = "2026-11-18T03:00:00.000Z", valor = 975.50)
        )
    )

    private val sampleUfDto = IndicatorDto(
        version = "1.7.0",
        autor = "mindicador.cl",
        codigo = "uf",
        nombre = "Unidad de fomento (UF)",
        unidadMedida = "Pesos",
        serie = listOf(
            IndicatorSerieDto(fecha = "2026-11-18T03:00:00.000Z", valor = 38500.25)
        )
    )

    @Before
    fun setUp() {
        repository = IndicatorRepository(api = api)
    }

    @Test
    fun getDolar_exito_retornaDtoConValorActual() = runTest {
        coEvery { api.getDolar() } returns Response.success(sampleDolarDto)

        val result = repository.getDolar()

        assertTrue(result.isSuccess)
        val dto = result.getOrNull()
        assertNotNull(dto)
        assertEquals("dolar", dto?.codigo)
        assertEquals(975.50, dto?.valorActual ?: 0.0, 0.001)
        assertEquals("2026-11-18T03:00:00.000Z", dto?.fechaActual)
    }

    @Test
    fun getDolar_errorHttp_retornaFailure() = runTest {
        val errorBody = "Error".toResponseBody("text/plain".toMediaTypeOrNull())
        coEvery { api.getDolar() } returns Response.error(503, errorBody)

        val result = repository.getDolar()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("503") == true)
    }

    @Test
    fun getDolar_excepcionRed_retornaFailure() = runTest {
        coEvery { api.getDolar() } throws RuntimeException("Sin red")

        val result = repository.getDolar()

        assertTrue(result.isFailure)
        assertEquals("Sin red", result.exceptionOrNull()?.message)
    }

    @Test
    fun getUf_exito_retornaDtoConValorUf() = runTest {
        coEvery { api.getUf() } returns Response.success(sampleUfDto)

        val result = repository.getUf()

        assertTrue(result.isSuccess)
        assertEquals(38500.25, result.getOrNull()?.valorActual ?: 0.0, 0.001)
    }

    @Test
    fun getUf_errorHttp_retornaFailure() = runTest {
        val errorBody = "Error".toResponseBody("text/plain".toMediaTypeOrNull())
        coEvery { api.getUf() } returns Response.error(500, errorBody)

        val result = repository.getUf()

        assertTrue(result.isFailure)
    }
}
