package com.example.oylfrutossecos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oylfrutossecos.data.model.Product
import com.example.oylfrutossecos.domain.validation.Validators
import com.example.oylfrutossecos.ui.components.FieldErrorIcon
import com.example.oylfrutossecos.ui.components.FieldSupportText
import com.example.oylfrutossecos.ui.components.ProductImage
import com.example.oylfrutossecos.ui.theme.BrandAccent
import com.example.oylfrutossecos.ui.theme.BrandPrimary
import com.example.oylfrutossecos.ui.theme.BrandSecondary
import com.example.oylfrutossecos.ui.theme.StatusWarning
import com.example.oylfrutossecos.ui.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    productViewModel: ProductViewModel,
    onNavigateToEditProduct: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val products by productViewModel.products.collectAsState()
    val searchQuery by productViewModel.searchQuery.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var productToEditPrice by remember { mutableStateOf<Product?>(null) }
    var newPriceInput by remember { mutableStateOf("") }
    var priceError by remember { mutableStateOf<String?>(null) }

    var productToAdjustStock by remember { mutableStateOf<Product?>(null) }
    var newStockInput by remember { mutableStateOf("") }
    var stockError by remember { mutableStateOf<String?>(null) }

    val lowStockCount = products.count { it.isLowStock }
    val totalStockUnits = products.sumOf { it.stock }

    // Diálogo Editar Precio
    if (productToEditPrice != null) {
        val prod = productToEditPrice!!
        AlertDialog(
            onDismissRequest = { productToEditPrice = null },
            title = {
                Text(
                    text = "Editar Precio - ${prod.name}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text("Precio actual: ${prod.formattedPrice}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPriceInput,
                        onValueChange = {
                            newPriceInput = it
                            priceError = null
                        },
                        label = { Text("Nuevo precio (CLP)") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = BrandPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = priceError != null,
                        supportingText = { FieldSupportText(priceError) },
                        trailingIcon = { FieldErrorIcon(priceError) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val err = Validators.validatePrice(newPriceInput)
                        if (err != null) {
                            priceError = err
                            return@Button
                        }
                        val priceVal = newPriceInput.toDoubleOrNull() ?: return@Button
                        productViewModel.updatePriceDirect(prod.id, priceVal) { success ->
                            productToEditPrice = null
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Precio de ${prod.name} actualizado a $$priceVal")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("Guardar Precio")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToEditPrice = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo Fijar Stock Directo
    if (productToAdjustStock != null) {
        val prod = productToAdjustStock!!
        AlertDialog(
            onDismissRequest = { productToAdjustStock = null },
            title = {
                Text(
                    text = "Ajustar Stock - ${prod.name}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text("Stock actual: ${prod.stock} ${prod.unit}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newStockInput,
                        onValueChange = {
                            newStockInput = it
                            stockError = null
                        },
                        label = { Text("Nuevo stock (${prod.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = stockError != null,
                        supportingText = { FieldSupportText(stockError) },
                        trailingIcon = { FieldErrorIcon(stockError) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val err = Validators.validateStock(newStockInput)
                        if (err != null) {
                            stockError = err
                            return@Button
                        }
                        val stockVal = newStockInput.toIntOrNull() ?: return@Button
                        productViewModel.updateStockDirect(prod.id, stockVal) { success ->
                            productToAdjustStock = null
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Stock de ${prod.name} actualizado a $stockVal ${prod.unit}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("Guardar Stock")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToAdjustStock = null }) {
                    Text("Cancelar")
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
                        text = "Inventario y Stock",
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
        ) {
            // Tarjetas de Resumen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalStockUnits kg",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = BrandPrimary)
                        )
                        Text(
                            text = "${products.size} productos en total",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (lowStockCount > 0) StatusWarning.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (lowStockCount > 0) StatusWarning else BrandSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$lowStockCount críticos",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (lowStockCount > 0) StatusWarning else BrandSecondary
                            )
                        )
                        Text(
                            text = if (lowStockCount > 0) "Stock <= 10 kg" else "Niveles óptimos",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                        )
                    }
                }
            }

            // Buscador de producto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { productViewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar producto o SKU en inventario...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { productViewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lista de Productos del Inventario
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandPrimary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (product.isLowStock) StatusWarning.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(50.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = BrandPrimary.copy(alpha = 0.1f)
                                    ) {
                                        ProductImage(
                                            imagePath = product.imageUrl,
                                            contentDescription = product.name,
                                            placeholderTint = BrandPrimary,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = product.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "SKU: ${product.barcode} · ${product.category}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                                        )
                                        Text(
                                            text = "${product.formattedPrice} / ${product.unit}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = BrandPrimary
                                            )
                                        )
                                    }

                                    if (product.isLowStock) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = StatusWarning.copy(alpha = 0.2f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Warning, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Bajo",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = StatusWarning,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Controles de Ajuste de Stock y Precio
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Control de Stock
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedIconButton(
                                            onClick = { productViewModel.adjustStock(product.id, -1) },
                                            modifier = Modifier.size(32.dp),
                                            shape = CircleShape,
                                            enabled = product.stock > 0
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Menos", modifier = Modifier.size(16.dp))
                                        }

                                        Text(
                                            text = "${product.stock} ${product.unit}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (product.isLowStock) StatusWarning else BrandSecondary
                                            ),
                                            modifier = Modifier
                                                .padding(horizontal = 10.dp)
                                                .clickable {
                                                    productToAdjustStock = product
                                                    newStockInput = product.stock.toString()
                                                    stockError = null
                                                }
                                        )

                                        OutlinedIconButton(
                                            onClick = { productViewModel.adjustStock(product.id, +1) },
                                            modifier = Modifier.size(32.dp),
                                            shape = CircleShape
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Más", modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    // Botones de acción (Editar Precio y Editar Producto Completo)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(
                                            onClick = {
                                                productToEditPrice = product
                                                newPriceInput = product.price.toInt().toString()
                                                priceError = null
                                            }
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Precio")
                                        }

                                        TextButton(
                                            onClick = { onNavigateToEditProduct(product.id) }
                                        ) {
                                            Text("Ficha")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
