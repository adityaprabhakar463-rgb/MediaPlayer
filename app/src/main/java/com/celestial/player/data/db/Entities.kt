package com.celestial.player.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracks",
    indices = [
        Index("albumId"),
        Index("artistId"),
        Index("folderPath"),
    ],
)
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val albumId: String,
    val albumTitle: String,
    val trackIndex: Int,
    val durationMs: Long,
    val mediaUri: String,
    val folderPath: String,
    val artworkPath: String?,
    val mimeType: String,
    val dateAddedMs: Long,
    val sizeBytes: Long,
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAtMs: Long,
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("playlistId"), Index("trackId")],
)
data class PlaylistTrackEntity(
    val playlistId: String,
    val trackId: String,
    val position: Int,
)

@Entity(
    tableName = "favourites",
    indices = [Index("trackId")],
)
data class FavouriteEntity(
    @PrimaryKey val trackId: String,
    val addedAtMs: Long,
)

@Entity(
    tableName = "recents",
    indices = [Index("trackId"), Index("playedAtMs")],
)
data class RecentEntity(
    @PrimaryKey val trackId: String,
    val playedAtMs: Long,
)

data class RecentTrackRow(
    @Embedded val track: TrackEntity,
    val playedAtMs: Long,
)
