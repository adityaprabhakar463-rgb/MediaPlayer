package com.celestial.backend.library

import com.celestial.backend.api.ApiProblem
import com.celestial.backend.api.FavouriteDto
import com.celestial.backend.api.PlaylistDto
import com.celestial.backend.api.RecentPlayDto
import com.celestial.backend.api.SyncChangeDto
import com.celestial.backend.catalog.Catalog
import io.ktor.http.HttpStatusCode
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InvalidDeviceSecret : RuntimeException("Device secret does not match the registered device")

data class UserAccount(
    val id: String,
    val installationId: String,
    val deviceSecret: String,
    val deviceInfo: String?,
    val createdAt: String,
    var lastSeenAt: String,
)

interface LibraryStore {
    fun registerDevice(installationId: String, deviceSecret: String, deviceInfo: String?): Pair<UserAccount, Boolean>
    fun account(userId: String): UserAccount
    fun playlists(userId: String): List<PlaylistDto>
    fun playlist(userId: String, playlistId: String): PlaylistDto
    fun createPlaylist(userId: String, name: String, trackIds: List<String>): PlaylistDto
    fun updatePlaylist(userId: String, playlistId: String, name: String?, trackIds: List<String>?): PlaylistDto
    fun deletePlaylist(userId: String, playlistId: String): Boolean
    fun favourites(userId: String): List<FavouriteDto>
    fun addFavourite(userId: String, trackId: String): FavouriteDto
    fun removeFavourite(userId: String, trackId: String): Boolean
    fun recents(userId: String): List<RecentPlayDto>
    fun recordRecent(userId: String, trackId: String, positionMs: Long): RecentPlayDto
    fun changes(userId: String, sinceVersion: Long): Pair<List<SyncChangeDto>, Long>
    fun acknowledge(userId: String, version: Long): Long

    companion object {
        operator fun invoke(
            catalog: Catalog = Catalog,
            clock: () -> Instant = { Instant.now() },
        ): LibraryStore = InMemoryLibraryStore(catalog, clock)
    }
}

private data class PlaylistState(
    val id: String,
    var name: String,
    var trackIds: List<String>,
    var version: Long,
    var updatedAt: String,
)

private data class RecentState(
    val trackId: String,
    val playedAtMs: Long,
    val positionMs: Long,
)

private class UserLibrary(
    var version: Long = 1L,
) {
    val playlists = mutableListOf<PlaylistState>()
    val favourites = linkedMapOf<String, String>()
    val recents = mutableListOf<RecentState>()
    val changes = mutableListOf<SyncChangeDto>()
    var acknowledgedVersion: Long = 0L
}

/**
 * In-memory persistence for anonymous devices and their library.
 */
class InMemoryLibraryStore(
    private val catalog: Catalog = Catalog,
    private val clock: () -> Instant = { Instant.now() },
) : LibraryStore {
    private val accountsByInstallation = ConcurrentHashMap<String, UserAccount>()
    private val accountsById = ConcurrentHashMap<String, UserAccount>()
    private val libraries = ConcurrentHashMap<String, UserLibrary>()
    private val lock = Any()

    private fun now(): String = clock().toString()

    override fun registerDevice(
        installationId: String,
        deviceSecret: String,
        deviceInfo: String?,
    ): Pair<UserAccount, Boolean> = synchronized(lock) {
        require(installationId.isNotBlank()) { "installationId must not be blank" }
        require(deviceSecret.length >= 8) { "deviceSecret must contain at least 8 characters" }

        val existing = accountsByInstallation[installationId]
        if (existing != null) {
            if (!constantTimeEquals(existing.deviceSecret, deviceSecret)) throw InvalidDeviceSecret()
            existing.lastSeenAt = now()
            ensureLibrary(existing.id)
            return existing to false
        }

        val account = UserAccount(
            id = UUID.randomUUID().toString(),
            installationId = installationId,
            deviceSecret = deviceSecret,
            deviceInfo = deviceInfo,
            createdAt = now(),
            lastSeenAt = now(),
        )
        accountsByInstallation[installationId] = account
        accountsById[account.id] = account
        seedLibrary(account.id)
        account to true
    }

    override fun account(userId: String): UserAccount =
        accountsById[userId] ?: throw ApiProblem(
            HttpStatusCode.Unauthorized,
            "Unknown account",
            "The access token references an account that no longer exists.",
            "https://celestial.app/problems/unknown-account",
        )

    private fun ensureLibrary(userId: String): UserLibrary {
        account(userId)
        return libraries.getOrPut(userId) { seededLibrary() }
    }

    private fun seedLibrary(userId: String) {
        libraries[userId] = seededLibrary()
    }

    private fun seededLibrary(): UserLibrary {
        val library = UserLibrary()
        val timestamp = now()
        catalog.defaultPlaylists().forEach { (id, trackIds) ->
            library.playlists += PlaylistState(
                id = id,
                name = defaultPlaylistName(id),
                trackIds = trackIds,
                version = 1L,
                updatedAt = timestamp,
            )
        }
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
        val seededTracks = catalog.tracks.takeLast(offsets.size).reversed()
        seededTracks.forEachIndexed { index, track ->
            library.recents += RecentState(
                trackId = track.id,
                playedAtMs = clock().toEpochMilli() - offsets[index],
                positionMs = 0L,
            )
        }
        return library
    }

    private fun defaultPlaylistName(id: String): String = when (id) {
        "pl-1" -> "Night Drive"
        "pl-2" -> "Deep Focus"
        "pl-3" -> "Slow Orbit"
        "pl-4" -> "Dawn Commute"
        "pl-5" -> "Starfield Study"
        else -> "Playlist"
    }

    override fun playlists(userId: String): List<PlaylistDto> = synchronized(lock) {
        ensureLibrary(userId).playlists.map { it.toDto() }
    }

    override fun playlist(userId: String, playlistId: String): PlaylistDto = synchronized(lock) {
        ensureLibrary(userId).playlists.firstOrNull { it.id == playlistId }?.toDto()
            ?: throw ApiProblem(
                HttpStatusCode.NotFound,
                "Playlist not found",
                "No playlist with id '$playlistId' exists for this account.",
                "https://celestial.app/problems/playlist-not-found",
            )
    }

    override fun createPlaylist(userId: String, name: String, trackIds: List<String>): PlaylistDto = synchronized(lock) {
        val library = ensureLibrary(userId)
        val validated = validateTrackIds(trackIds)
        val playlist = PlaylistState(
            id = "pl_${UUID.randomUUID()}",
            name = name,
            trackIds = validated,
            version = 1L,
            updatedAt = now(),
        )
        library.playlists.add(0, playlist)
        recordChange(library, "playlist", playlist.id, "CREATE")
        playlist.toDto()
    }

    override fun updatePlaylist(
        userId: String,
        playlistId: String,
        name: String?,
        trackIds: List<String>?,
    ): PlaylistDto = synchronized(lock) {
        val library = ensureLibrary(userId)
        val playlist = library.playlists.firstOrNull { it.id == playlistId }
            ?: throw ApiProblem(
                HttpStatusCode.NotFound,
                "Playlist not found",
                "No playlist with id '$playlistId' exists for this account.",
                "https://celestial.app/problems/playlist-not-found",
            )
        name?.let { playlist.name = it }
        trackIds?.let { playlist.trackIds = validateTrackIds(it) }
        playlist.version += 1
        playlist.updatedAt = now()
        recordChange(library, "playlist", playlist.id, "UPDATE")
        playlist.toDto()
    }

    override fun deletePlaylist(userId: String, playlistId: String): Boolean = synchronized(lock) {
        val library = ensureLibrary(userId)
        val removed = library.playlists.removeAll { it.id == playlistId }
        if (removed) recordChange(library, "playlist", playlistId, "DELETE")
        removed
    }

    override fun favourites(userId: String): List<FavouriteDto> = synchronized(lock) {
        ensureLibrary(userId).favourites.map { (trackId, createdAt) ->
            FavouriteDto(trackId, createdAt)
        }
    }

    override fun addFavourite(userId: String, trackId: String): FavouriteDto = synchronized(lock) {
        val track = catalog.trackById(trackId)
            ?: throw ApiProblem(
                HttpStatusCode.NotFound,
                "Track not found",
                "Track '$trackId' is not part of the curated catalog.",
                "https://celestial.app/problems/track-not-found",
            )
        val library = ensureLibrary(userId)
        val alreadyFavourite = library.favourites.containsKey(track.id)
        val createdAt = library.favourites.getOrPut(track.id) { now() }
        if (!alreadyFavourite) recordChange(library, "favourite", trackId, "CREATE")
        FavouriteDto(trackId, createdAt)
    }

    override fun removeFavourite(userId: String, trackId: String): Boolean = synchronized(lock) {
        val library = ensureLibrary(userId)
        val removed = library.favourites.remove(trackId) != null
        if (removed) recordChange(library, "favourite", trackId, "DELETE")
        removed
    }

    override fun recents(userId: String): List<RecentPlayDto> = synchronized(lock) {
        ensureLibrary(userId).recents.map {
            RecentPlayDto(it.trackId, it.playedAtMs, it.positionMs)
        }
    }

    override fun recordRecent(userId: String, trackId: String, positionMs: Long): RecentPlayDto = synchronized(lock) {
        val track = catalog.trackById(trackId)
            ?: throw ApiProblem(
                HttpStatusCode.NotFound,
                "Track not found",
                "Track '$trackId' is not part of the curated catalog.",
                "https://celestial.app/problems/track-not-found",
            )
        val library = ensureLibrary(userId)
        library.recents.removeAll { it.trackId == track.id }
        val recent = RecentState(track.id, clock().toEpochMilli(), positionMs.coerceAtLeast(0L))
        library.recents.add(0, recent)
        while (library.recents.size > 100) library.recents.removeAt(library.recents.lastIndex)
        recordChange(library, "recent", track.id, "UPDATE")
        RecentPlayDto(recent.trackId, recent.playedAtMs, recent.positionMs)
    }

    override fun changes(userId: String, sinceVersion: Long): Pair<List<SyncChangeDto>, Long> = synchronized(lock) {
        val library = ensureLibrary(userId)
        library.changes.filter { it.version > sinceVersion } to library.version
    }

    override fun acknowledge(userId: String, version: Long): Long = synchronized(lock) {
        val library = ensureLibrary(userId)
        library.acknowledgedVersion = maxOf(library.acknowledgedVersion, version)
        library.acknowledgedVersion
    }

    private fun validateTrackIds(trackIds: List<String>): List<String> {
        val invalid = trackIds.firstOrNull { catalog.trackById(it) == null }
        if (invalid != null) {
            throw ApiProblem(
                HttpStatusCode.UnprocessableEntity,
                "Unknown track",
                "Track '$invalid' is not part of the curated catalog.",
                "https://celestial.app/problems/track-not-found",
            )
        }
        return trackIds.distinct()
    }

    private fun recordChange(library: UserLibrary, entityType: String, entityId: String, operation: String) {
        library.version += 1
        library.changes += SyncChangeDto(
            id = UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            operation = operation,
            version = library.version,
            createdAt = now(),
        )
        while (library.changes.size > 500) library.changes.removeAt(0)
    }

    private fun PlaylistState.toDto() = PlaylistDto(
        id = id,
        name = name,
        trackIds = trackIds.toList(),
        version = version,
        updatedAt = updatedAt,
    )

    private fun constantTimeEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(), b.toByteArray())
}
