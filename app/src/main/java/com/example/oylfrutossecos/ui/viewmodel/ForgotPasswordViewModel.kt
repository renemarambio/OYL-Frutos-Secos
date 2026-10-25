package com.example.oylfrutossecos.ui.viewmodel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oylfrutossecos.data.local.AppDatabaseHelper
import com.example.oylfrutossecos.data.local.VerifyCodeResult
import com.example.oylfrutossecos.data.model.User
import com.example.oylfrutossecos.data.repository.AuthRepository
import com.example.oylfrutossecos.domain.validation.Validators
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ForgotPasswordStep {
    STEP_1_IDENTIFY,
    STEP_2_CHOOSE_METHOD,
    STEP_3_VERIFY_CODE,
    STEP_3_VERIFY_QUESTION,
    STEP_4_NEW_PASSWORD,
    SUCCESS
}

enum class RecoveryMethod {
    EMAIL,
    SMS,
    SECURITY_QUESTION
}

data class ForgotPasswordUiState(
    val currentStep: ForgotPasswordStep = ForgotPasswordStep.STEP_1_IDENTIFY,
    val email: String = "",
    val emailError: String? = null,
    val step1InfoMessage: String? = null,
    val maskedEmail: String = "",
    val maskedPhone: String? = null,
    val hasPhone: Boolean = false,
    val hasSecurityQuestion: Boolean = false,
    val securityQuestion: String = "",
    val selectedMethod: RecoveryMethod? = null,
    val verificationCode: String = "",
    val verificationCodeError: String? = null,
    val securityAnswer: String = "",
    val securityAnswerError: String? = null,
    val attemptsLeft: Int = 3,
    val isLocked: Boolean = false,
    val lockRemainingSeconds: Int = 0,
    val resendCountdownSeconds: Int = 0,
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val showSimulationDialog: Boolean = false,
    val simulationDialogTitle: String = "",
    val simulationDialogMessage: String = "",
    val simulatedCodeForTesting: String? = null
)

/**
 * Servicio desacoplado para simular o conectar en el futuro un proveedor real de correo/SMS
 * (ej: Twilio, SendGrid, Firebase).
 */
interface RecoveryDeliveryService {
    suspend fun sendCode(target: String, code: String, isSms: Boolean, context: Context): Boolean
}

class DefaultRecoveryDeliveryService : RecoveryDeliveryService {
    override suspend fun sendCode(target: String, code: String, isSms: Boolean, context: Context): Boolean {
        // Notificación local del sistema Android (canal 'oyl_recovery')
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (notificationManager != null) {
                val channelId = "oyl_recovery_channel"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        "Recuperación de Contraseña OYL",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Notificaciones de códigos de verificación de OYL Frutos Secos"
                    }
                    notificationManager.createNotificationChannel(channel)
                }

                val title = if (isSms) "[Simulación SMS] Código OYL" else "[Simulación Correo] Código OYL"
                val text = "Tu código de recuperación es: $code (válido por 5 minutos)"

                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setStyle(NotificationCompat.BigTextStyle().bigText("$text\nDestino: $target"))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(1001, notification)
            }
        } catch (_: Exception) {
            // Si no tiene permisos de notificación en Android 13+, el diálogo en pantalla asegura la visibilidad
        }
        return true
    }
}

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository = AuthRepository.getInstance(),
    private val deliveryService: RecoveryDeliveryService = DefaultRecoveryDeliveryService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private var targetUser: User? = null
    private var resendTimerJob: Job? = null
    private var lockTimerJob: Job? = null

    // --- PASO 1: IDENTIFICACIÓN ---

    fun onEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailError = null,
                step1InfoMessage = null,
                generalError = null
            )
        }
    }

    fun submitEmail(context: Context) {
        val email = _uiState.value.email.trim()
        val emailError = Validators.validateEmail(email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null, step1InfoMessage = null) }
            delay(300)

            val user = authRepository.getUserByEmail(email, context)
            if (user != null) {
                targetUser = user
                val maskedEmail = Validators.maskEmail(user.email)
                val hasPhone = user.phone.isNotBlank() && user.phone.length > 5
                val maskedPhone = if (hasPhone) Validators.maskPhone(user.phone) else null
                val hasSecQ = user.securityQuestion.isNotBlank() && user.securityAnswerHash.isNotBlank()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentStep = ForgotPasswordStep.STEP_2_CHOOSE_METHOD,
                        maskedEmail = maskedEmail,
                        maskedPhone = maskedPhone,
                        hasPhone = hasPhone,
                        hasSecurityQuestion = hasSecQ,
                        securityQuestion = user.securityQuestion,
                        generalError = null,
                        step1InfoMessage = null
                    )
                }
            } else {
                // Por seguridad, si el correo no existe se muestra un mensaje genérico sin revelar si la cuenta existe
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        step1InfoMessage = "Si el correo está registrado, podrás recuperar tu cuenta."
                    )
                }
            }
        }
    }

    // --- PASO 2: ELEGIR MÉTODO ---

    fun selectMethod(method: RecoveryMethod, context: Context) {
        val user = targetUser ?: return
        _uiState.update { it.copy(selectedMethod = method, generalError = null) }

        when (method) {
            RecoveryMethod.EMAIL -> sendCode(isSms = false, context = context)
            RecoveryMethod.SMS -> sendCode(isSms = true, context = context)
            RecoveryMethod.SECURITY_QUESTION -> {
                _uiState.update {
                    it.copy(
                        currentStep = ForgotPasswordStep.STEP_3_VERIFY_QUESTION,
                        securityAnswer = "",
                        securityAnswerError = null,
                        generalError = null
                    )
                }
            }
        }
    }

    private fun sendCode(isSms: Boolean, context: Context) {
        val user = targetUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            val result = authRepository.generateRecoveryCode(user.email, context)
            result.onSuccess { code ->
                val targetText = if (isSms) user.phone else user.email
                deliveryService.sendCode(targetText, code, isSms, context)

                startResendCountdown()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentStep = ForgotPasswordStep.STEP_3_VERIFY_CODE,
                        verificationCode = "",
                        verificationCodeError = null,
                        attemptsLeft = 3,
                        simulatedCodeForTesting = code,
                        showSimulationDialog = true,
                        simulationDialogTitle = if (isSms) "Simulación de Envío por SMS" else "Simulación de Envío por Correo",
                        simulationDialogMessage = "Se ha simulado el envío de un código de 6 dígitos a:\n${if (isSms) _uiState.value.maskedPhone else _uiState.value.maskedEmail}\n\nCódigo generado: $code\n(Válido por 5 minutos, máx. 3 intentos)"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = err.message ?: "Error al generar código de verificación"
                    )
                }
            }
        }
    }

    fun dismissSimulationDialog() {
        _uiState.update { it.copy(showSimulationDialog = false) }
    }

    fun resendCode(context: Context) {
        if (_uiState.value.resendCountdownSeconds > 0 || _uiState.value.isLocked) return
        val isSms = _uiState.value.selectedMethod == RecoveryMethod.SMS
        sendCode(isSms, context)
    }

    private fun startResendCountdown() {
        resendTimerJob?.cancel()
        _uiState.update { it.copy(resendCountdownSeconds = 60) }
        resendTimerJob = viewModelScope.launch {
            for (i in 59 downTo 0) {
                delay(1000)
                _uiState.update { it.copy(resendCountdownSeconds = i) }
            }
        }
    }

    private fun startLockCountdown(seconds: Int = 300) {
        lockTimerJob?.cancel()
        _uiState.update { it.copy(isLocked = true, lockRemainingSeconds = seconds) }
        lockTimerJob = viewModelScope.launch {
            for (i in seconds - 1 downTo 0) {
                delay(1000)
                _uiState.update { it.copy(lockRemainingSeconds = i) }
            }
            _uiState.update { it.copy(isLocked = false, lockRemainingSeconds = 0, attemptsLeft = 3) }
        }
    }

    // --- PASO 3: VERIFICACIÓN ---

    fun onVerificationCodeChanged(code: String) {
        val filtered = code.filter { it.isDigit() }.take(6)
        _uiState.update {
            it.copy(
                verificationCode = filtered,
                verificationCodeError = null,
                generalError = null
            )
        }
    }

    fun verifyCode(context: Context) {
        val state = _uiState.value
        val user = targetUser ?: return

        if (state.isLocked) {
            val min = state.lockRemainingSeconds / 60
            val sec = state.lockRemainingSeconds % 60
            _uiState.update {
                it.copy(generalError = "Recuperación bloqueada. Espera ${min}m ${sec}s.")
            }
            return
        }

        if (state.verificationCode.length != 6) {
            _uiState.update { it.copy(verificationCodeError = "Ingresa el código completo de 6 dígitos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            delay(200)

            val result = authRepository.verifyRecoveryCode(user.email, state.verificationCode, context)
            when (result) {
                is VerifyCodeResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentStep = ForgotPasswordStep.STEP_4_NEW_PASSWORD,
                            generalError = null
                        )
                    }
                }
                is VerifyCodeResult.Error -> {
                    if (result.isLocked) {
                        startLockCountdown(300)
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            attemptsLeft = result.attemptsLeft,
                            verificationCodeError = result.message,
                            generalError = if (result.isLocked) result.message else null
                        )
                    }
                }
            }
        }
    }

    fun onSecurityAnswerChanged(answer: String) {
        _uiState.update {
            it.copy(
                securityAnswer = answer,
                securityAnswerError = null,
                generalError = null
            )
        }
    }

    fun verifySecurityAnswer() {
        val user = targetUser ?: return
        val answer = _uiState.value.securityAnswer

        if (answer.trim().isBlank()) {
            _uiState.update { it.copy(securityAnswerError = "Ingresa tu respuesta de seguridad") }
            return
        }

        // Comparar respuesta sin distinguir mayúsculas ni espacios
        val isCorrect = Validators.verifySecurityAnswer(answer, user.securityAnswerHash)
        if (isCorrect) {
            _uiState.update {
                it.copy(
                    currentStep = ForgotPasswordStep.STEP_4_NEW_PASSWORD,
                    securityAnswerError = null,
                    generalError = null
                )
            }
        } else {
            val newAttempts = (_uiState.value.attemptsLeft - 1).coerceAtLeast(0)
            if (newAttempts == 0) {
                startLockCountdown(300)
                _uiState.update {
                    it.copy(
                        securityAnswerError = "Respuesta incorrecta. Has superado los 3 intentos permitidos.",
                        generalError = "Recuperación bloqueada durante 5 minutos por intentos fallidos."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        attemptsLeft = newAttempts,
                        securityAnswerError = "Respuesta incorrecta. Te quedan $newAttempts intento(s)."
                    )
                }
            }
        }
    }

    // --- PASO 4: NUEVA CONTRASEÑA ---

    fun onNewPasswordFieldChanged(password: String) {
        _uiState.update {
            it.copy(
                newPassword = password,
                newPasswordError = null,
                generalError = null
            )
        }
    }

    fun onConfirmPasswordFieldChanged(confirm: String) {
        _uiState.update {
            it.copy(
                confirmPassword = confirm,
                confirmPasswordError = null,
                generalError = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun saveNewPassword(context: Context, onSuccess: (String) -> Unit) {
        val user = targetUser ?: return
        val newPass = _uiState.value.newPassword
        val confPass = _uiState.value.confirmPassword

        val passErr = Validators.validateRegistrationPassword(newPass)
        val confErr = Validators.validateConfirmPassword(newPass, confPass)
        val sameErr = Validators.validateNewPasswordNotSame(newPass, user.password)

        if (listOf(passErr, confErr, sameErr).any { it != null }) {
            _uiState.update {
                it.copy(
                    newPasswordError = passErr ?: sameErr,
                    confirmPasswordError = confErr
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            val result = authRepository.resetPassword(user.email, newPass, context)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentStep = ForgotPasswordStep.SUCCESS
                    )
                }
                delay(500)
                onSuccess(user.email)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = err.message ?: "Error al actualizar la contraseña"
                    )
                }
            }
        }
    }

    fun navigateBack(): Boolean {
        return when (_uiState.value.currentStep) {
            ForgotPasswordStep.STEP_1_IDENTIFY -> false // Volver al login
            ForgotPasswordStep.STEP_2_CHOOSE_METHOD -> {
                _uiState.update { it.copy(currentStep = ForgotPasswordStep.STEP_1_IDENTIFY) }
                true
            }
            ForgotPasswordStep.STEP_3_VERIFY_CODE, ForgotPasswordStep.STEP_3_VERIFY_QUESTION -> {
                _uiState.update { it.copy(currentStep = ForgotPasswordStep.STEP_2_CHOOSE_METHOD) }
                true
            }
            ForgotPasswordStep.STEP_4_NEW_PASSWORD -> {
                _uiState.update { it.copy(currentStep = ForgotPasswordStep.STEP_2_CHOOSE_METHOD) }
                true
            }
            ForgotPasswordStep.SUCCESS -> false
        }
    }

    override fun onCleared() {
        super.onCleared()
        resendTimerJob?.cancel()
        lockTimerJob?.cancel()
    }
}
