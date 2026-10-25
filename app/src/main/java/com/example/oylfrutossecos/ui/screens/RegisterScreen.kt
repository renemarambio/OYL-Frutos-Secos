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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Spa
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.oylfrutossecos.domain.validation.Validators
import com.example.oylfrutossecos.ui.components.FieldErrorIcon
import com.example.oylfrutossecos.ui.components.FieldSupportText
import com.example.oylfrutossecos.ui.theme.BrandAccent
import com.example.oylfrutossecos.ui.theme.BrandPrimary
import com.example.oylfrutossecos.ui.theme.BrandSecondary
import com.example.oylfrutossecos.ui.theme.StatusError
import com.example.oylfrutossecos.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by authViewModel.registerState.collectAsState()
    val context = LocalContext.current
    var questionMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Registro de Cliente",
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = BrandPrimary.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Crea tu Cuenta de Cliente",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary
                )
            )

            Text(
                text = "Accede al catálogo de frutos secos y recibe en tu dirección",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Tarjeta con los campos del formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Nombre completo
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { authViewModel.onRegisterFieldChanged(name = it) },
                        label = { Text("Nombre Completo") },
                        placeholder = { Text("Ej: Constanza Morales") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = state.nameError != null,
                        supportingText = { FieldSupportText(state.nameError) },
                        trailingIcon = { FieldErrorIcon(state.nameError) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Correo electrónico
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { authViewModel.onRegisterFieldChanged(email = it) },
                        label = { Text("Correo Electrónico") },
                        placeholder = { Text("cliente@correo.cl") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BrandPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = state.emailError != null,
                        supportingText = { FieldSupportText(state.emailError) },
                        trailingIcon = { FieldErrorIcon(state.emailError) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Teléfono
                    OutlinedTextField(
                        value = state.phone,
                        onValueChange = { authViewModel.onRegisterFieldChanged(phone = it) },
                        label = { Text("Teléfono de Contacto") },
                        placeholder = { Text("+56 9 1234 5678") },
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
                        onValueChange = { authViewModel.onRegisterFieldChanged(address = it) },
                        label = { Text("Dirección de Despacho (Calle, número y comuna)") },
                        placeholder = { Text("Av. Providencia 1240, Dpto 802, Providencia") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = BrandPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.addressError != null,
                        supportingText = { FieldSupportText(state.addressError) },
                        trailingIcon = { FieldErrorIcon(state.addressError) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Contraseña (mínimo 8 caracteres)
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { authViewModel.onRegisterFieldChanged(password = it) },
                        label = { Text("Contraseña (al menos 8 caracteres)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandPrimary) },
                        trailingIcon = {
                            IconButton(onClick = { authViewModel.toggleRegisterPasswordVisibility() }) {
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

                    // Confirmar contraseña
                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = { authViewModel.onRegisterFieldChanged(confirmPassword = it) },
                        label = { Text("Confirmar Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandPrimary) },
                        trailingIcon = {
                            IconButton(onClick = { authViewModel.toggleRegisterConfirmVisibility() }) {
                                Icon(
                                    imageVector = if (state.isConfirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = BrandPrimary
                                )
                            }
                        },
                        visualTransformation = if (state.isConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = state.confirmPasswordError != null,
                        supportingText = { FieldSupportText(state.confirmPasswordError) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Selector de Pregunta de Seguridad
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = state.securityQuestion,
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
                                        authViewModel.onRegisterFieldChanged(securityQuestion = q)
                                        questionMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Respuesta de Seguridad (Obligatoria)
                    OutlinedTextField(
                        value = state.securityAnswer,
                        onValueChange = { authViewModel.onRegisterFieldChanged(securityAnswer = it) },
                        label = { Text("Respuesta de Seguridad (Obligatoria)") },
                        placeholder = { Text("Ej: Pelusa / Santiago") },
                        leadingIcon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = BrandPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = state.securityAnswerError != null,
                        supportingText = {
                            FieldSupportText(state.securityAnswerError ?: "Se guardará como hash seguro para recuperar tu cuenta")
                        },
                        trailingIcon = { FieldErrorIcon(state.securityAnswerError) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Error general (ej: correo ya repetido)
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
                        onClick = { authViewModel.register(context, onRegisterSuccess) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
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
                            Text("Registrarme", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "¿Ya tienes una cuenta?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Inicia Sesión",
                        color = BrandPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
