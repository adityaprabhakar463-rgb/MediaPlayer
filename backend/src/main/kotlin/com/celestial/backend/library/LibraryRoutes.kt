package com.celestial.backend.library

import com.celestial.backend.api.ApiProblem
import com.celestial.backend.api.CreatePlaylistRequest
import com.celestial.backend.api.FavouritesResponse
import com.celestial.backend.api.PlaylistsResponse
import com.celestial.backend.api.RecordRecentRequest
import com.celestial.backend.api.RecentsResponse
import com.celestial.backend.api.UpdatePlaylistRequest
import com.celestial.backend.api.userId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.libraryRoutes(store: LibraryStore) {
    authenticate("auth-jwt") {
        route("/api/v1/library") {
            // Playlists
            get("/playlists") {
                call.respond(PlaylistsResponse(store.playlists(call.userId())))
            }

            post("/playlists") {
                val request = call.receive<CreatePlaylistRequest>()
                val name = validatePlaylistName(request.name)
                call.respond(
                    HttpStatusCode.Created,
                    store.createPlaylist(call.userId(), name, request.trackIds),
                )
            }

            get("/playlists/{id}") {
                call.respond(store.playlist(call.userId(), call.parameters["id"].orEmpty()))
            }

            patch("/playlists/{id}") {
                val request = call.receive<UpdatePlaylistRequest>()
                if (request.name == null && request.trackIds == null) {
                    throw ApiProblem(
                        HttpStatusCode.BadRequest,
                        "Empty update",
                        "Provide at least one of name or trackIds.",
                        "https://celestial.app/problems/invalid-request",
                    )
                }
                request.name?.let { validatePlaylistName(it) }
                call.respond(
                    store.updatePlaylist(
                        userId = call.userId(),
                        playlistId = call.parameters["id"].orEmpty(),
                        name = request.name?.trim(),
                        trackIds = request.trackIds,
                    ),
                )
            }

            delete("/playlists/{id}") {
                val removed = store.deletePlaylist(call.userId(), call.parameters["id"].orEmpty())
                if (!removed) {
                    throw ApiProblem(
                        HttpStatusCode.NotFound,
                        "Playlist not found",
                        "No playlist with that id exists for this account.",
                        "https://celestial.app/problems/playlist-not-found",
                    )
                }
                call.respond(HttpStatusCode.NoContent)
            }

            // Favourites
            get("/favourites") {
                call.respond(FavouritesResponse(store.favourites(call.userId())))
            }

            post("/favourites/{trackId}") {
                call.respond(
                    HttpStatusCode.Created,
                    store.addFavourite(call.userId(), call.parameters["trackId"].orEmpty()),
                )
            }

            delete("/favourites/{trackId}") {
                val removed = store.removeFavourite(call.userId(), call.parameters["trackId"].orEmpty())
                if (!removed) {
                    throw ApiProblem(
                        HttpStatusCode.NotFound,
                        "Favourite not found",
                        "The track is not currently favourited.",
                        "https://celestial.app/problems/favourite-not-found",
                    )
                }
                call.respond(HttpStatusCode.NoContent)
            }

            // Recents
            get("/recents") {
                call.respond(RecentsResponse(store.recents(call.userId())))
            }

            post("/recents") {
                val request = call.receive<RecordRecentRequest>()
                call.respond(
                    HttpStatusCode.Created,
                    store.recordRecent(
                        userId = call.userId(),
                        trackId = request.trackId,
                        positionMs = request.positionMs,
                    ),
                )
            }
        }
    }
}

private fun validatePlaylistName(raw: String): String {
    val name = raw.trim()
    if (name.isEmpty() || name.length > 100) {
        throw ApiProblem(
            HttpStatusCode.BadRequest,
            "Invalid playlist name",
            "Playlist names must contain between 1 and 100 characters.",
            "https://celestial.app/problems/invalid-playlist-name",
        )
    }
    return name
}
