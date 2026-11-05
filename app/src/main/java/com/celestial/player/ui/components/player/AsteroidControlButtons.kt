package com.celestial.player.ui.components.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.celestial.player.ui.theme.CelestialTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AsteroidControlButton(
    isNext: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    var pressed by remember { mutableStateOf(false) }
    val displacement by animateFloatAsState(
        targetValue = if (pressed) (if (isNext) 8f else -8f) else 0f,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.5f),
        finishedListener = { pressed = false },
        label = "asteroidPoke",
    )

    val labelStr = if (isNext) "Next track" else "Previous track"
    val baseColor = CelestialTheme.colors.moonlight
    val deepSurface = CelestialTheme.colors.deepSurface
    val starlight = CelestialTheme.colors.starlight

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Asteroid $labelStr" }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = {
                    pressed = true
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    translationX = displacement
                },
        ) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (this.size.width / 2f) * 0.65f

            val path = Path()
            val points = 8
            for (i in 0 until points) {
                val angle = i * 2f * Math.PI.toFloat() / points
                val r = radius * (0.8f + (i % 3) * 0.12f)
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()

            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseColor,
                        deepSurface,
                    ),
                    center = center - Offset(radius * 0.2f, radius * 0.2f),
                    radius = radius * 1.2f,
                ),
            )

            val arrowPath = Path()
            val arrowSize = radius * 0.55f
            val dir = if (isNext) 1f else -1f

            arrowPath.moveTo(center.x - dir * arrowSize * 0.4f, center.y - arrowSize * 0.5f)
            arrowPath.lineTo(center.x + dir * arrowSize * 0.5f, center.y)
            arrowPath.lineTo(center.x - dir * arrowSize * 0.4f, center.y + arrowSize * 0.5f)

            drawPath(
                path = arrowPath,
                color = starlight,
            )
        }
    }
}
