package com.celestial.player.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
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
import com.celestial.player.domain.model.Album
import com.celestial.player.domain.model.Artist
import com.celestial.player.domain.model.Playlist
import com.celestial.player.domain.model.Track
import com.celestial.player.ui.components.glass.GlassArtwork
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
    sampleAlbums: List<Album> = emptyList(),
    sampleArtists: List<Artist> = emptyList(),
    playlists: List<Playlist> = emptyList(),
    suggestedTrackIds: List<com.celestial.player.domain.model.TrackId>,
    isFavourite: (Track) -> Boolean,
    currentTrackId: com.celestial.player.domain.model.TrackId?,
    colorSeedFor: (Track) -> Int,
    onPlayTrack: (Track, List<Track>) -> Unit,
    onToggleFavourite: (Track) -> Unit,
    onOpenAlbum: ((Album) -> Unit)? = null,
    onOpenArtist: ((Artist) -> Unit)? = null,
    onOpenPlaylist: ((Playlist) -> Unit)? = null,
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

    val queryTracks = remember(debouncedQuery, sampleTracks) {
        if (debouncedQuery.isBlank()) emptyList() else
            sampleTracks.filter {
                it.title.contains(debouncedQuery, ignoreCase = true) ||
                    it.artistName.contains(debouncedQuery, ignoreCase = true) ||
                    it.albumTitle.contains(debouncedQuery, ignoreCase = true)
            }
    }

    val queryAlbums = remember(debouncedQuery, sampleAlbums) {
        if (debouncedQuery.isBlank()) emptyList() else
            sampleAlbums.filter {
                it.title.contains(debouncedQuery, ignoreCase = true) ||
                    it.artistName.contains(debouncedQuery, ignoreCase = true)
            }
    }

    val queryArtists = remember(debouncedQuery, sampleArtists) {
        if (debouncedQuery.isBlank()) emptyList() else
            sampleArtists.filter {
                it.name.contains(debouncedQuery, ignoreCase = true)
            }
    }

    val queryPlaylists = remember(debouncedQuery, playlists) {
        if (debouncedQuery.isBlank()) emptyList() else
            playlists.filter {
                it.name.contains(debouncedQuery, ignoreCase = true)
            }
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
                val hasResults = when (selectedCategory) {
                    SearchCategory.Songs -> queryTracks.isNotEmpty()
                    SearchCategory.Albums -> queryAlbums.isNotEmpty()
                    SearchCategory.Artists -> queryArtists.isNotEmpty()
                    SearchCategory.Playlists -> queryPlaylists.isNotEmpty()
                }

                if (!hasResults) {
                    item(key = "no-results") {
                        SearchEmptyState(
                            query = debouncedQuery,
                            categoryLabel = selectedCategory.label,
                        )
                    }
                } else {
                    item(key = "results-header") {
                        GlassSectionHeader(text = selectedCategory.label)
                    }
                    item(key = "results-count") {
                        val count = when (selectedCategory) {
                            SearchCategory.Songs -> queryTracks.size
                            SearchCategory.Albums -> queryAlbums.size
                            SearchCategory.Artists -> queryArtists.size
                            SearchCategory.Playlists -> queryPlaylists.size
                        }
                        Text(
                            text = "$count ${selectedCategory.label.lowercase()} match \"$debouncedQuery\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = CelestialTheme.colors.moonlight,
                            modifier = Modifier.padding(horizontal = CelestialTheme.spacing.screenEdge),
                        )
                    }

                    when (selectedCategory) {
                        SearchCategory.Songs -> {
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

                        SearchCategory.Albums -> {
                            items(items = queryAlbums, key = { album: Album -> "album-${album.id.value}" }) { album ->
                                GlassListRow(
                                    title = album.title,
                                    subtitle = album.artistName,
                                    onClick = { onOpenAlbum?.invoke(album) },
                                    modifier = Modifier.padding(horizontal = CelestialTheme.spacing.sm),
                                    leading = {
                                        GlassArtwork(
                                            size = 48.dp,
                                            colorSeed = album.colorSeed,
                                            shape = MaterialTheme.shapes.medium,
                                            icon = Icons.Filled.Album,
                                        )
                                    },
                                )
                            }
                        }

                        SearchCategory.Artists -> {
                            items(items = queryArtists, key = { artist: Artist -> "artist-${artist.id.value}" }) { artist ->
                                GlassListRow(
                                    title = artist.name,
                                    subtitle = "${artist.albumIds.size} albums · ${artist.trackIds.size} tracks",
                                    onClick = { onOpenArtist?.invoke(artist) },
                                    modifier = Modifier.padding(horizontal = CelestialTheme.spacing.sm),
                                    leading = {
                                        GlassArtwork(
                                            size = 48.dp,
                                            colorSeed = artist.id.value.hashCode(),
                                            shape = MaterialTheme.shapes.extraLarge,
                                            icon = Icons.Filled.Person,
                                            initials = artist.name,
                                        )
                                    },
                                )
                            }
                        }

                        SearchCategory.Playlists -> {
                            items(items = queryPlaylists, key = { playlist: Playlist -> "playlist-${playlist.id.value}" }) { playlist ->
                                GlassListRow(
                                    title = playlist.name,
                                    subtitle = "${playlist.trackIds.size} tracks",
                                    onClick = { onOpenPlaylist?.invoke(playlist) },
                                    modifier = Modifier.padding(horizontal = CelestialTheme.spacing.sm),
                                    leading = {
                                        GlassArtwork(
                                            size = 48.dp,
                                            colorSeed = playlist.id.value.hashCode(),
                                            shape = MaterialTheme.shapes.medium,
                                            icon = Icons.AutoMirrored.Filled.QueueMusic,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchEmptyState(
    query: String,
    categoryLabel: String,
    modifier: Modifier = Modifier,
) {
    GlassEmptyState(
        title = "No $categoryLabel matching",
        description = "Nothing in $categoryLabel matches \"$query\".",
        modifier = modifier,
    )
}

