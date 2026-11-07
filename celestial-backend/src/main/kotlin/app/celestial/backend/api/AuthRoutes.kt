package app.celestial.backend.api

import app.celestial.backend.auth.AuthService
import app.celestial.backend.auth.DeviceRegisterRequest
import app.celestial.backend.auth.RefreshRequest
import app.celestial.backend.problem.ApiException
import app.celestial.backend.problem.Problems
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(auth: AuthService) {
    route("/api/v1/auth") {
        post("/device/register") {
            val req = call.receive<DeviceRegisterRequest>()
            val res = auth.register(req, call.request.local.uri)
            call.respond(HttpStatusCode.Created, res)
        }
        post("/token/refresh") {
            val req = call.receive<RefreshRequest>()
            val res = auth.refresh(req, call.request.local.uri)
            call.respond(HttpStatusCode.OK, res)
        }
        post("/revoke") {
            val authHeader = call.request.headers[HttpHeaders.Authorization]
                ?: throw ApiException(Problems.unauthorized("Missing Authorization header", call.request.local.uri))
            val token = authHeader.removePrefix("Bearer ").trim()
            if (token.isBlank()) throw ApiException(Problems.unauthorized("Missing bearer token", call.request.local.uri))
            val res = auth.revokeByAccessToken(token, call.request.local.uri)
            call.respond(HttpStatusCode.OK, res)
        }
    }
}
