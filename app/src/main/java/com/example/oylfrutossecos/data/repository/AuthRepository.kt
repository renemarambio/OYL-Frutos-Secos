package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository private constructor() {

    private val _currentUser = MutableStateFlow<User?>(
        User(
            name = "Oscar Yáñez",
            email = "oscar.yanez@oylfrutossecos.cl",
            role = "Administrador OYL",
            phone = "+56 9 9123 4567"
        )
    )
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun login(email: String, name: String = "Oscar Yáñez"): Boolean {
        _currentUser.value = User(
            name = if (name.isBlank()) "Usuario OYL" else name,
            email = email,
            role = "Administrador de Ventas",
            phone = "+56 9 9123 4567"
        )
        return true
    }

    fun logout() {
        _currentUser.value = null
    }

    companion object {
        @Volatile
        private var INSTANCE: AuthRepository? = null

        fun getInstance(): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AuthRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}
