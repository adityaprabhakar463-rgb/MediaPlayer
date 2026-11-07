package com.celestial.backend.database

import com.celestial.backend.api.ApiProblem
import com.celestial.backend.api.FavouriteDto
import com.celestial.backend.api.PlaylistDto
import com.celestial.backend.api.RecentPlayDto
import com.celestial.backend.api.SyncChangeDto
import com.celestial.backend.catalog.Catalog
import com.celestial.backend.library.InvalidDeviceSecret
import com.celestial.backend.library.LibraryStore
import com.celestial.backend.library.UserAccount
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

/**
 * PostgreSQL / Exposed implementation of [LibraryStore].
 *
 * Persists accounts, devices, playlists, playlist items, favourites, recent plays,
 * and sync changes using durable relational tables.
 */
class PostgresLibraryStore(
    private val catalog: Catalog = Catalog,
    private val clock: () -> Instant = { Instant.now() },
) : LibraryStore {

    private fun now(): Instant = clock()

    private fun safeUuid(id: String): UUID = try {
        UUID.fromString(id)
    } catch (_: Exception) {
        throw ApiProblem(
            HttpStatusCode.Unauthorized,
            "Unknown account",
            "The access token references an account that no longer exists.",
            "https://celestial.app/problems/unknown-account",
        )
    }

    override fun registerDevice(
        installationId: String,
        deviceSecret: String,
        deviceInfo: String?,
    ): Pair<UserAccount, Boolean> {
        require(installationId.isNotBlank()) { "installationId must not be blank" }
        require(deviceSecret.length >= 8) { "deviceSecret must contain at least 8 characters" }

        return transaction {
            val existingDevice = DevicesTable.selectAll()
                .where { DevicesTable.installationId eq installationId }
                .singleOrNull()

            if (existingDevice != null) {
                val storedSecret = existingDevice[DevicesTable.deviceSecret]
                if (!constantTimeEquals(storedSecret, deviceSecret)) {
                    throw InvalidDeviceSecret()
                }
                val userId = existingDevice[DevicesTable.userId]
                val currentInstant = now()
                UsersTable.update({ UsersTable.id eq userId }) {
                    it[lastSeenAt] = currentInstant
                }
                val userRow = UsersTable.selectAll().where { UsersTable.id eq userId }.single()
                val account = UserAccount(
                    id = userId.toString(),
                    installationId = installationId,
                    deviceSecret = storedSecret,
                    deviceInfo = existingDevice[DevicesTable.deviceInfo],
                    createdAt = userRow[UsersTable.createdAt].toString(),
                    lastSeenAt = currentInstant.toString(),
                )
                account to false
            } else {
                val userId = UUID.randomUUID()
                val deviceId = UUID.randomUUID()
                val currentInstant = now()

                UsersTable.insert {
                    it[UsersTable.id] = userId
                    it[UsersTable.deviceId] = installationId
                    it[UsersTable.anonymous] = true
                    it[UsersTable.createdAt] = currentInstant
                    it[UsersTable.lastSeenAt] = currentInstant
                }

                DevicesTable.insert {
                    it[DevicesTable.id] = deviceId
                    it[DevicesTable.userId] = userId
                    it[DevicesTable.installationId] = installationId
                    it[DevicesTable.deviceSecret] = deviceSecret
                    it[DevicesTable.deviceInfo] = deviceInfo
                    it[DevicesTable.registeredAt] = currentInstant
                }

                seedStarterLibrary(userId, currentInstant)

                val account = UserAccount(
                    id = userId.toString(),
                    installationId = installationId,
                    deviceSecret = deviceSecret,
                    deviceInfo = deviceInfo,
                    createdAt = currentInstant.toString(),
                    lastSeenAt = currentInstant.toString(),
                )
                account to true
            }
        }
    }

    private fun seedStarterLibrary(userId: UUID, timestamp: Instant) {
        catalog.defaultPlaylists().forEach { (playlistId, trackIds) ->
            PlaylistsTable.insert {
                it[id] = playlistId
                it[PlaylistsTable.userId] = userId
                it[name] = defaultPlaylistName(playlistId)
                it[version] = 1L
                it[createdAt] = timestamp
                it[updatedAt] = timestamp
            }
            trackIds.forEachIndexed { index, trackId ->
                PlaylistItemsTable.insert {
                    it[id] = UUID.randomUUID()
                    it[PlaylistItemsTable.playlistId] = playlistId
                    it[PlaylistItemsTable.trackId] = trackId
                    it[position] = index
                    it[addedAt] = timestamp
                }
            }
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
            val playedAtInstant = timestamp.minusMillis(offsets[index])
            RecentPlaysTable.insert {
                it[id] = UUID.randomUUID()
                it[RecentPlaysTable.userId] = userId
                it[trackId] = track.id
                it[playedAt] = playedAtInstant
                it[positionMs] = 0
                it[version] = 1L
            }
        }
    }

    private fun defaultPlaylistName(id: String): String = when (id) {
        "pl-1" -> "Night Drive"
        "pl-2" -> "Deep Focus"
        "pl-3" -> "Slow Orbit"
        "pl-4" -> "Dawn Commute"
        "pl-5" -> "Starfield Study"
        else -> "Playlist"
    }

    override fun account(userId: String): UserAccount {
        val userUuid = safeUuid(userId)
        return transaction {
            val userRow = UsersTable.selectAll()
                .where { (UsersTable.id eq userUuid) and UsersTable.deletedAt.isNull() }
                .singleOrNull()
                ?: throw ApiProblem(
                    HttpStatusCode.Unauthorized,
                    "Unknown account",
                    "The access token references an account that no longer exists.",
                    "https://celestial.app/problems/unknown-account",
                )

            val deviceRow = DevicesTable.selectAll()
                .where { DevicesTable.userId eq userUuid }
                .firstOrNull()

            UserAccount(
                id = userUuid.toString(),
                installationId = deviceRow?.get(DevicesTable.installationId) ?: "",
                deviceSecret = deviceRow?.get(DevicesTable.deviceSecret) ?: "",
                deviceInfo = deviceRow?.get(DevicesTable.deviceInfo),
                createdAt = userRow[UsersTable.createdAt].toString(),
                lastSeenAt = userRow[UsersTable.lastSeenAt].toString(),
            )
        }
    }

    override fun playlists(userId: String): List<PlaylistDto> {
        val userUuid = safeUuid(userId)
        account(userId)
        return transaction {
            val playlistRows = PlaylistsTable.selectAll()
                .where { (PlaylistsTable.userId eq userUuid) and PlaylistsTable.deletedAt.isNull() }
                .orderBy(PlaylistsTable.createdAt to SortOrder.DESC)
                .toList()

            val playlistIds = playlistRows.map { it[PlaylistsTable.id] }
            val itemsByPlaylist = if (playlistIds.isNotEmpty()) {
                PlaylistItemsTable.selectAll()
                    .where { PlaylistItemsTable.playlistId inList playlistIds }
                    .orderBy(PlaylistItemsTable.position to SortOrder.ASC)
                    .groupBy { it[PlaylistItemsTable.playlistId] }
            } else {
                emptyMap()
            }

            playlistRows.map { row ->
                val id = row[PlaylistsTable.id]
                val trackIds = itemsByPlaylist[id]?.map { it[PlaylistItemsTable.trackId] } ?: emptyList()
                PlaylistDto(
                    id = id,
                    name = row[PlaylistsTable.name],
                    trackIds = trackIds,
                    version = row[PlaylistsTable.version],
                    updatedAt = row[PlaylistsTable.updatedAt].toString(),
                )
            }
        }
    }

    override fun playlist(userId: String, playlistId: String): PlaylistDto {
        val userUuid = safeUuid(userId)
        account(userId)
        return transaction {
            val row = PlaylistsTable.selectAll()
                .where { (PlaylistsTable.userId eq userUuid) and (PlaylistsTable.id eq playlistId) and PlaylistsTable.deletedAt.isNull() }
                .singleOrNull()
                ?: throw ApiProblem(
                    HttpStatusCode.NotFound,
                    "Playlist not found",
                    "No playlist with id '$playlistId' exists for this account.",
                    "https://celestial.app/problems/playlist-not-found",
                )

            val trackIds = PlaylistItemsTable.selectAll()
                .where { PlaylistItemsTable.playlistId eq playlistId }
                .orderBy(PlaylistItemsTable.position to SortOrder.ASC)
                .map { it[PlaylistItemsTable.trackId] }

            PlaylistDto(
                id = playlistId,
                name = row[PlaylistsTable.name],
                trackIds = trackIds,
                version = row[PlaylistsTable.version],
                updatedAt = row[PlaylistsTable.updatedAt].toString(),
            )
        }
    }

    override fun createPlaylist(userId: String, name: String, trackIds: List<String>): PlaylistDto {
        val userUuid = safeUuid(userId)
        account(userId)
        val validated = validateTrackIds(trackIds)
        val playlistId = "pl_${UUID.randomUUID()}"
        val currentInstant = now()

        return transaction {
            PlaylistsTable.insert {
                it[id] = playlistId
                it[PlaylistsTable.userId] = userUuid
                it[PlaylistsTable.name] = name
                it[version] = 1L
                it[createdAt] = currentInstant
                it[updatedAt] = currentInstant
            }

            validated.forEachIndexed { index, trackId ->
                PlaylistItemsTable.insert {
                    it[id] = UUID.randomUUID()
                    it[PlaylistItemsTable.playlistId] = playlistId
                    it[PlaylistItemsTable.trackId] = trackId
                    it[position] = index
                    it[addedAt] = currentInstant
                }
            }

            recordChange(userUuid, "playlist", playlistId, "CREATE")

            PlaylistDto(
                id = playlistId,
                name = name,
                trackIds = validated,
                version = 1L,
                updatedAt = currentInstant.toString(),
            )
        }
    }

    override fun updatePlaylist(
        userId: String,
        playlistId: String,
        name: String?,
        trackIds: List<String>?,
    ): PlaylistDto {
        val userUuid = safeUuid(userId)
        account(userId)
        val validated = trackIds?.let { validateTrackIds(it) }
        val currentInstant = now()

        return transaction {
            val existing = PlaylistsTable.selectAll()
                .where { (PlaylistsTable.userId eq userUuid) and (PlaylistsTable.id eq playlistId) and PlaylistsTable.deletedAt.isNull() }
                .singleOrNull()
                ?: throw ApiProblem(
                    HttpStatusCode.NotFound,
                    "Playlist not found",
                    "No playlist with id '$playlistId' exists for this account.",
                    "https://celestial.app/problems/playlist-not-found",
                )

            val newVersion = existing[PlaylistsTable.version] + 1
            val updatedName = name ?: existing[PlaylistsTable.name]

            PlaylistsTable.update({ (PlaylistsTable.userId eq userUuid) and (PlaylistsTable.id eq playlistId) }) {
                if (name != null) it[PlaylistsTable.name] = name
                it[version] = newVersion
                it[updatedAt] = currentInstant
            }

            val finalTrackIds: List<String> = if (validated != null) {
                PlaylistItemsTable.deleteWhere { PlaylistItemsTable.playlistId eq playlistId }
                validated.forEachIndexed { index, trackId ->
                    PlaylistItemsTable.insert {
                        it[id] = UUID.randomUUID()
                        it[PlaylistItemsTable.playlistId] = playlistId
                        it[PlaylistItemsTable.trackId] = trackId
                        it[position] = index
                        it[addedAt] = currentInstant
                    }
                }
                validated
            } else {
                PlaylistItemsTable.selectAll()
                    .where { PlaylistItemsTable.playlistId eq playlistId }
                    .orderBy(PlaylistItemsTable.position to SortOrder.ASC)
                    .map { it[PlaylistItemsTable.trackId] }
            }

            recordChange(userUuid, "playlist", playlistId, "UPDATE")

            PlaylistDto(
                id = playlistId,
                name = updatedName,
                trackIds = finalTrackIds,
                version = newVersion,
                updatedAt = currentInstant.toString(),
            )
        }
    }

    override fun deletePlaylist(userId: String, playlistId: String): Boolean {
        val userUuid = safeUuid(userId)
        account(userId)
        val currentInstant = now()

        return transaction {
            val existing = PlaylistsTable.selectAll()
                .where { (PlaylistsTable.userId eq userUuid) and (PlaylistsTable.id eq playlistId) and PlaylistsTable.deletedAt.isNull() }
                .singleOrNull() ?: return@transaction false

            PlaylistsTable.update({ (PlaylistsTable.userId eq userUuid) and (PlaylistsTable.id eq playlistId) }) {
                it[deletedAt] = currentInstant
                it[version] = existing[version] + 1
                it[updatedAt] = currentInstant
            }

            recordChange(userUuid, "playlist", playlistId, "DELETE")
            true
        }
    }

    override fun favourites(userId: String): List<FavouriteDto> {
        val userUuid = safeUuid(userId)
        account(userId)
        return transaction {
            FavouritesTable.selectAll()
                .where { FavouritesTable.userId eq userUuid }
                .orderBy(FavouritesTable.createdAt to SortOrder.DESC)
                .map { row ->
                    FavouriteDto(
                        trackId = row[FavouritesTable.trackId],
                        createdAt = row[FavouritesTable.createdAt].toString(),
                    )
                }
        }
    }

    override fun addFavourite(userId: String, trackId: String): FavouriteDto {
        val userUuid = safeUuid(userId)
        val track = catalog.trackById(trackId)
            ?: throw ApiProblem(
                HttpStatusCode.NotFound,
                "Track not found",
                "Track '$trackId' is not part of the curated catalog.",
                "https://celestial.app/problems/track-not-found",
            )
        account(userId)
        val currentInstant = now()

        return transaction {
            val existing = FavouritesTable.selectAll()
                .where { (FavouritesTable.userId eq userUuid) and (FavouritesTable.trackId eq track.id) }
                .singleOrNull()

            if (existing != null) {
                FavouriteDto(track.id, existing[FavouritesTable.createdAt].toString())
            } else {
                FavouritesTable.insert {
                    it[id] = UUID.randomUUID()
                    it[FavouritesTable.userId] = userUuid
                    it[FavouritesTable.trackId] = track.id
                    it[createdAt] = currentInstant
                }
                recordChange(userUuid, "favourite", track.id, "CREATE")
                FavouriteDto(track.id, currentInstant.toString())
            }
        }
    }

    override fun removeFavourite(userId: String, trackId: String): Boolean {
        val userUuid = safeUuid(userId)
        account(userId)

        return transaction {
            val deletedCount = FavouritesTable.deleteWhere {
                (FavouritesTable.userId eq userUuid) and (FavouritesTable.trackId eq trackId)
            }
            if (deletedCount > 0) {
                recordChange(userUuid, "favourite", trackId, "DELETE")
                true
            } else {
                false
            }
        }
    }

    override fun recents(userId: String): List<RecentPlayDto> {
        val userUuid = safeUuid(userId)
        account(userId)

        return transaction {
            RecentPlaysTable.selectAll()
                .where { RecentPlaysTable.userId eq userUuid }
                .orderBy(RecentPlaysTable.playedAt to SortOrder.DESC)
                .limit(100)
                .map { row ->
                    RecentPlayDto(
                        trackId = row[RecentPlaysTable.trackId],
                        playedAtMs = row[RecentPlaysTable.playedAt].toEpochMilli(),
                        positionMs = row[RecentPlaysTable.positionMs].toLong(),
                    )
                }
        }
    }

    override fun recordRecent(userId: String, trackId: String, positionMs: Long): RecentPlayDto {
        val userUuid = safeUuid(userId)
        val track = catalog.trackById(trackId)
            ?: throw ApiProblem(
                HttpStatusCode.NotFound,
                "Track not found",
                "Track '$trackId' is not part of the curated catalog.",
                "https://celestial.app/problems/track-not-found",
            )
        account(userId)
        val currentInstant = now()

        return transaction {
            RecentPlaysTable.deleteWhere {
                (RecentPlaysTable.userId eq userUuid) and (RecentPlaysTable.trackId eq track.id)
            }

            RecentPlaysTable.insert {
                it[id] = UUID.randomUUID()
                it[RecentPlaysTable.userId] = userUuid
                it[RecentPlaysTable.trackId] = track.id
                it[playedAt] = currentInstant
                it[RecentPlaysTable.positionMs] = positionMs.coerceAtLeast(0L).toInt()
                it[version] = 1L
            }

            val totalRecents = RecentPlaysTable.selectAll()
                .where { RecentPlaysTable.userId eq userUuid }
                .count()

            if (totalRecents > 100) {
                val oldestToKeep = RecentPlaysTable.selectAll()
                    .where { RecentPlaysTable.userId eq userUuid }
                    .orderBy(RecentPlaysTable.playedAt to SortOrder.DESC)
                    .limit(100)
                    .map { it[RecentPlaysTable.id] }

                RecentPlaysTable.deleteWhere {
                    (RecentPlaysTable.userId eq userUuid) and (RecentPlaysTable.id notInList oldestToKeep)
                }
            }

            recordChange(userUuid, "recent", track.id, "UPDATE")

            RecentPlayDto(
                trackId = track.id,
                playedAtMs = currentInstant.toEpochMilli(),
                positionMs = positionMs.coerceAtLeast(0L),
            )
        }
    }

    override fun changes(userId: String, sinceVersion: Long): Pair<List<SyncChangeDto>, Long> {
        val userUuid = safeUuid(userId)
        account(userId)

        return transaction {
            val maxVer = SyncChangesTable.select(SyncChangesTable.version.max())
                .where { SyncChangesTable.userId eq userUuid }
                .singleOrNull()?.getOrNull(SyncChangesTable.version.max()) ?: 0L

            val changesList = SyncChangesTable.selectAll()
                .where { (SyncChangesTable.userId eq userUuid) and (SyncChangesTable.version greater sinceVersion) }
                .orderBy(SyncChangesTable.version to SortOrder.ASC)
                .map { row ->
                    SyncChangeDto(
                        id = row[SyncChangesTable.id].toString(),
                        entityType = row[SyncChangesTable.entityType],
                        entityId = row[SyncChangesTable.entityId],
                        operation = row[SyncChangesTable.operation],
                        version = row[SyncChangesTable.version],
                        createdAt = row[SyncChangesTable.createdAt].toString(),
                    )
                }

            changesList to maxVer
        }
    }

    override fun acknowledge(userId: String, version: Long): Long {
        safeUuid(userId)
        account(userId)
        return version
    }

    private fun recordChange(userUuid: UUID, entityType: String, entityId: String, operation: String) {
        val maxVer = SyncChangesTable.select(SyncChangesTable.version.max())
            .where { SyncChangesTable.userId eq userUuid }
            .singleOrNull()?.getOrNull(SyncChangesTable.version.max()) ?: 0L

        val nextVer = maxVer + 1L
        val currentInstant = now()

        SyncChangesTable.insert {
            it[id] = UUID.randomUUID()
            it[userId] = userUuid
            it[SyncChangesTable.entityType] = entityType
            it[SyncChangesTable.entityId] = entityId
            it[SyncChangesTable.operation] = operation
            it[version] = nextVer
            it[createdAt] = currentInstant
        }

        val totalChanges = SyncChangesTable.selectAll()
            .where { SyncChangesTable.userId eq userUuid }
            .count()

        if (totalChanges > 500) {
            val newestIds = SyncChangesTable.selectAll()
                .where { SyncChangesTable.userId eq userUuid }
                .orderBy(SyncChangesTable.version to SortOrder.DESC)
                .limit(500)
                .map { it[SyncChangesTable.id] }

            SyncChangesTable.deleteWhere {
                (SyncChangesTable.userId eq userUuid) and (SyncChangesTable.id notInList newestIds)
            }
        }
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

    private fun constantTimeEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(), b.toByteArray())
}
