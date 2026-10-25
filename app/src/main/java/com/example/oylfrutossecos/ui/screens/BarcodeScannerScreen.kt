package com.example.oylfrutossecos.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.oylfrutossecos.data.model.Product
import com.example.oylfrutossecos.ui.theme.BrandAccent
import com.example.oylfrutossecos.ui.theme.BrandPrimary
import com.example.oylfrutossecos.ui.theme.BrandSecondary
import com.example.oylfrutossecos.ui.theme.StatusError
import com.example.oylfrutossecos.ui.theme.StatusWarning
import com.example.oylfrutossecos.ui.viewmodel.ProductViewModel
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.example.oylfrutossecos.data.model.UserRole
import com.example.oylfrutossecos.ui.viewmodel.AuthViewModel

/**
 * Pantalla y Componente Composable Modular para la lectura funcional de Códigos QR
 * usando ZXing (zxing-android-embedded) integrado con SQLite (AppDatabaseHelper)
 * para la aplicación "OYL Frutos Secos".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel? = null,
    pickMode: Boolean = false,
    onNavigateToProductDetail: (Long) -> Unit,
    onNavigateToEditProduct: ((Long) -> Unit)? = null,
    onNavigateBack: () -> Unit
) {
    val currentUser by (authViewModel?.currentUser ?: remember { mutableStateOf(null) }).let {
        if (authViewModel != null) authViewModel.currentUser.collectAsState() else remember { mutableStateOf(null) }
    }
    val isAdmin = currentUser?.role == UserRole.ADMIN

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }
    val products by productViewModel.products.collectAsState()

    // Estados del escáner
    var scannedCode by remember { mutableStateOf("") }
    var detectedProduct by remember { mutableStateOf<Product?>(null) }
    var isSearchingDb by remember { mutableStateOf(false) }
    var scanMessage by remember { mutableStateOf<String?>(null) }
    var scanMessageType by remember { mutableStateOf("info") } // "success", "warning", "error", "info"

    // Procesa el código leído. La consulta a la base de datos la hace el ViewModel (MVVM).
    fun processScannedResult(code: String) {
        val cleanCode = code.trim()
        if (pickMode) {
            // Modo formulario: devolver el código leído al formulario de producto
            productViewModel.onFormFieldChanged(barcode = cleanCode)
            onNavigateBack()
            return
        }
        scannedCode = cleanCode
        isSearchingDb = true
        scanMessage = null

        productViewModel.findProductByCode(cleanCode) { product ->
            isSearchingDb = false
            detectedProduct = product
            if (product != null) {
                scanMessageType = "success"
                scanMessage = "Producto encontrado: ${product.name}"
            } else {
                scanMessageType = "warning"
                scanMessage = "Se leyó el código '$cleanCode', pero no está registrado en el inventario."
            }
        }
    }

    // 1. Launcher de ZXing usando ScanContract()
    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result.contents != null) {
            // CASO ÉXITO: Se leyó una cadena desde el QR (ejemplo: "PROD-001" o "780998810001")
            val contents = result.contents
            processScannedResult(contents)
        } else {
            // CASO CANCELACIÓN: El usuario presionó volver o canceló el escaneo
            scanMessageType = "info"
            scanMessage = "Escaneo cancelado por el usuario."
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Escaneo de código cancelado")
            }
        }
    }

    // 2. Launcher para solicitud de Permiso de Cámara en tiempo de ejecución
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido -> Configurar y lanzar ZXing
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                setPrompt("Apunta al código QR o de barra del fruto seco OYL")
                setCameraId(0) // Cámara trasera
                setBeepEnabled(true)
                setBarcodeImageEnabled(false)
                setOrientationLocked(false)
            }
            scanLauncher.launch(options)
        } else {
            // Permiso denegado
            scanMessageType = "error"
            scanMessage = "Se requiere permiso de cámara para escanear códigos QR."
            Toast.makeText(
                context,
                "Permiso de cámara denegado. Actívalo en los ajustes de la aplicación.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Función para iniciar el escaneo validando permisos
    fun launchQrScanner() {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                setPrompt("Apunta al código QR o barra del empaque OYL")
                setCameraId(0)
                setBeepEnabled(true)
                setBarcodeImageEnabled(false)
                setOrientationLocked(false)
            }
            scanLauncher.launch(options)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Generador de etiquetas QR
    var selectedProductForQr by remember { mutableStateOf(products.firstOrNull()) }

    // Animación de línea láser en el preview
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (pickMode) "Escanear código del producto" else "Escáner de códigos",
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
            // Pestañas Lector / Generador
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = BrandPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Escanear", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Etiqueta QR", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.QrCode, contentDescription = null) }
                )
            }

            if (selectedTab == 0) {
                // PESTAÑA 1: ESCÁNER QR FUNCIONAL CON ZXING Y CONSULTA A SQLITE
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Escanea el código del producto",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Apunta la cámara al código QR o de barras del envase para ver el producto y su stock.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Botón Principal: Lanzador de Cámara con ZXing
                    Button(
                        onClick = { launchQrScanner() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Abrir escáner",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mensaje de estado (Éxito, Cancelación, Permiso, etc.)
                    if (scanMessage != null) {
                        val (msgBg, msgColor, msgIcon) = when (scanMessageType) {
                            "success" -> Triple(BrandSecondary.copy(alpha = 0.15f), BrandSecondary, Icons.Default.CheckCircle)
                            "warning" -> Triple(StatusWarning.copy(alpha = 0.15f), StatusWarning, Icons.Default.Warning)
                            "error" -> Triple(StatusError.copy(alpha = 0.15f), StatusError, Icons.Default.ErrorOutline)
                            else -> Triple(BrandPrimary.copy(alpha = 0.1f), BrandPrimary, Icons.Default.Info)
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = msgBg
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = msgIcon,
                                    contentDescription = null,
                                    tint = msgColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = scanMessage!!,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = msgColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Visor de búsqueda / Indicador de carga
                    if (isSearchingDb) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = BrandPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Buscando producto...",
                                style = MaterialTheme.typography.bodySmall.copy(color = BrandPrimary)
                            )
                        }
                    }

                    // Tarjeta con la Información del Producto Asociado en SQLite
                    if (detectedProduct != null) {
                        val prod = detectedProduct!!
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = BrandSecondary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = prod.category,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = BrandSecondary
                                            )
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (prod.isLowStock) StatusWarning.copy(alpha = 0.12f) else BrandSecondary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "Stock: ${prod.stock} ${prod.unit}",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (prod.isLowStock) StatusWarning else BrandSecondary
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = prod.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )

                                Text(
                                    text = "Código / SKU: ${prod.barcode}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Precio: ${prod.formattedPrice} / ${prod.unit}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BrandPrimary
                                    )
                                )

                                if (prod.ingredients.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Insumos: ${prod.ingredients}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                        ),
                                        maxLines = 2
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                if (isAdmin) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { onNavigateToProductDetail(prod.id) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                                        ) {
                                            Text("Ver Detalle", fontWeight = FontWeight.Bold)
                                        }
                                        if (onNavigateToEditProduct != null) {
                                            Button(
                                                onClick = { onNavigateToEditProduct(prod.id) },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = BrandSecondary)
                                            ) {
                                                Text("Editar Producto", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { onNavigateToProductDetail(prod.id) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                                    ) {
                                        Icon(imageVector = Icons.Default.Spa, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Ver Detalle y Comprar", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Simulación o Entrada Manual (Útil para pruebas y emuladores sin cámara física)
                    Text(
                        text = "O prueba ingresando / seleccionando un código:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        products.take(3).forEach { p ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { processScannedResult(p.barcode) },
                                shape = RoundedCornerShape(10.dp),
                                color = BrandSecondary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = p.name.take(10) + "...",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BrandSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = scannedCode,
                        onValueChange = { code ->
                            scannedCode = code
                            // En modo búsqueda se consulta mientras se escribe; en modo formulario se confirma con el botón
                            if (code.isNotBlank() && !pickMode) processScannedResult(code)
                        },
                        label = { Text("Código QR / SKU leído manualmente") },
                        placeholder = { Text("Ej: PROD-001 o 780998810001") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { if (scannedCode.isNotBlank()) processScannedResult(scannedCode) }) {
                                Icon(
                                    imageVector = if (pickMode) Icons.Default.CheckCircle else Icons.Default.Search,
                                    contentDescription = if (pickMode) "Usar este código" else "Buscar",
                                    tint = BrandPrimary
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            } else {
                // PESTAÑA 2: GENERADOR DE ETIQUETA QR PARA EMPAQUES
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Etiqueta QR para envases",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Representación visual QR
                    Card(
                        modifier = Modifier.size(240.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // QR real generado con ZXing a partir del código del producto
                            val qrBitmap = remember(selectedProductForQr?.barcode) {
                                selectedProductForQr?.barcode
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { code ->
                                        runCatching {
                                            BarcodeEncoder().encodeBitmap(code, BarcodeFormat.QR_CODE, 512, 512)
                                        }.getOrNull()
                                    }
                            }
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Código QR del producto",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = "Este producto no tiene código asignado",
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    selectedProductForQr?.let { prod ->
                        Text(
                            text = "SKU: ${prod.barcode}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${prod.name} - ${prod.formattedPrice}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = BrandSecondary, fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Lote 2026-OYL · Origen: ${prod.origin}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Seleccionar otro producto para generar etiqueta:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        products.take(4).forEach { prod ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedProductForQr = prod },
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedProductForQr?.id == prod.id) BrandPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.QrCode, contentDescription = null, tint = BrandPrimary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("SKU: ${prod.barcode}", fontSize = 11.sp, color = Color.Gray)
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
