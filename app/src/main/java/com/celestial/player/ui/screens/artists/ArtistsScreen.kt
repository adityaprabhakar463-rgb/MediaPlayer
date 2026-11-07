package com.celestial.player.ui.screens.artists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.celestial.player.domain.model.Artist
import com.celestial.player.ui.components.glass.GlassArtwork
import com.celestial.player.ui.components.glass.GlassListRow
import com.celestial.player.ui.screens.common.DestinationScreen
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun ArtistsScreen(
    artists: List<Artist>,
    onOpenArtist: (Artist) -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    DestinationScreen(
        title = "Artists",
        subtitle = "${artists.size} performers",
        modifier = modifier,
        onBack = onBack,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(CelestialTheme.spacing.sm),
        ) {
            items(items = artists, key = { it.id.value }, contentType = { "artist" }) { artist ->
                ArtistRow(artist = artist, onOpen = { onOpenArtist(artist) })
            }
        }
    }
}

@Composable
fun ArtistRow(
    artist: Artist,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val trackWord = if (artist.trackIds.size == 1) "track" else "tracks"
    val albumWord = if (artist.albumIds.size == 1) "album" else "albums"
    GlassListRow(
        title = artist.name,
        subtitle = "${artist.albumIds.size} $albumWord · ${artist.trackIds.size} $trackWord",
        onClick = onOpen,
        modifier = modifier,
        leading = {
            GlassArtwork(
                size = 52.dp,
                colorSeed = artist.id.value.hashCode(),
                shape = MaterialTheme.shapes.extraLarge,
                icon = null,
                initials = artist.name,
            )
        },
    )
}
