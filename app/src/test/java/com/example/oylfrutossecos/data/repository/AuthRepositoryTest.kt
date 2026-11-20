package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.model.UserRole
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    private lateinit var repository: AuthRepository

    @Before
    fun setUp() {
        repository = AuthRepository.getInstance()
    }

    @Test
    fun login_adminEmail_retornaUsuarioAdminPredefinido() = runTest {
        val result = repository.login("admin@oyl.cl", "Admin123!")

        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals("Oscar Yáñez", user?.name)
        assertEquals(UserRole.ADMIN, user?.role)
        assertEquals(user, repository.currentUser.value)
    }

    @Test
    fun login_clienteEmail_retornaUsuarioCliente() = runTest {
        val result = repository.login("cliente@oyl.cl", "Cliente123")

        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals("Cliente Demo OYL", user?.name)
        assertEquals(UserRole.CLIENTE, user?.role)
    }

    @Test
    fun login_emailGenericoCliente_creaUsuarioCliente() = runTest {
        val result = repository.login("maria.perez@correo.cl", "123456")

        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(UserRole.CLIENTE, user?.role)
    }

    @Test
    fun login_emailConPalabraAdmin_creaUsuarioAdmin() = runTest {
        val result = repository.login("nuevo.admin@oyl.cl", "123456")

        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertEquals(UserRole.ADMIN, user?.role)
    }

    @Test
    fun logout_limpiaUsuarioActual() = runTest {
        repository.login("admin@oyl.cl", "Admin123!")
        assertNotNull(repository.currentUser.value)

        repository.logout()
        assertNull(repository.currentUser.value)
    }
}
