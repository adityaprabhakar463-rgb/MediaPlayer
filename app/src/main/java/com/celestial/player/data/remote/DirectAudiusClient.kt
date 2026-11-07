package com.celestial.player.data.remote

import android.util.Log
import com.celestial.player.BuildConfig
import com.celestial.player.domain.model.AlbumId
import com.celestial.player.domain.model.ArtistId
import com.celestial.player.domain.model.Track
import com.celestial.player.domain.model.TrackId
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AudiusTrackData(
    val id: String,
    val title: String = "",
    val user: AudiusUserData = AudiusUserData(),
    val duration: Int = 0,
    val artwork: AudiusArtworkData? = null,
    @SerialName("is_streamable") val isStreamable: Boolean = true,
    @SerialName("is_available") val isAvailable: Boolean = true,
    @SerialName("is_delete") val isDelete: Boolean = false,
)

@Serializable
data class AudiusUserData(
    val id: String = "",
    val name: String = "",
    val handle: String = "",
)

@Serializable
data class AudiusArtworkData(
    @SerialName("150x150") val small: String? = null,
    @SerialName("480x480") val medium: String? = null,
    @SerialName("1000x1000") val large: String? = null,
)

@Serializable
private data class AudiusDataList<T>(val data: List<T> = emptyList())

object DirectAudiusClient {
    private const val APP_NAME = "Celestial"
    private const val DISCOVERY_URL = "https://discoveryprovider.audius.co"
    private const val TAG = "DirectAudiusClient"

    var configuredApiKey: String? = BuildConfig.AUDIUS_API_KEY.ifBlank { null }

    fun updateApiKey(key: String?) {
        configuredApiKey = key?.trim()?.ifBlank { null }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private val client = HttpClient(Android) {
        expectSuccess = false
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 6_000
            socketTimeoutMillis = 15_000
        }
    }

    // Mapping Celestial catalog track ID -> Audius track ID for direct streaming
    val audiusIdMap = mapOf(
        // Zero Gravity (Nebula Pulse)
        "album-zero-gravity-1" to "dGON5PJ",
        "album-zero-gravity-2" to "2PWPRN3",
        "album-zero-gravity-3" to "WY99aBZ",
        "album-zero-gravity-4" to "73WMowW",
        // Meditation in the Tao (OttoTAO)
        "album-tao-1" to "91Ax2Vk",
        "album-tao-2" to "mlGxoR5",
        "album-tao-3" to "Mo3qE4Z",
        // Celestial Drift (Lunaress)
        "album-celestial-drift-1" to "pl2wmkY",
        "album-celestial-drift-2" to "9O8dyQw",
        "album-celestial-drift-3" to "mgvpdEv",
        "album-celestial-drift-4" to "B5zO4z9",
        // Dream Space (Dean Evenson)
        "album-dream-space-1" to "zzaz5",
        "album-dream-space-2" to "0XbXV",
        "album-dream-space-3" to "vqPq6",
        // Of the Nescient Mind (Tutara Peak)
        "album-nescient-mind-1" to "QVP0VmR",
        "album-nescient-mind-2" to "NY88j4",
        "album-nescient-mind-3" to "y9EKBMw",
        "album-nescient-mind-4" to "N66gdk8",
        // Chill Lounge (BlueTreeAudio)
        "album-chill-lounge-1" to "lvBPA",
        "album-chill-lounge-2" to "VQGWW",
        "album-chill-lounge-3" to "95VAa",
        // Centauria Skyline (eddie edditter)
        "album-centauria-1" to "l4voKZO",
        "album-centauria-2" to "83MZbEJ",
        "album-centauria-3" to "NPW7ZRN",
        "album-centauria-4" to "xy2WGG7",
        // By Yourself, Boy (Tutara Peak)
        "album-by-yourself-1" to "BKJoW9J",
        "album-by-yourself-2" to "P9A4W8k",
        "album-by-yourself-3" to "9bxJ8GO",
        "album-by-yourself-4" to "gvwrYbx",

        // Backward compatibility fallbacks for legacy sample IDs
        "album-pulse-1" to "dGON5PJ",
        "album-pulse-2" to "2PWPRN3",
        "album-pulse-3" to "WY99aBZ",
        "album-pulse-4" to "73WMowW",
        "album-singularity-1" to "91Ax2Vk",
        "album-singularity-2" to "mlGxoR5",
        "album-singularity-3" to "Mo3qE4Z",
        "album-tidlocke-1" to "pl2wmkY",
        "album-tidlocke-2" to "9O8dyQw",
        "album-tidlocke-3" to "mgvpdEv",
        "album-tidlocke-4" to "B5zO4z9",
        "album-mist-1" to "zzaz5",
        "album-mist-2" to "0XbXV",
        "album-mist-3" to "vqPq6",
        "album-crystal-1" to "QVP0VmR",
        "album-crystal-2" to "NY88j4",
        "album-crystal-3" to "y9EKBMw",
        "album-frost-1" to "lvBPA",
        "album-frost-2" to "VQGWW",
        "album-frost-3" to "95VAa",
        "album-frost-4" to "N66gdk8",
        "album-violetdusk-1" to "l4voKZO",
        "album-violetdusk-2" to "83MZbEJ",
        "album-violetdusk-3" to "NPW7ZRN",
        "album-ashes-1" to "BKJoW9J",
        "album-ashes-2" to "P9A4W8k",
        "album-ashes-3" to "9bxJ8GO",
    )

    suspend fun checkHealth(): Boolean = runCatching {
        val response = client.get("$DISCOVERY_URL/health_check")
        response.status.isSuccess()
    }.getOrElse {
        Log.d(TAG, "Audius health check failed: ${it.message}")
        false
    }

    fun resolveDirectStreamUrl(trackId: String): String {
        val audiusId = audiusIdMap[trackId] ?: trackId
        val base = "$DISCOVERY_URL/v1/tracks/$audiusId/stream?app_name=$APP_NAME"
        val key = configuredApiKey
        return if (!key.isNullOrBlank()) {
            "$base&api_key=$key"
        } else {
            base
        }
    }

    suspend fun searchTracks(query: String): List<Track> = runCatching {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()
        val response = client.get("$DISCOVERY_URL/v1/tracks/search") {
            parameter("query", trimmed)
            parameter("app_name", APP_NAME)
            parameter("limit", 30)
            configuredApiKey?.takeIf { it.isNotBlank() }?.let {
                parameter("api_key", it)
            }
        }
        if (response.status.isSuccess()) {
            val list = response.body<AudiusDataList<AudiusTrackData>>().data.filter {
                it.isStreamable && it.isAvailable && !it.isDelete && it.duration > 0
            }
            list.mapIndexed { index, trackData ->
                Track(
                    id = TrackId(trackData.id),
                    title = trackData.title.ifBlank { "Untitled track" },
                    artistId = ArtistId(trackData.user.id.ifBlank { "audius-artist" }),
                    artistName = trackData.user.name.ifBlank { trackData.user.handle.ifBlank { "Audius Artist" } },
                    albumId = AlbumId("audius-search"),
                    albumTitle = "Audius Stream",
                    trackIndex = index + 1,
                    durationMs = trackData.duration.toLong() * 1000L,
                )
            }
        } else {
            emptyList()
        }
    }.getOrElse {
        Log.w(TAG, "Audius direct search error: ${it.message}")
        emptyList()
    }
}
