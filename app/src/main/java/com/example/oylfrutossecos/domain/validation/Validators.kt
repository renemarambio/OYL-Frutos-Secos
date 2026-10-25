package com.example.oylfrutossecos.domain.validation

/**
 * Reglas de validación centralizadas de la aplicación.
 *
 * Cada función recibe el valor ingresado y devuelve:
 *  - null  -> el valor es válido
 *  - String -> mensaje de error para mostrar bajo el campo
 */
object Validators {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // Celular chileno: +56 9 XXXX XXXX (se aceptan espacios y el +56 opcional)
    private val CHILE_PHONE_REGEX = Regex("^(\\+?56)?9\\d{8}$")

    private val BARCODE_REGEX = Regex("^[A-Za-z0-9-]{4,20}$")

    const val MIN_LOGIN_PASSWORD_LENGTH = 6
    const val MIN_REGISTER_PASSWORD_LENGTH = 8
    const val MIN_NAME_LENGTH = 3
    const val MIN_ADDRESS_LENGTH = 8
    const val MAX_PRICE = 1_000_000.0
    const val MAX_STOCK = 10_000

    fun validateEmail(email: String): String? {
        val value = email.trim()
        return when {
            value.isBlank() -> "El correo electrónico es obligatorio"
            !EMAIL_REGEX.matches(value) -> "Ingresa un correo válido (ej: usuario@correo.cl)"
            else -> null
        }
    }

    fun validatePassword(password: String): String? = when {
        password.isBlank() -> "La contraseña es obligatoria"
        password.length < MIN_LOGIN_PASSWORD_LENGTH -> "Debe tener al menos $MIN_LOGIN_PASSWORD_LENGTH caracteres"
        else -> null
    }

    fun validateRegistrationPassword(password: String): String? = when {
        password.isBlank() -> "La contraseña es obligatoria"
        password.length < MIN_REGISTER_PASSWORD_LENGTH -> "La contraseña debe tener al menos $MIN_REGISTER_PASSWORD_LENGTH caracteres"
        else -> null
    }

    fun validateConfirmPassword(password: String, confirm: String): String? = when {
        confirm.isBlank() -> "Confirma tu contraseña"
        password != confirm -> "Las contraseñas no coinciden"
        else -> null
    }

    fun validatePersonName(name: String): String? {
        val value = name.trim()
        return when {
            value.isBlank() -> "El nombre es obligatorio"
            value.length < MIN_NAME_LENGTH -> "El nombre debe tener al menos $MIN_NAME_LENGTH letras"
            value.any { it.isDigit() } -> "El nombre no puede contener números"
            else -> null
        }
    }

    fun validateProductName(name: String): String? {
        val value = name.trim()
        return when {
            value.isBlank() -> "El nombre del producto es obligatorio"
            value.length < MIN_NAME_LENGTH -> "El nombre debe tener al menos $MIN_NAME_LENGTH caracteres"
            else -> null
        }
    }

    fun validatePrice(price: String): String? {
        val value = price.trim().toDoubleOrNull()
        return when {
            price.isBlank() -> "El precio es obligatorio"
            value == null -> "Ingresa solo números"
            value <= 0 -> "El precio debe ser mayor a 0"
            value > MAX_PRICE -> "El precio no puede superar $1.000.000"
            else -> null
        }
    }

    fun validateStock(stock: String): String? {
        val value = stock.trim().toIntOrNull()
        return when {
            stock.isBlank() -> "El stock es obligatorio"
            value == null -> "Ingresa un número entero"
            value < 0 -> "El stock no puede ser negativo"
            value > MAX_STOCK -> "El stock no puede superar $MAX_STOCK"
            else -> null
        }
    }

    /** El código es opcional: si viene vacío se genera uno automáticamente. */
    fun validateBarcode(barcode: String): String? {
        val value = barcode.trim()
        return when {
            value.isEmpty() -> null
            !BARCODE_REGEX.matches(value) -> "Usa entre 4 y 20 letras, números o guiones"
            else -> null
        }
    }

    fun validateChileanPhone(phone: String): String? {
        val digits = phone.replace(" ", "").replace("-", "")
        return when {
            digits.isBlank() -> "El teléfono es obligatorio"
            !CHILE_PHONE_REGEX.matches(digits) -> "Formato válido: +56 9 1234 5678"
            else -> null
        }
    }

    val SECURITY_QUESTIONS = listOf(
        "¿Nombre de tu primera mascota?",
        "¿Ciudad donde naciste?",
        "¿Nombre de tu colegio de infancia?",
        "¿Cuál es tu fruto seco favorito?"
    )

    fun hashSecurityAnswer(answer: String): String {
        val normalized = answer.trim().lowercase()
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(normalized.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifySecurityAnswer(input: String, expectedHash: String): Boolean {
        if (expectedHash.isBlank()) return false
        return hashSecurityAnswer(input) == expectedHash.lowercase()
    }

    fun maskEmail(email: String): String {
        val clean = email.trim()
        val atIndex = clean.indexOf('@')
        if (atIndex <= 0) return clean
        val username = clean.substring(0, atIndex)
        val domain = clean.substring(atIndex)
        return when {
            username.length <= 2 -> "${username.first()}****$domain"
            else -> "${username.take(2)}****$domain"
        }
    }

    fun maskPhone(phone: String): String {
        val clean = phone.trim()
        val digits = clean.filter { it.isDigit() }
        val last4 = if (digits.length >= 4) digits.takeLast(4) else digits
        return "+56 9 ****$last4"
    }

    fun validateSecurityAnswer(answer: String): String? {
        val value = answer.trim()
        return when {
            value.isBlank() -> "La respuesta de seguridad es obligatoria"
            value.length < 2 -> "La respuesta debe tener al menos 2 caracteres"
            else -> null
        }
    }

    fun validateNewPasswordNotSame(newPassword: String, oldPassword: String): String? {
        return if (oldPassword.isNotBlank() && newPassword == oldPassword) {
            "La nueva contraseña no puede ser igual a la anterior"
        } else {
            null
        }
    }

    fun validateAddress(address: String): String? {
        val value = address.trim()
        return when {
            value.isBlank() -> "La dirección es obligatoria"
            value.length < MIN_ADDRESS_LENGTH -> "Escribe calle, número y comuna"
            !value.any { it.isDigit() } -> "Incluye el número de la dirección"
            else -> null
        }
    }
}
