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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oylfrutossecos.ui.theme.BrandAccent
import com.example.oylfrutossecos.ui.theme.BrandPrimary
import com.example.oylfrutossecos.ui.theme.BrandSecondary
import com.example.oylfrutossecos.ui.theme.StatusWarning
import com.example.oylfrutossecos.ui.viewmodel.AuthViewModel
import com.example.oylfrutossecos.ui.viewmodel.ProductViewModel
import com.example.oylfrutossecos.ui.viewmodel.SalesViewModel

data class QuickActionItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badge: String? = null,
    val iconTint: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    productViewModel: ProductViewModel,
    salesViewModel: SalesViewModel,
    onNavigateToCatalog: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToDeliveryMap: () -> Unit,
    onNavigateToSalesHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onLogout: (() -> Unit)? = null
) {
    val user by authViewModel.currentUser.collectAsState()
    val products by productViewModel.products.collectAsState()
    val sales by salesViewModel.sales.collectAsState()

    val lowStockCount = products.count { it.isLowStock }
    val totalStockUnits = products.sumOf { it.stock }

    val quickActions = listOf(
        QuickActionItem(
            "Inventario",
            if (lowStockCount > 0) "$lowStockCount bajo stock" else "Control de stock y precios",
            Icons.Default.Inventory2,
            if (lowStockCount > 0) "$lowStockCount!" else null,
            if (lowStockCount > 0) StatusWarning else BrandPrimary,
            onNavigateToInventory
        ),
        QuickActionItem("Catálogo", "${products.size} productos", Icons.Default.ShoppingBag, null, BrandPrimary, onNavigateToCatalog),
        QuickActionItem("Categorías", "4 grupos activos", Icons.Default.Category, null, BrandSecondary, onNavigateToCategories),
        QuickActionItem("Nuevo Producto", "Registro en bodega", Icons.Default.AddBusiness, null, BrandPrimary, onNavigateToAddProduct),
        QuickActionItem("Escanear código", "QR o código de barra", Icons.Default.QrCodeScanner, null, BrandPrimary, onNavigateToScanner),
        QuickActionItem("Mapa Reparto", "Pedidos en el mapa", Icons.Default.LocalShipping, null, BrandSecondary, onNavigateToDeliveryMap),
        QuickActionItem("Historial Ventas", "${sales.size} órdenes en BD", Icons.Default.History, null, BrandPrimary, onNavigateToSalesHistory),
        QuickActionItem("Mi Perfil", "Administrador OYL", Icons.Default.Person, null, BrandSecondary, onNavigateToProfile),
        QuickActionItem("Ajustes", "Configuración y datos", Icons.Default.Settings, null, Color.Gray, onNavigateToSettings),
        QuickActionItem("Ayuda", "Soporte técnico", Icons.AutoMirrored.Filled.HelpOutline, null, BrandPrimary, onNavigateToSupport)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = BrandAccent
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "OYL Frutos Secos",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Panel de Administración",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Mi Perfil",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes",
                            tint = Color.White
                        )
                    }
                    if (onLogout != null) {
                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Cerrar sesión",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandPrimary)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Saludo personalizado al Administrador
            item {
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
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = BrandPrimary.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = (user?.name?.take(1) ?: "A").uppercase(),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BrandPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¡Hola, ${user?.name ?: "Administrador"}! 👨‍💼",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Perfil: Administrador · ${user?.email ?: ""}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }
            }

            // Banner informativo del Rol Administrador
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(BrandPrimary, BrandSecondary)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandAccent
                            ) {
                                Text(
                                    text = "PANEL ADMINISTRADOR",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Control Operativo de Inventario y Ventas",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "El administrador supervisa stock, gestiona precios y despacha los pedidos realizados por los clientes.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            )
                        }
                    }
                }
            }

            // Resumen de Métricas (Tarjetas)
            item {
                Text(
                    text = "Resumen del Sistema",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Productos",
                        value = products.size.toString(),
                        subtitle = "$totalStockUnits kg en bodega",
                        icon = Icons.Default.Inventory2,
                        tint = BrandPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Bajo Stock",
                        value = lowStockCount.toString(),
                        subtitle = if (lowStockCount > 0) "¡Requiere reorden!" else "Óptimo",
                        icon = Icons.Default.NotificationsActive,
                        tint = if (lowStockCount > 0) StatusWarning else BrandSecondary,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Ventas",
                        value = sales.size.toString(),
                        subtitle = "Órdenes registradas",
                        icon = Icons.Default.History,
                        tint = BrandSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Accesos Directos (Grid de botones)
            item {
                Text(
                    text = "Módulos de Administración",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    quickActions.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { action ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { action.onClick() },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                modifier = Modifier.size(40.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                color = action.iconTint.copy(alpha = 0.12f)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = action.icon,
                                                        contentDescription = action.title,
                                                        tint = action.iconTint,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }

                                            if (action.badge != null) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = BrandAccent
                                                ) {
                                                    Text(
                                                        text = action.badge,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.Black
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = action.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )

                                        Text(
                                            text = action.subtitle,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        )
                                    }
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = RoundedCornerShape(8.dp),
                color = tint.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = tint
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1
            )
        }
    }
}
