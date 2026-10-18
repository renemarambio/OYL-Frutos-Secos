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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.oylfrutossecos.ui.components.FieldErrorIcon
import com.example.oylfrutossecos.ui.components.FieldSupportText
import com.example.oylfrutossecos.ui.components.ProductImage
import com.example.oylfrutossecos.ui.theme.BrandPrimary
import com.example.oylfrutossecos.ui.theme.BrandSecondary
import com.example.oylfrutossecos.ui.theme.StatusError
import com.example.oylfrutossecos.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: Long = 0L,
    productViewModel: ProductViewModel,
    onNavigateToCamera: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val formState by productViewModel.formState.collectAsState()
    var categoryExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val categories = listOf("Frutos Secos", "Semillas", "Deshidratados", "Mixes Especiales")

    // Se inicializa el formulario solo la primera vez que se abre la pantalla.
    // rememberSaveable conserva el valor al ir a la cámara o al escáner y volver,
    // así no se borra lo que el usuario ya había escrito.
    var formInitialized by rememberSaveable(productId) { mutableStateOf(false) }

    LaunchedEffect(productId) {
        if (!formInitialized) {
            if (productId > 0) {
                productViewModel.loadProductById(productId)
            } else {
                productViewModel.resetForm()
            }
            formInitialized = true
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar producto?") },
            text = { Text("Esta acción eliminará el producto del inventario de forma permanente.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        productViewModel.deleteProduct(formState.id) {
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Eliminar", color = StatusError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (formState.id > 0) "Editar Producto" else "Nuevo Producto",
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
                actions = {
                    if (formState.id > 0) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.White
                            )
                        }
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
            // Vista previa de la foto del producto (tomada con la cámara)
            ProductImage(
                imagePath = formState.imageUrl,
                contentDescription = "Foto del producto",
                placeholderTint = BrandPrimary.copy(alpha = 0.5f),
                placeholderSize = 56.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BrandPrimary.copy(alpha = 0.08f))
            )

            // Botones de acción rápida: Cámara y Escáner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToCamera,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = BrandSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (formState.imageUrl.isBlank()) "Tomar foto" else "Cambiar foto", color = BrandSecondary, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onNavigateToScanner,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Escanear SKU", color = BrandPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            // Nombre
            OutlinedTextField(
                value = formState.name,
                onValueChange = { productViewModel.onFormFieldChanged(name = it) },
                label = { Text("Nombre del producto *") },
                leadingIcon = { Icon(Icons.Default.Inventory2, contentDescription = null, tint = BrandPrimary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = formState.nameError != null,
                supportingText = { FieldSupportText(formState.nameError) },
                trailingIcon = { FieldErrorIcon(formState.nameError) },
                shape = RoundedCornerShape(12.dp)
            )

            // Selector de Categoría
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = formState.category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                productViewModel.onFormFieldChanged(category = cat)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Precio y Stock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = formState.price,
                    onValueChange = { productViewModel.onFormFieldChanged(price = it) },
                    label = { Text("Precio CLP *") },
                    prefix = { Text("$ ") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = formState.priceError != null,
                    supportingText = { FieldSupportText(formState.priceError) },
                    trailingIcon = { FieldErrorIcon(formState.priceError) },
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = formState.stock,
                    onValueChange = { productViewModel.onFormFieldChanged(stock = it) },
                    label = { Text("Stock *") },
                    suffix = { Text(formState.unit) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = formState.stockError != null,
                    supportingText = { FieldSupportText(formState.stockError) },
                    trailingIcon = { FieldErrorIcon(formState.stockError) },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Unidad y Origen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = formState.unit,
                    onValueChange = { productViewModel.onFormFieldChanged(unit = it) },
                    label = { Text("Unidad (ej: kg, bolsa)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = formState.origin,
                    onValueChange = { productViewModel.onFormFieldChanged(origin = it) },
                    label = { Text("Origen / Valle") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = BrandPrimary) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Código de Barras / SKU
            OutlinedTextField(
                value = formState.barcode,
                onValueChange = { productViewModel.onFormFieldChanged(barcode = it) },
                label = { Text("Código de barras / SKU") },
                leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null, tint = BrandPrimary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = formState.barcodeError != null,
                supportingText = { FieldSupportText(formState.barcodeError, helper = "Opcional: si lo dejas vacío se genera uno") },
                trailingIcon = { FieldErrorIcon(formState.barcodeError) },
                shape = RoundedCornerShape(12.dp)
            )

            // Insumos e Ingredientes
            OutlinedTextField(
                value = formState.ingredients,
                onValueChange = { productViewModel.onFormFieldChanged(ingredients = it) },
                label = { Text("Insumos e Ingredientes") },
                placeholder = { Text("Ej: 100% almendra tostada sin aditivos...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            // Descripción Detallada
            OutlinedTextField(
                value = formState.description,
                onValueChange = { productViewModel.onFormFieldChanged(description = it) },
                label = { Text("Descripción Detallada") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Botón Guardar
            Button(
                onClick = {
                    productViewModel.saveProduct {
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                enabled = !formState.isSaving
            ) {
                if (formState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (formState.id > 0) "Guardar cambios" else "Guardar producto",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
