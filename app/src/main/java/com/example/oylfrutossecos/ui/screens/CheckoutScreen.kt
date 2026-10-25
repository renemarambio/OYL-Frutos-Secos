package com.example.oylfrutossecos.ui.screens

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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oylfrutossecos.data.model.Sale
import com.example.oylfrutossecos.ui.components.FieldErrorIcon
import com.example.oylfrutossecos.ui.components.FieldSupportText
import com.example.oylfrutossecos.ui.theme.BrandAccent
import com.example.oylfrutossecos.ui.theme.BrandPrimary
import com.example.oylfrutossecos.ui.theme.BrandSecondary
import com.example.oylfrutossecos.ui.viewmodel.AuthViewModel
import com.example.oylfrutossecos.ui.viewmodel.CartViewModel
import com.example.oylfrutossecos.ui.viewmodel.SalesViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    salesViewModel: SalesViewModel,
    authViewModel: AuthViewModel,
    onNavigateToMap: () -> Unit,
    onNavigateToMyOrders: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()
    val subtotal by cartViewModel.subtotal.collectAsState()
    val deliveryFee by cartViewModel.deliveryFee.collectAsState()
    val total by cartViewModel.total.collectAsState()
    val selectedZone by cartViewModel.selectedDeliveryZone.collectAsState()

    val selectedLat by cartViewModel.selectedLat.collectAsState()
    val selectedLng by cartViewModel.selectedLng.collectAsState()
    val selectedAddressDesc by cartViewModel.selectedAddressDescription.collectAsState()
    val distanceKm by cartViewModel.distanceKm.collectAsState()

    val formState by salesViewModel.checkoutForm.collectAsState()
    var successSale by remember { mutableStateOf<Sale?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Precargar datos del usuario cliente activo si los campos están vacíos
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            salesViewModel.onCheckoutFieldChanged(
                name = if (formState.customerName.isBlank()) user.name else null,
                email = if (formState.customerEmail.isBlank()) user.email else null,
                phone = if (formState.customerPhone.isBlank()) user.phone else null,
                address = if (formState.customerAddress.isBlank()) (user.address ?: selectedAddressDesc) else null
            )
        }
    }

    // Sincronizar coordenadas y dirección seleccionadas en el mapa con el formulario
    LaunchedEffect(selectedLat, selectedLng, selectedAddressDesc) {
        salesViewModel.onCheckoutFieldChanged(
            latitude = selectedLat,
            longitude = selectedLng,
            deliveryZone = selectedZone.name,
            address = if (formState.customerAddress.isBlank() && selectedAddressDesc.isNotBlank()) selectedAddressDesc else null
        )
    }

    val paymentMethods = listOf(
        Pair("Tarjeta Débito/Crédito", Icons.Default.CreditCard),
        Pair("Transferencia Bancaria", Icons.Default.AccountBalance),
        Pair("Efectivo Contra Entrega", Icons.Default.LocalAtm)
    )

    // Diálogo de confirmación de pedido exitoso
    if (successSale != null) {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = BrandSecondary.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BrandSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "¡Pedido Confirmado con Éxito!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Orden N°: ${successSale!!.orderNumber}",
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Total: ${successSale!!.formattedTotal}",
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Despacho a: ${successSale!!.customerAddress}",
                        style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "El pedido quedó registrado en SQLite y el stock se descontó de los productos.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val email = successSale?.customerEmail ?: ""
                        successSale = null
                        if (email.isNotBlank()) {
                            salesViewModel.loadCustomerOrders(email)
                        }
                        onNavigateToMyOrders()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("Ver Mis Pedidos")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        successSale = null
                        onNavigateBack()
                    }
                ) {
                    Text("Volver al Catálogo")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Confirmación de Pedido",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ubicación de Despacho en OpenStreetMap
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = BrandPrimary
                        )
                        Text(
                            text = "Punto de Entrega en Mapa (OpenStreetMap)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Ubicación: $selectedAddressDesc",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = String.format(Locale.US, "Coordenadas: %.5f, %.5f (a %.1f km de la tienda)", selectedLat, selectedLng, distanceKm),
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Tarifa de Despacho calculada: ${selectedZone.formattedFee}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BrandSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onNavigateToMap,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = BrandPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver / Cambiar Ubicación en el Mapa", color = BrandPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Datos del Cliente y Despacho
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Datos de Contacto y Dirección",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = formState.customerName,
                        onValueChange = { salesViewModel.onCheckoutFieldChanged(name = it) },
                        label = { Text("Nombre Completo") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = formState.nameError != null,
                        supportingText = { FieldSupportText(formState.nameError) },
                        trailingIcon = { FieldErrorIcon(formState.nameError) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = formState.customerEmail,
                        onValueChange = { salesViewModel.onCheckoutFieldChanged(email = it) },
                        label = { Text("Correo Electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BrandPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = formState.emailError != null,
                        supportingText = { FieldSupportText(formState.emailError) },
                        trailingIcon = { FieldErrorIcon(formState.emailError) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = formState.customerPhone,
                        onValueChange = { salesViewModel.onCheckoutFieldChanged(phone = it) },
                        label = { Text("Teléfono de contacto") },
                        placeholder = { Text("+56 9 1234 5678") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BrandPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = formState.phoneError != null,
                        supportingText = { FieldSupportText(formState.phoneError) },
                        trailingIcon = { FieldErrorIcon(formState.phoneError) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = formState.customerAddress,
                        onValueChange = { salesViewModel.onCheckoutFieldChanged(address = it) },
                        label = { Text("Dirección Exacta (Calle, Número, Depto)") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = BrandPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = formState.addressError != null,
                        supportingText = { FieldSupportText(formState.addressError) },
                        trailingIcon = { FieldErrorIcon(formState.addressError) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Método de Pago
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Método de Pago",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    paymentMethods.forEach { (method, icon) ->
                        val isSelected = formState.paymentMethod == method
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { salesViewModel.onCheckoutFieldChanged(paymentMethod = method) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) BrandPrimary.copy(alpha = 0.08f) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { salesViewModel.onCheckoutFieldChanged(paymentMethod = method) },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(imageVector = icon, contentDescription = null, tint = BrandPrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = method,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Resumen de la Orden
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resumen del Pedido",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.quantity}x ${item.product.name}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = item.formattedSubtotal,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                        Text(cartViewModel.formatPrice(subtotal), fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val ivaAmount = subtotal * 0.19
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("IVA (19%)", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                        Text(cartViewModel.formatPrice(ivaAmount), style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Despacho (${selectedZone.name})", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                        Text(selectedZone.formattedFee, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL A PAGAR",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = cartViewModel.formatPrice(total),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Botón Confirmar Pedido
            Button(
                onClick = {
                    salesViewModel.onCheckoutFieldChanged(
                        latitude = selectedLat,
                        longitude = selectedLng,
                        deliveryZone = selectedZone.name
                    )
                    salesViewModel.confirmOrder(
                        cartItems = cartItems,
                        deliveryFee = deliveryFee,
                        onSuccess = { sale ->
                            cartViewModel.clearCart()
                            successSale = sale
                        },
                        onError = { errMsg ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(errMsg)
                            }
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandSecondary),
                enabled = !formState.isProcessing && cartItems.isNotEmpty()
            ) {
                if (formState.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONFIRMAR PEDIDO",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
