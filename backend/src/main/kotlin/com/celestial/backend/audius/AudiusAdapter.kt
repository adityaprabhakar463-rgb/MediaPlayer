package com.celestial.backend.audius

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class AudiusTrack(
    val id: String,
    val title: String = "",
    val user: AudiusUser = AudiusUser(),
    /** Audius returns duration in whole seconds. */
    val duration: Int = 0,
    val artwork: AudiusArtwork? = null,
    @SerialName("is_streamable") val isStreamable: Boolean = true,
    @SerialName("is_available") val isAvailable: Boolean = true,
    @SerialName("is_delete") val isDelete: Boolean = false,
)

@Serializable
data class AudiusUser(
    val id: String = "",
    val name: String = "",
    val handle: String = "",
)

@Serializable
data class AudiusArtwork(
    @SerialName("150x150") val small: String? = null,
    @SerialName("480x480") val medium: String? = null,
    @SerialName("1000x1000") val large: String? = null,
)

private val audiusJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
}

/**
 * Provider boundary described in `docs/backend/06`.
 *
 * Stream resolution is intentionally server-side and deterministic: the curated catalog owns the
 * Celestial-id -> Audius-id mapping, so a play request never needs a discovery-node round trip just
 * to construct the short-lived stream endpoint. The client (Media3) follows Audius' `302` redirect
 * to the signed creator-node URL.
 */
interface TrackSource {
    suspend fun searchTracks(query: String): List<AudiusTrack>
    suspend fun getTrack(trackId: String): AudiusTrack?
    suspend fun resolveStreamUrl(trackId: String): String?
}

class AudiusAdapter(
    private val appName: String = "Celestial",
    private val discoveryApi: String = System.getenv("AUDIUS_API_BASE")
        ?: "https://discoveryprovider.audius.co",
    private val externalIdByCatalogId: Map<String, String> = emptyMap(),
    private val client: HttpClient = defaultClient(),
) : TrackSource {

    private val baseUrl = discoveryApi.trimEnd('/')
    private val cache = ConcurrentHashMap<String, CacheEntry>()

    override suspend fun searchTracks(query: String): List<AudiusTrack> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()
        val response = runCatching {
            client.get("$baseUrl/v1/tracks/search") {
                parameter("query", trimmed)
                parameter("app_name", appName)
                parameter("limit", 30)
            }
        }.getOrElse {
            logger.warn("Audius search failed: ${it.message}")
            return emptyList()
        }
        if (!response.status.isSuccess()) {
            logger.warn("Audius search returned ${response.status}")
            return emptyList()
        }
        return runCatching { response.body<AudiusListResponse<AudiusTrack>>().data }
            .getOrElse {
                logger.warn("Audius search parse failed: ${it.message}")
                emptyList()
            }
            .filter { it.isPlayable() }
    }

    override suspend fun getTrack(trackId: String): AudiusTrack? {
        val cached = cache[trackId]?.takeIf { it.expiresAtMs > System.currentTimeMillis() }
        if (cached != null) return cached.track

        val response = runCatching {
            client.get("$baseUrl/v1/tracks/$trackId") {
                parameter("app_name", appName)
            }
        }.getOrElse {
            logger.warn("Audius track lookup failed: ${it.message}")
            return null
        }
        if (!response.status.isSuccess()) return null
        val track = runCatching { response.body<AudiusTrackResponse>().data }.getOrNull() ?: return null
        cache[trackId] = CacheEntry(track, System.currentTimeMillis() + CACHE_TTL_MS)
        return track
    }

    override suspend fun resolveStreamUrl(trackId: String): String? {
        val externalId = externalIdByCatalogId[trackId] ?: return null
        return "$baseUrl/v1/tracks/$externalId/stream?app_name=$appName"
    }

    private fun AudiusTrack.isPlayable(): Boolean =
        isStreamable && isAvailable && !isDelete && duration > 0

    private data class CacheEntry(val track: AudiusTrack, val expiresAtMs: Long)

    private companion object {
        const val CACHE_TTL_MS = 5 * 60_000L
        val logger = LoggerFactory.getLogger(AudiusAdapter::class.java)

        fun defaultClient(): HttpClient = HttpClient(CIO) {
            expectSuccess = false
            install(ContentNegotiation) { json(audiusJson) }
            install(HttpTimeout) {
                requestTimeoutMillis = 10_000
                connectTimeoutMillis = 5_000
                socketTimeoutMillis = 10_000
            }
        }
    }
}

@Serializable
private data class AudiusListResponse<T>(val data: List<T> = emptyList())

@Serializable
private data class AudiusTrackResponse(val data: AudiusTrack)
