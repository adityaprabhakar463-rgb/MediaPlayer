package com.celestial.player.ui.navigation

import androidx.compose.runtime.mutableStateListOf
import com.celestial.player.domain.model.AlbumId
import com.celestial.player.domain.model.ArtistId
import com.celestial.player.domain.model.PlaylistId

sealed interface NavTarget {
    data object Universe : NavTarget
    data object Playlists : NavTarget
    data object Search : NavTarget
    data object AllSongs : NavTarget
    data object Albums : NavTarget
    data object Artists : NavTarget
    data object Favourites : NavTarget
    data object RecentlyPlayed : NavTarget
    data class PlaylistDetail(val playlistId: PlaylistId) : NavTarget
    data class AlbumDetail(val albumId: AlbumId) : NavTarget
    data class ArtistDetail(val artistId: ArtistId) : NavTarget
}

class CelestialNavState {
    val stack = mutableStateListOf<NavTarget>(NavTarget.Universe)
    val current: NavTarget
        get() = stack.last()

    fun navigate(target: NavTarget) {
        if (target == current) return
        stack.add(target)
    }

    fun back() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    fun isAtRoot(): Boolean = stack.size <= 1
}
