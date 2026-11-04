package com.celestial.player.ui.screens.albums

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.celestial.player.domain.model.Album
import com.celestial.player.domain.model.Track
import com.celestial.player.ui.components.glass.GlassArtwork
import com.celestial.player.ui.components.glass.GlassIconButton
import com.celestial.player.ui.screens.common.DestinationScreen
import com.celestial.player.ui.screens.common.trackListItems
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun AlbumDetailScreen(
    album: Album,
    tracks: List<Track>,
    isFavourite: (Track) -> Boolean,
    currentTrackId: com.celestial.player.domain.model.TrackId?,
    onBack: () -> Unit,
    onPlay: (Track, List<Track>) -> Unit,
    onToggleFavourite: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    DestinationScreen(
        title = album.title,
        subtitle = album.artistName,
        modifier = modifier,
        onBack = onBack,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(CelestialTheme.spacing.sm),
        ) {
            item(key = "album-header", contentType = "albumHeader") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = CelestialTheme.spacing.screenEdge,
                            end = CelestialTheme.spacing.screenEdge,
                            top = CelestialTheme.spacing.md,
                            bottom = CelestialTheme.spacing.sm,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CelestialTheme.spacing.lg),
                ) {
                    GlassArtwork(
                        size = 96.dp,
                        colorSeed = album.colorSeed,
                        shape = MaterialTheme.shapes.medium,
                        icon = Icons.Filled.Album,
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(CelestialTheme.spacing.sm),
                    ) {
                        Text(
                            text = "${tracks.size} tracks",
                            style = MaterialTheme.typography.bodySmall,
                            color = CelestialTheme.colors.moonlight,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(CelestialTheme.spacing.sm)) {
                            GlassIconButton(
                                icon = Icons.Filled.PlayArrow,
                                contentDescription = "Play album",
                                enabled = tracks.isNotEmpty(),
                                onClick = { tracks.firstOrNull()?.let { onPlay(it, tracks) } },
                            )
                            GlassIconButton(
                                icon = Icons.Filled.Shuffle,
                                contentDescription = "Shuffle album",
                                enabled = tracks.isNotEmpty(),
                                onClick = { tracks.randomOrNull()?.let { onPlay(it, tracks) } },
                            )
                        }
                    }
                }
            }
            trackListItems(
                tracks = tracks,
                subtitleFor = { "Track ${it.trackIndex} · ${com.celestial.player.ui.screens.common.formatTrackDuration(it.durationMs)}" },
                isFavourite = isFavourite,
                isCurrent = { it.id == currentTrackId },
                onPlay = { track -> onPlay(track, tracks) },
                onToggleFavourite = onToggleFavourite,
                trackColorSeed = { album.colorSeed },
            )
        }
    }
}
