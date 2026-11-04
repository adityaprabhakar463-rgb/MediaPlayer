package com.celestial.player.ui.screens.playlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.celestial.player.domain.model.Playlist
import com.celestial.player.ui.components.glass.GlassArtwork
import com.celestial.player.ui.components.glass.GlassEmptyState
import com.celestial.player.ui.components.glass.GlassIconButton
import com.celestial.player.ui.screens.common.DestinationScreen
import com.celestial.player.ui.screens.common.trackListItems
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    tracks: List<com.celestial.player.domain.model.Track>,
    isFavourite: (com.celestial.player.domain.model.Track) -> Boolean,
    currentTrackId: com.celestial.player.domain.model.TrackId?,
    onBack: () -> Unit,
    onPlay: (com.celestial.player.domain.model.Track, List<com.celestial.player.domain.model.Track>) -> Unit,
    onToggleFavourite: (com.celestial.player.domain.model.Track) -> Unit,
    colorSeedFor: (com.celestial.player.domain.model.Track) -> Int,
    modifier: Modifier = Modifier,
) {
    DestinationScreen(
        title = playlist.name,
        subtitle = when (tracks.size) {
            0 -> "No tracks yet"
            1 -> "1 track"
            else -> "${tracks.size} tracks"
        },
        modifier = modifier,
        onBack = onBack,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(CelestialTheme.spacing.sm),
        ) {
            item(key = "playlist-header", contentType = "playlistHeader") {
                PlaylistDetailHeader(
                    playlist = playlist,
                    tracks = tracks,
                    onPlayAll = { tracks.firstOrNull()?.let { onPlay(it, tracks) } },
                    onShuffleAll = { tracks.randomOrNull()?.let { onPlay(it, tracks) } },
                )
            }
            if (tracks.isEmpty()) {
                item(key = "playlist-empty") {
                    GlassEmptyState(
                        title = "Empty playlist",
                        description = "Add tracks to fill this constellation.",
                    )
                }
            } else {
                trackListItems(
                    tracks = tracks,
                    subtitleFor = { "${it.artistName} · ${it.albumTitle}" },
                    isFavourite = isFavourite,
                    isCurrent = { it.id == currentTrackId },
                    onPlay = { track -> onPlay(track, tracks) },
                    onToggleFavourite = onToggleFavourite,
                    trackColorSeed = colorSeedFor,
                )
            }
        }
    }
}

@Composable
fun PlaylistDetailHeader(
    playlist: Playlist,
    tracks: List<com.celestial.player.domain.model.Track>,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CelestialTheme.colors
    val spacing = CelestialTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = spacing.screenEdge,
                end = spacing.screenEdge,
                top = spacing.md,
                bottom = spacing.md,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        GlassArtwork(
            size = 96.dp,
            colorSeed = playlist.id.value.hashCode(),
            shape = MaterialTheme.shapes.medium,
            icon = Icons.AutoMirrored.Filled.QueueMusic,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            if (tracks.isEmpty()) {
                Text(
                    text = "Empty playlist",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.moonlight,
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    GlassIconButton(
                        icon = Icons.Filled.PlayArrow,
                        contentDescription = "Play playlist",
                        onClick = onPlayAll,
                    )
                    GlassIconButton(
                        icon = Icons.Filled.Shuffle,
                        contentDescription = "Shuffle playlist",
                        onClick = onShuffleAll,
                    )
                }
            }
        }
    }
}
