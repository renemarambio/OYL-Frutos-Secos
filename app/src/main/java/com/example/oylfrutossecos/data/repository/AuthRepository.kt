package com.example.oylfrutossecos.data.repository

import android.content.Context
import com.example.oylfrutossecos.data.local.AppDatabaseHelper
import com.example.oylfrutossecos.data.model.User
import com.example.oylfrutossecos.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AuthRepository private constructor() {

    private val _currentUser = MutableStateFlow<User?>(
        User(
            id = 1L,
            name = "Oscar Yáñez",
            email = "admin@oyl.cl",
            password = "Admin123!",
            role = UserRole.ADMIN,
            phone = "+56 9 9123 4567",
            address = "Av. Providencia #1820, Providencia"
        )
    )
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    suspend fun login(
        email: String,
        password: String = "",
        context: Context? = null
    ): Result<User> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()

        // 1. Si hay context, consultar SQLite
        if (context != null) {
            val db = AppDatabaseHelper.getInstance(context)
            val existingUser = db.getUserByEmail(cleanEmail)

            if (existingUser != null) {
                // Si viene contraseña, validar; si no viene (login directo demo), permitir
                if (password.isNotBlank() && existingUser.password.isNotBlank() && existingUser.password != password) {
                    return@withContext Result.failure(IllegalArgumentException("Contraseña incorrecta"))
                }
                _currentUser.value = existingUser
                return@withContext Result.success(existingUser)
            }
        }

        // 2. Compatibilidad con credenciales precargadas directas
        val fallbackUser = when (cleanEmail) {
            "admin@oyl.cl", "oscar.yanez@oylfrutossecos.cl" -> {
                User(
                    id = 1L,
                    name = "Oscar Yáñez",
                    email = cleanEmail,
                    password = if (password.isBlank()) "Admin123!" else password,
                    role = UserRole.ADMIN,
                    phone = "+56 9 9123 4567",
                    address = "Av. Providencia #1820, Providencia",
                    securityQuestion = "¿Nombre de tu primera mascota?",
                    securityAnswerHash = com.example.oylfrutossecos.domain.validation.Validators.hashSecurityAnswer("Almendra")
                )
            }
            "cliente@oyl.cl" -> {
                User(
                    id = 2L,
                    name = "Cliente Demo OYL",
                    email = cleanEmail,
                    password = if (password.isBlank()) "Cliente123" else password,
                    role = UserRole.CLIENTE,
                    phone = "+56 9 8765 4321",
                    address = "Av. Providencia 1240, Dpto 802, Providencia",
                    securityQuestion = "¿Ciudad donde naciste?",
                    securityAnswerHash = com.example.oylfrutossecos.domain.validation.Validators.hashSecurityAnswer("Santiago")
                )
            }
            else -> {
                // Nuevo usuario generado si se loguea como invitado/demo
                val nameFromEmail = cleanEmail.substringBefore("@").replace(".", " ")
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                val isEmailAdmin = cleanEmail.contains("admin") || cleanEmail.contains("vendedor")
                User(
                    id = System.currentTimeMillis(),
                    name = if (nameFromEmail.isBlank()) "Usuario OYL" else nameFromEmail,
                    email = cleanEmail,
                    password = password,
                    role = if (isEmailAdmin) UserRole.ADMIN else UserRole.CLIENTE,
                    phone = "+56 9 8765 4321",
                    address = "Santiago, Chile"
                )
            }
        }

        // Si hay BD, persistir el usuario generado para consistencia
        if (context != null) {
            try {
                val db = AppDatabaseHelper.getInstance(context)
                if (db.getUserByEmail(cleanEmail) == null) {
                    val id = db.insertUser(fallbackUser)
                    val saved = fallbackUser.copy(id = id)
                    _currentUser.value = saved
                    return@withContext Result.success(saved)
                }
            } catch (_: Exception) {}
        }

        _currentUser.value = fallbackUser
        Result.success(fallbackUser)
    }

    suspend fun registerClient(
        user: User,
        context: Context
    ): Result<User> = withContext(Dispatchers.IO) {
        val db = AppDatabaseHelper.getInstance(context)
        val cleanEmail = user.email.trim().lowercase()
        if (db.getUserByEmail(cleanEmail) != null) {
            return@withContext Result.failure(IllegalArgumentException("El correo ya se encuentra registrado"))
        }

        val clientUser = user.copy(
            email = cleanEmail,
            role = UserRole.CLIENTE
        )
        val id = db.insertUser(clientUser)
        val created = clientUser.copy(id = id)
        _currentUser.value = created
        Result.success(created)
    }

    suspend fun updateProfile(
        user: User,
        context: Context
    ): Result<User> = withContext(Dispatchers.IO) {
        val db = AppDatabaseHelper.getInstance(context)
        val rows = db.updateUser(user)
        if (rows > 0 || user.id == 0L) {
            _currentUser.value = user
            Result.success(user)
        } else {
            // Intentar reinsertar o refrescar
            val id = db.insertUser(user)
            val updated = user.copy(id = id)
            _currentUser.value = updated
            Result.success(updated)
        }
    }

    suspend fun getUserByEmail(email: String, context: Context): User? = withContext(Dispatchers.IO) {
        val db = AppDatabaseHelper.getInstance(context)
        db.getUserByEmail(email.trim().lowercase())
    }

    suspend fun generateRecoveryCode(email: String, context: Context): Result<String> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val db = AppDatabaseHelper.getInstance(context)
        val user = db.getUserByEmail(cleanEmail)
            ?: return@withContext Result.failure(IllegalArgumentException("Usuario no encontrado"))

        // Generar código aleatorio de 6 dígitos (simulado)
        val code = (100000..999999).random().toString()
        db.savePasswordResetCode(cleanEmail, code)
        Result.success(code)
    }

    suspend fun verifyRecoveryCode(email: String, code: String, context: Context): com.example.oylfrutossecos.data.local.VerifyCodeResult = withContext(Dispatchers.IO) {
        val db = AppDatabaseHelper.getInstance(context)
        db.verifyResetCode(email, code)
    }

    suspend fun resetPassword(email: String, newPassword: String, context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        val db = AppDatabaseHelper.getInstance(context)
        val success = db.updateUserPassword(email, newPassword)
        if (success) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("No se pudo actualizar la contraseña"))
        }
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
