package com.celestial.player.ui.components.backdrop

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.celestial.player.ui.theme.CelestialTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val StarSeed = 0xCE1E
private val TwoPi = (2.0 * PI).toFloat()

private data class BackdropStar(
    val xFraction: Float,
    val yFraction: Float,
    val radiusFraction: Float,
    val phase: Float,
    val tintIndex: Int,
)

@Composable
fun DeepSpaceBackdrop(modifier: Modifier = Modifier) {
    val colors = CelestialTheme.colors
    val effects = CelestialTheme.effects
    val ambient = effects.ambientMotion && !effects.reducedMotion

    val stars = remember {
        val random = Random(StarSeed)
        List(110) {
            BackdropStar(
                xFraction = random.nextFloat(),
                yFraction = random.nextFloat(),
                radiusFraction = 0.9f + random.nextFloat() * 1.4f,
                phase = random.nextFloat() * TwoPi,
                tintIndex = random.nextInt(3),
            )
        }
    }

    val twinklePhase = rememberInfiniteTransition(label = "starTwinkle").animateFloat(
        initialValue = 0f,
        targetValue = TwoPi,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "starTwinklePhase",
    ).value

    val cometPhaseOne = rememberInfiniteTransition(label = "cometPhase").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cometTravel",
    ).value

    val cometPhaseTwo = rememberInfiniteTransition(label = "cometPhase2").animateFloat(
        initialValue = 0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 41_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cometTravel2",
    ).value

    val baseColors = listOf(
        colors.deepSpace,
        Color(0xFF060914),
        colors.spaceVoid,
    )
    val starTints = listOf(colors.starlight, colors.auroraBlue, colors.moonlight)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(baseColors)),
    ) {
        stars.forEach { star ->
            val cx = size.width * star.xFraction
            val cy = size.height * star.yFraction
            val radius = star.radiusFraction * 0.9f
            val alpha = if (ambient) {
                val wave = ((sin(twinklePhase + star.phase)) + 1f) / 2f
                0.26f + wave * 0.42f
            } else {
                0.42f
            }
            drawCircle(
                color = starTints[star.tintIndex].copy(alpha = alpha),
                radius = radius,
                center = Offset(cx, cy),
            )
        }
        if (ambient) {
            drawComet(starlightFrom = colors.starlight, phase = cometPhaseOne, yFraction = 0.07f, tailFraction = 0.09f)
            drawComet(starlightFrom = colors.starlight, phase = cometPhaseTwo, yFraction = 0.03f, tailFraction = 0.07f)
        }
    }
}

private fun DrawScope.drawComet(
    starlightFrom: Color,
    phase: Float,
    yFraction: Float,
    tailFraction: Float,
) {
    val progress = phase % 1f
    val start = Offset(
        x = size.width * (-0.05f + progress * 1.1f),
        y = size.height * yFraction + progress * size.height * 0.12f,
    )
    val direction = UnitDirection
    val end = start + direction * (tailFraction * size.width)
    Path().apply {
        moveTo(start.x, start.y)
        lineTo(end.x, end.y)
    }.let { path ->
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(
                    starlightFrom.copy(alpha = 0.0f),
                    starlightFrom.copy(alpha = 0.16f),
                ),
                start = end,
                end = start,
            ),
            style = Stroke(width = 1.6f, cap = StrokeCap.Round),
        )
    }
}

private val UnitDirection = Offset(x = 1f, y = 0f).let { base ->
    val radians = Math.toRadians(10.0)
    Offset(
        x = base.x * cos(radians).toFloat(),
        y = base.y * sin(radians).toFloat(),
    )
}
