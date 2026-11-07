package app.celestial.backend.auth

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object Users : UUIDTable("users") {
    val anonymous = bool("anonymous").default(true)
    val linkedEmail = text("linked_email").nullable().uniqueIndex()
    val passwordHash = text("password_hash").nullable()
    val linkedProvider = text("linked_provider").nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
    val lastSeenAt = timestampWithTimeZone("last_seen_at")
    val deletedAt = timestampWithTimeZone("deleted_at").nullable()
}

object Devices : UUIDTable("devices") {
    val userId = reference("user_id", Users)
    val installationId = text("installation_id").uniqueIndex()
    val deviceSecretHash = text("device_secret_hash")
    val deviceInfo = text("device_info").nullable()
    val registeredAt = timestampWithTimeZone("registered_at")
    val revokedAt = timestampWithTimeZone("revoked_at").nullable()
}

object RefreshTokens : UUIDTable("refresh_tokens") {
    val userId = reference("user_id", Users)
    val deviceId = reference("device_id", Devices)
    val tokenHash = text("token_hash").uniqueIndex()
    val expiresAt = timestampWithTimeZone("expires_at")
    val createdAt = timestampWithTimeZone("created_at")
    val revokedAt = timestampWithTimeZone("revoked_at").nullable()
    val replacedById = uuid("replaced_by_id").nullable()
}
