package com.example.oylfrutossecos.ui.viewmodel

import com.example.oylfrutossecos.data.remote.dto.IndicatorDto
import com.example.oylfrutossecos.data.remote.dto.IndicatorSerieDto
import com.example.oylfrutossecos.data.repository.IndicatorRepository
import com.example.oylfrutossecos.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IndicatorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: IndicatorRepository = mockk()

    @Test
    fun loadDolar_exito_emiteEstadoSuccessConValorYFecha() = runTest {
        val dto = IndicatorDto(
            version = "1.0",
            autor = "mindicador",
            codigo = "dolar",
            nombre = "Dólar observado",
            serie = listOf(IndicatorSerieDto(fecha = "2026-11-18T00:00:00.000Z", valor = 980.25))
        )
        coEvery { repository.getDolar() } returns Result.success(dto)

        val viewModel = IndicatorViewModel(repository = repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is IndicatorUiState.Success)
        val success = state as IndicatorUiState.Success
        assertEquals(980.25, success.valorDolar, 0.001)
        assertEquals("18/11/2026", success.fecha)
    }

    @Test
    fun loadDolar_error_emiteEstadoError() = runTest {
        coEvery { repository.getDolar() } returns Result.failure(RuntimeException("Error en API externa"))

        val viewModel = IndicatorViewModel(repository = repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is IndicatorUiState.Error)
        val error = state as IndicatorUiState.Error
        assertEquals("Error en API externa", error.message)
    }

    @Test
    fun loadDolar_sinSerieOValorNulo_emiteEstadoError() = runTest {
        val dto = IndicatorDto(
            version = "1.0",
            autor = "mindicador",
            codigo = "dolar",
            nombre = "Dólar observado",
            serie = emptyList()
        )
        coEvery { repository.getDolar() } returns Result.success(dto)

        val viewModel = IndicatorViewModel(repository = repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is IndicatorUiState.Error)
    }
}
