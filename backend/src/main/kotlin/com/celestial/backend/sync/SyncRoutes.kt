package com.celestial.backend.sync

import com.celestial.backend.api.AckRequest
import com.celestial.backend.api.SyncResponse
import com.celestial.backend.api.userId
import com.celestial.backend.library.LibraryStore
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.syncRoutes(store: LibraryStore) {
    authenticate("auth-jwt") {
        route("/api/v1/sync") {
            get("/changes") {
                val sinceVersion = call.request.queryParameters["since_version"]?.toLongOrNull() ?: 0L
                val (changes, serverVersion) = store.changes(call.userId(), sinceVersion.coerceAtLeast(0L))
                call.respond(SyncResponse(changes, serverVersion))
            }

            post("/ack") {
                val request = call.receive<AckRequest>()
                val acknowledged = store.acknowledge(call.userId(), request.version)
                call.respond(mapOf("status" to "acknowledged", "version" to acknowledged))
            }
        }
    }
}
