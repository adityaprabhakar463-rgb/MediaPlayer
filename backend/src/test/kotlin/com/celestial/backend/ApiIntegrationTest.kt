package com.celestial.backend

import com.celestial.backend.api.AuthResponse
import com.celestial.backend.api.CatalogSnapshot
import com.celestial.backend.api.CreatePlaylistRequest
import com.celestial.backend.api.DeviceRegisterRequest
import com.celestial.backend.api.FavouritesResponse
import com.celestial.backend.api.PlaybackResponse
import com.celestial.backend.api.PlaylistDto
import com.celestial.backend.api.PlaylistsResponse
import com.celestial.backend.api.RecentPlayDto
import com.celestial.backend.api.RecentsResponse
import com.celestial.backend.api.RecordRecentRequest
import com.celestial.backend.api.RefreshTokenRequest
import com.celestial.backend.api.SearchResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ApiIntegrationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun HttpRequestBuilder.auth(token: String) {
        header(HttpHeaders.Authorization, "Bearer $token")
    }

    @Test
    fun `anonymous device can authenticate and sync its library`() = testApplication {
        application { module() }
        val client = createClient {
            install(ContentNegotiation) { json(json) }
        }

        assertEquals(HttpStatusCode.OK, client.get("/health").status)

        val catalog: CatalogSnapshot = client.get("/api/v1/catalog").body()
        assertTrue(catalog.tracks.isNotEmpty(), "catalog must expose curated tracks")
        assertEquals(8, catalog.albums.size)
        assertEquals(7, catalog.artists.size)
        val track = catalog.tracks.first()

        val search: SearchResponse = client.get("/api/v1/catalog/search?q=nebula").body()
        assertTrue(search.tracks.isNotEmpty() || search.albums.isNotEmpty())

        val unauthorized = client.get("/api/v1/library/playlists")
        assertEquals(HttpStatusCode.Unauthorized, unauthorized.status)
        assertTrue(
            unauthorized.headers[HttpHeaders.ContentType]?.startsWith("application/problem+json") == true,
            "errors must be RFC 9457 problem+json",
        )

        val registration: AuthResponse = client.post("/api/v1/auth/device/register") {
            contentType(ContentType.Application.Json)
            setBody(DeviceRegisterRequest("installation-test", "device-secret-test", "integration test"))
        }.body()
        assertTrue(registration.accessToken.isNotBlank())
        assertTrue(registration.refreshToken.isNotBlank())
        val token = registration.accessToken

        val seeded: PlaylistsResponse = client.get("/api/v1/library/playlists") {
            auth(token)
        }.body()
        assertEquals(5, seeded.playlists.size, "new devices receive the starter library")

        val created: PlaylistDto = client.post("/api/v1/library/playlists") {
            auth(token)
            contentType(ContentType.Application.Json)
            setBody(CreatePlaylistRequest("Integration playlist", listOf(track.id)))
        }.body()
        assertEquals(listOf(track.id), created.trackIds)

        val favouritesBefore: FavouritesResponse = client.get("/api/v1/library/favourites") {
            auth(token)
        }.body()
        assertTrue(favouritesBefore.favourites.isEmpty())

        client.post("/api/v1/library/favourites/${track.id}") { auth(token) }
        val favouritesAfter: FavouritesResponse = client.get("/api/v1/library/favourites") {
            auth(token)
        }.body()
        assertEquals(listOf(track.id), favouritesAfter.favourites.map { it.trackId })

        client.post("/api/v1/library/recents") {
            auth(token)
            contentType(ContentType.Application.Json)
            setBody(RecordRecentRequest(track.id, 1_500L))
        }
        val recents: RecentsResponse = client.get("/api/v1/library/recents") {
            auth(token)
        }.body()
        val firstRecent: RecentPlayDto = recents.recents.first()
        assertEquals(track.id, firstRecent.trackId)

        val playback = client.get("/api/v1/catalog/playback/${track.id}") { auth(token) }
        assertEquals(HttpStatusCode.OK, playback.status, "curated tracks resolve through Audius")
        val playbackBody: PlaybackResponse = playback.body()
        assertEquals("audius", playbackBody.provider)
        assertTrue(playbackBody.streamUrl.contains("/v1/tracks/"))
        assertTrue(playbackBody.streamUrl.contains("app_name=Celestial"))

        val rotated: AuthResponse = client.post("/api/v1/auth/token/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(registration.refreshToken))
        }.body()
        assertNotEquals(registration.refreshToken, rotated.refreshToken)
        assertNotEquals(registration.accessToken, rotated.accessToken)

        val reused = client.post("/api/v1/auth/token/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(registration.refreshToken))
        }
        assertEquals(HttpStatusCode.Unauthorized, reused.status, "refresh tokens are single-use")
    }

    @Test
    fun `device registration is idempotent and rejects a different secret`() = testApplication {
        application { module() }
        val client = createClient { install(ContentNegotiation) { json(json) } }

        fun HttpResponse.isCreatedOrOk() = status == HttpStatusCode.Created || status == HttpStatusCode.OK

        val first = client.post("/api/v1/auth/device/register") {
            contentType(ContentType.Application.Json)
            setBody(DeviceRegisterRequest("installation-2", "first-device-secret", null))
        }
        assertTrue(first.isCreatedOrOk())

        val second = client.post("/api/v1/auth/device/register") {
            contentType(ContentType.Application.Json)
            setBody(DeviceRegisterRequest("installation-2", "first-device-secret", null))
        }
        assertTrue(second.isCreatedOrOk())

        val mismatch = client.post("/api/v1/auth/device/register") {
            contentType(ContentType.Application.Json)
            setBody(DeviceRegisterRequest("installation-2", "different-device-secret", null))
        }
        assertEquals(HttpStatusCode.Unauthorized, mismatch.status)
    }
}
