package com.celestial.player.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.celestial.player.ui.components.glass.CelestialGlassSurface
import com.celestial.player.ui.components.glass.GlassArtworkPlaceholder
import com.celestial.player.ui.components.glass.GlassEmptyState
import com.celestial.player.ui.components.glass.GlassErrorState
import com.celestial.player.ui.components.glass.GlassIconButton
import com.celestial.player.ui.components.glass.GlassListRow
import com.celestial.player.ui.components.glass.GlassLoadingState
import com.celestial.player.ui.components.glass.GlassSectionHeader
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun DesignFoundationGallery() {
    val colors = CelestialTheme.colors
    val spacing = CelestialTheme.spacing

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.spaceVoid),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Column(
                modifier = Modifier.padding(
                    start = spacing.screenEdge,
                    end = spacing.screenEdge,
                    top = spacing.xl,
                    bottom = spacing.lg,
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Text(
                    text = "Celestial",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.starlight,
                )
                Text(
                    text = "Design foundation · Phase 1",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.moonlight,
                )
            }

            GlassSectionHeader(text = "Glass surface")
            CelestialGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenEdge),
            ) {
                Text(
                    text = "Translucent dark glass lets the starfield breathe through. " +
                        "Borders stay hairline, depth stays soft.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.starlight,
                    modifier = Modifier.padding(spacing.lg),
                )
            }

            GlassSectionHeader(text = "Signature controls")
            Row(
                modifier = Modifier.padding(horizontal = spacing.screenEdge),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                GlassIconButton(
                    icon = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    onClick = {},
                )
                GlassIconButton(
                    icon = Icons.Default.Favorite,
                    contentDescription = "Favourite",
                    selected = true,
                    stateDescription = "Added to favourites",
                    onClick = {},
                )
                GlassIconButton(
                    icon = Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    onClick = {},
                )
                GlassIconButton(
                    icon = Icons.Default.Pause,
                    contentDescription = "Pause",
                    enabled = false,
                    onClick = {},
                )
            }

            GlassSectionHeader(text = "List rows")
            Column(
                modifier = Modifier.padding(horizontal = spacing.screenEdge),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                GlassListRow(
                    title = "Starlight Reverie",
                    subtitle = "Nova Aurelia · 12 songs",
                    leading = { GlassArtworkPlaceholder() },
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = colors.moonlight,
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
                GlassListRow(
                    title = "Comet Tail Sessions",
                    subtitle = "Vesper Lyne · 8 songs",
                    leading = { GlassArtworkPlaceholder() },
                    onClick = {},
                )
            }

            GlassSectionHeader(text = "States")
            GlassEmptyState(
                title = "No playlists yet",
                description = "Playlists you create will gather here like formed worlds.",
                icon = Icons.Default.LibraryMusic,
                actionLabel = "Create playlist",
                onAction = {},
            )
            GlassLoadingState(label = "Scanning library")

            Box(modifier = Modifier.padding(bottom = spacing.xxl)) {
                GlassErrorState(
                    message = "The library could not be read. Check storage permission and try again.",
                    onRetry = {},
                )
            }
        }
    }
}
