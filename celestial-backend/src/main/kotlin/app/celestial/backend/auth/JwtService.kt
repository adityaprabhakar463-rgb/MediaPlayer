package app.celestial.backend.auth

import app.celestial.backend.config.AppConfig
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.time.Instant
import java.util.Date
import java.util.UUID

class JwtService(private val config: AppConfig) {
    private val accessAlg = Algorithm.HMAC256(config.jwtAccessSecret)
    private val refreshAlg = Algorithm.HMAC256(config.jwtRefreshSecret)

    fun issueAccessToken(userId: UUID, deviceId: UUID): String {
        val now = Instant.now()
        return JWT.create()
            .withIssuer(config.jwtIssuer)
            .withAudience(config.jwtAudience)
            .withSubject(userId.toString())
            .withClaim("device_id", deviceId.toString())
            .withClaim("typ", "access")
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(config.jwtAccessTtlMinutes * 60)))
            .sign(accessAlg)
    }

    fun issueRefreshJwt(userId: UUID, deviceId: UUID, tokenId: UUID): String {
        val now = Instant.now()
        return JWT.create()
            .withIssuer(config.jwtIssuer)
            .withAudience(config.jwtAudience)
            .withSubject(userId.toString())
            .withClaim("device_id", deviceId.toString())
            .withClaim("jti", tokenId.toString())
            .withClaim("typ", "refresh")
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(config.jwtRefreshTtlDays * 24 * 60 * 60)))
            .sign(refreshAlg)
    }

    fun verifyAccess(token: String): DecodedJWT = JWT.require(accessAlg)
        .withIssuer(config.jwtIssuer)
        .withAudience(config.jwtAudience)
        .withClaim("typ", "access")
        .build()
        .verify(token)

    fun verifyRefresh(token: String): DecodedJWT = JWT.require(refreshAlg)
        .withIssuer(config.jwtIssuer)
        .withAudience(config.jwtAudience)
        .withClaim("typ", "refresh")
        .build()
        .verify(token)

    fun accessTtlSeconds(): Long = config.jwtAccessTtlMinutes * 60
}
