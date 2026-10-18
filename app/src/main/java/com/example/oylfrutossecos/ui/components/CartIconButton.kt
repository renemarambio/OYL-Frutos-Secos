package com.example.oylfrutossecos.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.oylfrutossecos.ui.theme.BrandAccent

/**
 * Ícono del carrito con contador animado.
 * Cuando se agrega un producto el ícono "salta" y el número cambia con un deslizamiento,
 * así el usuario recibe retroalimentación inmediata de que la acción funcionó.
 */
@Composable
fun CartIconButton(
    cartCount: Int,
    onClick: () -> Unit,
    tint: Color = Color.White
) {
    val bounce = remember { Animatable(1f) }
    var previousCount by remember { mutableIntStateOf(cartCount) }

    LaunchedEffect(cartCount) {
        if (cartCount > previousCount) {
            bounce.animateTo(1.3f, animationSpec = tween(durationMillis = 120))
            bounce.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        previousCount = cartCount
    }

    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (cartCount > 0) {
                    Badge(containerColor = BrandAccent, contentColor = Color.Black) {
                        AnimatedContent(
                            targetState = cartCount,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                                } else {
                                    (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
                                }
                            },
                            label = "cart_count"
                        ) { count ->
                            Text(count.toString(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Carrito, $cartCount productos",
                tint = tint,
                modifier = Modifier.scale(bounce.value)
            )
        }
    }
}
