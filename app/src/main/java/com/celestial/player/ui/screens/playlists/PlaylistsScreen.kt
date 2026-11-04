package com.celestial.player.ui.screens.playlists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.celestial.player.domain.model.Playlist
import com.celestial.player.ui.components.glass.GlassEmptyState
import com.celestial.player.ui.components.glass.GlassIconButton
import com.celestial.player.ui.screens.common.DestinationScreen
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
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
        actions = {
            GlassIconButton(
                icon = Icons.Filled.Add,
                contentDescription = "Create playlist",
                onClick = { creating = true },
            )
        },
    ) {
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
