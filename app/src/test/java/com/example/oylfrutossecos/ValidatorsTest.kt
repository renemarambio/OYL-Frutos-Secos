package com.example.oylfrutossecos

import com.example.oylfrutossecos.domain.validation.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test
    fun email_valido_y_invalido() {
        assertNull(Validators.validateEmail("admin@oylfrutossecos.cl"))
        assertNotNull(Validators.validateEmail(""))
        assertNotNull(Validators.validateEmail("   "))
        assertNotNull(Validators.validateEmail("admin@"))
        assertNotNull(Validators.validateEmail("sin-arroba.cl"))
    }

    @Test
    fun password_requiere_largo_minimo() {
        assertNull(Validators.validatePassword("123456"))
        assertNotNull(Validators.validatePassword("123"))
        assertNotNull(Validators.validatePassword(""))
    }

    @Test
    fun validateRegistrationPassword_requiere_8_caracteres() {
        assertNull(Validators.validateRegistrationPassword("12345678"))
        assertNotNull(Validators.validateRegistrationPassword("1234567"))
        assertNotNull(Validators.validateRegistrationPassword(""))
    }

    @Test
    fun validateConfirmPassword_compara_ambas_passwords() {
        assertNull(Validators.validateConfirmPassword("Clave123", "Clave123"))
        assertNotNull(Validators.validateConfirmPassword("Clave123", "Clave456"))
        assertNotNull(Validators.validateConfirmPassword("Clave123", ""))
    }

    @Test
    fun validatePersonName_valida_longitud_y_digitos() {
        assertNull(Validators.validatePersonName("Rene Marambio"))
        assertNotNull(Validators.validatePersonName(""))
        assertNotNull(Validators.validatePersonName("Re"))
        assertNotNull(Validators.validatePersonName("Rene123"))
    }

    @Test
    fun validateProductName_valida_longitud() {
        assertNull(Validators.validateProductName("Nuez Mariposa"))
        assertNotNull(Validators.validateProductName(""))
        assertNotNull(Validators.validateProductName("Nu"))
    }

    @Test
    fun precio_y_stock() {
        assertNull(Validators.validatePrice("9900"))
        assertNotNull(Validators.validatePrice(""))
        assertNotNull(Validators.validatePrice("0"))
        assertNotNull(Validators.validatePrice("-100"))
        assertNotNull(Validators.validatePrice("abc"))
        assertNotNull(Validators.validatePrice("1000001"))

        assertNull(Validators.validateStock("25"))
        assertNotNull(Validators.validateStock(""))
        assertNotNull(Validators.validateStock("-1"))
        assertNotNull(Validators.validateStock("2.5"))
        assertNotNull(Validators.validateStock("10001"))
    }

    @Test
    fun telefono_chileno() {
        assertNull(Validators.validateChileanPhone("+56 9 9123 4567"))
        assertNull(Validators.validateChileanPhone("912345678"))
        assertNotNull(Validators.validateChileanPhone(""))
        assertNotNull(Validators.validateChileanPhone("12345"))
        assertNotNull(Validators.validateChileanPhone("+56 2 2123 4567"))
    }

    @Test
    fun direccion_y_codigo() {
        assertNull(Validators.validateAddress("Av. Providencia 1240, Providencia"))
        assertNotNull(Validators.validateAddress(""))
        assertNotNull(Validators.validateAddress("Casa"))
        assertNotNull(Validators.validateAddress("Av. Providencia sin numero"))

        assertNull(Validators.validateBarcode(""))
        assertNull(Validators.validateBarcode("   "))
        assertNull(Validators.validateBarcode("780998810001"))
        assertNotNull(Validators.validateBarcode("@@"))
        assertNotNull(Validators.validateBarcode("12"))
    }

    @Test
    fun security_answer_hashing_and_case_insensitivity() {
        val hash = Validators.hashSecurityAnswer("Almendra")
        assertNotNull(hash)
        assertTrue(Validators.verifySecurityAnswer("almendra", hash))
        assertTrue(Validators.verifySecurityAnswer("  ALMENDRA  ", hash))
        assertTrue(Validators.verifySecurityAnswer("AlmEndRa", hash))
        assertFalse(Validators.verifySecurityAnswer("Nuez", hash))
        assertFalse(Validators.verifySecurityAnswer("Almendra", ""))
    }

    @Test
    fun masking_helpers() {
        assertEquals("cl****@oyl.cl", Validators.maskEmail("cliente@oyl.cl"))
        assertEquals("a****@oyl.cl", Validators.maskEmail("ab@oyl.cl"))
        assertEquals("sincorreo", Validators.maskEmail("sincorreo"))
        assertEquals("+56 9 ****4321", Validators.maskPhone("+56 9 8765 4321"))
        assertEquals("+56 9 ****12", Validators.maskPhone("12"))
    }

    @Test
    fun security_answer_and_password_not_same_validation() {
        assertNull(Validators.validateSecurityAnswer("Pelusa"))
        assertNotNull(Validators.validateSecurityAnswer(""))
        assertNotNull(Validators.validateSecurityAnswer("A"))

        assertNotNull(Validators.validateNewPasswordNotSame("Admin123!", "Admin123!"))
        assertNull(Validators.validateNewPasswordNotSame("NuevaPassword123!", "Admin123!"))
        assertNull(Validators.validateNewPasswordNotSame("NuevaPassword123!", ""))
    }

    @Test
    fun security_questions_contiene_opciones() {
        assertTrue(Validators.SECURITY_QUESTIONS.size >= 4)
    }
}
