package com.example.oylfrutossecos.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.oylfrutossecos.data.local.ImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Muestra la foto guardada de un producto. Si el producto no tiene foto,
 * muestra el ícono por defecto. La imagen se lee del disco en segundo plano.
 */
@Composable
fun ProductImage(
    imagePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholderTint: Color = Color.Gray,
    placeholderSize: Dp = 32.dp
) {
    var image by remember(imagePath) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(imagePath) {
        image = withContext(Dispatchers.IO) {
            ImageStorage.loadBitmap(imagePath)?.asImageBitmap()
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val current = image
        if (current != null) {
            Image(
                bitmap = current,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = contentDescription,
                tint = placeholderTint,
                modifier = Modifier.size(placeholderSize)
            )
        }
    }
}
