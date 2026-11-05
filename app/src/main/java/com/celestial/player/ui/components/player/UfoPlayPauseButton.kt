package com.celestial.player.ui.components.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun UfoPlayPauseButton(
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
) {
    val beamAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 0.9f else 0f,
        animationSpec = tween(durationMillis = 240),
        label = "ufoBeamAlpha",
    )

    val beamGlowColor = CelestialTheme.colors.auroraBlue
    val domeColor = CelestialTheme.colors.starlight
    val bodyColor = CelestialTheme.colors.cosmicViolet
    val surfaceColor = CelestialTheme.colors.deepSurface
    val glassBorder = CelestialTheme.colors.glassBorder

    val labelStr = if (isPlaying) "Pause playback" else "Start playback"

    Box(
        modifier = modifier
            .size(size)
            .semantics {
                contentDescription = "UFO $labelStr"
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onTogglePlayPause,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = if (isPlaying) 1.06f else 1.0f
                    scaleY = if (isPlaying) 1.06f else 1.0f
                },
        ) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val ufoW = this.size.width * 0.78f
            val ufoH = ufoW * 0.45f

            // Glass backing circle
            drawCircle(
                color = surfaceColor.copy(alpha = 0.85f),
                radius = this.size.width / 2f,
                center = center,
            )
            drawCircle(
                color = glassBorder,
                radius = this.size.width / 2f - 1.dp.toPx(),
                center = center,
                style = Stroke(width = 1.5.dp.toPx()),
            )

            // Tractor beam / engine glow when playing
            if (beamAlpha > 0f) {
                val beamPath = Path().apply {
                    moveTo(center.x - ufoW * 0.25f, center.y + ufoH * 0.2f)
                    lineTo(center.x + ufoW * 0.25f, center.y + ufoH * 0.2f)
                    lineTo(center.x + ufoW * 0.48f, this@Canvas.size.height - 4.dp.toPx())
                    lineTo(center.x - ufoW * 0.48f, this@Canvas.size.height - 4.dp.toPx())
                    close()
                }
                drawPath(
                    path = beamPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            beamGlowColor.copy(alpha = 0.8f * beamAlpha),
                            beamGlowColor.copy(alpha = 0.2f * beamAlpha),
                            Color.Transparent,
                        ),
                        startY = center.y + ufoH * 0.2f,
                        endY = this.size.height,
                    ),
                )
            }

            // UFO Command Dome
            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(domeColor, domeColor.copy(alpha = 0.6f)),
                ),
                topLeft = Offset(center.x - ufoW * 0.24f, center.y - ufoH * 0.7f),
                size = Size(ufoW * 0.48f, ufoH * 0.8f),
            )

            // UFO Metallic Saucer Body
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        bodyColor,
                        surfaceColor,
                    ),
                    center = center,
                    radius = ufoW * 0.55f,
                ),
                topLeft = Offset(center.x - ufoW * 0.5f, center.y - ufoH * 0.28f),
                size = Size(ufoW, ufoH * 0.65f),
            )

            // Saucer rim highlight border
            drawOval(
                color = glassBorder,
                topLeft = Offset(center.x - ufoW * 0.5f, center.y - ufoH * 0.28f),
                size = Size(ufoW, ufoH * 0.65f),
                style = Stroke(width = 2.dp.toPx()),
            )

            // Glowing port lights / engine windows
            val lights = 5
            for (i in 0 until lights) {
                val lx = center.x + ufoW * (-0.32f + i * 0.16f)
                val ly = center.y + ufoH * 0.05f
                val lightColor = if (isPlaying) beamGlowColor else Color.White.copy(alpha = 0.4f)
                drawCircle(
                    color = lightColor,
                    radius = 3.dp.toPx(),
                    center = Offset(lx, ly),
                )
            }
        }
    }
}
