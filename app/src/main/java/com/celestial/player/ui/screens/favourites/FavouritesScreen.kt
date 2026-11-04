package com.celestial.player.ui.screens.favourites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.celestial.player.domain.model.Track
import com.celestial.player.ui.components.glass.GlassEmptyState
import com.celestial.player.ui.components.glass.GlassIconButton
import com.celestial.player.ui.screens.common.DestinationScreen
import com.celestial.player.ui.screens.common.trackListItems
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun FavouritesScreen(
    tracks: List<Track>,
    currentTrackId: com.celestial.player.domain.model.TrackId?,
    colorSeedFor: (Track) -> Int,
    onPlay: (Track, List<Track>) -> Unit,
    onToggleFavourite: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    DestinationScreen(
        title = "Favourites",
        subtitle = if (tracks.isEmpty()) "Nothing marked yet" else "${tracks.size} marked with a heart",
        modifier = modifier,
        actions = {
            if (tracks.size > 1) {
                GlassIconButton(
                    icon = Icons.Filled.Shuffle,
                    contentDescription = "Shuffle favourites",
                    onClick = { tracks.randomOrNull()?.let { onPlay(it, tracks) } },
                )
            }
        },
    ) {
        if (tracks.isEmpty()) {
            GlassEmptyState(
                title = "No favourites yet",
                description = "Tap the heart next to a song to mark it as a favourite.",
                icon = Icons.Filled.FavoriteBorder,
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp),
            ) {
                trackListItems(
                    tracks = tracks,
                    subtitleFor = { "${it.artistName} · ${it.albumTitle}" },
                    isFavourite = { true },
                    isCurrent = { it.id == currentTrackId },
                    onPlay = { track -> onPlay(track, tracks) },
                    onToggleFavourite = onToggleFavourite,
                    trackColorSeed = colorSeedFor,
                )
            }
        }
    }
}
