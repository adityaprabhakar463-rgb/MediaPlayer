package com.celestial.player.ui.screens.playlists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.celestial.player.domain.model.Playlist
import com.celestial.player.ui.components.glass.CelestialGlassSurface
import com.celestial.player.ui.components.glass.GlassEmptyState
import com.celestial.player.ui.components.glass.GlassIconButton
import com.celestial.player.ui.screens.common.DestinationScreen
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    libraryStatusText: String? = null,
    isLibraryReady: Boolean = false,
    onRescan: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onOpenPlaylist: (Playlist) -> Unit,
    onPlayPlaylist: (Playlist) -> Unit,
    onCreatePlaylist: (String) -> Unit,
) {
    var creating by remember { mutableStateOf(false) }
    DestinationScreen(
        title = "Playlists",
        subtitle = "Curated constellations of music",
        modifier = modifier,
        onBack = onBack,
        actions = {
            GlassIconButton(
                icon = Icons.Filled.Add,
                contentDescription = "Create playlist",
                onClick = { creating = true },
            )
        },
    ) {
        if (libraryStatusText != null) {
            CelestialGlassSurface(
                selected = isLibraryReady,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CelestialTheme.spacing.screenEdge, vertical = CelestialTheme.spacing.xs)
                    .then(if (onRescan != null) Modifier.clickable { onRescan() } else Modifier)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isLibraryReady) Icons.Filled.Folder else Icons.Filled.FolderOff,
                        contentDescription = null,
                        tint = if (isLibraryReady) CelestialTheme.colors.auroraBlue else CelestialTheme.colors.starlight.copy(alpha = 0.6f)
                    )
                    Text(
                        text = libraryStatusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = CelestialTheme.colors.starlight,
                        modifier = Modifier.weight(1f)
                    )
                    if (onRescan != null) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Rescan music folder",
                            tint = CelestialTheme.colors.starlight.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = creating) {
            CreatePlaylistAction(
                onCreate = { name ->
                    onCreatePlaylist(name)
                    creating = false
                },
                onDismiss = { creating = false },
            )
        }
        if (playlists.isEmpty() && !creating) {
            GlassEmptyState(
                title = "No playlists yet",
                description = "Create your first playlist with the + button.",
                icon = Icons.Filled.LibraryMusic,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(CelestialTheme.spacing.sm),
            ) {
                items(items = playlists, key = { it.id.value }, contentType = { "playlist" }) { playlist ->
                    PlaylistRow(
                        playlist = playlist,
                        onOpen = { onOpenPlaylist(playlist) },
                        onPlay = { onPlayPlaylist(playlist) },
                    )
                }
            }
        }
    }
}
