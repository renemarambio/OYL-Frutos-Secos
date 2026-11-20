package com.example.oylfrutossecos.ui.viewmodel

import com.example.oylfrutossecos.data.model.User
import com.example.oylfrutossecos.data.model.UserRole
import com.example.oylfrutossecos.data.repository.AuthRepository
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
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private lateinit var viewModel: AuthViewModel

    private val sampleUser = User(
        id = 1L,
        name = "Oscar Yáñez",
        email = "admin@oyl.cl",
        password = "Admin123!",
        role = UserRole.ADMIN,
        phone = "+56 9 9123 4567",
        address = "Av. Providencia 1820"
    )

    @Before
    fun setUp() {
        viewModel = AuthViewModel(authRepository = authRepository)
    }

    @Test
    fun onEmailChanged_actualizaEmailYLimpiaErrores() {
        viewModel.onEmailChanged("usuario@correo.cl")
        assertEquals("usuario@correo.cl", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun onPasswordChanged_actualizaPasswordYLimpiaErrores() {
        viewModel.onPasswordChanged("123456")
        assertEquals("123456", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun togglePasswordVisibility_cambiaVisibilidad() {
        assertFalse(viewModel.uiState.value.isPasswordVisible)
        viewModel.togglePasswordVisibility()
        assertTrue(viewModel.uiState.value.isPasswordVisible)
    }

    @Test
    fun login_camposInvalidos_marcaErrores() {
        viewModel.onEmailChanged("")
        viewModel.onPasswordChanged("123")

        var success = false
        viewModel.login(onSuccess = { success = true })

        assertFalse(success)
        assertNotNull(viewModel.uiState.value.emailError)
        assertNotNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun login_exitoso_actualizaEstadoYEjecutaCallback() = runTest {
        coEvery { authRepository.login("admin@oyl.cl", "Admin123!") } returns Result.success(sampleUser)

        viewModel.onEmailChanged("admin@oyl.cl")
        viewModel.onPasswordChanged("Admin123!")

        var success = false
        viewModel.login(onSuccess = { success = true })
        advanceUntilIdle()

        assertTrue(success)
        assertTrue(viewModel.uiState.value.isSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun login_credencialesErroneas_muestraError() = runTest {
        coEvery { authRepository.login("admin@oyl.cl", "123456") } returns Result.failure(IllegalArgumentException("Contraseña incorrecta"))

        viewModel.onEmailChanged("admin@oyl.cl")
        viewModel.onPasswordChanged("123456")

        var success = false
        viewModel.login(onSuccess = { success = true })
        advanceUntilIdle()

        assertFalse(success)
        assertEquals("Contraseña incorrecta", viewModel.uiState.value.generalError)
    }

    @Test
    fun loginAsDemo_cargaCredencialesYEjecutaLogin() = runTest {
        coEvery { authRepository.login("admin@oyl.cl", "Admin123!") } returns Result.success(sampleUser)

        var success = false
        viewModel.loginAsDemo(onSuccess = { success = true })
        advanceUntilIdle()

        assertTrue(success)
        assertEquals("admin@oyl.cl", viewModel.uiState.value.email)
    }

    @Test
    fun onRegisterFieldChanged_actualizaCampos() {
        viewModel.onRegisterFieldChanged(name = "Juan Perez", email = "juan@oyl.cl", phone = "+56 9 1234 5678")
        val reg = viewModel.registerState.value
        assertEquals("Juan Perez", reg.name)
        assertEquals("juan@oyl.cl", reg.email)
        assertEquals("+56 9 1234 5678", reg.phone)
    }

    @Test
    fun toggleRegisterPasswordVisibility_cambiaVisibilidad() {
        assertFalse(viewModel.registerState.value.isPasswordVisible)
        viewModel.toggleRegisterPasswordVisibility()
        assertTrue(viewModel.registerState.value.isPasswordVisible)
    }

    @Test
    fun toggleRegisterConfirmVisibility_cambiaVisibilidad() {
        assertFalse(viewModel.registerState.value.isConfirmVisible)
        viewModel.toggleRegisterConfirmVisibility()
        assertTrue(viewModel.registerState.value.isConfirmVisible)
    }

    @Test
    fun register_camposInvalidos_marcaErrores() {
        viewModel.onRegisterFieldChanged(
            name = "",
            email = "invalido",
            phone = "12",
            address = "",
            password = "123",
            confirmPassword = "456",
            securityAnswer = ""
        )

        var success = false
        viewModel.register(context = mockk(), onSuccess = { success = true })

        assertFalse(success)
        val reg = viewModel.registerState.value
        assertNotNull(reg.nameError)
        assertNotNull(reg.emailError)
        assertNotNull(reg.phoneError)
        assertNotNull(reg.addressError)
        assertNotNull(reg.passwordError)
        assertNotNull(reg.confirmPasswordError)
        assertNotNull(reg.securityAnswerError)
    }

    @Test
    fun initProfileState_conUsuario_inicializaValores() {
        viewModel.initProfileState(sampleUser)

        val prof = viewModel.profileState.value
        assertEquals("Oscar Yáñez", prof.name)
        assertEquals("admin@oyl.cl", prof.email)
        assertEquals("+56 9 9123 4567", prof.phone)
    }

    @Test
    fun onProfileFieldChanged_actualizaValores() {
        viewModel.onProfileFieldChanged(name = "Oscar Yañez Editado", phone = "+56 9 9999 8888")

        val prof = viewModel.profileState.value
        assertEquals("Oscar Yañez Editado", prof.name)
        assertEquals("+56 9 9999 8888", prof.phone)
    }

    @Test
    fun toggleProfilePasswordVisibility_cambiaVisibilidad() {
        assertFalse(viewModel.profileState.value.isPasswordVisible)
        viewModel.toggleProfilePasswordVisibility()
        assertTrue(viewModel.profileState.value.isPasswordVisible)
    }

    @Test
    fun register_exitoso_limpiaEstadoYNotificaExito() = runTest {
        val mockContext: android.content.Context = mockk(relaxed = true)
        coEvery { authRepository.registerClient(any(), mockContext) } returns Result.success(sampleUser)

        viewModel.onRegisterFieldChanged(
            name = "Rene Marambio",
            email = "rene@oyl.cl",
            phone = "+56 9 9123 4567",
            address = "Av. Providencia 1240, Providencia",
            password = "Password123",
            confirmPassword = "Password123",
            securityQuestion = "¿Ciudad donde naciste?",
            securityAnswer = "Santiago"
        )

        var registered = false
        viewModel.register(context = mockContext, onSuccess = { registered = true })
        advanceUntilIdle()

        assertTrue(registered)
        assertEquals("", viewModel.registerState.value.name)
    }

    @Test
    fun register_error_muestraGeneralError() = runTest {
        val mockContext: android.content.Context = mockk(relaxed = true)
        coEvery { authRepository.registerClient(any(), mockContext) } returns Result.failure(RuntimeException("Correo ya registrado"))

        viewModel.onRegisterFieldChanged(
            name = "Rene Marambio",
            email = "rene@oyl.cl",
            phone = "+56 9 9123 4567",
            address = "Av. Providencia 1240, Providencia",
            password = "Password123",
            confirmPassword = "Password123",
            securityQuestion = "¿Ciudad donde naciste?",
            securityAnswer = "Santiago"
        )

        var registered = false
        viewModel.register(context = mockContext, onSuccess = { registered = true })
        advanceUntilIdle()

        assertFalse(registered)
        assertEquals("Correo ya registrado", viewModel.registerState.value.generalError)
    }

    @Test
    fun updateProfile_exitoso_actualizaUsuario() = runTest {
        val mockContext: android.content.Context = mockk(relaxed = true)
        coEvery { authRepository.updateProfile(any(), mockContext) } returns Result.success(sampleUser)
        coEvery { authRepository.currentUser } returns kotlinx.coroutines.flow.MutableStateFlow(sampleUser)

        val vm = AuthViewModel(authRepository)
        vm.initProfileState(sampleUser)
        vm.onProfileFieldChanged(name = "Oscar Yáñez Actualizado", phone = "+56 9 9123 4567", address = "Av. Providencia 1820, Providencia")

        var updated = false
        vm.updateProfile(context = mockContext, onSuccess = { updated = true })
        advanceUntilIdle()

        assertTrue(updated)
        assertEquals("¡Perfil actualizado con éxito!", vm.profileState.value.successMessage)
    }

    @Test
    fun updateProfile_error_muestraGeneralError() = runTest {
        val mockContext: android.content.Context = mockk(relaxed = true)
        coEvery { authRepository.updateProfile(any(), mockContext) } returns Result.failure(RuntimeException("Error en BD"))
        coEvery { authRepository.currentUser } returns kotlinx.coroutines.flow.MutableStateFlow(sampleUser)

        val vm = AuthViewModel(authRepository)
        vm.initProfileState(sampleUser)
        vm.onProfileFieldChanged(name = "Oscar Yáñez", phone = "+56 9 9123 4567", address = "Av. Providencia 1820, Providencia")

        var updated = false
        vm.updateProfile(context = mockContext, onSuccess = { updated = true })
        advanceUntilIdle()

        assertFalse(updated)
        assertEquals("Error en BD", vm.profileState.value.generalError)
    }

    @Test
    fun login_conContext_exitoso() = runTest {
        val mockContext: android.content.Context = mockk(relaxed = true)
        coEvery { authRepository.login("admin@oyl.cl", "Admin123!", mockContext) } returns Result.success(sampleUser)

        viewModel.onEmailChanged("admin@oyl.cl")
        viewModel.onPasswordChanged("Admin123!")

        var role: UserRole? = null
        viewModel.login(mockContext) { role = it }
        advanceUntilIdle()

        assertEquals(UserRole.ADMIN, role)
        assertTrue(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun loginAsDemoAdmin_cargaCredencialesYEjecuta() = runTest {
        val mockContext: android.content.Context = mockk(relaxed = true)
        coEvery { authRepository.login("admin@oyl.cl", "Admin123!", mockContext) } returns Result.success(sampleUser)

        var role: UserRole? = null
        viewModel.loginAsDemoAdmin(mockContext) { role = it }
        advanceUntilIdle()

        assertEquals(UserRole.ADMIN, role)
    }

    @Test
    fun loginAsDemoClient_cargaCredencialesYEjecuta() = runTest {
        val mockContext: android.content.Context = mockk(relaxed = true)
        val clientUser = sampleUser.copy(role = UserRole.CLIENTE)
        coEvery { authRepository.login("cliente@oyl.cl", "Cliente123", mockContext) } returns Result.success(clientUser)

        var role: UserRole? = null
        viewModel.loginAsDemoClient(mockContext) { role = it }
        advanceUntilIdle()

        assertEquals(UserRole.CLIENTE, role)
    }

    @Test
    fun logout_limpiaEstados() {
        viewModel.onEmailChanged("admin@oyl.cl")
        var loggedOut = false
        viewModel.logout { loggedOut = true }

        assertTrue(loggedOut)
        assertEquals("", viewModel.uiState.value.email)
        assertEquals("", viewModel.registerState.value.email)
    }
}
