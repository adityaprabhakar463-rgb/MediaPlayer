package com.celestial.player.ui.components.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.celestial.player.ui.theme.CelestialTheme
import kotlin.math.atan2

@Composable
fun SeekRingView(
    progressFraction: Float,
    durationMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    ringColor: Color = CelestialTheme.colors.starlight,
    glowColor: Color = CelestialTheme.colors.nebula,
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(progressFraction) }

    val effectiveFraction = if (isDragging) dragFraction else progressFraction
    val secondsStr = (durationMs * effectiveFraction / 1000).toInt()

    Box(
        modifier = modifier
            .size(size)
            .semantics {
                contentDescription = "Orbital Seek Ring"
                stateDescription = "Position ${secondsStr}s of ${durationMs / 1000}s"
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        val fraction = calculateFractionFromOffset(offset, this.size.width.toFloat(), this.size.height.toFloat())
                        onSeekTo((fraction * durationMs).toLong())
                    }
                }
                .pointerInput(durationMs) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragFraction = calculateFractionFromOffset(offset, this.size.width.toFloat(), this.size.height.toFloat())
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeekTo((dragFraction * durationMs).toLong())
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onDrag = { change, _ ->
                            dragFraction = calculateFractionFromOffset(change.position, this.size.width.toFloat(), this.size.height.toFloat())
                        },
                    )
                },
        ) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val ringRadius = (this.size.width / 2f) * 0.88f
            val strokeWidthPx = 4.dp.toPx()

            drawCircle(
                color = ringColor.copy(alpha = 0.18f),
                radius = ringRadius,
                center = center,
                style = Stroke(width = strokeWidthPx),
            )

            val sweepAngle = 360f * effectiveFraction
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        ringColor.copy(alpha = 0.6f),
                        glowColor,
                        ringColor,
                    ),
                    center = center,
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidthPx * 1.5f, cap = StrokeCap.Round),
            )

            val tipAngleRad = Math.toRadians((-90f + sweepAngle).toDouble())
            val tipX = center.x + ringRadius * kotlin.math.cos(tipAngleRad).toFloat()
            val tipY = center.y + ringRadius * kotlin.math.sin(tipAngleRad).toFloat()

            drawCircle(
                color = glowColor,
                radius = strokeWidthPx * 2.2f,
                center = Offset(tipX, tipY),
            )
            drawCircle(
                color = Color.White,
                radius = strokeWidthPx * 1.1f,
                center = Offset(tipX, tipY),
            )
        }
    }
}

private fun calculateFractionFromOffset(offset: Offset, width: Float, height: Float): Float {
    val centerX = width / 2f
    val centerY = height / 2f
    val dx = offset.x - centerX
    val dy = offset.y - centerY

    var angleRad = atan2(dy, dx)
    angleRad += (Math.PI / 2).toFloat()
    if (angleRad < 0) {
        angleRad += (2 * Math.PI).toFloat()
    }
    return (angleRad / (2 * Math.PI)).toFloat().coerceIn(0f, 1f)
}
