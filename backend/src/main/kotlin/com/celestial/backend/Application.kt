package com.celestial.backend

import com.celestial.backend.api.ApiProblem
import com.celestial.backend.api.ProblemDetails
import com.celestial.backend.api.respondProblem
import com.celestial.backend.audius.AudiusAdapter
import com.celestial.backend.auth.TokenService
import com.celestial.backend.auth.authRoutes
import com.celestial.backend.catalog.catalogRoutes
import com.celestial.backend.database.DatabaseFactory
import com.celestial.backend.database.PostgresLibraryStore
import com.celestial.backend.library.LibraryStore
import com.celestial.backend.library.libraryRoutes
import com.celestial.backend.sync.syncRoutes
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("com.celestial.backend.Application")

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module(storeOverride: LibraryStore? = null) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    val dbUrl = environment.config.propertyOrNull("database.url")?.getString()
        ?: System.getenv("CELESTIAL_DB_URL")
        ?: "jdbc:postgresql://localhost:5432/celestial"
    val dbUser = environment.config.propertyOrNull("database.user")?.getString()
        ?: System.getenv("CELESTIAL_DB_USER")
        ?: "celestial"
    val dbPass = environment.config.propertyOrNull("database.password")?.getString()
        ?: System.getenv("CELESTIAL_DB_PASSWORD")
        ?: "celestial_secret"

    if (storeOverride == null) {
        DatabaseFactory.init(dbUrl, dbUser, dbPass)
    }

    val store = storeOverride ?: PostgresLibraryStore()
    val tokens = TokenService()
    val trackSource = AudiusAdapter(externalIdByCatalogId = com.celestial.backend.catalog.Catalog.externalIds)

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Celestial API"
            verifier(tokens.verifier)
            validate { credential ->
                val subject = credential.payload.subject
                val tokenType = credential.payload.getClaim("typ").asString()
                if (!subject.isNullOrBlank() && tokenType == "access") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respondProblem(
                    HttpStatusCode.Unauthorized,
                    "Authentication required",
                    "Send a valid Bearer access token.",
                    "https://celestial.app/problems/unauthorized",
                )
            }
        }
    }

    install(StatusPages) {
        exception<ApiProblem> { call, cause ->
            call.respondProblem(cause.status, cause.problemTitle, cause.problemDetail, cause.type)
        }
        exception<ContentTransformationException> { call, cause ->
            call.respondProblem(
                HttpStatusCode.BadRequest,
                "Malformed request body",
                cause.message,
                "https://celestial.app/problems/invalid-request",
            )
        }
        exception<Throwable> { call, cause ->
            logger.error("Unhandled request failure", cause)
            call.respondProblem(
                HttpStatusCode.InternalServerError,
                "Internal server error",
                "The server could not complete the request.",
                "https://celestial.app/problems/internal-error",
            )
        }
    }

    routing {
        get("/health") {
            call.respond(mapOf("status" to "healthy", "service" to "celestial-backend"))
        }
        get("/ready") {
            call.respond(mapOf("status" to "ready"))
        }
        get("/version") {
            call.respond(
                com.celestial.backend.api.VersionResponse(
                    version = "1.0.0",
                    catalogVersion = com.celestial.backend.catalog.Catalog.VERSION,
                ),
            )
        }
        authRoutes(store, tokens)
        catalogRoutes(trackSource)
        libraryRoutes(store)
        syncRoutes(store)
    }

    logger.info(
        "Celestial backend started. Library storage is backed by PostgreSQL/Exposed with schema migrations; tokens are HMAC-signed with the " +
            "configured issuer. Set CELESTIAL_JWT_SECRET for non-development environments.",
    )
}
