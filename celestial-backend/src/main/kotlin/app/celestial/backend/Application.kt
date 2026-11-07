package app.celestial.backend

import app.celestial.backend.api.authRoutes
import app.celestial.backend.api.healthRoutes
import app.celestial.backend.auth.AuthService
import app.celestial.backend.auth.JwtService
import app.celestial.backend.config.AppConfig
import app.celestial.backend.db.DatabaseFactory
import app.celestial.backend.problem.ApiException
import app.celestial.backend.problem.ProblemResponse
import app.celestial.backend.problem.Problems
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    val config = AppConfig.fromEnv()
    embeddedServer(Netty, port = config.port, module = Application::module).start(wait = true)
}

fun Application.module(configOverride: AppConfig? = null, skipDb: Boolean = false) {
    val config = configOverride ?: AppConfig.fromEnv()
    if (!skipDb) {
        DatabaseFactory.init(config)
    }
    val jwt = JwtService(config)
    val auth = AuthService(config, jwt)

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        })
    }
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respondText(
                text = Json.encodeToString(ProblemResponse.serializer(), cause.problem),
                contentType = ContentType.parse("application/problem+json"),
                status = cause.httpStatus,
            )
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled error", cause)
            val problem = Problems.internal(instance = call.request.local.uri)
            call.respondText(
                text = Json.encodeToString(ProblemResponse.serializer(), problem),
                contentType = ContentType.parse("application/problem+json"),
                status = HttpStatusCode.InternalServerError,
            )
        }
    }

    routing {
        healthRoutes {
            try { if (skipDb) true else transaction { exec("SELECT 1") { true } ?: false } } catch (_: Exception) { false }
        }
        authRoutes(auth)
    }
}
