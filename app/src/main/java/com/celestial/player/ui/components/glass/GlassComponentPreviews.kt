package com.celestial.player.ui.components.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.celestial.player.ui.theme.CelestialTheme

@Composable
private fun CelestialPreviewStage(content: @Composable () -> Unit) {
    CelestialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CelestialTheme.colors.spaceVoid)
                .padding(16.dp),
        ) {
            content()
        }
    }
}

@Preview(name = "Glass surface", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun CelestialGlassSurfacePreview() {
    CelestialPreviewStage {
        CelestialGlassSurface(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Translucent dark glass over deep space",
                style = MaterialTheme.typography.bodyMedium,
                color = CelestialTheme.colors.starlight,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(name = "Icon buttons", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun GlassIconButtonPreview() {
    CelestialPreviewStage {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassIconButton(
                icon = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                onClick = {},
            )
            GlassIconButton(
                icon = Icons.Default.Favorite,
                contentDescription = "Favourite",
                selected = true,
                onClick = {},
                stateDescription = "Added to favourites",
            )
            GlassIconButton(
                icon = Icons.Default.Repeat,
                contentDescription = "Repeat",
                enabled = false,
                onClick = {},
            )
            GlassIconButton(
                icon = Icons.Default.Pause,
                contentDescription = "Pause",
                onClick = {},
            )
        }
    }
}

@Preview(name = "List rows", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun GlassListRowPreview() {
    CelestialPreviewStage {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassListRow(
                title = "Starlight Reverie",
                subtitle = "Nova Aurelia · 12 songs",
                leading = { GlassArtworkPlaceholder() },
                trailing = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = CelestialTheme.colors.moonlight,
                    )
                },
                onClick = {},
            )
            GlassListRow(
                title = "Orbit Drift",
                subtitle = "Selected playlist",
                selected = true,
                leading = { GlassArtworkPlaceholder() },
                onClick = {},
            )
        }
    }
}

@Preview(name = "List row · 1.5x font", showBackground = true, backgroundColor = 0xFF000000, fontScale = 1.5f)
@Composable
private fun GlassListRowScaledFontPreview() {
    CelestialPreviewStage {
        GlassListRow(
            title = "Starlight Reverie",
            subtitle = "Nova Aurelia · 12 songs",
            leading = { GlassArtworkPlaceholder() },
            onClick = {},
        )
    }
}

@Preview(name = "Section header", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun GlassSectionHeaderPreview() {
    CelestialPreviewStage {
        GlassSectionHeader(text = "Recently played")
    }
}

@Preview(name = "Empty state", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun GlassEmptyStatePreview() {
    CelestialPreviewStage {
        GlassEmptyState(
            title = "No playlists yet",
            description = "Playlists you create will gather here like formed worlds.",
            icon = Icons.Default.PlayArrow,
            actionLabel = "Create playlist",
            onAction = {},
        )
    }
}

@Preview(name = "Loading state", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun GlassLoadingStatePreview() {
    CelestialPreviewStage {
        GlassLoadingState(label = "Scanning library")
    }
}

@Preview(name = "Error state", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun GlassErrorStatePreview() {
    CelestialPreviewStage {
        GlassErrorState(
            message = "The library could not be read. Check storage permission and try again.",
            onRetry = {},
        )
    }
}
