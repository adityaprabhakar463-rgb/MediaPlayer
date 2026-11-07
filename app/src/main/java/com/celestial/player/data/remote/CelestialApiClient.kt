package com.celestial.player.data.remote

import android.content.Context
import android.os.Build
import android.util.Log
import com.celestial.player.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class ApiException(
    val status: Int,
    val title: String,
    val detail: String? = null,
) : RuntimeException(detail?.let { "$title: $it" } ?: title)

/**
 * Single HTTP entry point for the Android client.
 *
 * Responsibilities:
 *  - anonymous device bootstrap and token refresh (single-flight);
 *  - typed access to catalog, library and playback endpoints;
 *  - converting RFC 9457 problem responses into [ApiException].
 *
 * The base URL defaults to the emulator loopback bridge. Override it with
 * `-PcelestialApiBaseUrl=http://<host>:8080` for a physical device.
 */
class CelestialApiClient(
    context: Context,
    baseUrl: String = BuildConfig.CELESTIAL_API_BASE_URL,
) {
    val baseUrl: String = baseUrl.trimEnd('/')
    private val session = SessionStore(context.applicationContext)
    private val authMutex = Mutex()

    val deviceInstallationId: String get() = session.installationId

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
            connectTimeoutMillis = 5_000
            socketTimeoutMillis = 15_000
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
    }

    suspend fun checkHealth(): Boolean = runCatching {
        client.get("$baseUrl/health").status.isSuccess()
    }.getOrElse {
        Log.d(TAG, "Health check failed: ${it.javaClass.simpleName}")
        false
    }

    /** Ensures a valid access token exists, registering the device when necessary. */
    suspend fun ensureAuthenticated(): String {
        session.accessToken?.let { return it }
        return authMutex.withLock {
            session.accessToken ?: registerDevice().also(session::save).accessToken
        }
    }

    suspend fun registerDevice(): AuthResponseDto {
        val response = client.post("$baseUrl/api/v1/auth/device/register") {
            setBody(
                DeviceRegisterRequest(
                    installationId = session.installationId,
                    deviceSecret = session.deviceSecret,
                    deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})",
                ),
            )
        }
        return response.decodeOrThrow()
    }

    suspend fun fetchCatalog(): CatalogSnapshotDto =
        client.get("$baseUrl/api/v1/catalog").decodeOrThrow()

    suspend fun searchCatalog(query: String): SearchResponseDto =
        client.get("$baseUrl/api/v1/catalog/search") {
            parameter("q", query)
            parameter("limit", 50)
        }.decodeOrThrow()

    suspend fun getPlaylists(): List<PlaylistDto> = authRequest { token ->
        client.get("$baseUrl/api/v1/library/playlists") {
            bearer(token)
        }.decodeOrThrow<PlaylistsResponse>().playlists
    }

    suspend fun createPlaylist(name: String, trackIds: List<String> = emptyList()): PlaylistDto =
        authRequest { token ->
            client.post("$baseUrl/api/v1/library/playlists") {
                bearer(token)
                setBody(CreatePlaylistRequest(name, trackIds))
            }.decodeOrThrow()
        }

    suspend fun updatePlaylist(
        playlistId: String,
        name: String? = null,
        trackIds: List<String>? = null,
    ): PlaylistDto = authRequest { token ->
        client.patch("$baseUrl/api/v1/library/playlists/$playlistId") {
            bearer(token)
            setBody(UpdatePlaylistRequest(name, trackIds))
        }.decodeOrThrow()
    }

    suspend fun deletePlaylist(playlistId: String) {
        authRequest { token ->
            val response = client.delete("$baseUrl/api/v1/library/playlists/$playlistId") {
                bearer(token)
            }
            response.requireSuccess()
        }
    }

    suspend fun getFavourites(): List<String> = authRequest { token ->
        client.get("$baseUrl/api/v1/library/favourites") {
            bearer(token)
        }.decodeOrThrow<FavouritesResponse>().favourites.map { it.trackId }
    }

    suspend fun addFavourite(trackId: String) {
        authRequest { token ->
            val response = client.post("$baseUrl/api/v1/library/favourites/$trackId") {
                bearer(token)
            }
            response.requireSuccess()
        }
    }

    suspend fun removeFavourite(trackId: String) {
        authRequest { token ->
            val response = client.delete("$baseUrl/api/v1/library/favourites/$trackId") {
                bearer(token)
            }
            response.requireSuccess()
        }
    }

    suspend fun getRecents(): List<RecentPlayDto> = authRequest { token ->
        client.get("$baseUrl/api/v1/library/recents") {
            bearer(token)
        }.decodeOrThrow<RecentsResponse>().recents
    }

    suspend fun recordRecent(trackId: String, positionMs: Long = 0L) {
        authRequest { token ->
            val response = client.post("$baseUrl/api/v1/library/recents") {
                bearer(token)
                setBody(RecordRecentRequest(trackId, positionMs))
            }
            response.requireSuccess()
        }
    }

    suspend fun resolveStreamUrl(trackId: String): PlaybackResponse =
        authRequest { token ->
            client.get("$baseUrl/api/v1/catalog/playback/$trackId") {
                bearer(token)
            }.decodeOrThrow()
        }

    fun currentUserId(): String? = session.userId

    private suspend fun <T> authRequest(block: suspend (token: String) -> T): T {
        val token = ensureAuthenticated()
        return try {
            block(token)
        } catch (e: ApiException) {
            if (e.status != 401) throw e
            val refreshed = refreshOrReRegister(staleToken = token)
            block(refreshed)
        }
    }

    private suspend fun refreshOrReRegister(staleToken: String): String = authMutex.withLock {
        // Another request may have refreshed while this one was in flight.
        session.accessToken?.takeIf { it != staleToken }?.let { return@withLock it }

        val refreshToken = session.refreshToken
        if (refreshToken != null) {
            val response = client.post("$baseUrl/api/v1/auth/token/refresh") {
                setBody(RefreshTokenRequest(refreshToken))
            }
            if (response.status.isSuccess()) {
                val auth: AuthResponseDto = response.body()
                session.save(auth)
                return@withLock auth.accessToken
            }
        }
        session.clearTokens()
        registerDevice().also(session::save).accessToken
    }

    private fun io.ktor.client.request.HttpRequestBuilder.bearer(token: String) {
        header(HttpHeaders.Authorization, "Bearer $token")
    }

    private suspend inline fun <reified T> HttpResponse.decodeOrThrow(): T {
        if (status.isSuccess()) return body()
        throw problem()
    }

    private suspend fun HttpResponse.requireSuccess() {
        if (!status.isSuccess()) throw problem()
    }

    private suspend fun HttpResponse.problem(): ApiException {
        val raw = bodyAsText()
        val parsed = runCatching { json.decodeFromString<ProblemDetails>(raw) }.getOrNull()
        val fallback = raw.take(200).ifBlank { "HTTP ${status.value}" }
        return ApiException(
            status = status.value,
            title = parsed?.title ?: "Request failed",
            detail = parsed?.detail ?: fallback,
        )
    }

    private companion object {
        const val TAG = "CelestialApiClient"
    }
}
