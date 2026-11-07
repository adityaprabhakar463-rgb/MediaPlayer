package com.celestial.backend.api

import kotlinx.serialization.Serializable

/**
 * Wire contract shared with the Android client.
 *
 * The Android app duplicates these shapes in `CelestialApiClient.kt`. Keep the field names and
 * nullability in sync — kotlinx.serialization fails a request when a non-optional field is missing.
 */

@Serializable
data class TrackDto(
    val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val albumId: String,
    val albumTitle: String,
    val trackIndex: Int,
    val durationMs: Long,
    val artworkUrl: String? = null,
    val isStreamable: Boolean = true,
    val isAvailable: Boolean = true,
    val isDelete: Boolean = false,
)

@Serializable
data class AlbumDto(
    val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val colorSeed: Int,
    val trackIds: List<String>,
)

@Serializable
data class ArtistDto(
    val id: String,
    val name: String,
    val albumIds: List<String> = emptyList(),
    val trackIds: List<String> = emptyList(),
)

@Serializable
data class CatalogSnapshot(
    val version: Long,
    val tracks: List<TrackDto>,
    val albums: List<AlbumDto>,
    val artists: List<ArtistDto>,
)

@Serializable
data class SearchResponse(
    val query: String,
    val tracks: List<TrackDto>,
    val albums: List<AlbumDto> = emptyList(),
    val artists: List<ArtistDto> = emptyList(),
)

@Serializable
data class PlaylistDto(
    val id: String,
    val name: String,
    val trackIds: List<String> = emptyList(),
    val version: Long = 1,
    val updatedAt: String,
)

@Serializable
data class PlaylistsResponse(
    val playlists: List<PlaylistDto>,
)

@Serializable
data class CreatePlaylistRequest(
    val name: String,
    val trackIds: List<String> = emptyList(),
)

@Serializable
data class UpdatePlaylistRequest(
    val name: String? = null,
    val trackIds: List<String>? = null,
)

@Serializable
data class FavouriteDto(
    val trackId: String,
    val createdAt: String,
)

@Serializable
data class FavouritesResponse(
    val favourites: List<FavouriteDto>,
)

@Serializable
data class RecentPlayDto(
    val trackId: String,
    val playedAtMs: Long,
    val positionMs: Long = 0L,
)

@Serializable
data class RecentsResponse(
    val recents: List<RecentPlayDto>,
)

@Serializable
data class RecordRecentRequest(
    val trackId: String,
    val positionMs: Long = 0L,
)

@Serializable
data class DeviceRegisterRequest(
    val installationId: String,
    val deviceSecret: String,
    val deviceInfo: String? = null,
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val expiresIn: Long,
    val tokenType: String = "Bearer",
)

@Serializable
data class MeResponse(
    val userId: String,
    val installationId: String,
    val anonymous: Boolean,
    val createdAt: String,
)

@Serializable
data class PlaybackResponse(
    val trackId: String,
    val streamUrl: String,
    val provider: String,
    val expiresAt: String? = null,
)

@Serializable
data class SyncChangeDto(
    val id: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val version: Long,
    val createdAt: String,
)

@Serializable
data class SyncResponse(
    val changes: List<SyncChangeDto>,
    val serverVersion: Long,
)

@Serializable
data class AckRequest(
    val version: Long,
)

@Serializable
data class VersionResponse(
    val version: String,
    val catalogVersion: Long,
)

@Serializable
data class ProblemDetails(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String? = null,
    val instance: String? = null,
)
