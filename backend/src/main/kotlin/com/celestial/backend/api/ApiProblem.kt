package com.celestial.backend.api

import io.ktor.http.HttpStatusCode

/**
 * Thrown by stores/routes when a request is well-formed but cannot be fulfilled.
 * `Application.kt` maps this to an RFC 9457 `application/problem+json` response.
 */
class ApiProblem(
    val status: HttpStatusCode,
    val problemTitle: String,
    val problemDetail: String? = null,
    val type: String = "about:blank",
) : RuntimeException(problemDetail ?: problemTitle)
