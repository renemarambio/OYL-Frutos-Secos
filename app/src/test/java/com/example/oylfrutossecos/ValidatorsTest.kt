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
}
