package com.celestial.player.ui.screens.universe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.celestial.player.ui.components.backdrop.DeepSpaceBackdrop
import com.celestial.player.ui.components.glass.CelestialGlassSurface
import com.celestial.player.ui.components.glass.GlassIconButton
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun UniversePlaceholderScreen(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    currentTrackTitle: String?,
    onOpenPlaylists: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAllSongs: () -> Unit,
    onOpenAlbums: () -> Unit,
    onOpenArtists: () -> Unit,
    onOpenFavourites: () -> Unit,
    onOpenRecentlyPlayed: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = CelestialTheme.colors
    val spacing = CelestialTheme.spacing
    Box(modifier = modifier.fillMaxSize()) {
        DeepSpaceBackdrop()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .safeDrawingPadding()
                .padding(horizontal = spacing.screenEdge),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Celestial",
                        style = MaterialTheme.typography.displayLarge,
                        color = colors.starlight,
                    )
                    Text(
                        text = "Solar system view arrives with the 3D universe",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.moonlight,
                    )
                }
                GlassIconButton(
                    icon = Icons.Filled.Settings,
                    contentDescription = "Open settings",
                    onClick = onOpenSettings,
                )
            }
            if (!isPlaying && currentTrackTitle == null) {
                Text(
                    text = "Tap any destination to travel there",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.moonlight,
                )
            } else {
                Text(
                    text = if (isPlaying) "Now in orbit: $currentTrackTitle" else "Currently loaded: $currentTrackTitle",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.moonlight,
                )
            }
            UniverseDestinationChip("Playlists", Icons.AutoMirrored.Filled.QueueMusic, onOpenPlaylists)
            UniverseDestinationChip("Search", Icons.Filled.Search, onOpenSearch)
            UniverseDestinationChip("All Songs", Icons.Filled.LibraryMusic, onOpenAllSongs)
            UniverseDestinationChip("Albums", Icons.Filled.Album, onOpenAlbums)
            UniverseDestinationChip("Artists", Icons.Filled.Person, onOpenArtists)
            UniverseDestinationChip("Favourites", Icons.Filled.Favorite, onOpenFavourites)
            UniverseDestinationChip("Recently Played", Icons.Filled.History, onOpenRecentlyPlayed)
        }
    }
}

@Composable
private fun UniverseDestinationChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    val colors = CelestialTheme.colors
    val spacing = CelestialTheme.spacing
    CelestialGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.cosmicViolet,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = colors.starlight,
            )
        }
    }
}
