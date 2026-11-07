package app.celestial.backend.auth

import de.mkammerer.argon2.Argon2Factory
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object Security {
    private val random = SecureRandom()
    private val argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)

    fun randomToken(bytes: Int = 32): String {
        val buf = ByteArray(bytes)
        random.nextBytes(buf)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf)
    }

    /** For high-entropy tokens/secrets (refresh tokens, device secrets). */
    fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /** For low-entropy passwords (upgrade/link flow). */
    fun hashPassword(password: CharArray): String = argon2.hash(3, 65536, 1, password)

    fun verifyPassword(hash: String, password: CharArray): Boolean =
        try { argon2.verify(hash, password) } finally { argon2.wipeArray(password) }
}
