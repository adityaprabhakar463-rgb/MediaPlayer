package com.celestial.player.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import com.celestial.player.domain.model.Playlist
import com.celestial.player.domain.model.PlaylistId
import com.celestial.player.domain.model.RecentPlay
import com.celestial.player.domain.model.Track
import com.celestial.player.domain.model.TrackId

class LibraryStateHolder(
    private val library: SampleLibrary,
) {
    val favourites = mutableStateSetOf<TrackId>()

    private val _playlists = mutableStateListOf<Playlist>().apply { addAll(library.playlists) }
    val playlists: List<Playlist> get() = _playlists.toList()

    private val _recents = mutableStateListOf<RecentPlay>()

    init {
        val now = System.currentTimeMillis()
        val offsets = listOf(
            4 * 60_000L,
            22 * 60_000L,
            55 * 60_000L,
            3 * 3_600_000L,
            7 * 3_600_000L,
            26 * 3_600_000L,
            2 * 86_400_000L,
            5 * 86_400_000L,
        )
        val seeded = library.tracks.takeLast(offsets.size).reversed()
        seeded.forEachIndexed { index, track ->
            _recents.add(RecentPlay(track.id, now - offsets[index]))
        }
    }

    val recents: List<RecentPlay> get() = _recents.toList()

    fun isFavourite(id: TrackId): Boolean = favourites.contains(id)

    fun toggleFavourite(id: TrackId) {
        if (!favourites.add(id)) favourites.remove(id)
    }

    fun favouriteTracks(): List<Track> =
        library.tracks.filter { favourites.contains(it.id) }

    fun recentTracks(limit: Int = 50): List<RecentPlay> =
        _recents.take(limit)

    fun recordPlayed(track: Track) {
        _recents.removeAll { it.trackId == track.id }
        _recents.add(0, RecentPlay(track.id, System.currentTimeMillis()))
        while (_recents.size > 100) _recents.removeAt(_recents.lastIndex)
    }

    fun createPlaylist(name: String, tracks: List<Track> = emptyList()) {
        val id = PlaylistId("pl-${_playlists.size + 1}-user")
        _playlists.add(
            Playlist(
                id,
                name.ifBlank { "Untitled playlist" },
                tracks.map { it.id },
            ),
        )
    }

    fun playlistById(id: PlaylistId): Playlist? = _playlists.firstOrNull { it.id == id }

    fun trackById(id: TrackId): Track? = library.trackById(id)
}
