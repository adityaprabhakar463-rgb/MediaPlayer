package com.celestial.player.ui.screens.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.celestial.player.ui.components.backdrop.DeepSpaceBackdrop
import com.celestial.player.ui.components.glass.GlassIconButton
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun DestinationScreen(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val colors = CelestialTheme.colors
    val spacing = CelestialTheme.spacing
    Box(modifier = modifier.fillMaxSize()) {
        DeepSpaceBackdrop()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeTopPadding(),
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        start = spacing.screenEdge,
                        end = spacing.screenEdge,
                        top = spacing.md,
                        bottom = spacing.sm,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onBack != null) {
                    GlassIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack,
                        modifier = Modifier.padding(end = spacing.sm),
                    )
                }
                TitleBlock(
                    title = title,
                    subtitle = subtitle,
                    modifier = Modifier.weight(1f),
                )
                actions()
            }
            content()
        }
    }
}

@Composable
private fun TitleBlock(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
) {
    val colors = CelestialTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CelestialTheme.spacing.xs)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.starlight,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = colors.moonlight,
            )
        }
    }
}

@Composable
private fun Modifier.safeTopPadding(): Modifier = this
    .statusBarsPadding()
    .padding(bottom = CelestialTheme.spacing.sm)

fun formatTrackDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(java.util.Locale.ROOT, "%d:%02d", minutes, seconds)
}
