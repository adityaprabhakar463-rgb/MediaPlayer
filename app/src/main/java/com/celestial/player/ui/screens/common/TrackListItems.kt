package com.celestial.player.ui.screens.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.celestial.player.domain.model.Track
import com.celestial.player.ui.components.glass.GlassTrackRow
import com.celestial.player.ui.theme.CelestialTheme

fun LazyListScope.trackListItems(
    tracks: List<Track>,
    subtitleFor: (Track) -> String,
    isFavourite: (Track) -> Boolean,
    isCurrent: (Track) -> Boolean,
    onPlay: (Track) -> Unit,
    onToggleFavourite: ((Track) -> Unit)?,
    trackColorSeed: (Track) -> Int,
) {
    items(items = tracks, key = { it.id.value }, contentType = { "track" }) { track ->
        GlassTrackRow(
            track = track,
            subtitle = subtitleFor(track),
            colorSeed = trackColorSeed(track),
            isFavourite = isFavourite(track),
            isCurrent = isCurrent(track),
            onToggleFavourite = onToggleFavourite?.let { f -> ({ f(track) }) },
            onClick = { onPlay(track) },
                modifier = Modifier.padding(
                    horizontal = com.celestial.player.ui.theme.CelestialTheme.spacing.sm,
                ),
        )
    }
}

fun LazyListScope.sectionHeader(text: String) {
    item(key = "header-$text", contentType = "sectionHeader") {
        com.celestial.player.ui.components.glass.GlassSectionHeader(text = text)
    }
}

