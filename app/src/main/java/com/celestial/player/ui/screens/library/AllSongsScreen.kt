package com.celestial.player.ui.screens.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.celestial.player.domain.model.Track
import com.celestial.player.ui.components.glass.GlassIconButton
import com.celestial.player.ui.screens.common.DestinationScreen
import com.celestial.player.ui.screens.common.trackListItems
import com.celestial.player.ui.theme.CelestialTheme

enum class SongSortOption(
    val label: String,
) {
    Title("Title A–Z"),
    Artist("Artist A–Z"),
    Duration("Longest first"),
}

@Composable
fun AllSongsScreen(
    tracks: List<Track>,
    isFavourite: (Track) -> Boolean,
    currentTrackId: com.celestial.player.domain.model.TrackId?,
    colorSeedFor: (Track) -> Int,
    onPlayTrack: (Track, List<Track>) -> Unit,
    onToggleFavourite: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sortOption by remember { mutableStateOf(SongSortOption.Title) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    val sortedTracks = remember(tracks, sortOption) {
        when (sortOption) {
            SongSortOption.Title -> tracks.sortedBy { it.title.lowercase() }
            SongSortOption.Artist -> tracks.sortedBy { it.artistName.lowercase() }
            SongSortOption.Duration -> tracks.sortedByDescending { it.durationMs }
        }
    }

    DestinationScreen(
        title = "All Songs",
        subtitle = "${tracks.size} tracks in orbit",
        modifier = modifier,
        actions = {
            Box {
                GlassIconButton(
                    icon = Icons.Filled.Sort,
                    contentDescription = "Sort songs",
                    onClick = { sortMenuOpen = true },
                )
                DropdownMenu(
                    expanded = sortMenuOpen,
                    onDismissRequest = { sortMenuOpen = false },
                ) {
                    SongSortOption.values().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                sortMenuOpen = false
                                sortOption = option
                            },
                        )
                    }
                }
            }
        },
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
        ) {
            trackListItems(
                tracks = sortedTracks,
                subtitleFor = { "${it.artistName} · ${it.albumTitle}" },
                isFavourite = isFavourite,
                isCurrent = { it.id == currentTrackId },
                onPlay = { track -> onPlayTrack(track, sortedTracks) },
                onToggleFavourite = onToggleFavourite,
                trackColorSeed = colorSeedFor,
            )
        }
    }
}
