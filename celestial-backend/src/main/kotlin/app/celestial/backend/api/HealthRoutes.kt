package app.celestial.backend.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(val status: String)

@Serializable
data class VersionResponse(val name: String = "celestial-backend", val version: String = "0.1.0-m0")

fun Route.healthRoutes(dbReady: () -> Boolean) {
    get("/health") {
        call.respond(HttpStatusCode.OK, HealthResponse("ok"))
    }
    get("/ready") {
        if (dbReady()) call.respond(HttpStatusCode.OK, HealthResponse("ready"))
        else call.respond(HttpStatusCode.ServiceUnavailable, HealthResponse("not_ready"))
    }
    get("/version") {
        call.respond(HttpStatusCode.OK, VersionResponse())
    }
}
