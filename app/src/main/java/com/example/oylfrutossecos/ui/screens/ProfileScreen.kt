package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.oylfrutossecos.domain.validation.Validators
import com.example.oylfrutossecos.data.model.UserRole
import com.example.oylfrutossecos.ui.components.FieldErrorIcon
import com.example.oylfrutossecos.ui.components.FieldSupportText
import com.example.oylfrutossecos.ui.theme.BrandAccent
import com.example.oylfrutossecos.ui.theme.BrandPrimary
import com.example.oylfrutossecos.ui.theme.BrandSecondary
import com.example.oylfrutossecos.ui.theme.StatusError
import com.example.oylfrutossecos.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onLogoutSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val state by authViewModel.profileState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var questionMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        authViewModel.initProfileState(currentUser)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Perfil",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout(onLogoutSuccess)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandPrimary)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con inicial y Badge de Rol
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = if (currentUser?.role == UserRole.ADMIN) BrandPrimary.copy(alpha = 0.15f) else BrandSecondary.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = (currentUser?.name?.take(1) ?: "U").uppercase(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentUser?.role == UserRole.ADMIN) BrandPrimary else BrandSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.name ?: "Usuario",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = currentUser?.email ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentUser?.role == UserRole.ADMIN) BrandPrimary.copy(alpha = 0.12f) else BrandSecondary.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (currentUser?.role == UserRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = if (currentUser?.role == UserRole.ADMIN) BrandPrimary else BrandSecondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentUser?.role?.displayName ?: "Usuario",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentUser?.role == UserRole.ADMIN) BrandPrimary else BrandSecondary,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Formulario de Edición de Perfil
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Datos Personales",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = BrandPrimary
                    )

                    // Nombre
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { authViewModel.onProfileFieldChanged(name = it) },
                        label = { Text("Nombre Completo") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = state.nameError != null,
                        supportingText = { FieldSupportText(state.nameError) },
                        trailingIcon = { FieldErrorIcon(state.nameError) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Teléfono
                    OutlinedTextField(
                        value = state.phone,
                        onValueChange = { authViewModel.onProfileFieldChanged(phone = it) },
                        label = { Text("Teléfono de Contacto") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BrandPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = state.phoneError != null,
                        supportingText = { FieldSupportText(state.phoneError) },
                        trailingIcon = { FieldErrorIcon(state.phoneError) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Dirección
                    OutlinedTextField(
                        value = state.address,
                        onValueChange = { authViewModel.onProfileFieldChanged(address = it) },
                        label = { Text("Dirección") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = BrandPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.addressError != null,
                        supportingText = { FieldSupportText(state.addressError) },
                        trailingIcon = { FieldErrorIcon(state.addressError) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Cambiar Contraseña (opcional)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = BrandPrimary
                    )

                    // Nueva Contraseña
                    OutlinedTextField(
                        value = state.newPassword,
                        onValueChange = { authViewModel.onProfileFieldChanged(newPassword = it) },
                        label = { Text("Nueva Contraseña (8+ caracteres)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandPrimary) },
                        trailingIcon = {
                            IconButton(onClick = { authViewModel.toggleProfilePasswordVisibility() }) {
                                Icon(
                                    imageVector = if (state.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = BrandPrimary
                                )
                            }
                        },
                        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = state.passwordError != null,
                        supportingText = { FieldSupportText(state.passwordError) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Confirmar Nueva Contraseña
                    if (state.newPassword.isNotBlank()) {
                        OutlinedTextField(
                            value = state.confirmPassword,
                            onValueChange = { authViewModel.onProfileFieldChanged(confirmPassword = it) },
                            label = { Text("Confirmar Nueva Contraseña") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandPrimary) },
                            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            isError = state.confirmPasswordError != null,
                            supportingText = { FieldSupportText(state.confirmPasswordError) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Pregunta de Seguridad",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = BrandPrimary
                    )

                    // Selector de Pregunta de Seguridad
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = state.newSecurityQuestion,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pregunta de Seguridad") },
                            leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = BrandPrimary) },
                            trailingIcon = {
                                IconButton(onClick = { questionMenuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Desplegar preguntas",
                                        tint = BrandPrimary
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { questionMenuExpanded = true },
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = questionMenuExpanded,
                            onDismissRequest = { questionMenuExpanded = false }
                        ) {
                            Validators.SECURITY_QUESTIONS.forEach { q ->
                                DropdownMenuItem(
                                    text = { Text(q) },
                                    onClick = {
                                        authViewModel.onProfileFieldChanged(newSecurityQuestion = q)
                                        questionMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Respuesta de Seguridad
                    OutlinedTextField(
                        value = state.securityAnswer,
                        onValueChange = { authViewModel.onProfileFieldChanged(securityAnswer = it) },
                        label = { Text("Nueva Respuesta (opcional si no cambias pregunta)") },
                        placeholder = { Text("Escribe una nueva respuesta") },
                        leadingIcon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = BrandPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = state.securityAnswerError != null,
                        supportingText = {
                            FieldSupportText(state.securityAnswerError ?: "Se guardará cifrada con hash SHA-256")
                        },
                        trailingIcon = { FieldErrorIcon(state.securityAnswerError) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Mensajes de éxito o error
                    if (state.successMessage != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = BrandSecondary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = state.successMessage!!,
                                color = BrandSecondary,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    if (state.generalError != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = StatusError.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = state.generalError!!,
                                color = StatusError,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            authViewModel.updateProfile(context) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Perfil actualizado con éxito")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar Cambios", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Botón Cerrar Sesión
            OutlinedButton(
                onClick = {
                    authViewModel.logout(onLogoutSuccess)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = StatusError)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold, color = StatusError)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
