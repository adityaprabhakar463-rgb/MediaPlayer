package com.celestial.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.time.Instant
import java.util.Date
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
)

/**
 * HMAC-signed access/refresh tokens with single-use refresh rotation.
 *
 * The secret is read from `CELESTIAL_JWT_SECRET`; the development fallback keeps local runs
 * frictionless but must be overridden in any deployed environment.
 */
class TokenService(
    private val secret: String = System.getenv("CELESTIAL_JWT_SECRET")
        ?: "celestial-development-secret-change-me",
    private val issuer: String = System.getenv("CELESTIAL_JWT_ISSUER") ?: "https://celestial.app",
    private val audience: String = "celestial-android",
    private val accessTtlSeconds: Long = 3_600L,
    private val refreshTtlSeconds: Long = 30L * 24 * 60 * 60,
) {
    private val algorithm: Algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    private val activeRefreshTokens = ConcurrentHashMap.newKeySet<String>()

    fun issuePair(userId: String): TokenPair {
        val now = Instant.now()
        val refreshId = UUID.randomUUID().toString()

        val accessToken = JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId)
            .withJWTId(UUID.randomUUID().toString())
            .withClaim("typ", "access")
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(accessTtlSeconds)))
            .sign(algorithm)

        val refreshToken = JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId)
            .withJWTId(refreshId)
            .withClaim("typ", "refresh")
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(refreshTtlSeconds)))
            .sign(algorithm)

        activeRefreshTokens += refreshId
        return TokenPair(accessToken, refreshToken, accessTtlSeconds)
    }

    fun userIdFromAccessToken(token: String): String? = runCatching {
        val decoded = verifier.verify(token)
        if (decoded.getClaim("typ").asString() != "access") return@runCatching null
        decoded.subject?.takeIf { it.isNotBlank() }
    }.getOrNull()

    /**
     * Verifies a refresh token and immediately revokes it. Returns a fresh pair.
     * Throws [InvalidRefreshToken] when the token is malformed, expired, or already rotated.
     */
    fun rotateRefreshToken(refreshToken: String): Pair<String, TokenPair> {
        val decoded = runCatching { verifier.verify(refreshToken) }
            .getOrElse { throw InvalidRefreshToken("Refresh token is invalid or expired") }
        if (decoded.getClaim("typ").asString() != "refresh") {
            throw InvalidRefreshToken("Token is not a refresh token")
        }
        val tokenId = decoded.id ?: throw InvalidRefreshToken("Refresh token has no id")
        if (!activeRefreshTokens.remove(tokenId)) {
            throw InvalidRefreshToken("Refresh token has already been used or revoked")
        }
        val userId = decoded.subject?.takeIf { it.isNotBlank() }
            ?: throw InvalidRefreshToken("Refresh token has no subject")
        return userId to issuePair(userId)
    }

    fun revokeRefreshToken(refreshToken: String) {
        val decoded = runCatching { verifier.verify(refreshToken) }.getOrNull() ?: return
        decoded.id?.let { activeRefreshTokens.remove(it) }
    }
}

class InvalidRefreshToken(message: String) : RuntimeException(message)
