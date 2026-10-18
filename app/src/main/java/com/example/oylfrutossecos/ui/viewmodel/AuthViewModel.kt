package com.example.oylfrutossecos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oylfrutossecos.data.model.User
import com.example.oylfrutossecos.data.repository.AuthRepository
import com.example.oylfrutossecos.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
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

        // Validación centralizada en domain/validation/Validators
        val emailError = Validators.validateEmail(email)
        val passwordError = Validators.validatePassword(password)
        val hasError = emailError != null || passwordError != null

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
