package com.example.oylfrutossecos.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.oylfrutossecos.BuildConfig
import java.io.File

/**
 * Muestra la foto de un producto cargada con Coil desde ms-productos (vía HTTP)
 * o desde el almacenamiento local (fotos capturadas con la cámara o content://).
 * Si imageUrl empieza con "/", antepone la URL base de ms-productos (BuildConfig.PRODUCTS_BASE_URL).
 * Si la imagen falla o está vacía, muestra el ícono por defecto (Spa).
 */
@Composable
fun ProductImage(
    imagePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholderTint: Color = Color.Gray,
    placeholderSize: Dp = 32.dp,
    contentScale: ContentScale = ContentScale.Crop
) {
    val model = resolveImageModel(imagePath)

    if (model == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = contentDescription,
                tint = placeholderTint,
                modifier = Modifier.size(placeholderSize)
            )
        }
    } else {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(model)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            loading = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(placeholderSize / 2),
                        strokeWidth = 2.dp,
                        color = placeholderTint
                    )
                }
            },
            error = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = contentDescription,
                        tint = placeholderTint,
                        modifier = Modifier.size(placeholderSize)
                    )
                }
            }
        )
    }
}

private fun resolveImageModel(imagePath: String): Any? {
    val trimmed = imagePath.trim()
    if (trimmed.isEmpty()) return null

    return when {
        trimmed.startsWith("/") -> {
            val base = BuildConfig.PRODUCTS_BASE_URL.removeSuffix("/")
            "$base$trimmed"
        }
        trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
        trimmed.startsWith("content://") || trimmed.startsWith("file://") -> trimmed
        File(trimmed).exists() -> File(trimmed)
        else -> trimmed
    }
}
