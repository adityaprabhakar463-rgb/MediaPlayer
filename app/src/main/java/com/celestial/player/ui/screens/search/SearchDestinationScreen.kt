package com.celestial.player.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.celestial.player.domain.model.Track
import com.celestial.player.ui.components.glass.GlassEmptyState
import com.celestial.player.ui.components.glass.GlassIconButton
import com.celestial.player.ui.components.glass.GlassListRow
import com.celestial.player.ui.components.glass.GlassSearchField
import com.celestial.player.ui.components.glass.GlassSectionHeader
import com.celestial.player.ui.screens.common.DestinationScreen
import com.celestial.player.ui.screens.common.trackListItems
import com.celestial.player.ui.theme.CelestialTheme
import kotlinx.coroutines.delay

@Composable
fun SearchDestinationScreen(
    sampleTracks: List<Track>,
    suggestedTrackIds: List<com.celestial.player.domain.model.TrackId>,
    isFavourite: (Track) -> Boolean,
    currentTrackId: com.celestial.player.domain.model.TrackId?,
    colorSeedFor: (Track) -> Int,
    onPlayTrack: (Track, List<Track>) -> Unit,
    onToggleFavourite: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var debouncedQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SearchCategory.Songs) }
    val recentSearches = remember { mutableStateListOf<String>() }

    LaunchedEffect(query.text) {
        if (query.text == debouncedQuery) return@LaunchedEffect
        delay(280)
        debouncedQuery = query.text
        val trimmed = query.text.trim()
        if (trimmed.length >= 2 && !recentSearches.contains(trimmed)) {
            recentSearches.add(0, trimmed)
            if (recentSearches.size > 6) recentSearches.removeAt(recentSearches.lastIndex)
        }
    }

    val queryTracks = if (debouncedQuery.isBlank()) emptyList() else
        sampleTracks.filter {
            it.title.contains(debouncedQuery, ignoreCase = true) ||
                it.artistName.contains(debouncedQuery, ignoreCase = true) ||
                it.albumTitle.contains(debouncedQuery, ignoreCase = true)
        }

    DestinationScreen(
        title = "Search",
        subtitle = "Find songs, albums, artists and playlists",
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(CelestialTheme.spacing.md)) {
            GlassSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search Celestial",
            )
            SearchCategoryTabs(
                selected = selectedCategory,
                onSelected = { selectedCategory = it },
            )
        }
        LazyColumn(
            contentPadding = PaddingValues(bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(CelestialTheme.spacing.sm),
        ) {
            if (debouncedQuery.isBlank()) {
                if (recentSearches.isNotEmpty()) {
                    item(key = "recent-header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = CelestialTheme.spacing.screenEdge),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Recent searches",
                                style = MaterialTheme.typography.labelMedium,
                                color = CelestialTheme.colors.moonlight,
                                modifier = Modifier.weight(1f),
                            )
                            GlassIconButton(
                                icon = Icons.Filled.History,
                                contentDescription = "Clear recent searches",
                                onClick = { recentSearches.clear() },
                            )
                        }
                    }
                    items(items = recentSearches, key = { "recent-$it" }) { recent ->
                        GlassListRow(
                            title = recent,
                            onClick = {
                                query = TextFieldValue(recent)
                                debouncedQuery = recent
                            },
                            modifier = Modifier.padding(horizontal = CelestialTheme.spacing.sm),
                            leading = {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = CelestialTheme.colors.moonlight,
                                    modifier = Modifier.padding(CelestialTheme.spacing.sm),
                                )
                            },
                        )
                    }
                }
                item(key = "suggested-header") {
                    Box(Modifier.padding(top = CelestialTheme.spacing.sm)) {
                        GlassSectionHeader(text = "Suggested")
                    }
                }
                trackListItems(
                    tracks = sampleTracks.filter { suggestedTrackIds.contains(it.id) },
                    subtitleFor = { it.artistName },
                    isFavourite = isFavourite,
                    isCurrent = { it.id == currentTrackId },
                    onPlay = { track -> onPlayTrack(track, sampleTracks) },
                    onToggleFavourite = onToggleFavourite,
                    trackColorSeed = colorSeedFor,
                )
            } else {
                if (queryTracks.isEmpty()) {
                    item(key = "no-results") {
                        SearchEmptyState(query = debouncedQuery)
                    }
                } else {
                    item(key = "results-header") {
                        GlassSectionHeader(
                            text = when (selectedCategory) {
                                SearchCategory.Songs -> "Songs"
                                SearchCategory.Albums -> "Albums"
                                SearchCategory.Artists -> "Artists"
                                SearchCategory.Playlists -> "Playlists"
                            },
                        )
                    }
                    item(key = "results-count") {
                        Text(
                            text = "${queryTracks.size} matches in songs",
                            style = MaterialTheme.typography.bodySmall,
                            color = CelestialTheme.colors.moonlight,
                            modifier = Modifier.padding(horizontal = CelestialTheme.spacing.screenEdge),
                        )
                    }
                    trackListItems(
                        tracks = queryTracks,
                        subtitleFor = { "${it.artistName} · ${it.albumTitle}" },
                        isFavourite = isFavourite,
                        isCurrent = { it.id == currentTrackId },
                        onPlay = { track -> onPlayTrack(track, queryTracks) },
                        onToggleFavourite = onToggleFavourite,
                        trackColorSeed = colorSeedFor,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchEmptyState(query: String, modifier: Modifier = Modifier) {
    GlassEmptyState(
        title = "No results",
        description = "Nothing in the sample library matches \"$query\".",
        modifier = modifier,
    )
}
