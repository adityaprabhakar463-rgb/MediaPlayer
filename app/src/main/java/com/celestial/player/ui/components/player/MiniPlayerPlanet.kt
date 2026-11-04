package com.celestial.player.ui.components.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.celestial.player.playback.PlayerUiState
import com.celestial.player.ui.components.glass.GlassArtwork
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun MiniPlayerPlanet(
    state: PlayerUiState,
    modifier: Modifier = Modifier,
    albumColorSeed: Int = 0,
    planetSize: Dp = 64.dp,
    onClick: (() -> Unit)? = null,
) {
    val colors = CelestialTheme.colors
    val tapDescription = "Current track: ${state.currentTrack?.title ?: "none"}"
    val stateText = when {
        state.currentTrack == null -> "No music playing"
        state.isPlaying -> "Playing ${state.currentTrack?.artistName}, ${(state.progressFraction * 100).toInt()}% complete"
        else -> "Paused: ${state.currentTrack?.artistName}"
    }
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(planetSize)
            .semantics {
                contentDescription = tapDescription
                stateDescription = stateText
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(planetSize)) {
            drawArc(
                color = colors.glassBorder.copy(alpha = 0.14f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
            if (state.progressFraction > 0f) {
                drawArc(
                    color = colors.cosmicViolet,
                    startAngle = -90f,
                    sweepAngle = 360f * state.progressFraction,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                )
            }
        }
        GlassArtwork(
            modifier = Modifier.align(Alignment.Center),
            size = planetSize * 0.72f,
            colorSeed = albumColorSeed,
            shape = CircleShape,
            icon = null,
        )
    }
}
