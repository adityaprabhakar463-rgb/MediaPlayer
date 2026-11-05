package com.celestial.player.ui.screens.artists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.celestial.player.domain.model.Album
import com.celestial.player.domain.model.Artist
import com.celestial.player.ui.components.glass.GlassArtwork
import com.celestial.player.ui.components.glass.GlassListRow
import com.celestial.player.ui.components.glass.GlassSectionHeader
import com.celestial.player.ui.screens.common.DestinationScreen
import com.celestial.player.ui.screens.common.trackListItems
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun ArtistDetailScreen(
    artist: Artist,
    tracks: List<com.celestial.player.domain.model.Track>,
    albums: List<Album>,
    isFavourite: (com.celestial.player.domain.model.Track) -> Boolean,
    currentTrackId: com.celestial.player.domain.model.TrackId?,
    onBack: () -> Unit,
    onPlay: (com.celestial.player.domain.model.Track, List<com.celestial.player.domain.model.Track>) -> Unit,
    onToggleFavourite: (com.celestial.player.domain.model.Track) -> Unit,
    onOpenAlbum: (Album) -> Unit,
    modifier: Modifier = Modifier,
) {
    DestinationScreen(
        title = artist.name,
        subtitle = "${artist.albumIds.size} albums",
        modifier = modifier,
        onBack = onBack,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(CelestialTheme.spacing.sm),
        ) {
            item(key = "artist-header", contentType = "artistHeader") {
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
                        colorSeed = artist.id.value.hashCode(),
                        shape = MaterialTheme.shapes.extraLarge,
                        icon = null,
                        initials = artist.name,
                    )
                }
            }
            if (albums.isNotEmpty()) {
                item(key = "artist-albums-header") {
                    GlassSectionHeader(text = "Albums")
                }
                items(items = albums, key = { "artist-album-${it.id.value}" }) { album ->
                    GlassListRow(
                        title = album.title,
                        subtitle = "Album",
                        onClick = { onOpenAlbum(album) },
                        leading = {
                            GlassArtwork(
                                size = 48.dp,
                                colorSeed = album.colorSeed,
                                icon = Icons.AutoMirrored.Filled.QueueMusic,
                            )
                        },
                    )
                }
            }
            item(key = "artist-tracks-header") {
                GlassSectionHeader(text = "Songs")
            }
            trackListItems(
                tracks = tracks,
                subtitleFor = { "${it.albumTitle}" },
                isFavourite = isFavourite,
                isCurrent = { it.id == currentTrackId },
                onPlay = { track -> onPlay(track, tracks) },
                onToggleFavourite = onToggleFavourite,
                trackColorSeed = { artist.id.value.hashCode() },
            )
        }
    }
}
