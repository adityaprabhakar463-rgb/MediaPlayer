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
)

@Immutable
data class Album(
    val id: AlbumId,
    val title: String,
    val artistId: ArtistId,
    val artistName: String,
    val colorSeed: Int,
    val trackIds: List<TrackId>,
)

@Immutable
data class Artist(
    val id: ArtistId,
    val name: String,
    val albumIds: List<AlbumId>,
    val trackIds: List<TrackId>,
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
