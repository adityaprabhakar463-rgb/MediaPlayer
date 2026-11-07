package app.celestial.backend.config

data class AppConfig(
    val port: Int = envInt("PORT", 8080),
    val databaseUrl: String = env("DATABASE_URL", "jdbc:postgresql://localhost:5432/celestial"),
    val databaseUser: String = env("DATABASE_USER", "celestial"),
    val databasePassword: String = env("DATABASE_PASSWORD", "celestial"),
    val jwtIssuer: String = env("JWT_ISSUER", "https://celestial.app"),
    val jwtAudience: String = env("JWT_AUDIENCE", "celestial-api"),
    val jwtAccessSecret: String = env("JWT_ACCESS_SECRET", "dev-access-secret-change-me"),
    val jwtRefreshSecret: String = env("JWT_REFRESH_SECRET", "dev-refresh-secret-change-me"),
    val jwtAccessTtlMinutes: Long = envLong("JWT_ACCESS_TTL_MINUTES", 15),
    val jwtRefreshTtlDays: Long = envLong("JWT_REFRESH_TTL_DAYS", 30),
) {
    companion object {
        fun fromEnv(): AppConfig = AppConfig()
    }
}

private fun env(name: String, default: String): String = System.getenv(name)?.takeIf { it.isNotBlank() } ?: default
private fun envInt(name: String, default: Int): Int = env(name, default.toString()).toIntOrNull() ?: default
private fun envLong(name: String, default: Long): Long = env(name, default.toString()).toLongOrNull() ?: default
