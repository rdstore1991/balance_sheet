package com.mfstracker.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tracker_settings")

class ThemePreferences(private val context: Context) {

    private companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
    }

    /**
     * Emits null if the user has not manually chosen a theme yet (allowing system default),
     * or true/false once toggled.
     */
    val isDarkModeFlow: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        prefs[DARK_MODE_KEY]
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = enabled
        }
    }
}

