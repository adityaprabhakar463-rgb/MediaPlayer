package com.celestial.player.ui.screens.recentplay

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.celestial.player.domain.model.Track
import com.celestial.player.ui.components.glass.GlassEmptyState
import com.celestial.player.ui.screens.common.DestinationScreen
import com.celestial.player.ui.screens.common.trackListItems
import com.celestial.player.ui.theme.CelestialTheme

@Composable
fun RecentlyPlayedScreen(
    recentlyPlayed: List<com.celestial.player.domain.model.RecentPlay>,
    trackFor: (com.celestial.player.domain.model.TrackId) -> Track?,
    currentTrackId: com.celestial.player.domain.model.TrackId?,
    colorSeedFor: (Track) -> Int,
    onPlay: (Track, List<Track>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val playedTracks = recentlyPlayed.mapNotNull { trackFor(it.trackId) }

    DestinationScreen(
        title = "Recently Played",
        subtitle = if (playedTracks.isEmpty()) "Nothing in the log yet" else "${playedTracks.size} entries",
        modifier = modifier,
    ) {
        if (playedTracks.isEmpty()) {
            GlassEmptyState(
                title = "Nothing played yet",
                description = "Songs you play appear here with when you last touched them.",
                icon = Icons.Filled.History,
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp),
            ) {
                trackListItems(
                    tracks = playedTracks,
                    subtitleFor = { track ->
                        val lastWasAt = recentlyPlayed.firstOrNull { it.trackId == track.id }
                        val recency = lastWasAt?.let { formatRecency(it.playedAtMs) } ?: ""
                        if (recency.isEmpty()) track.artistName else "$recency · ${track.artistName}"
                    },
                    isFavourite = { false },
                    isCurrent = { it.id == currentTrackId },
                    onPlay = { track -> onPlay(track, playedTracks) },
                    onToggleFavourite = null,
                    trackColorSeed = colorSeedFor,
                )
            }
        }
    }
}

private fun formatRecency(playedAtMs: Long): String {
    val elapsed = System.currentTimeMillis() - playedAtMs
    val minutes = elapsed / 60_000
    val hours = elapsed / 3_600_000
    val days = elapsed / 86_400_000
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"
        days < 7 -> "$days d ago"
        else -> "$days d ago"
    }
}
