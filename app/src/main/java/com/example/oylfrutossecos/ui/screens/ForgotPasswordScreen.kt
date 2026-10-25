package com.example.oylfrutossecos.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oylfrutossecos.ui.components.FieldErrorIcon
import com.example.oylfrutossecos.ui.components.FieldSupportText
import com.example.oylfrutossecos.ui.theme.BrandAccent
import com.example.oylfrutossecos.ui.theme.BrandPrimary
import com.example.oylfrutossecos.ui.theme.BrandSecondary
import com.example.oylfrutossecos.ui.theme.StatusError
import com.example.oylfrutossecos.ui.viewmodel.ForgotPasswordStep
import com.example.oylfrutossecos.ui.viewmodel.ForgotPasswordViewModel
import com.example.oylfrutossecos.ui.viewmodel.RecoveryMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onNavigateBack: () -> Unit,
    onPasswordResetSuccess: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Diálogo emergente de simulación de envío de código
    if (uiState.showSimulationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSimulationDialog() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = uiState.simulationDialogTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = uiState.simulationDialogMessage,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Código: ${uiState.simulatedCodeForTesting ?: ""}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = BrandPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        uiState.simulatedCodeForTesting?.let {
                            clipboardManager.setText(AnnotatedString(it))
                            Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                        }
                        viewModel.dismissSimulationDialog()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("Copiar y Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSimulationDialog() }) {
                    Text("Cerrar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recuperar Contraseña",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!viewModel.navigateBack()) {
                            onNavigateBack()
                        }
                    }) {
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
            // Icono principal
            Surface(
                modifier = Modifier.size(70.dp),
                shape = CircleShape,
                color = BrandPrimary.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Paso actual según máquina de estados
            when (uiState.currentStep) {
                ForgotPasswordStep.STEP_1_IDENTIFY -> {
                    Step1IdentifyView(
                        uiState = uiState,
                        onEmailChange = { viewModel.onEmailChanged(it) },
                        onSubmit = { viewModel.submitEmail(context) }
                    )
                }
                ForgotPasswordStep.STEP_2_CHOOSE_METHOD -> {
                    Step2ChooseMethodView(
                        uiState = uiState,
                        onSelectMethod = { viewModel.selectMethod(it, context) }
                    )
                }
                ForgotPasswordStep.STEP_3_VERIFY_CODE -> {
                    Step3VerifyCodeView(
                        uiState = uiState,
                        onCodeChange = { viewModel.onVerificationCodeChanged(it) },
                        onVerify = { viewModel.verifyCode(context) },
                        onResend = { viewModel.resendCode(context) }
                    )
                }
                ForgotPasswordStep.STEP_3_VERIFY_QUESTION -> {
                    Step3VerifyQuestionView(
                        uiState = uiState,
                        onAnswerChange = { viewModel.onSecurityAnswerChanged(it) },
                        onVerify = { viewModel.verifySecurityAnswer() }
                    )
                }
                ForgotPasswordStep.STEP_4_NEW_PASSWORD -> {
                    Step4NewPasswordView(
                        uiState = uiState,
                        onNewPasswordChange = { viewModel.onNewPasswordFieldChanged(it) },
                        onConfirmPasswordChange = { viewModel.onConfirmPasswordFieldChanged(it) },
                        onToggleVisibility = { viewModel.togglePasswordVisibility() },
                        onSave = {
                            viewModel.saveNewPassword(context) { email ->
                                onPasswordResetSuccess(email)
                            }
                        }
                    )
                }
                ForgotPasswordStep.SUCCESS -> {
                    SuccessView(
                        email = uiState.email,
                        onProceed = { onPasswordResetSuccess(uiState.email) }
                    )
                }
            }
        }
    }
}

// --- VISTAS DE CADA PASO ---

@Composable
private fun Step1IdentifyView(
    uiState: com.example.oylfrutossecos.ui.viewmodel.ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Text(
        text = "Paso 1: Identificación",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BrandPrimary)
    )
    Text(
        text = "Ingresa el correo electrónico asociado a tu cuenta de OYL",
        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, textAlign = TextAlign.Center)
    )

    Spacer(modifier = Modifier.height(18.dp))

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text("Correo Electrónico") },
                placeholder = { Text("ejemplo@oyl.cl") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BrandPrimary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = uiState.emailError != null,
                supportingText = { FieldSupportText(uiState.emailError) },
                trailingIcon = { FieldErrorIcon(uiState.emailError) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Mensaje genérico de seguridad si el correo no existe
            if (uiState.step1InfoMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BrandSecondary.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = BrandSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.step1InfoMessage,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = BrandSecondary)
                        )
                    }
                }
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Continuar", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun Step2ChooseMethodView(
    uiState: com.example.oylfrutossecos.ui.viewmodel.ForgotPasswordUiState,
    onSelectMethod: (RecoveryMethod) -> Unit
) {
    Text(
        text = "Paso 2: Método de Verificación",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BrandPrimary)
    )
    Text(
        text = "Selecciona cómo deseas verificar tu identidad:",
        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, textAlign = TextAlign.Center)
    )

    Spacer(modifier = Modifier.height(18.dp))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        // Opción A: Correo
        MethodOptionCard(
            title = "Enviar código a mi correo",
            subtitle = uiState.maskedEmail,
            icon = Icons.Default.Email,
            onClick = { onSelectMethod(RecoveryMethod.EMAIL) }
        )

        // Opción B: SMS (solo si tiene teléfono registrado)
        if (uiState.hasPhone && uiState.maskedPhone != null) {
            MethodOptionCard(
                title = "Enviar código por SMS a mi teléfono",
                subtitle = uiState.maskedPhone,
                icon = Icons.Default.PhoneAndroid,
                onClick = { onSelectMethod(RecoveryMethod.SMS) }
            )
        }

        // Opción C: Pregunta de seguridad
        if (uiState.hasSecurityQuestion) {
            MethodOptionCard(
                title = "Responder mi pregunta de seguridad",
                subtitle = "Responde la pregunta configurada en tu perfil",
                icon = Icons.Default.Security,
                onClick = { onSelectMethod(RecoveryMethod.SECURITY_QUESTION) }
            )
        }

        if (uiState.generalError != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = StatusError.copy(alpha = 0.12f)
            ) {
                Text(
                    text = uiState.generalError,
                    color = StatusError,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun MethodOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                shape = CircleShape,
                color = BrandPrimary.copy(alpha = 0.12f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
private fun Step3VerifyCodeView(
    uiState: com.example.oylfrutossecos.ui.viewmodel.ForgotPasswordUiState,
    onCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit
) {
    Text(
        text = "Paso 3: Verificación de Código",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BrandPrimary)
    )
    Text(
        text = "Ingresa el código de 6 dígitos que fue enviado",
        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
    )

    Spacer(modifier = Modifier.height(18.dp))

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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLocked) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StatusError.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusError)
                        Spacer(modifier = Modifier.width(8.dp))
                        val min = uiState.lockRemainingSeconds / 60
                        val sec = uiState.lockRemainingSeconds % 60
                        Text(
                            text = "Bloqueado por 3 intentos fallidos. Espera ${min}m ${sec}s.",
                            color = StatusError,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.verificationCode,
                onValueChange = onCodeChange,
                label = { Text("Código de 6 dígitos") },
                placeholder = { Text("123456") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = uiState.verificationCodeError != null,
                supportingText = { FieldSupportText(uiState.verificationCodeError) },
                enabled = !uiState.isLocked && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Intentos restantes: ${uiState.attemptsLeft} de 3",
                    fontSize = 11.sp,
                    color = if (uiState.attemptsLeft <= 1) StatusError else Color.Gray
                )
                Text(
                    text = "Expira en 5 min",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = onVerify,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLocked && !uiState.isLoading && uiState.verificationCode.length == 6,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Verificar Código", fontWeight = FontWeight.Bold)
                }
            }

            // Botón de reenvío con cuenta regresiva de 60s
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (uiState.resendCountdownSeconds > 0) {
                    Text(
                        text = "Reenviar código en ${uiState.resendCountdownSeconds}s",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                } else {
                    TextButton(
                        onClick = onResend,
                        enabled = !uiState.isLocked && !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reenviar código", fontWeight = FontWeight.Bold, color = BrandPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun Step3VerifyQuestionView(
    uiState: com.example.oylfrutossecos.ui.viewmodel.ForgotPasswordUiState,
    onAnswerChange: (String) -> Unit,
    onVerify: () -> Unit
) {
    Text(
        text = "Paso 3: Pregunta de Seguridad",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BrandPrimary)
    )
    Text(
        text = "Responde la pregunta configurada en tu registro",
        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
    )

    Spacer(modifier = Modifier.height(18.dp))

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BrandPrimary.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = BrandPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = uiState.securityQuestion,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            OutlinedTextField(
                value = uiState.securityAnswer,
                onValueChange = onAnswerChange,
                label = { Text("Tu Respuesta") },
                placeholder = { Text("Escribe tu respuesta aquí") },
                singleLine = true,
                isError = uiState.securityAnswerError != null,
                supportingText = {
                    FieldSupportText(uiState.securityAnswerError ?: "No distingue mayúsculas ni espacios.")
                },
                trailingIcon = { FieldErrorIcon(uiState.securityAnswerError) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            if (uiState.generalError != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StatusError.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.generalError,
                        color = StatusError,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Button(
                onClick = onVerify,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState.securityAnswer.isNotBlank() && !uiState.isLocked,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) {
                Text("Verificar Respuesta", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun Step4NewPasswordView(
    uiState: com.example.oylfrutossecos.ui.viewmodel.ForgotPasswordUiState,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onSave: () -> Unit
) {
    Text(
        text = "Paso 4: Nueva Contraseña",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BrandPrimary)
    )
    Text(
        text = "Crea una nueva contraseña segura para tu cuenta",
        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
    )

    Spacer(modifier = Modifier.height(18.dp))

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = uiState.newPassword,
                onValueChange = onNewPasswordChange,
                label = { Text("Nueva Contraseña (mínimo 8 caracteres)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandPrimary) },
                trailingIcon = {
                    IconButton(onClick = onToggleVisibility) {
                        Icon(
                            imageVector = if (uiState.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = BrandPrimary
                        )
                    }
                },
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = uiState.newPasswordError != null,
                supportingText = { FieldSupportText(uiState.newPasswordError) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("Confirmar Nueva Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandPrimary) },
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = uiState.confirmPasswordError != null,
                supportingText = { FieldSupportText(uiState.confirmPasswordError) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            if (uiState.generalError != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StatusError.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.generalError,
                        color = StatusError,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Guardar Contraseña", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SuccessView(
    email: String,
    onProceed: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = BrandSecondary,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = "¡Contraseña Actualizada!",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BrandSecondary)
            )
            Text(
                text = "Tu contraseña ha sido modificada exitosamente. Ahora puedes iniciar sesión con tus nuevas credenciales.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )

            Button(
                onClick = onProceed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) {
                Text("Ir a Iniciar Sesión", fontWeight = FontWeight.Bold)
            }
        }
    }
}
