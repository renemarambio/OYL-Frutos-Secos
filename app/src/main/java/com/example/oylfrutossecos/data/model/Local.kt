package com.example.oylfrutossecos.data.model

data class Local(
    val id: String,
    val nombre: String,
    val direccion: String,
    val comuna: String,
    val horario: String,
    val telefono: String,
    val latitud: Double,
    val longitud: Double,
    val esCasaMatriz: Boolean = false
)
