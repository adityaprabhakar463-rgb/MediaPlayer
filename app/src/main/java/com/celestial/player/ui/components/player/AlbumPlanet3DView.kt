package com.celestial.player.ui.components.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.celestial.player.ui.theme.CelestialTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AlbumPlanet3DView(
    albumColorSeed: Int,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
) {
    val reducedMotion = CelestialTheme.effects.reducedMotion
    val transition = rememberInfiniteTransition(label = "planetRotation")
    val rotationAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotationAngle",
    )

    val currentRotation = if (isPlaying && !reducedMotion) rotationAngle else 0f
    val rotRad = Math.toRadians(currentRotation.toDouble()).toFloat()

    val baseColor1 = remember(albumColorSeed) {
        Color(0xFF000000L or (albumColorSeed.toLong() and 0xFFFFFFL))
    }
    val baseColor2 = remember(albumColorSeed) {
        Color(0xFF000000L or ((albumColorSeed xor 0x00FF88).toLong() and 0xFFFFFFL))
    }
    val atmosphereColor = remember(albumColorSeed) {
        Color(0x66000000L or ((albumColorSeed or 0x4488FF).toLong() and 0xFFFFFFL))
    }

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Now Playing 3D Album Planet Surface" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (this.size.width / 2f) * 0.78f

            val lightOffsetX = radius * 0.35f * cos(rotRad)
            val lightOffsetY = radius * 0.35f * sin(rotRad)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        atmosphereColor.copy(alpha = 0.45f),
                        atmosphereColor.copy(alpha = 0.15f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = radius * 1.35f,
                ),
                radius = radius * 1.35f,
                center = center,
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseColor2,
                        baseColor1,
                        Color(0xFF030308),
                    ),
                    center = center + Offset(lightOffsetX, lightOffsetY),
                    radius = radius * 1.3f,
                ),
                radius = radius,
                center = center,
            )

            val bands = 5
            for (i in 0 until bands) {
                val bandRadius = radius * (0.3f + i * 0.14f)
                val bandAlpha = 0.12f + i * 0.04f
                drawCircle(
                    color = Color.White.copy(alpha = bandAlpha),
                    radius = bandRadius,
                    center = center,
                    style = Stroke(width = (2f + i * 1.5f).dp.toPx()),
                )
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.Transparent,
                    ),
                    center = center + Offset(-radius * 0.3f, -radius * 0.3f),
                    radius = radius * 0.6f,
                ),
                radius = radius,
                center = center,
            )
        }
    }
}
