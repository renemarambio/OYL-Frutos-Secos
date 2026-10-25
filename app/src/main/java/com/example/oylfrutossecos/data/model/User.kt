package com.example.oylfrutossecos.data.model

/**
 * Roles del sistema: ADMIN gestiona inventario y ventas; CLIENTE realiza compras y pedidos.
 */
enum class UserRole {
    ADMIN,
    CLIENTE;

    val displayName: String
        get() = when (this) {
            ADMIN -> "Administrador OYL"
            CLIENTE -> "Cliente"
        }

    companion object {
        fun fromString(value: String): UserRole {
            return when (value.uppercase().trim()) {
                "ADMIN", "ADMINISTRADOR", "ADMINISTRADOR OYL", "ADMINISTRADOR DE VENTAS" -> ADMIN
                else -> CLIENTE
            }
        }
    }
}

data class User(
    val id: Long = 1L,
    val name: String,
    val email: String,
    val password: String = "",
    val role: UserRole = UserRole.ADMIN,
    val phone: String = "+56 9 9123 4567",
    val address: String = "Av. Providencia 1820, Santiago",
    val securityQuestion: String = "",
    val securityAnswerHash: String = ""
)
