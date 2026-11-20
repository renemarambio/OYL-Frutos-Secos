package com.example.oylfrutossecos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.oylfrutossecos.data.ServiceLocator
import com.example.oylfrutossecos.data.repository.IndicatorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface IndicatorUiState {
    object Loading : IndicatorUiState
    data class Success(
        val valorDolar: Double,
        val fecha: String,
        val nombre: String
    ) : IndicatorUiState
    data class Error(val message: String) : IndicatorUiState
}

class IndicatorViewModel(
    private val repository: IndicatorRepository = ServiceLocator.provideIndicatorRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<IndicatorUiState>(IndicatorUiState.Loading)
    val uiState: StateFlow<IndicatorUiState> = _uiState.asStateFlow()

    init {
        loadDolar()
    }

    fun loadDolar() {
        viewModelScope.launch {
            _uiState.value = IndicatorUiState.Loading
            val result = repository.getDolar()
            result.onSuccess { dto ->
                val valor = dto.valorActual
                val fechaRaw = dto.fechaActual ?: ""
                val fechaFormatted = formatFecha(fechaRaw)
                if (valor != null) {
                    _uiState.value = IndicatorUiState.Success(
                        valorDolar = valor,
                        fecha = fechaFormatted,
                        nombre = dto.nombre ?: "Dólar observado"
                    )
                } else {
                    _uiState.value = IndicatorUiState.Error("No se encontró el valor del indicador")
                }
            }.onFailure { ex ->
                _uiState.value = IndicatorUiState.Error(
                    ex.message ?: "No se pudo obtener el valor del dólar"
                )
            }
        }
    }

    private fun formatFecha(raw: String): String {
        return try {
            if (raw.length >= 10) {
                val parts = raw.substring(0, 10).split("-")
                if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else raw
            } else {
                raw
            }
        } catch (_: Exception) {
            raw
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                IndicatorViewModel(
                    repository = ServiceLocator.provideIndicatorRepository()
                )
            }
        }
    }
}
