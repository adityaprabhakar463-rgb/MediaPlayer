package app.celestial.backend.auth

import app.celestial.backend.config.AppConfig
import app.celestial.backend.problem.ApiException
import app.celestial.backend.problem.Problems
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class AuthService(
    private val config: AppConfig,
    private val jwt: JwtService,
) {
    fun register(req: DeviceRegisterRequest, instance: String?): DeviceRegisterResponse = transaction {
        if (req.installationId.isBlank()) throw ApiException(Problems.badRequest("installationId required", instance))

        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val existing = Devices.selectAll().where { Devices.installationId eq req.installationId }.singleOrNull()
        if (existing != null && existing[Devices.revokedAt] == null) {
            throw ApiException(Problems.conflict("Device already registered", instance))
        }

        val userId = UUID.randomUUID()
        val deviceId = UUID.randomUUID()
        val deviceSecret = Security.randomToken(32)
        val refreshTokenId = UUID.randomUUID()
        val refreshJwt = jwt.issueRefreshJwt(userId, deviceId, refreshTokenId)
        val refreshExp = now.plusDays(config.jwtRefreshTtlDays)

        Users.insert {
            it[id] = userId
            it[anonymous] = true
            it[createdAt] = now
            it[updatedAt] = now
            it[lastSeenAt] = now
        }
        Devices.insert {
            it[id] = deviceId
            it[Devices.userId] = userId
            it[installationId] = req.installationId.trim()
            it[deviceSecretHash] = Security.sha256(deviceSecret)
            it[deviceInfo] = req.deviceInfo
            it[registeredAt] = now
        }
        RefreshTokens.insert {
            it[id] = refreshTokenId
            it[RefreshTokens.userId] = userId
            it[RefreshTokens.deviceId] = deviceId
            it[tokenHash] = Security.sha256(refreshJwt)
            it[expiresAt] = refreshExp
            it[createdAt] = now
        }

        DeviceRegisterResponse(
            userId = userId.toString(),
            deviceId = deviceId.toString(),
            deviceSecret = deviceSecret,
            accessToken = jwt.issueAccessToken(userId, deviceId),
            refreshToken = refreshJwt,
            expiresInSeconds = jwt.accessTtlSeconds(),
        )
    }

    fun refresh(req: RefreshRequest, instance: String?): RefreshResponse = transaction {
        if (req.refreshToken.isBlank()) throw ApiException(Problems.badRequest("refreshToken required", instance))
        val decoded = try {
            jwt.verifyRefresh(req.refreshToken)
        } catch (e: Exception) {
            throw ApiException(Problems.unauthorized("Invalid refresh token", instance))
        }
        val tokenId = decoded.getClaim("jti").asString()?.let(UUID::fromString)
            ?: throw ApiException(Problems.unauthorized("Invalid refresh token", instance))
        val userId = UUID.fromString(decoded.subject)
        val deviceId = UUID.fromString(decoded.getClaim("device_id").asString())
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val hash = Security.sha256(req.refreshToken)

        val row = RefreshTokens.selectAll().where { (RefreshTokens.id eq tokenId) and (RefreshTokens.tokenHash eq hash) }.singleOrNull()
            ?: throw ApiException(Problems.unauthorized("Refresh token not found", instance))
        if (row[RefreshTokens.revokedAt] != null) throw ApiException(Problems.unauthorized("Refresh token revoked", instance))
        if (row[RefreshTokens.expiresAt].isBefore(now)) throw ApiException(Problems.unauthorized("Refresh token expired", instance))
        if (row[RefreshTokens.userId] != userId || row[RefreshTokens.deviceId] != deviceId) {
            throw ApiException(Problems.unauthorized("Refresh token mismatch", instance))
        }

        val newTokenId = UUID.randomUUID()
        val newRefresh = jwt.issueRefreshJwt(userId, deviceId, newTokenId)
        val refreshExp = now.plusDays(config.jwtRefreshTtlDays)

        RefreshTokens.update({ RefreshTokens.id eq tokenId }) {
            it[revokedAt] = now
            it[replacedById] = newTokenId
        }
        RefreshTokens.insert {
            it[id] = newTokenId
            it[RefreshTokens.userId] = userId
            it[RefreshTokens.deviceId] = deviceId
            it[tokenHash] = Security.sha256(newRefresh)
            it[expiresAt] = refreshExp
            it[createdAt] = now
        }
        Users.update({ Users.id eq userId }) { it[lastSeenAt] = now; it[updatedAt] = now }

        RefreshResponse(
            accessToken = jwt.issueAccessToken(userId, deviceId),
            refreshToken = newRefresh,
            expiresInSeconds = jwt.accessTtlSeconds(),
        )
    }

    fun revokeByAccessToken(accessToken: String, instance: String?): RevokeResponse = transaction {
        val decoded = try { jwt.verifyAccess(accessToken) } catch (e: Exception) {
            throw ApiException(Problems.unauthorized("Invalid access token", instance))
        }
        val deviceId = UUID.fromString(decoded.getClaim("device_id").asString())
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        RefreshTokens.update({ (RefreshTokens.deviceId eq deviceId) and (RefreshTokens.revokedAt.isNull()) }) {
            it[revokedAt] = now
        }
        RevokeResponse(true)
    }
}
