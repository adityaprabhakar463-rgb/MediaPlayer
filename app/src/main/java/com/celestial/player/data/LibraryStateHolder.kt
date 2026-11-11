package com.celestial.player.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import com.celestial.player.data.db.CelestialDatabase
import com.celestial.player.data.db.FavouriteEntity
import com.celestial.player.data.db.PlaylistEntity
import com.celestial.player.data.db.RecentEntity
import com.celestial.player.data.db.TrackEntity
import com.celestial.player.data.local.FolderMediaScanner
import com.celestial.player.data.local.LibraryPreferences
import com.celestial.player.domain.model.Album
import com.celestial.player.domain.model.Playlist
import com.celestial.player.domain.model.PlaylistId
import com.celestial.player.domain.model.RecentPlay
import com.celestial.player.domain.model.Track
import com.celestial.player.domain.model.TrackId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

enum class LibraryStatus {
    NoFolder,
    Scanning,
    Ready,
    Error,
}

/**
 * Single source of truth for the on-device music library.
 *
 * Files are indexed from one user-chosen SAF folder (recursively) into Room. The UI observes
 * Compose state lists derived from Room so every screen updates after a scan without extra wiring.
 * Playlists, favourites and recents are also persisted locally.
 */
class LibraryStateHolder(
    context: Context,
    private val coroutineScope: CoroutineScope,
    private val database: CelestialDatabase,
) {
    private val appContext = context.applicationContext
    private val dao = database.libraryDao()
    private val scanner = FolderMediaScanner(appContext)
    private val preferences = LibraryPreferences(appContext)

    var folderUri by mutableStateOf(preferences.musicFolderUri)
        private set

    var folderName by mutableStateOf(preferences.musicFolderName)
        private set

    var status by mutableStateOf(
        if (preferences.musicFolderUri != null) LibraryStatus.Scanning else LibraryStatus.NoFolder,
    )
        private set

    var statusText by mutableStateOf(
        if (preferences.musicFolderUri != null) "Opening your music folder…" else "No music folder selected",
    )
        private set

    var isScanning by mutableStateOf(false)
        private set

    var scannedFileCount by mutableIntStateOf(0)
        private set

    private var scanJob: kotlinx.coroutines.Job? = null
    private var scanToken = 0

    val hasFolder: Boolean get() = folderUri != null

    private val _tracks = mutableStateListOf<Track>()
    val tracks: List<Track> get() = _tracks

    private val _albums = mutableStateListOf<Album>()

    private val _playlists = mutableStateListOf<Playlist>()
    val playlists: List<Playlist> get() = _playlists

    private val _recents = mutableStateListOf<RecentPlay>()
    val recents: List<RecentPlay> get() = _recents

    val favourites = mutableStateSetOf<TrackId>()

    init {
        observeLibrary()
        if (hasFolder) startScan() else publishStatus()
    }

    fun selectFolder(uri: Uri) {
        val resolver = appContext.contentResolver
        runCatching {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }.onFailure { Log.w(TAG, "Could not persist folder permission", it) }

        folderUri = uri.toString()
        preferences.musicFolderUri = folderUri
        scanToken++
        val token = scanToken
        scanJob?.cancel()
        isScanning = false
        scanJob = coroutineScope.launch {
            folderName = scanner.displayName(uri) ?: preferences.musicFolderName
            preferences.musicFolderName = folderName
            scanFolder(token)
        }
    }

    fun rescan() {
        startScan()
    }

    private fun startScan() {
        val token = ++scanToken
        scanJob?.cancel()
        isScanning = false
        scanJob = coroutineScope.launch { scanFolder(token) }
    }

    fun forgetFolder() {
        scanToken++
        scanJob?.cancel()
        isScanning = false
        val uri = folderUri?.let(Uri::parse)
        if (uri != null) {
            runCatching {
                appContext.contentResolver.releasePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
        }
        folderUri = null
        folderName = null
        preferences.musicFolderUri = null
        preferences.musicFolderName = null
        coroutineScope.launch {
            dao.replaceLibrary(emptyList())
            scanner.clearArtworkCache()
            status = LibraryStatus.NoFolder
            statusText = "No music folder selected"
        }
    }

    fun isFavourite(id: TrackId): Boolean = favourites.contains(id)

    fun toggleFavourite(id: TrackId) {
        val makeFavourite = !favourites.contains(id)
        if (makeFavourite) favourites.add(id) else favourites.remove(id)
        coroutineScope.launch {
            if (makeFavourite) {
                dao.addFavourite(FavouriteEntity(id.value, System.currentTimeMillis()))
            } else {
                dao.removeFavourite(id.value)
            }
        }
    }

    fun favouriteTracks(): List<Track> = _tracks.filter { favourites.contains(it.id) }

    fun recordPlayed(track: Track) {
        coroutineScope.launch {
            dao.recordRecent(RecentEntity(track.id.value, System.currentTimeMillis()))
            dao.trimRecents()
        }
    }

    fun recentTracks(limit: Int = 50): List<RecentPlay> = _recents.take(limit)

    fun createPlaylist(name: String, tracks: List<Track> = emptyList()) {
        val cleanName = name.trim().ifBlank { "Untitled playlist" }
        val id = "pl-${UUID.randomUUID()}"
        coroutineScope.launch {
            dao.insertPlaylist(PlaylistEntity(id, cleanName, System.currentTimeMillis()))
            dao.replacePlaylistTracks(id, tracks.map { it.id.value })
        }
    }

    fun deletePlaylist(id: PlaylistId) {
        coroutineScope.launch { dao.deletePlaylist(id.value) }
    }

    fun renamePlaylist(id: PlaylistId, name: String) {
        val cleanName = name.trim().ifBlank { "Untitled playlist" }
        coroutineScope.launch {
            val existing = dao.getPlaylist(id.value) ?: return@launch
            dao.insertPlaylist(existing.copy(name = cleanName))
        }
    }

    fun addTrackToPlaylist(playlistId: PlaylistId, track: Track) {
        val playlist = playlistById(playlistId) ?: return
        if (playlist.trackIds.contains(track.id)) return
        coroutineScope.launch {
            dao.replacePlaylistTracks(playlistId.value, (playlist.trackIds + track.id).map { it.value })
        }
    }

    fun removeTrackFromPlaylist(playlistId: PlaylistId, track: Track) {
        val playlist = playlistById(playlistId) ?: return
        coroutineScope.launch {
            dao.replacePlaylistTracks(playlistId.value, (playlist.trackIds - track.id).map { it.value })
        }
    }

    fun playlistById(id: PlaylistId): Playlist? = _playlists.firstOrNull { it.id == id }

    fun trackById(id: TrackId): Track? = _tracks.firstOrNull { it.id == id }

    fun colorSeedFor(track: Track): Int =
        _albums.firstOrNull { it.id == track.albumId }?.colorSeed ?: track.albumId.value.hashCode()

    fun artworkPathFor(track: Track): String? =
        track.artworkPath ?: _albums.firstOrNull { it.id == track.albumId }?.artworkPath

    fun localSearch(query: String): List<Track> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return _tracks.filter {
            it.title.contains(trimmed, ignoreCase = true) ||
                it.artistName.contains(trimmed, ignoreCase = true) ||
                it.albumTitle.contains(trimmed, ignoreCase = true) ||
                it.folderPath.contains(trimmed, ignoreCase = true)
        }
    }

    suspend fun search(query: String): List<Track> = withContext(Dispatchers.Default) {
        localSearch(query)
    }

    private fun observeLibrary() {
        coroutineScope.launch {
            dao.observeTracks().collect { rows ->
                _tracks.clear()
                _tracks.addAll(rows.map { it.toDomain() })
                rebuildDerivedCollections()
            }
        }
        coroutineScope.launch {
            combine(
                dao.observePlaylists(),
                dao.observePlaylistTracks(),
            ) { playlists, entries ->
                val positions = entries.groupBy { it.playlistId }
                playlists.map { entity ->
                    Playlist(
                        id = PlaylistId(entity.id),
                        name = entity.name,
                        trackIds = positions[entity.id]
                            .orEmpty()
                            .sortedBy { it.position }
                            .map { TrackId(it.trackId) },
                    )
                }
            }.collect { playlists ->
                _playlists.clear()
                _playlists.addAll(playlists)
            }
        }
        coroutineScope.launch {
            dao.observeFavouriteIds().collect { ids ->
                favourites.clear()
                ids.forEach { favourites.add(TrackId(it)) }
            }
        }
        coroutineScope.launch {
            dao.observeRecentTracks().collect { rows ->
                _recents.clear()
                _recents.addAll(rows.map { RecentPlay(TrackId(it.track.id), it.playedAtMs) })
            }
        }
    }

    private fun rebuildDerivedCollections() {
        val albums = _tracks.groupBy { it.albumId }
            .map { (albumId, albumTracks) ->
                val first = albumTracks.minByOrNull { it.trackIndex } ?: albumTracks.first()
                Album(
                    id = albumId,
                    title = first.albumTitle,
                    artistId = first.artistId,
                    artistName = first.artistName,
                    colorSeed = albumId.value.hashCode(),
                    trackIds = albumTracks
                        .sortedWith(compareBy({ it.trackIndex }, { it.title.lowercase() }))
                        .map { it.id },
                    artworkPath = albumTracks.firstNotNullOfOrNull { it.artworkPath },
                )
            }
            .sortedBy { it.title.lowercase() }

        _albums.clear()
        _albums.addAll(albums)
    }

    private suspend fun scanFolder(token: Int) {
        val uriString = folderUri
        if (uriString == null) {
            publishStatus()
            return
        }

        val uri = Uri.parse(uriString)
        if (!hasPersistedReadAccess(uri)) {
            if (token == scanToken) {
                status = LibraryStatus.Error
                statusText = "Folder access was lost. Choose the folder again."
                isScanning = false
            }
            return
        }

        isScanning = true
        status = LibraryStatus.Scanning
        scannedFileCount = 0
        statusText = "Scanning ${folderName ?: "your folder"}…"

        try {
            val result = scanner.scan(uri) { count ->
                if (token == scanToken) {
                    coroutineScope.launch { scannedFileCount = count }
                }
            }
            if (token != scanToken) return
            result.rootName?.let { name ->
                folderName = name
                preferences.musicFolderName = name
            }
            dao.replaceLibrary(result.tracks)
            status = LibraryStatus.Ready
            statusText = when {
                result.tracks.isEmpty() -> "No audio files in ${folderName ?: "this folder"}"
                else -> "${result.tracks.size} tracks · ${folderName ?: "Library"}"
            }
            Log.i(TAG, "Scan complete: ${result.tracks.size} tracks from ${result.rootName}")
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            if (token != scanToken) return
            Log.w(TAG, "Folder scan failed", error)
            status = LibraryStatus.Error
            statusText = "Scan failed · ${error.message ?: error.javaClass.simpleName}"
        } finally {
            if (token == scanToken) isScanning = false
        }
    }

    private fun publishStatus() {
        status = if (hasFolder) LibraryStatus.Ready else LibraryStatus.NoFolder
        statusText = when {
            !hasFolder -> "No music folder selected"
            _tracks.isEmpty() -> "No audio files in ${folderName ?: "this folder"}"
            else -> "${_tracks.size} tracks · ${folderName ?: "Library"}"
        }
    }

    private fun hasPersistedReadAccess(uri: Uri): Boolean {
        // Some providers report the persisted tree URI with a trailing slash, so compare by path.
        val target = uri.toString().trimEnd('/')
        return appContext.contentResolver.persistedUriPermissions.any {
            it.isReadPermission && it.uri.toString().trimEnd('/') == target
        }
    }

    private fun TrackEntity.toDomain(): Track = Track(
        id = TrackId(id),
        title = title,
        artistId = com.celestial.player.domain.model.ArtistId(artistId),
        artistName = artistName,
        albumId = com.celestial.player.domain.model.AlbumId(albumId),
        albumTitle = albumTitle,
        trackIndex = trackIndex,
        durationMs = durationMs,
        mediaUri = mediaUri,
        folderPath = folderPath,
        artworkPath = artworkPath,
        mimeType = mimeType,
        dateAddedMs = dateAddedMs,
        sizeBytes = sizeBytes,
    )

    private companion object {
        const val TAG = "LibraryStateHolder"
    }
}
