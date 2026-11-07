package com.celestial.backend.database

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : Table("users") {
    val id = uuid("id")
    val deviceId = varchar("device_id", 255).uniqueIndex()
    val anonymous = bool("anonymous").default(true)
    val linkedEmail = varchar("linked_email", 255).uniqueIndex().nullable()
    val linkedPasswordHash = varchar("linked_password_hash", 255).nullable()
    val createdAt = timestamp("created_at")
    val lastSeenAt = timestamp("last_seen_at")
    val deletedAt = timestamp("deleted_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

object DevicesTable : Table("devices") {
    val id = uuid("id")
    val userId = uuid("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val installationId = varchar("installation_id", 255).uniqueIndex()
    val deviceSecret = varchar("device_secret", 255)
    val deviceInfo = text("device_info").nullable()
    val registeredAt = timestamp("registered_at")
    val revokedAt = timestamp("revoked_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

object SessionsTable : Table("sessions") {
    val id = uuid("id")
    val userId = uuid("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val deviceId = uuid("device_id").references(DevicesTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val refreshTokenHash = varchar("refresh_token_hash", 255)
    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at")
    val revokedAt = timestamp("revoked_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

object CuratedArtistsTable : Table("curated_artists") {
    val id = varchar("id", 255)
    val audiusUserId = varchar("audius_user_id", 255).uniqueIndex()
    val name = varchar("name", 255)
    val handle = varchar("handle", 255)
    val isVerified = bool("is_verified").default(true)
    val approvedBy = varchar("approved_by", 255).nullable()
    val approvedAt = timestamp("approved_at")
    val removedAt = timestamp("removed_at").nullable()
    val removalReason = text("removal_reason").nullable()

    override val primaryKey = PrimaryKey(id)
}

object TracksTable : Table("tracks") {
    val id = varchar("id", 255)
    val externalId = varchar("external_id", 255).uniqueIndex()
    val provider = varchar("provider", 50).default("audius")
    val title = varchar("title", 500)
    val artistName = varchar("artist_name", 255)
    val albumName = varchar("album_name", 255).nullable()
    val durationMs = integer("duration_ms")
    val artworkUrl = text("artwork_url").nullable()
    val isStreamable = bool("is_streamable").default(true)
    val isAvailable = bool("is_available").default(true)
    val isDelete = bool("is_delete").default(false)
    val curatedArtistId = varchar("curated_artist_id", 255).references(CuratedArtistsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val version = long("version").default(1L)
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object PlaylistsTable : Table("playlists") {
    val id = varchar("id", 255)
    val userId = uuid("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val version = long("version").default(1L)
    val deletedAt = timestamp("deleted_at").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object PlaylistItemsTable : Table("playlist_items") {
    val id = uuid("id")
    val playlistId = varchar("playlist_id", 255).references(PlaylistsTable.id, onDelete = ReferenceOption.CASCADE)
    val trackId = varchar("track_id", 255)
    val position = integer("position")
    val addedAt = timestamp("added_at")

    override val primaryKey = PrimaryKey(id)
}

object FavouritesTable : Table("favourites") {
    val id = uuid("id")
    val userId = uuid("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val trackId = varchar("track_id", 255)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("idx_favourites_user_track", userId, trackId)
    }
}

object RecentPlaysTable : Table("recent_plays") {
    val id = uuid("id")
    val userId = uuid("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val trackId = varchar("track_id", 255)
    val playedAt = timestamp("played_at")
    val positionMs = integer("position_ms").default(0)
    val version = long("version").default(1L)

    override val primaryKey = PrimaryKey(id)
}

object SyncChangesTable : Table("sync_changes") {
    val id = uuid("id")
    val userId = uuid("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val entityType = varchar("entity_type", 50)
    val entityId = varchar("entity_id", 255)
    val operation = varchar("operation", 20)
    val version = long("version")
    val deviceId = uuid("device_id").references(DevicesTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index("idx_sync_changes_user_ver", false, userId, version)
    }
}
