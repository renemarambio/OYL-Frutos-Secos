package com.example.oylfrutossecos.ui.viewmodel

import android.content.Context
import com.example.oylfrutossecos.data.local.VerifyCodeResult
import com.example.oylfrutossecos.data.model.User
import com.example.oylfrutossecos.data.model.UserRole
import com.example.oylfrutossecos.data.repository.AuthRepository
import com.example.oylfrutossecos.domain.validation.Validators
import com.example.oylfrutossecos.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val deliveryService: RecoveryDeliveryService = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private lateinit var viewModel: ForgotPasswordViewModel

    private val sampleUser = User(
        id = 1L,
        name = "Oscar Yáñez",
        email = "admin@oyl.cl",
        password = "Admin123!",
        role = UserRole.ADMIN,
        phone = "+56 9 9123 4567",
        address = "Av. Providencia 1820",
        securityQuestion = "¿Nombre de tu primera mascota?",
        securityAnswerHash = Validators.hashSecurityAnswer("Almendra")
    )

    @Before
    fun setUp() {
        viewModel = ForgotPasswordViewModel(
            authRepository = authRepository,
            deliveryService = deliveryService
        )
    }

    @Test
    fun onEmailChanged_actualizaEmail() {
        viewModel.onEmailChanged("admin@oyl.cl")
        assertEquals("admin@oyl.cl", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun submitEmail_emailInvalido_muestraError() {
        viewModel.onEmailChanged("correo-invalido")
        viewModel.submitEmail(context)

        assertNotNull(viewModel.uiState.value.emailError)
        assertEquals(ForgotPasswordStep.STEP_1_IDENTIFY, viewModel.uiState.value.currentStep)
    }

    @Test
    fun submitEmail_usuarioNoEncontrado_muestraMensajeInformativo() = runTest {
        coEvery { authRepository.getUserByEmail("nadie@oyl.cl", context) } returns null

        viewModel.onEmailChanged("nadie@oyl.cl")
        viewModel.submitEmail(context)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.step1InfoMessage)
        assertEquals(ForgotPasswordStep.STEP_1_IDENTIFY, viewModel.uiState.value.currentStep)
    }

    @Test
    fun submitEmail_usuarioEncontrado_avanzaAPaso2() = runTest {
        coEvery { authRepository.getUserByEmail("admin@oyl.cl", context) } returns sampleUser

        viewModel.onEmailChanged("admin@oyl.cl")
        viewModel.submitEmail(context)
        advanceUntilIdle()

        assertEquals(ForgotPasswordStep.STEP_2_CHOOSE_METHOD, viewModel.uiState.value.currentStep)
        assertTrue(viewModel.uiState.value.hasPhone)
        assertTrue(viewModel.uiState.value.hasSecurityQuestion)
    }

    @Test
    fun selectMethod_email_generaCodigoYAvanzaPaso3() = runTest {
        coEvery { authRepository.getUserByEmail("admin@oyl.cl", context) } returns sampleUser
        coEvery { authRepository.generateRecoveryCode("admin@oyl.cl", context) } returns Result.success("123456")
        coEvery { deliveryService.sendCode(any(), any(), any(), any()) } returns true

        viewModel.onEmailChanged("admin@oyl.cl")
        viewModel.submitEmail(context)
        advanceUntilIdle()

        viewModel.selectMethod(RecoveryMethod.EMAIL, context)
        advanceUntilIdle()

        assertEquals(ForgotPasswordStep.STEP_3_VERIFY_CODE, viewModel.uiState.value.currentStep)
        assertEquals(RecoveryMethod.EMAIL, viewModel.uiState.value.selectedMethod)
    }

    @Test
    fun selectMethod_securityQuestion_avanzaPasoPregunta() = runTest {
        coEvery { authRepository.getUserByEmail("admin@oyl.cl", context) } returns sampleUser

        viewModel.onEmailChanged("admin@oyl.cl")
        viewModel.submitEmail(context)
        advanceUntilIdle()

        viewModel.selectMethod(RecoveryMethod.SECURITY_QUESTION, context)

        assertEquals(ForgotPasswordStep.STEP_3_VERIFY_QUESTION, viewModel.uiState.value.currentStep)
    }

    @Test
    fun verifyCode_codigoCorrecto_avanzaAPaso4() = runTest {
        coEvery { authRepository.getUserByEmail("admin@oyl.cl", context) } returns sampleUser
        coEvery { authRepository.generateRecoveryCode("admin@oyl.cl", context) } returns Result.success("123456")
        coEvery { authRepository.verifyRecoveryCode("admin@oyl.cl", "123456", context) } returns VerifyCodeResult.Success

        viewModel.onEmailChanged("admin@oyl.cl")
        viewModel.submitEmail(context)
        advanceUntilIdle()
        viewModel.selectMethod(RecoveryMethod.EMAIL, context)
        advanceUntilIdle()

        viewModel.onVerificationCodeChanged("123456")
        viewModel.verifyCode(context)
        advanceUntilIdle()

        assertEquals(ForgotPasswordStep.STEP_4_NEW_PASSWORD, viewModel.uiState.value.currentStep)
    }

    @Test
    fun verifySecurityAnswer_correcta_avanzaAPaso4() = runTest {
        coEvery { authRepository.getUserByEmail("admin@oyl.cl", context) } returns sampleUser

        viewModel.onEmailChanged("admin@oyl.cl")
        viewModel.submitEmail(context)
        advanceUntilIdle()
        viewModel.selectMethod(RecoveryMethod.SECURITY_QUESTION, context)

        viewModel.onSecurityAnswerChanged("Almendra")
        viewModel.verifySecurityAnswer()

        assertEquals(ForgotPasswordStep.STEP_4_NEW_PASSWORD, viewModel.uiState.value.currentStep)
    }

    @Test
    fun verifySecurityAnswer_incorrecta_muestraError() = runTest {
        coEvery { authRepository.getUserByEmail("admin@oyl.cl", context) } returns sampleUser

        viewModel.onEmailChanged("admin@oyl.cl")
        viewModel.submitEmail(context)
        advanceUntilIdle()
        viewModel.selectMethod(RecoveryMethod.SECURITY_QUESTION, context)

        viewModel.onSecurityAnswerChanged("RespuestaFalsa")
        viewModel.verifySecurityAnswer()

        assertNotNull(viewModel.uiState.value.securityAnswerError)
        assertEquals(ForgotPasswordStep.STEP_3_VERIFY_QUESTION, viewModel.uiState.value.currentStep)
    }

    @Test
    fun saveNewPassword_exito_avanzaASuccess() = runTest {
        coEvery { authRepository.getUserByEmail("admin@oyl.cl", context) } returns sampleUser
        coEvery { authRepository.resetPassword("admin@oyl.cl", "NuevaClave123", context) } returns Result.success(Unit)

        viewModel.onEmailChanged("admin@oyl.cl")
        viewModel.submitEmail(context)
        advanceUntilIdle()
        viewModel.selectMethod(RecoveryMethod.SECURITY_QUESTION, context)
        viewModel.onSecurityAnswerChanged("Almendra")
        viewModel.verifySecurityAnswer()

        viewModel.onNewPasswordFieldChanged("NuevaClave123")
        viewModel.onConfirmPasswordFieldChanged("NuevaClave123")
        var callbackSuccess = false
        viewModel.saveNewPassword(context) { callbackSuccess = true }
        advanceUntilIdle()

        assertTrue(callbackSuccess)
        assertEquals(ForgotPasswordStep.SUCCESS, viewModel.uiState.value.currentStep)
    }

    @Test
    fun togglePasswordVisibility_cambiaEstado() {
        assertFalse(viewModel.uiState.value.isPasswordVisible)
        viewModel.togglePasswordVisibility()
        assertTrue(viewModel.uiState.value.isPasswordVisible)
    }

    @Test
    fun navigateBack_retrocedePasos() = runTest {
        coEvery { authRepository.getUserByEmail("admin@oyl.cl", context) } returns sampleUser

        viewModel.onEmailChanged("admin@oyl.cl")
        viewModel.submitEmail(context)
        advanceUntilIdle()

        assertEquals(ForgotPasswordStep.STEP_2_CHOOSE_METHOD, viewModel.uiState.value.currentStep)
        val handled = viewModel.navigateBack()
        assertTrue(handled)
        assertEquals(ForgotPasswordStep.STEP_1_IDENTIFY, viewModel.uiState.value.currentStep)
    }
}
