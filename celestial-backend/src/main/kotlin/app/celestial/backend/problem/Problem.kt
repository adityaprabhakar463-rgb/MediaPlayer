package app.celestial.backend.problem

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

@Serializable
data class ProblemResponse(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String? = null,
    val instance: String? = null,
    val retry_after: Int? = null,
)

class ApiException(
    val problem: ProblemResponse,
    val httpStatus: HttpStatusCode = HttpStatusCode.fromValue(problem.status),
) : RuntimeException(problem.detail ?: problem.title)

object Problems {
    private const val BASE = "https://celestial.app/problems"

    fun unauthorized(detail: String, instance: String? = null) = ProblemResponse(
        type = "$BASE/unauthorized", title = "Unauthorized", status = 401, detail = detail, instance = instance
    )

    fun forbidden(detail: String, instance: String? = null) = ProblemResponse(
        type = "$BASE/forbidden", title = "Forbidden", status = 403, detail = detail, instance = instance
    )

    fun notFound(detail: String, instance: String? = null) = ProblemResponse(
        type = "$BASE/not-found", title = "Not found", status = 404, detail = detail, instance = instance
    )

    fun conflict(detail: String, instance: String? = null) = ProblemResponse(
        type = "$BASE/conflict", title = "Conflict", status = 409, detail = detail, instance = instance
    )

    fun badRequest(detail: String, instance: String? = null) = ProblemResponse(
        type = "$BASE/bad-request", title = "Bad request", status = 400, detail = detail, instance = instance
    )

    fun rateLimited(detail: String, instance: String? = null, retryAfterSeconds: Int = 30) = ProblemResponse(
        type = "$BASE/rate-limited", title = "Too many requests", status = 429,
        detail = detail, instance = instance, retry_after = retryAfterSeconds
    )

    fun internal(detail: String = "Unexpected error", instance: String? = null) = ProblemResponse(
        type = "$BASE/internal", title = "Internal server error", status = 500, detail = detail, instance = instance
    )
}
