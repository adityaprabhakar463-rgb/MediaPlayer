package com.celestial.backend.catalog

import com.celestial.backend.api.ApiProblem
import com.celestial.backend.api.PlaybackResponse
import com.celestial.backend.api.SearchResponse
import com.celestial.backend.audius.TrackSource
import com.celestial.backend.api.userId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.catalogRoutes(trackSource: TrackSource) {
    route("/api/v1/catalog") {
        get {
            call.respond(Catalog.snapshot())
        }

        get("/search") {
            val query = call.request.queryParameters["q"].orEmpty()
            val limit = (call.request.queryParameters["limit"]?.toIntOrNull() ?: 50)
                .coerceIn(1, 100)
            val result = Catalog.search(query)
            call.respond(
                SearchResponse(
                    query = query,
                    tracks = result.tracks.take(limit),
                    albums = result.albums.take(limit),
                    artists = result.artists.take(limit),
                ),
            )
        }

        get("/tracks") {
            call.respond(Catalog.tracks)
        }

        get("/tracks/{id}") {
            val track = Catalog.trackById(call.parameters["id"].orEmpty())
                ?: throw ApiProblem(
                    HttpStatusCode.NotFound,
                    "Track not found",
                    "The requested track is not in the curated catalog.",
                    "https://celestial.app/problems/track-not-found",
                )
            call.respond(track)
        }

        get("/albums/{id}") {
            val album = Catalog.albums.firstOrNull { it.id == call.parameters["id"] }
                ?: throw ApiProblem(
                    HttpStatusCode.NotFound,
                    "Album not found",
                    "The requested album is not in the curated catalog.",
                    "https://celestial.app/problems/album-not-found",
                )
            call.respond(album)
        }

        get("/artists/{id}") {
            val artist = Catalog.artists.firstOrNull { it.id == call.parameters["id"] }
                ?: throw ApiProblem(
                    HttpStatusCode.NotFound,
                    "Artist not found",
                    "The requested artist is not in the curated catalog.",
                    "https://celestial.app/problems/artist-not-found",
                )
            call.respond(artist)
        }

        authenticate("auth-jwt") {
            get("/playback/{trackId}") {
                val trackId = call.parameters["trackId"].orEmpty()
                val track = Catalog.trackById(trackId)
                    ?: throw ApiProblem(
                        HttpStatusCode.NotFound,
                        "Track not found",
                        "The requested track is not in the curated catalog.",
                        "https://celestial.app/problems/track-not-found",
                    )
                if (!track.isStreamable || !track.isAvailable || track.isDelete) {
                    throw ApiProblem(
                        HttpStatusCode.Conflict,
                        "Track unavailable",
                        "This track is not streamable, is deleted, or is unavailable from its provider.",
                        "https://celestial.app/problems/track-unavailable",
                    )
                }
                // Resolve the token/subject so unauthenticated callers are rejected before adapter work.
                call.userId()
                val streamUrl = trackSource.resolveStreamUrl(track.id)
                    ?: throw ApiProblem(
                        HttpStatusCode.Conflict,
                        "Stream provider not mapped",
                        "The curated track is not yet mapped to an Audius external id. Add the mapping during catalog ingestion.",
                        "https://celestial.app/problems/stream-not-mapped",
                    )
                call.respond(
                    PlaybackResponse(
                        trackId = track.id,
                        streamUrl = streamUrl,
                        provider = "audius",
                    ),
                )
            }
        }
    }
}
