package com.celestial.player.data

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import com.celestial.player.data.remote.ApiException
import com.celestial.player.data.remote.CelestialApiClient
import com.celestial.player.data.remote.toDomain
import com.celestial.player.domain.model.Album
import com.celestial.player.domain.model.Artist
import com.celestial.player.domain.model.Playlist
import com.celestial.player.domain.model.PlaylistId
import com.celestial.player.domain.model.RecentPlay
import com.celestial.player.domain.model.Track
import com.celestial.player.domain.model.TrackId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

enum class BackendStatus {
    Checking,
    Online,
    Offline,
}

/**
 * Single source of truth for catalog and library state.
 *
 * The holder starts with the offline sample library, then replaces it with the backend snapshot on
 * the first successful sync. Mutations are optimistic: the UI updates immediately and the remote
 * call is reconciled in the background. Pending favourites, recents and offline-created playlists
 * are retried on the next successful sync.
 */
class LibraryStateHolder(
    val apiClient: CelestialApiClient,
    private val coroutineScope: CoroutineScope,
) {
    var backendStatus by mutableStateOf(BackendStatus.Checking)
        private set

    var backendStatusText by mutableStateOf("Connecting to Celestial…")
        private set

    var isSyncing by mutableStateOf(false)
        private set

    var lastSyncError by mutableStateOf<String?>(null)
        private set

    var catalogVersion by mutableLongStateOf(0L)
        private set

    val isBackendConnected: Boolean
        get() = backendStatus == BackendStatus.Online

    private val _tracks = mutableStateListOf<Track>().apply { addAll(SampleLibrary.tracks) }
    val tracks: List<Track> get() = _tracks

    private val _albums = mutableStateListOf<Album>().apply { addAll(SampleLibrary.albums) }
    val albums: List<Album> get() = _albums

    private val _artists = mutableStateListOf<Artist>().apply { addAll(SampleLibrary.artists) }
    val artists: List<Artist> get() = _artists

    private val _playlists = mutableStateListOf<Playlist>().apply { addAll(SampleLibrary.playlists) }
    val playlists: List<Playlist> get() = _playlists

    private val _recents = mutableStateListOf<RecentPlay>().apply { addAll(sampleRecents()) }
    val recents: List<RecentPlay> get() = _recents

    val favourites = mutableStateSetOf<TrackId>()

    /** Track id -> desired state. Survives transient sync failures. */
    private val pendingFavourites = linkedMapOf<String, Boolean>()
    private val pendingRecents = ArrayDeque<String>()
    private val pendingPlaylists = mutableSetOf<String>()
    private var retryJob: Job? = null
    private var retryAttempt = 0

    init {
        sync()
    }

    fun sync(manual: Boolean = false) {
        if (isSyncing) return
        if (manual) {
            retryJob?.cancel()
            retryJob = null
            retryAttempt = 0
        }
        isSyncing = true
        backendStatus = BackendStatus.Checking
        backendStatusText = "Connecting to ${apiClient.baseUrl}…"
        lastSyncError = null

        coroutineScope.launch {
            try {
                if (!apiClient.checkHealth()) {
                    markOffline("Server unreachable")
                    scheduleRetry()
                    return@launch
                }

                apiClient.ensureAuthenticated()
                val snapshot = apiClient.fetchCatalog()
                val playlists = apiClient.getPlaylists()
                val favouriteIds = apiClient.getFavourites()
                val recents = apiClient.getRecents()

                applyCatalog(snapshot)
                applyPlaylists(playlists.map { it.toDomain() })
                applyFavourites(favouriteIds)
                applyRecents(recents.map { it.toDomain() })

                catalogVersion = snapshot.version
                backendStatus = BackendStatus.Online
                backendStatusText = "Connected · ${apiClient.baseUrl}"
                lastSyncError = null
                retryAttempt = 0
                retryJob?.cancel()
                retryJob = null

                flushPendingWork()
            } catch (e: Exception) {
                Log.w(TAG, "Sync failed", e)
                markOffline(e.readableMessage())
                scheduleRetry()
            } finally {
                isSyncing = false
            }
        }
    }

    fun isFavourite(id: TrackId): Boolean = favourites.contains(id)

    fun toggleFavourite(id: TrackId) {
        val makeFavourite = !favourites.contains(id)
        if (makeFavourite) favourites.add(id) else favourites.remove(id)
        pendingFavourites[id.value] = makeFavourite

        coroutineScope.launch {
            runCatching {
                if (makeFavourite) {
                    apiClient.addFavourite(id.value)
                } else {
                    apiClient.removeFavourite(id.value)
                }
            }.onSuccess {
                pendingFavourites.remove(id.value)
            }.onFailure { error ->
                if (error is ApiException && error.status == 404 && !makeFavourite) {
                    pendingFavourites.remove(id.value)
                    return@onFailure
                }
                noteFailure(error, "Favourite saved locally · will retry")
            }
        }
    }

    fun favouriteTracks(): List<Track> = _tracks.filter { favourites.contains(it.id) }

    fun recordPlayed(track: Track) {
        _recents.removeAll { it.trackId == track.id }
        _recents.add(0, RecentPlay(track.id, System.currentTimeMillis()))
        while (_recents.size > 100) _recents.removeAt(_recents.lastIndex)

        coroutineScope.launch {
            runCatching { apiClient.recordRecent(track.id.value) }
                .onFailure {
                    pendingRecents.addLast(track.id.value)
                    noteFailure(it, "Recent play queued · will retry")
                }
        }
    }

    fun recentTracks(limit: Int = 50): List<RecentPlay> = _recents.take(limit)

    fun createPlaylist(name: String, tracks: List<Track> = emptyList()) {
        val cleanName = name.trim().ifBlank { "Untitled playlist" }
        val localId = "local-${UUID.randomUUID()}"
        val local = Playlist(
            id = PlaylistId(localId),
            name = cleanName,
            trackIds = tracks.map { it.id },
        )
        _playlists.add(0, local)
        pendingPlaylists += localId

        if (!isBackendConnected) return
        coroutineScope.launch {
            runCatching { apiClient.createPlaylist(cleanName, tracks.map { it.id.value }) }
                .onSuccess { remote ->
                    pendingPlaylists -= localId
                    replacePlaylist(localId, remote.id, remote.name, remote.trackIds.map(::TrackId))
                    backendStatusText = "Playlist synced · ${apiClient.baseUrl}"
                }
                .onFailure {
                    noteFailure(it, "Playlist saved locally · will sync")
                }
        }
    }

    fun deletePlaylist(id: PlaylistId) {
        _playlists.removeAll { it.id == id }
        pendingPlaylists -= id.value
        coroutineScope.launch {
            runCatching { apiClient.deletePlaylist(id.value) }
                .onFailure { Log.d(TAG, "Playlist delete failed: ${it.readableMessage()}") }
        }
    }

    fun renamePlaylist(id: PlaylistId, name: String) {
        val cleanName = name.trim().ifBlank { "Untitled playlist" }
        val existing = playlistById(id) ?: return
        replacePlaylist(id.value, id.value, cleanName, existing.trackIds)
        coroutineScope.launch {
            runCatching {
                apiClient.updatePlaylist(
                    playlistId = id.value,
                    name = cleanName,
                    trackIds = existing.trackIds.map { it.value },
                )
            }.onFailure { Log.d(TAG, "Playlist rename failed: ${it.readableMessage()}") }
        }
    }

    fun addTrackToPlaylist(playlistId: PlaylistId, track: Track) {
        val playlist = playlistById(playlistId) ?: return
        if (playlist.trackIds.contains(track.id)) return
        val updated = playlist.trackIds + track.id
        replacePlaylist(playlistId.value, playlistId.value, playlist.name, updated)
        coroutineScope.launch {
            runCatching {
                apiClient.updatePlaylist(playlistId.value, trackIds = updated.map { it.value })
            }.onFailure { Log.d(TAG, "Add to playlist failed: ${it.readableMessage()}") }
        }
    }

    fun removeTrackFromPlaylist(playlistId: PlaylistId, track: Track) {
        val playlist = playlistById(playlistId) ?: return
        val updated = playlist.trackIds - track.id
        replacePlaylist(playlistId.value, playlistId.value, playlist.name, updated)
        coroutineScope.launch {
            runCatching {
                apiClient.updatePlaylist(playlistId.value, trackIds = updated.map { it.value })
            }.onFailure { Log.d(TAG, "Remove from playlist failed: ${it.readableMessage()}") }
        }
    }

    fun playlistById(id: PlaylistId): Playlist? = _playlists.firstOrNull { it.id == id }

    fun trackById(id: TrackId): Track? = _tracks.firstOrNull { it.id == id }

    fun colorSeedFor(track: Track): Int =
        _albums.firstOrNull { it.id == track.albumId }?.colorSeed ?: track.albumId.value.hashCode()

    fun localSearch(query: String): List<Track> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return _tracks.filter {
            it.title.contains(trimmed, ignoreCase = true) ||
                it.artistName.contains(trimmed, ignoreCase = true) ||
                it.albumTitle.contains(trimmed, ignoreCase = true)
        }
    }

    suspend fun remoteSearch(query: String): List<Track> {
        if (!isBackendConnected || query.trim().length < 2) return localSearch(query)
        return runCatching {
            apiClient.searchCatalog(query.trim()).tracks.map { it.toDomain() }
        }.getOrElse {
            Log.d(TAG, "Search fallback: ${it.readableMessage()}")
            localSearch(query)
        }
    }

    private fun applyCatalog(snapshot: com.celestial.player.data.remote.CatalogSnapshotDto) {
        if (snapshot.tracks.isEmpty()) return
        _tracks.clear()
        _tracks.addAll(snapshot.tracks.filter { it.isAvailable && !it.isDelete }.map { it.toDomain() })

        if (snapshot.albums.isNotEmpty()) {
            _albums.clear()
            _albums.addAll(snapshot.albums.map { it.toDomain() })
        }
        if (snapshot.artists.isNotEmpty()) {
            _artists.clear()
            _artists.addAll(snapshot.artists.map { it.toDomain() })
        }
    }

    private fun applyPlaylists(remote: List<Playlist>) {
        val localOnly = _playlists.filter { it.id.value.startsWith(LOCAL_PREFIX) }
        _playlists.clear()
        _playlists.addAll(localOnly + remote)
        pendingPlaylists.retainAll(localOnly.map { it.id.value }.toSet())
    }

    private fun applyFavourites(remoteIds: List<String>) {
        favourites.clear()
        remoteIds.forEach { favourites.add(TrackId(it)) }
        pendingFavourites.forEach { (trackId, shouldBeFavourite) ->
            if (shouldBeFavourite) favourites.add(TrackId(trackId)) else favourites.remove(TrackId(trackId))
        }
    }

    private fun applyRecents(remote: List<RecentPlay>) {
        _recents.clear()
        _recents.addAll(remote)
    }

    private fun flushPendingWork() {
        pendingFavourites.entries.toList().forEach { (trackId, shouldBeFavourite) ->
            coroutineScope.launch {
                runCatching {
                    if (shouldBeFavourite) {
                        apiClient.addFavourite(trackId)
                    } else {
                        apiClient.removeFavourite(trackId)
                    }
                }.onSuccess {
                    pendingFavourites.remove(trackId)
                }
            }
        }

        if (pendingRecents.isNotEmpty()) {
            val queued = pendingRecents.toList()
            pendingRecents.clear()
            coroutineScope.launch {
                queued.forEach { trackId ->
                    runCatching { apiClient.recordRecent(trackId) }
                        .onFailure { pendingRecents.addLast(trackId) }
                }
            }
        }

        _playlists.filter { it.id.value.startsWith(LOCAL_PREFIX) }.forEach { playlist ->
            coroutineScope.launch {
                runCatching {
                    apiClient.createPlaylist(playlist.name, playlist.trackIds.map { it.value })
                }.onSuccess { remote ->
                    pendingPlaylists -= playlist.id.value
                    replacePlaylist(playlist.id.value, remote.id, remote.name, remote.trackIds.map(::TrackId))
                }
            }
        }
    }

    private fun replacePlaylist(oldId: String, newId: String, name: String, trackIds: List<TrackId>) {
        val index = _playlists.indexOfFirst { it.id.value == oldId }
        val replacement = Playlist(PlaylistId(newId), name, trackIds)
        if (index >= 0) {
            _playlists[index] = replacement
        } else if (_playlists.none { it.id.value == newId }) {
            _playlists.add(0, replacement)
        }
    }

    private fun markOffline(reason: String) {
        backendStatus = BackendStatus.Offline
        backendStatusText = "Offline · local library ($reason)"
        lastSyncError = reason
    }

    /**
     * The server may start after the app, or the ADB tunnel may come up late. Retry with a bounded
     * exponential backoff so the UI recovers without requiring the user to kill and reopen the app.
     */
    private fun scheduleRetry() {
        if (retryAttempt >= MAX_SYNC_RETRIES) return
        retryJob?.cancel()
        val delayMs = (RETRY_BASE_DELAY_MS shl retryAttempt.coerceAtMost(4))
            .coerceAtMost(RETRY_MAX_DELAY_MS)
        retryAttempt++
        retryJob = coroutineScope.launch {
            delay(delayMs)
            retryJob = null
            sync()
        }
    }

    private fun noteFailure(error: Throwable, pendingMessage: String) {
        if (error is ApiException) {
            backendStatusText = pendingMessage
        } else {
            markOffline(error.readableMessage())
        }
    }

    private fun Throwable.readableMessage(): String =
        (this as? ApiException)?.let { it.detail ?: it.title } ?: (message ?: javaClass.simpleName)

    private fun sampleRecents(): List<RecentPlay> {
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
        return SampleLibrary.tracks.takeLast(offsets.size).reversed()
            .mapIndexed { index, track -> RecentPlay(track.id, now - offsets[index]) }
    }

    private companion object {
        const val TAG = "LibraryStateHolder"
        const val LOCAL_PREFIX = "local-"
        const val MAX_SYNC_RETRIES = 6
        const val RETRY_BASE_DELAY_MS = 4_000L
        const val RETRY_MAX_DELAY_MS = 60_000L
    }
}
