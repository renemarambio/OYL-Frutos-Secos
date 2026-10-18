package com.example.oylfrutossecos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oylfrutossecos.data.model.User
import com.example.oylfrutossecos.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "admin@oylfrutossecos.cl",
    val password: String = "Duoc2026!",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository.getInstance()

    val currentUser: StateFlow<User?> = authRepository.currentUser

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailError = null,
                generalError = null
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                generalError = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        var hasError = false
        var emailError: String? = null
        var passwordError: String? = null

        if (email.isBlank()) {
            emailError = "El correo electrónico es obligatorio"
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Ingresa un formato de correo válido (ej: usuario@correo.com)"
            hasError = true
        }

        if (password.isBlank()) {
            passwordError = "La contraseña es obligatoria"
            hasError = true
        } else if (password.length < 6) {
            passwordError = "La contraseña debe tener al menos 6 caracteres"
            hasError = true
        }

        if (hasError) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            kotlinx.coroutines.delay(600) // Simulación breve de red para feedback UX

            val userName = email.substringBefore("@").replace(".", " ").capitalizeWords()
            authRepository.login(email = email, name = userName)
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            onSuccess()
        }
    }

    fun loginAsDemo(onSuccess: () -> Unit) {
        _uiState.update {
            it.copy(
                email = "demo.vendedor@oylfrutossecos.cl",
                password = "DemoUser2026!"
            )
        }
        login(onSuccess)
    }

    fun logout(onLoggedOut: () -> Unit) {
        authRepository.logout()
        _uiState.update { LoginUiState(email = "", password = "") }
        onLoggedOut()
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}
