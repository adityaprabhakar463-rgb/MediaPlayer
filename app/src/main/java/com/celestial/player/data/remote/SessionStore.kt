package com.celestial.player.data.remote

import android.content.Context
import androidx.core.content.edit
import java.util.UUID

/**
 * Persists the anonymous device identity and token pair across launches.
 *
 * Tokens are kept in app-private preferences so the client survives process death without
 * re-registering every launch. For a production build, move the device secret and refresh token
 * to Android Keystore-backed storage as described in `docs/backend/05`.
 */
class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    val installationId: String = prefs.getString(KEY_INSTALLATION_ID, null)
        ?: UUID.randomUUID().toString().also {
            prefs.edit { putString(KEY_INSTALLATION_ID, it) }
        }

    val deviceSecret: String = prefs.getString(KEY_DEVICE_SECRET, null)
        ?: UUID.randomUUID().toString().also {
            prefs.edit { putString(KEY_DEVICE_SECRET, it) }
        }

    var accessToken: String? = prefs.getString(KEY_ACCESS_TOKEN, null)
        private set

    var refreshToken: String? = prefs.getString(KEY_REFRESH_TOKEN, null)
        private set

    var userId: String? = prefs.getString(KEY_USER_ID, null)
        private set

    var audiusApiKey: String? = prefs.getString(KEY_AUDIUS_API_KEY, null)
        private set

    @Synchronized
    fun saveAudiusApiKey(key: String?) {
        audiusApiKey = key?.trim()?.ifBlank { null }
        prefs.edit {
            if (audiusApiKey != null) putString(KEY_AUDIUS_API_KEY, audiusApiKey)
            else remove(KEY_AUDIUS_API_KEY)
        }
    }

    @Synchronized
    fun save(auth: AuthResponseDto) {
        accessToken = auth.accessToken
        refreshToken = auth.refreshToken
        userId = auth.userId
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, auth.accessToken)
            putString(KEY_REFRESH_TOKEN, auth.refreshToken)
            putString(KEY_USER_ID, auth.userId)
        }
    }

    @Synchronized
    fun clearTokens() {
        accessToken = null
        refreshToken = null
        userId = null
        prefs.edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_ID)
        }
    }

    private companion object {
        const val PREFS = "celestial_session"
        const val KEY_INSTALLATION_ID = "installation_id"
        const val KEY_DEVICE_SECRET = "device_secret"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_AUDIUS_API_KEY = "audius_api_key"
    }
}
