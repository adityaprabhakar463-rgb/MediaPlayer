package com.celestial.player.data.remote

import com.celestial.player.domain.model.Album
import com.celestial.player.domain.model.AlbumId
import com.celestial.player.domain.model.Artist
import com.celestial.player.domain.model.ArtistId
import com.celestial.player.domain.model.Playlist
import com.celestial.player.domain.model.PlaylistId
import com.celestial.player.domain.model.RecentPlay
import com.celestial.player.domain.model.Track
import com.celestial.player.domain.model.TrackId
import kotlinx.serialization.Serializable

/**
 * Wire DTOs mirroring `backend/src/main/kotlin/com/celestial/backend/api/ApiModels.kt`.
 * Keep field names and nullability in sync with the server contract.
 */

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
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val expiresIn: Long,
    val tokenType: String = "Bearer",
)

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
    val trackIds: List<String> = emptyList(),
)

@Serializable
data class ArtistDto(
    val id: String,
    val name: String,
    val albumIds: List<String> = emptyList(),
    val trackIds: List<String> = emptyList(),
)

@Serializable
data class CatalogSnapshotDto(
    val version: Long = 1,
    val tracks: List<TrackDto>,
    val albums: List<AlbumDto> = emptyList(),
    val artists: List<ArtistDto> = emptyList(),
)

@Serializable
data class SearchResponseDto(
    val query: String,
    val tracks: List<TrackDto> = emptyList(),
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
data class PlaybackResponse(
    val trackId: String,
    val streamUrl: String,
    val provider: String,
    val expiresAt: String? = null,
)

@Serializable
data class ProblemDetails(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val instance: String? = null,
)

fun TrackDto.toDomain(): Track = Track(
    id = TrackId(id),
    title = title,
    artistId = ArtistId(artistId),
    artistName = artistName,
    albumId = AlbumId(albumId),
    albumTitle = albumTitle,
    trackIndex = trackIndex,
    durationMs = durationMs,
)

fun AlbumDto.toDomain(): Album = Album(
    id = AlbumId(id),
    title = title,
    artistId = ArtistId(artistId),
    artistName = artistName,
    colorSeed = colorSeed,
    trackIds = trackIds.map(::TrackId),
)

fun ArtistDto.toDomain(): Artist = Artist(
    id = ArtistId(id),
    name = name,
    albumIds = albumIds.map(::AlbumId),
    trackIds = trackIds.map(::TrackId),
)

fun PlaylistDto.toDomain(): Playlist = Playlist(
    id = PlaylistId(id),
    name = name,
    trackIds = trackIds.map(::TrackId),
)

fun RecentPlayDto.toDomain(): RecentPlay = RecentPlay(
    trackId = TrackId(trackId),
    playedAtMs = playedAtMs,
)
