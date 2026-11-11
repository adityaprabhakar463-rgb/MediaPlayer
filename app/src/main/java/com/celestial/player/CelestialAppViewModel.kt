package com.celestial.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.celestial.player.data.LibraryStateHolder
import com.celestial.player.data.db.CelestialDatabase
import com.celestial.player.domain.model.TrackId
import com.celestial.player.playback.PlayerController
import com.celestial.player.ui.navigation.CelestialNavState

/**
 * Activity-scoped state that must survive configuration changes.
 *
 * Without this, rotating the device would recreate [LibraryStateHolder] and restart the SAF scan,
 * drop the navigation stack, and reconnect the media controller on every rotation.
 */
class CelestialAppViewModel(application: Application) : AndroidViewModel(application) {

    val library = LibraryStateHolder(
        context = application,
        coroutineScope = viewModelScope,
        database = CelestialDatabase.get(application),
    )

    val player = PlayerController(
        context = application,
        coroutineScope = viewModelScope,
        onTrackPlayed = library::recordPlayed,
        trackResolver = { mediaId -> library.trackById(TrackId(mediaId)) },
    )

    val nav = CelestialNavState()

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}
