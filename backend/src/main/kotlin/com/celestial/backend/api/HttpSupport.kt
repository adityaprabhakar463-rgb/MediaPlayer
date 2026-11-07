package com.celestial.backend.api

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respondText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets

private val problemJson = Json { encodeDefaults = true }

suspend fun ApplicationCall.respondProblem(
    status: HttpStatusCode,
    title: String,
    detail: String? = null,
    type: String = "about:blank",
) {
    val body = ProblemDetails(
        type = type,
        title = title,
        status = status.value,
        detail = detail,
        instance = request.local.uri,
    )
    respondText(
        text = problemJson.encodeToString(body),
        contentType = ContentType("application", "problem+json").withCharset(StandardCharsets.UTF_8),
        status = status,
    )
}

fun ApplicationCall.userId(): String {
    val principal = principal<JWTPrincipal>()
    return principal?.payload?.subject?.takeIf { it.isNotBlank() }
        ?: throw ApiProblem(
            HttpStatusCode.Unauthorized,
            "Authentication required",
            "Send a valid Bearer access token.",
            "https://celestial.app/problems/unauthorized",
        )
}
