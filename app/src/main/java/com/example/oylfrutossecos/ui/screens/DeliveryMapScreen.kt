package com.example.oylfrutossecos.ui.screens

import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.oylfrutossecos.data.model.UserRole
import com.example.oylfrutossecos.ui.theme.BrandAccent
import com.example.oylfrutossecos.ui.theme.BrandPrimary
import com.example.oylfrutossecos.ui.theme.BrandSecondary
import com.example.oylfrutossecos.ui.viewmodel.AuthViewModel
import com.example.oylfrutossecos.ui.viewmodel.CartViewModel
import com.example.oylfrutossecos.ui.viewmodel.SalesViewModel
import kotlinx.coroutines.launch
import android.util.Log
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/**
 * Fuente de respaldo sin API key: OpenStreetMap Francia HOT (Humanitarian OpenStreetMap Team).
 * Lista para usarse si MAPNIK presenta problemas de conectividad o cuota con solo cambiar una línea.
 */
val OsmFrHotTileSource = XYTileSource(
    "OSM_FR_HOT",
    0,
    19,
    256,
    ".png",
    arrayOf(
        "https://a.tile.openstreetmap.fr/hot/",
        "https://b.tile.openstreetmap.fr/hot/"
    ),
    "© OpenStreetMap contributors"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryMapScreen(
    authViewModel: AuthViewModel,
    cartViewModel: CartViewModel,
    salesViewModel: SalesViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.role == UserRole.ADMIN

    val selectedLat by cartViewModel.selectedLat.collectAsState()
    val selectedLng by cartViewModel.selectedLng.collectAsState()
    val distanceKm by cartViewModel.distanceKm.collectAsState()
    val selectedZone by cartViewModel.selectedDeliveryZone.collectAsState()
    val sales by salesViewModel.sales.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedOrderInfo by remember { mutableStateOf<String?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isAdmin) "Mapa de Pedidos OYL" else "Punto de Despacho OYL",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mapa OpenStreetMap Interactivo
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    // Reasignar userAgentValue antes de crear el MapView y registrarlo en Logcat
                    Configuration.getInstance().userAgentValue = "OYLFrutosSecos/1.0 (app academica Duoc UC)"
                    Log.d("OSM", Configuration.getInstance().userAgentValue)

                    MapView(ctx).apply {
                        // Fuente de tiles: OpenStreetMap Francia HOT (sin API key y alta disponibilidad)
                        setTileSource(OsmFrHotTileSource)
                        setMultiTouchControls(true) // Zoom con gestos táctiles (pinch-to-zoom)
                        zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS) // Botones de zoom

                        // Atribución oficial de OpenStreetMap
                        val copyrightOverlay = CopyrightOverlay(ctx).apply {
                            setCopyrightNotice("© OpenStreetMap contributors")
                        }
                        overlays.add(copyrightOverlay)

                        val storeGeoPoint = GeoPoint(CartViewModel.STORE_LAT, CartViewModel.STORE_LNG)
                        controller.setZoom(13.0)
                        controller.setCenter(storeGeoPoint)

                        // 1. Marcador Casa Matriz OYL
                        val storeMarker = Marker(this).apply {
                            position = storeGeoPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Casa Matriz OYL Frutos Secos"
                            snippet = "Providencia #1820 · Punto de despacho"
                        }
                        overlays.add(storeMarker)

                        if (!isAdmin) {
                            // 2. Modo CLIENTE: Marcador dinámico de destino y selector con toque
                            val clientMarker = Marker(this).apply {
                                position = GeoPoint(selectedLat, selectedLng)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Punto de Despacho Elegido"
                                snippet = "Toca cualquier punto del mapa para mover"
                            }
                            overlays.add(clientMarker)

                            // Overlay para detectar toque en el mapa
                            val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                    clientMarker.position = p
                                    cartViewModel.setDeliveryLocationFromMap(p.latitude, p.longitude)
                                    invalidate()
                                    return true
                                }

                                override fun longPressHelper(p: GeoPoint): Boolean {
                                    return false
                                }
                            })
                            overlays.add(0, eventsOverlay)
                        } else {
                            // 3. Modo ADMIN: Marcadores de todos los pedidos activos
                            sales.forEach { sale ->
                                val orderMarker = Marker(this).apply {
                                    position = GeoPoint(sale.latitude, sale.longitude)
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    title = "Pedido: ${sale.orderNumber}"
                                    snippet = "${sale.customerName} · ${sale.formattedTotal} · ${sale.customerAddress}"
                                    setOnMarkerClickListener { marker, _ ->
                                        marker.showInfoWindow()
                                        selectedOrderInfo = "${sale.orderNumber} - ${sale.customerName}: ${sale.customerAddress} (${sale.formattedTotal})"
                                        true
                                    }
                                }
                                overlays.add(orderMarker)
                            }
                        }
                    }
                },
                update = { mapView ->
                    // Si no es admin y cambia la coordenada seleccionada, actualiza la posición del marcador de cliente
                    if (!isAdmin) {
                        mapView.overlays.filterIsInstance<Marker>().find { it.title == "Punto de Despacho Elegido" }?.let { m ->
                            m.position = GeoPoint(selectedLat, selectedLng)
                            mapView.invalidate()
                        }
                    }
                }
            )

            // Panel Superior Informativo
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isAdmin) Icons.Default.LocalShipping else Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAdmin) "Mapa Logístico de Pedidos" else "Selecciona tu Ubicación de Entrega",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isAdmin) {
                                "${sales.size} pedidos con entrega geolocalizada en el Gran Santiago"
                            } else {
                                "Toca el mapa para fijar el punto; el despacho se calcula por distancia a la tienda"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Panel Inferior: Modo CLIENTE (Confirmar Ubicación y Costo) o ADMIN (Detalle de Pedido)
            if (!isAdmin) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Distancia a Casa Matriz:",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                                Text(
                                    text = "${String.format("%.1f", distanceKm)} km de Providencia #1820",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Costo de Despacho",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                                Text(
                                    text = selectedZone.formattedFee,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BrandPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Zona calculada: ${selectedZone.name} · Tiempo: ${selectedZone.estimatedTime}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BrandSecondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Ubicación fijada a ${String.format("%.1f", distanceKm)} km (${selectedZone.formattedFee})"
                                    )
                                }
                                onNavigateBack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirmar Dirección de Despacho", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (selectedOrderInfo != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = BrandPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = selectedOrderInfo!!,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { selectedOrderInfo = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar", tint = Color.Gray)
                        }
                    }
                }
            }

            // Atribución oficial de OpenStreetMap
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 74.dp, end = 14.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shadowElevation = 3.dp
            ) {
                Text(
                    text = "© OpenStreetMap contributors",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
