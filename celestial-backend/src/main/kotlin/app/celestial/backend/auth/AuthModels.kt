package app.celestial.backend.auth

import kotlinx.serialization.Serializable

@Serializable
data class DeviceRegisterRequest(
    val installationId: String,
    val deviceInfo: String? = null,
)

@Serializable
data class DeviceRegisterResponse(
    val userId: String,
    val deviceId: String,
    /** Returned once; client must store securely (Android Keystore). */
    val deviceSecret: String,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long,
)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long,
)

@Serializable
data class RevokeResponse(val revoked: Boolean = true)
