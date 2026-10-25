package com.example.oylfrutossecos.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oylfrutossecos.data.model.User
import com.example.oylfrutossecos.data.model.UserRole
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

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "+56 9 ",
    val address: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val securityQuestion: String = Validators.SECURITY_QUESTIONS[0],
    val securityAnswer: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmVisible: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val securityAnswerError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false
)

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val roleText: String = "",
    val phone: String = "",
    val address: String = "",
    val securityQuestion: String = Validators.SECURITY_QUESTIONS[0],
    val newSecurityQuestion: String = Validators.SECURITY_QUESTIONS[0],
    val securityAnswer: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val nameError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val securityAnswerError: String? = null,
    val generalError: String? = null,
    val successMessage: String? = null,
    val isLoading: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository.getInstance()

    val currentUser: StateFlow<User?> = authRepository.currentUser

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    // --- MÉTODOS LOGIN ---

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

    fun login(context: Context, onSuccess: (UserRole) -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        val emailError = Validators.validateEmail(email)
        val passwordError = Validators.validatePassword(password)
        val hasError = emailError != null || passwordError != null

        if (hasError) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            kotlinx.coroutines.delay(400)

            val result = authRepository.login(email = email, password = password, context = context)
            result.onSuccess { user ->
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                initProfileState(user)
                onSuccess(user.role)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = err.message ?: "Credenciales inválidas"
                    )
                }
            }
        }
    }

    // Sobrecarga de compatibilidad
    fun login(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        val emailError = Validators.validateEmail(email)
        val passwordError = Validators.validatePassword(password)
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            val result = authRepository.login(email = email, password = password)
            result.onSuccess { user ->
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                initProfileState(user)
                onSuccess()
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, generalError = err.message) }
            }
        }
    }

    fun loginAsDemoAdmin(context: Context, onSuccess: (UserRole) -> Unit) {
        _uiState.update {
            it.copy(
                email = "admin@oyl.cl",
                password = "Admin123!",
                emailError = null,
                passwordError = null,
                generalError = null
            )
        }
        login(context, onSuccess)
    }

    fun loginAsDemoClient(context: Context, onSuccess: (UserRole) -> Unit) {
        _uiState.update {
            it.copy(
                email = "cliente@oyl.cl",
                password = "Cliente123",
                emailError = null,
                passwordError = null,
                generalError = null
            )
        }
        login(context, onSuccess)
    }

    fun loginAsDemo(onSuccess: () -> Unit) {
        _uiState.update {
            it.copy(
                email = "admin@oyl.cl",
                password = "Admin123!"
            )
        }
        login(onSuccess)
    }

    // --- MÉTODOS REGISTRO (CLIENTE) ---

    fun onRegisterFieldChanged(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        address: String? = null,
        password: String? = null,
        confirmPassword: String? = null,
        securityQuestion: String? = null,
        securityAnswer: String? = null
    ) {
        _registerState.update { cur ->
            cur.copy(
                name = name ?: cur.name,
                email = email ?: cur.email,
                phone = phone ?: cur.phone,
                address = address ?: cur.address,
                password = password ?: cur.password,
                confirmPassword = confirmPassword ?: cur.confirmPassword,
                securityQuestion = securityQuestion ?: cur.securityQuestion,
                securityAnswer = securityAnswer ?: cur.securityAnswer,
                nameError = if (name != null) null else cur.nameError,
                emailError = if (email != null) null else cur.emailError,
                phoneError = if (phone != null) null else cur.phoneError,
                addressError = if (address != null) null else cur.addressError,
                passwordError = if (password != null) null else cur.passwordError,
                confirmPasswordError = if (confirmPassword != null) null else cur.confirmPasswordError,
                securityAnswerError = if (securityAnswer != null) null else cur.securityAnswerError,
                generalError = null
            )
        }
    }

    fun toggleRegisterPasswordVisibility() {
        _registerState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleRegisterConfirmVisibility() {
        _registerState.update { it.copy(isConfirmVisible = !it.isConfirmVisible) }
    }

    fun register(context: Context, onSuccess: () -> Unit) {
        val form = _registerState.value

        val nameErr = Validators.validatePersonName(form.name)
        val emailErr = Validators.validateEmail(form.email)
        val phoneErr = Validators.validateChileanPhone(form.phone)
        val addrErr = Validators.validateAddress(form.address)
        val passErr = Validators.validateRegistrationPassword(form.password)
        val confErr = Validators.validateConfirmPassword(form.password, form.confirmPassword)
        val ansErr = Validators.validateSecurityAnswer(form.securityAnswer)

        if (listOf(nameErr, emailErr, phoneErr, addrErr, passErr, confErr, ansErr).any { it != null }) {
            _registerState.update {
                it.copy(
                    nameError = nameErr,
                    emailError = emailErr,
                    phoneError = phoneErr,
                    addressError = addrErr,
                    passwordError = passErr,
                    confirmPasswordError = confErr,
                    securityAnswerError = ansErr
                )
            }
            return
        }

        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true, generalError = null) }
            val newUser = User(
                name = form.name.trim(),
                email = form.email.trim(),
                password = form.password,
                role = UserRole.CLIENTE,
                phone = form.phone.trim(),
                address = form.address.trim(),
                securityQuestion = form.securityQuestion,
                securityAnswerHash = Validators.hashSecurityAnswer(form.securityAnswer)
            )

            val result = authRepository.registerClient(newUser, context)
            result.onSuccess { created ->
                _registerState.update { RegisterUiState() }
                initProfileState(created)
                onSuccess()
            }.onFailure { err ->
                _registerState.update {
                    it.copy(
                        isLoading = false,
                        generalError = err.message ?: "Error al registrar la cuenta"
                    )
                }
            }
        }
    }

    // --- MÉTODOS PERFIL ---

    fun initProfileState(user: User? = currentUser.value) {
        val u = user ?: currentUser.value ?: return
        val currentQ = u.securityQuestion.ifBlank { Validators.SECURITY_QUESTIONS[0] }
        _profileState.value = ProfileUiState(
            name = u.name,
            email = u.email,
            roleText = u.role.displayName,
            phone = u.phone,
            address = u.address,
            securityQuestion = currentQ,
            newSecurityQuestion = currentQ,
            securityAnswer = ""
        )
    }

    fun onProfileFieldChanged(
        name: String? = null,
        phone: String? = null,
        address: String? = null,
        newPassword: String? = null,
        confirmPassword: String? = null,
        newSecurityQuestion: String? = null,
        securityAnswer: String? = null
    ) {
        _profileState.update { cur ->
            cur.copy(
                name = name ?: cur.name,
                phone = phone ?: cur.phone,
                address = address ?: cur.address,
                newPassword = newPassword ?: cur.newPassword,
                confirmPassword = confirmPassword ?: cur.confirmPassword,
                newSecurityQuestion = newSecurityQuestion ?: cur.newSecurityQuestion,
                securityAnswer = securityAnswer ?: cur.securityAnswer,
                nameError = if (name != null) null else cur.nameError,
                phoneError = if (phone != null) null else cur.phoneError,
                addressError = if (address != null) null else cur.addressError,
                passwordError = if (newPassword != null) null else cur.passwordError,
                confirmPasswordError = if (confirmPassword != null) null else cur.confirmPasswordError,
                securityAnswerError = if (securityAnswer != null) null else cur.securityAnswerError,
                generalError = null,
                successMessage = null
            )
        }
    }

    fun toggleProfilePasswordVisibility() {
        _profileState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun updateProfile(context: Context, onSuccess: () -> Unit) {
        val form = _profileState.value
        val current = currentUser.value ?: return

        val nameErr = Validators.validatePersonName(form.name)
        val phoneErr = Validators.validateChileanPhone(form.phone)
        val addrErr = Validators.validateAddress(form.address)

        val passErr = if (form.newPassword.isNotBlank()) Validators.validateRegistrationPassword(form.newPassword) else null
        val confErr = if (form.newPassword.isNotBlank()) Validators.validateConfirmPassword(form.newPassword, form.confirmPassword) else null

        // Si se ingresó respuesta de seguridad o se cambió la pregunta, validar la respuesta
        val ansErr = if (form.securityAnswer.isNotBlank() || form.newSecurityQuestion != form.securityQuestion) {
            Validators.validateSecurityAnswer(form.securityAnswer)
        } else null

        if (listOf(nameErr, phoneErr, addrErr, passErr, confErr, ansErr).any { it != null }) {
            _profileState.update {
                it.copy(
                    nameError = nameErr,
                    phoneError = phoneErr,
                    addressError = addrErr,
                    passwordError = passErr,
                    confirmPasswordError = confErr,
                    securityAnswerError = ansErr
                )
            }
            return
        }

        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true, generalError = null) }
            val updatedUser = current.copy(
                name = form.name.trim(),
                phone = form.phone.trim(),
                address = form.address.trim(),
                password = if (form.newPassword.isNotBlank()) form.newPassword else current.password,
                securityQuestion = if (form.newSecurityQuestion.isNotBlank()) form.newSecurityQuestion else current.securityQuestion,
                securityAnswerHash = if (form.securityAnswer.isNotBlank()) Validators.hashSecurityAnswer(form.securityAnswer) else current.securityAnswerHash
            )

            val result = authRepository.updateProfile(updatedUser, context)
            result.onSuccess { saved ->
                _profileState.update {
                    it.copy(
                        isLoading = false,
                        newPassword = "",
                        confirmPassword = "",
                        securityAnswer = "",
                        securityQuestion = saved.securityQuestion,
                        newSecurityQuestion = saved.securityQuestion,
                        successMessage = "¡Perfil actualizado con éxito!"
                    )
                }
                onSuccess()
            }.onFailure { err ->
                _profileState.update {
                    it.copy(
                        isLoading = false,
                        generalError = err.message ?: "Error al actualizar perfil"
                    )
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit = {}) {
        authRepository.logout()
        _uiState.update { LoginUiState(email = "", password = "") }
        _registerState.update { RegisterUiState() }
        _profileState.update { ProfileUiState() }
        onLoggedOut()
    }
}
