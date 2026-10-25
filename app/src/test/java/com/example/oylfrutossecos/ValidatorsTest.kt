package com.example.oylfrutossecos

import com.example.oylfrutossecos.domain.validation.Validators
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    @Test
    fun email_valido_y_invalido() {
        assertNull(Validators.validateEmail("admin@oylfrutossecos.cl"))
        assertNotNull(Validators.validateEmail(""))
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
    fun precio_y_stock() {
        assertNull(Validators.validatePrice("9900"))
        assertNotNull(Validators.validatePrice("0"))
        assertNotNull(Validators.validatePrice("abc"))
        assertNull(Validators.validateStock("25"))
        assertNotNull(Validators.validateStock("-1"))
        assertNotNull(Validators.validateStock("2.5"))
    }

    @Test
    fun telefono_chileno() {
        assertNull(Validators.validateChileanPhone("+56 9 9123 4567"))
        assertNull(Validators.validateChileanPhone("912345678"))
        assertNotNull(Validators.validateChileanPhone("12345"))
        assertNotNull(Validators.validateChileanPhone("+56 2 2123 4567"))
    }

    @Test
    fun direccion_y_codigo() {
        assertNull(Validators.validateAddress("Av. Providencia 1240, Providencia"))
        assertNotNull(Validators.validateAddress("Casa"))
        assertNull(Validators.validateBarcode(""))
        assertNull(Validators.validateBarcode("780998810001"))
        assertNotNull(Validators.validateBarcode("@@"))
    }

    @Test
    fun security_answer_hashing_and_case_insensitivity() {
        val hash = Validators.hashSecurityAnswer("Almendra")
        assertNotNull(hash)
        // Comparación insensible a mayúsculas y espacios
        org.junit.Assert.assertTrue(Validators.verifySecurityAnswer("almendra", hash))
        org.junit.Assert.assertTrue(Validators.verifySecurityAnswer("  ALMENDRA  ", hash))
        org.junit.Assert.assertTrue(Validators.verifySecurityAnswer("AlmEndRa", hash))
        org.junit.Assert.assertFalse(Validators.verifySecurityAnswer("Nuez", hash))
    }

    @Test
    fun masking_helpers() {
        org.junit.Assert.assertEquals("cl****@oyl.cl", Validators.maskEmail("cliente@oyl.cl"))
        org.junit.Assert.assertEquals("+56 9 ****4321", Validators.maskPhone("+56 9 8765 4321"))
    }

    @Test
    fun security_answer_and_password_not_same_validation() {
        assertNull(Validators.validateSecurityAnswer("Pelusa"))
        assertNotNull(Validators.validateSecurityAnswer(""))
        assertNotNull(Validators.validateSecurityAnswer("A"))

        assertNotNull(Validators.validateNewPasswordNotSame("Admin123!", "Admin123!"))
        assertNull(Validators.validateNewPasswordNotSame("NuevaPassword123!", "Admin123!"))
    }
}
