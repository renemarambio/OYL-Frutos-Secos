package com.example.oylfrutossecos.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.oylfrutossecos.data.local.MockDataProvider
import com.example.oylfrutossecos.data.model.Local
import com.example.oylfrutossecos.ui.theme.BrandAccent
import com.example.oylfrutossecos.ui.theme.BrandPrimary
import com.example.oylfrutossecos.ui.theme.BrandSecondary
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalesMapScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val locales = remember { MockDataProvider.locales }

    // Seleccionamos por defecto la Casa Matriz de Providencia #1820
    var selectedLocal by remember { mutableStateOf(locales.firstOrNull { it.esCasaMatriz } ?: locales.first()) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    val markerMap = remember { mutableMapOf<String, Marker>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Nuestros Locales",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
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
                    Configuration.getInstance().userAgentValue = "OYLFrutosSecos/1.0 (app academica Duoc UC)"
                    Log.d("OSM_LOCALES", Configuration.getInstance().userAgentValue)

                    MapView(ctx).apply {
                        setTileSource(OsmFrHotTileSource)
                        setMultiTouchControls(true)
                        zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)

                        val copyrightOverlay = CopyrightOverlay(ctx).apply {
                            setCopyrightNotice("© OpenStreetMap contributors")
                        }
                        overlays.add(copyrightOverlay)

                        // Centrado inicial en Santiago
                        val initialCenter = GeoPoint(selectedLocal.latitud, selectedLocal.longitud)
                        controller.setZoom(12.5)
                        controller.setCenter(initialCenter)

                        // Creación de un marcador por cada local
                        locales.forEach { local ->
                            val isSelected = local.id == selectedLocal.id
                            val marker = Marker(this).apply {
                                position = GeoPoint(local.latitud, local.longitud)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = local.nombre
                                snippet = "${local.direccion}, ${local.comuna}"
                                icon = createLocalMarkerBitmap(ctx, isSelected, local.esCasaMatriz)
                                setOnMarkerClickListener { m, _ ->
                                    selectedLocal = local
                                    controller.animateTo(GeoPoint(local.latitud, local.longitud))
                                    m.showInfoWindow()
                                    true
                                }
                            }
                            markerMap[local.id] = marker
                            overlays.add(marker)
                            if (isSelected) {
                                marker.showInfoWindow()
                            }
                        }

                        mapViewRef = this
                    }
                },
                update = { mapView ->
                    // Actualiza el resaltado visual de todos los marcadores según el seleccionado
                    locales.forEach { local ->
                        val marker = markerMap[local.id] ?: return@forEach
                        val isSelected = local.id == selectedLocal.id
                        marker.icon = createLocalMarkerBitmap(context, isSelected, local.esCasaMatriz)
                        if (isSelected) {
                            marker.showInfoWindow()
                        }
                    }
                    mapView.invalidate()
                }
            )

            // Panel Superior Informativo con resumen de sucursales
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
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = BrandPrimary.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Red de Sucursales OYL (${locales.size} locales)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Toca un marcador en el mapa para ver dirección, horario y cómo llegar.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Panel Inferior: Datos del Local Seleccionado
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
                    // Título y Badge de Casa Matriz / Comuna
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedLocal.nombre,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandPrimary
                                )
                            )
                            Text(
                                text = selectedLocal.comuna,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        if (selectedLocal.esCasaMatriz) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandAccent
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "CASA MATRIZ",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dirección
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${selectedLocal.direccion}, ${selectedLocal.comuna}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Horario
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = BrandSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedLocal.horario,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Teléfono
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedLocal.telefono,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BrandPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botón "Cómo llegar"
                    Button(
                        onClick = {
                            val uri = Uri.parse("geo:${selectedLocal.latitud},${selectedLocal.longitud}?q=${Uri.encode("${selectedLocal.nombre}, ${selectedLocal.direccion}, ${selectedLocal.comuna}")}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val webIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.google.com/maps/search/?api=1&query=${selectedLocal.latitud},${selectedLocal.longitud}")
                                )
                                context.startActivity(webIntent)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Directions, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cómo llegar a ${selectedLocal.nombre}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Genera el icono visual del marcador para cada local en el mapa OpenStreetMap.
 * Si está seleccionado, se dibuja más grande con un halo exterior y color distintivo resaltado.
 */
fun createLocalMarkerBitmap(
    context: Context,
    isSelected: Boolean,
    isCasaMatriz: Boolean
): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val dpSize = if (isSelected) 52 else 40
    val px = (dpSize * density).toInt()

    val bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val cx = px / 2f
    val cy = px / 2f
    val radius = px / 2f - 4 * density

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Halo exterior de selección
    if (isSelected) {
        paint.color = android.graphics.Color.parseColor("#E65100") // Naranja intenso resaltado
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4 * density
        canvas.drawCircle(cx, cy, radius + 2 * density, paint)
    }

    // Círculo principal
    paint.style = Paint.Style.FILL
    paint.color = when {
        isSelected -> android.graphics.Color.parseColor("#E65100") // Resaltado seleccionado
        isCasaMatriz -> android.graphics.Color.parseColor("#2E7D32") // Verde institucional Matriz
        else -> android.graphics.Color.parseColor("#388E3C") // Verde sucursales
    }
    canvas.drawCircle(cx, cy, radius, paint)

    // Borde blanco interior
    paint.style = Paint.Style.STROKE
    paint.color = android.graphics.Color.WHITE
    paint.strokeWidth = (if (isSelected) 2.5f else 2.0f) * density
    canvas.drawCircle(cx, cy, radius, paint)

    // Texto interior (estrella para Casa Matriz o "OYL" para sucursal)
    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.WHITE
    paint.textSize = (if (isSelected) 14 else 11) * density
    paint.typeface = Typeface.DEFAULT_BOLD
    paint.textAlign = Paint.Align.CENTER
    val textY = cy - (paint.descent() + paint.ascent()) / 2
    canvas.drawText(if (isCasaMatriz) "★" else "OYL", cx, textY, paint)

    return BitmapDrawable(context.resources, bitmap)
}
