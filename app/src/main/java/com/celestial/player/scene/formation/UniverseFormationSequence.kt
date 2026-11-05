package com.celestial.player.scene.formation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.celestial.player.ui.components.glass.CelestialGlassSurface
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun UniverseFormationSequence(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false,
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val duration = if (reducedMotion) 1200 else 6500
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
        )
        onFinished()
    }

    val p = progress.value

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinished,
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxDim = kotlin.math.max(size.width, size.height)

            // Stage 1 & 2: The Void & Big Bang (0.0 -> 0.3)
            val bangRadius = when {
                p < 0.05f -> 2f
                p < 0.35f -> ((p - 0.05f) / 0.3f) * maxDim * 0.9f
                else -> maxDim * 1.5f
            }
            val bangAlpha = when {
                p < 0.1f -> p * 10f
                p < 0.4f -> 1f
                else -> (1f - (p - 0.4f) * 1.6f).coerceIn(0f, 1f)
            }

            drawRect(color = Color(0xFF030308))

            if (bangAlpha > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f * bangAlpha),
                            Color(0xFF8B5CF6).copy(alpha = 0.5f * bangAlpha),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = bangRadius.coerceAtLeast(1f),
                    ),
                    radius = bangRadius.coerceAtLeast(1f),
                    center = center,
                )
            }

            // Stage 3 & 4: Nebula dust & Central Star ignition (0.3 -> 0.7)
            if (p >= 0.3f) {
                val starProgress = ((p - 0.3f) / 0.4f).coerceIn(0f, 1f)
                val starRadius = starProgress * 48.dp.toPx()

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFFDE047).copy(alpha = 0.8f),
                            Color(0xFF8B5CF6).copy(alpha = 0.3f),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = starRadius * 2.5f,
                    ),
                    radius = starRadius * 2.5f,
                    center = center,
                )
            }

            // Stage 5 & 6: Planet accretion & palette settling (0.7 -> 1.0)
            if (p >= 0.7f) {
                val planetProgress = ((p - 0.7f) / 0.3f).coerceIn(0f, 1f)
                val orbitDist = planetProgress * 180.dp.toPx()

                for (i in 0 until 6) {
                    val angle = i * Math.PI.toFloat() / 3f
                    val px = center.x + orbitDist * kotlin.math.cos(angle)
                    val py = center.y + orbitDist * kotlin.math.sin(angle) * 0.6f
                    drawCircle(
                        color = Color(0xFFC084FC).copy(alpha = planetProgress * 0.8f),
                        radius = 10.dp.toPx() * planetProgress,
                        center = Offset(px, py),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(24.dp)
                .clickable(onClick = onFinished),
        ) {
            CelestialGlassSurface(selected = true) {
                Text(
                    text = "Skip Intro",
                    style = MaterialTheme.typography.labelLarge,
                    color = CelestialTheme.colors.starlight,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}
