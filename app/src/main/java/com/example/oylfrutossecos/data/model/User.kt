package com.example.oylfrutossecos.data.model

data class User(
    val id: Long = 1,
    val name: String,
    val email: String,
    val role: String = "Administrador de Ventas",
    val phone: String = "+56 9 8765 4321"
)
