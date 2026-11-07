package com.celestial.backend.auth

import com.celestial.backend.api.ApiProblem
import com.celestial.backend.api.AuthResponse
import com.celestial.backend.api.DeviceRegisterRequest
import com.celestial.backend.api.MeResponse
import com.celestial.backend.api.RefreshTokenRequest
import com.celestial.backend.api.respondProblem
import com.celestial.backend.api.userId
import com.celestial.backend.library.InvalidDeviceSecret
import com.celestial.backend.library.LibraryStore
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(store: LibraryStore, tokens: TokenService) {
    route("/api/v1/auth") {
        post("/device/register") {
            val request = call.receive<DeviceRegisterRequest>()
            if (request.installationId.isBlank()) {
                throw ApiProblem(
                    HttpStatusCode.BadRequest,
                    "Invalid installation id",
                    "installationId must not be blank.",
                    "https://celestial.app/problems/invalid-request",
                )
            }
            if (request.deviceSecret.length < 8) {
                throw ApiProblem(
                    HttpStatusCode.BadRequest,
                    "Weak device secret",
                    "deviceSecret must contain at least 8 characters.",
                    "https://celestial.app/problems/invalid-request",
                )
            }

            val (account, created) = try {
                store.registerDevice(
                    installationId = request.installationId,
                    deviceSecret = request.deviceSecret,
                    deviceInfo = request.deviceInfo,
                )
            } catch (e: InvalidDeviceSecret) {
                return@post call.respondProblem(
                    HttpStatusCode.Unauthorized,
                    "Device secret mismatch",
                    "The installation id is already registered with a different secret.",
                    "https://celestial.app/problems/device-secret-mismatch",
                )
            }

            val pair = tokens.issuePair(account.id)
            call.respond(
                if (created) HttpStatusCode.Created else HttpStatusCode.OK,
                pair.toResponse(account.id),
            )
        }

        post("/token/refresh") {
            val request = call.receive<RefreshTokenRequest>()
            rotate(tokens, request.refreshToken) { userId, pair ->
                call.respond(HttpStatusCode.OK, pair.toResponse(userId))
            }
        }

        post("/revoke") {
            val request = call.receive<RefreshTokenRequest>()
            tokens.revokeRefreshToken(request.refreshToken)
            call.respond(HttpStatusCode.NoContent)
        }

        authenticate("auth-jwt") {
            get("/me") {
                val account = store.account(call.userId())
                call.respond(
                    MeResponse(
                        userId = account.id,
                        installationId = account.installationId,
                        anonymous = true,
                        createdAt = account.createdAt,
                    ),
                )
            }
        }
    }
}

private suspend fun rotate(
    tokens: TokenService,
    refreshToken: String,
    respond: suspend (String, TokenPair) -> Unit,
) {
    val (userId, pair) = try {
        tokens.rotateRefreshToken(refreshToken)
    } catch (e: InvalidRefreshToken) {
        throw ApiProblem(
            HttpStatusCode.Unauthorized,
            "Invalid refresh token",
            e.message,
            "https://celestial.app/problems/invalid-refresh-token",
        )
    }
    respond(userId, pair)
}

private fun TokenPair.toResponse(userId: String) = AuthResponse(
    accessToken = accessToken,
    refreshToken = refreshToken,
    userId = userId,
    expiresIn = expiresIn,
)
