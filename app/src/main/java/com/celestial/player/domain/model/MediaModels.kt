package com.celestial.player.domain.model

import androidx.compose.runtime.Immutable

@JvmInline
value class TrackId(val value: String)

@JvmInline
value class AlbumId(val value: String)

@JvmInline
value class ArtistId(val value: String)

@JvmInline
value class PlaylistId(val value: String)

/**
 * A single playable audio file found inside the user's chosen music folder.
 *
 * [mediaUri] is the SAF document URI used by Media3. [folderPath] is the human-readable path
 * relative to the library root, used for folder grouping and display.
 */
@Immutable
data class Track(
    val id: TrackId,
    val title: String,
    val artistId: ArtistId,
    val artistName: String,
    val albumId: AlbumId,
    val albumTitle: String,
    val trackIndex: Int,
    val durationMs: Long,
    val mediaUri: String,
    val folderPath: String,
    val artworkPath: String? = null,
    val mimeType: String = "",
    val dateAddedMs: Long = 0L,
    val sizeBytes: Long = 0L,
)

@Immutable
data class Album(
    val id: AlbumId,
    val title: String,
    val artistId: ArtistId,
    val artistName: String,
    val colorSeed: Int,
    val trackIds: List<TrackId>,
    val artworkPath: String? = null,
)

@Immutable
data class Playlist(
    val id: PlaylistId,
    val name: String,
    val trackIds: List<TrackId>,
)

@Immutable
data class RecentPlay(
    val trackId: TrackId,
    val playedAtMs: Long,
)
