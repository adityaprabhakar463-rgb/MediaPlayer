package com.celestial.player.ui.components.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun CelestialGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    enabled: Boolean = true,
    selected: Boolean = false,
    pressed: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = CelestialTheme.colors
    val fillAlpha = when {
        pressed -> 0.80f
        selected -> 0.70f
        else -> 0.62f
    }
    val baseFill = colors.deepSurface.copy(alpha = fillAlpha)
    val topFill = colors.deepSurface.copy(alpha = (fillAlpha + 0.08f).coerceAtMost(1f))
    val borderColor = when {
        selected -> colors.cosmicViolet.copy(alpha = 0.55f)
        else -> colors.glassBorder.copy(alpha = 0.10f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(topFill, baseFill),
                ),
            )
            .border(width = 1.dp, color = borderColor, shape = shape)
            .semantics { if (!enabled) disabled() },
        content = content,
    )
}
