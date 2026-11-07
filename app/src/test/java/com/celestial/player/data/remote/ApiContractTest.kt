package com.celestial.player.data.remote

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the Android side of the wire contract against silent server field renames.
 * Payloads mirror the shapes emitted by `backend/src/main/kotlin/com/celestial/backend/api`.
 */
class ApiContractTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Test
    fun `catalog snapshot decodes into domain models`() {
        val payload = """
            {
              "version": 1,
              "tracks": [
                {
                  "id": "album-pulse-1",
                  "title": "Ignition Waltz",
                  "artistId": "artist-nova",
                  "artistName": "Nova Vale",
                  "albumId": "album-pulse",
                  "albumTitle": "Stellar Pulse",
                  "trackIndex": 1,
                  "durationMs": 214000,
                  "isStreamable": true,
                  "isAvailable": true,
                  "isDelete": false
                }
              ],
              "albums": [
                {
                  "id": "album-pulse",
                  "title": "Stellar Pulse",
                  "artistId": "artist-nova",
                  "artistName": "Nova Vale",
                  "colorSeed": 3108847,
                  "trackIds": ["album-pulse-1"]
                }
              ],
              "artists": [
                {
                  "id": "artist-nova",
                  "name": "Nova Vale",
                  "albumIds": ["album-pulse"],
                  "trackIds": ["album-pulse-1"]
                }
              ]
            }
        """.trimIndent()

        val snapshot = json.decodeFromString<CatalogSnapshotDto>(payload)
        assertEquals(1, snapshot.tracks.size)
        assertEquals(1, snapshot.albums.size)

        val track = snapshot.tracks.single().toDomain()
        assertEquals("Ignition Waltz", track.title)
        assertEquals(214_000L, track.durationMs)
        assertEquals("artist-nova", track.artistId.value)

        val album = snapshot.albums.single().toDomain()
        assertEquals(0x2F6FEF, album.colorSeed)
        assertEquals(listOf("album-pulse-1"), album.trackIds.map { it.value })

        val artist = snapshot.artists.single().toDomain()
        assertEquals("Nova Vale", artist.name)
    }

    @Test
    fun `library list decodes and tolerates unknown fields`() {
        val payload = """
            {
              "playlists": [
                {
                  "id": "pl-1",
                  "name": "Night Drive",
                  "trackIds": ["album-pulse-1"],
                  "version": 2,
                  "updatedAt": "2026-11-05T12:00:00Z",
                  "futureField": "ignored"
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<PlaylistsResponse>(payload)
        val playlist = response.playlists.single().toDomain()
        assertEquals("Night Drive", playlist.name)
        assertEquals(1, playlist.trackIds.size)
    }

    @Test
    fun `problem details expose title and detail`() {
        val payload = """
            {
              "type": "https://celestial.app/problems/track-not-found",
              "title": "Track not found",
              "status": 404,
              "detail": "The requested track is not in the curated catalog.",
              "instance": "/api/v1/library/favourites/nope"
            }
        """.trimIndent()

        val problem = json.decodeFromString<ProblemDetails>(payload)
        assertEquals(404, problem.status)
        assertTrue(problem.detail!!.contains("curated catalog"))
    }
}
