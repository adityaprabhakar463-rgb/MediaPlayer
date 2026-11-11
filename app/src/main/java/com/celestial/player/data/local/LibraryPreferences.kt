package com.celestial.player.data.local

import android.content.Context
import androidx.core.content.edit

/** Persists the user's chosen library folder across launches. */
class LibraryPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var musicFolderUri: String?
        get() = prefs.getString(KEY_FOLDER_URI, null)
        set(value) = prefs.edit {
            if (value != null) putString(KEY_FOLDER_URI, value) else remove(KEY_FOLDER_URI)
        }

    var musicFolderName: String?
        get() = prefs.getString(KEY_FOLDER_NAME, null)
        set(value) = prefs.edit {
            if (value != null) putString(KEY_FOLDER_NAME, value) else remove(KEY_FOLDER_NAME)
        }

    var introCompleted: Boolean
        get() = prefs.getBoolean(KEY_INTRO_COMPLETED, false)
        set(value) = prefs.edit { putBoolean(KEY_INTRO_COMPLETED, value) }

    private companion object {
        const val PREFS = "celestial_prefs"
        const val KEY_FOLDER_URI = "music_folder_uri"
        const val KEY_FOLDER_NAME = "music_folder_name"
        const val KEY_INTRO_COMPLETED = "intro_completed"
    }
}
