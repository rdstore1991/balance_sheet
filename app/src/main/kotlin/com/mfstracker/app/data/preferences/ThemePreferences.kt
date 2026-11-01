package com.mfstracker.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tracker_settings")

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class CurrencyFormat(
    val prefix: String,
    val label: String,
    val sample: String
) {
    TAKA_SYMBOL(prefix = "৳", label = "Symbol (৳)", sample = "৳ 12,500.00"),
    BDT_CODE(prefix = "BDT", label = "Code (BDT)", sample = "BDT 12,500.00");

    companion object {
        fun fromStored(raw: String?): CurrencyFormat {
            return entries.firstOrNull { it.name == raw } ?: TAKA_SYMBOL
        }
    }
}

class ThemePreferences(private val context: Context) {

    private companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
        val CURRENCY_FORMAT_KEY = stringPreferencesKey("currency_format")
    }

    /**
     * Emits null if set to System Default, or true/false when Dark or Light mode is chosen.
     */
    val isDarkModeFlow: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        prefs[DARK_MODE_KEY]
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[DARK_MODE_KEY]) {
            true -> ThemeMode.DARK
            false -> ThemeMode.LIGHT
            null -> ThemeMode.SYSTEM
        }
    }

    val currencyFormatFlow: Flow<CurrencyFormat> = context.dataStore.data.map { prefs ->
        CurrencyFormat.fromStored(prefs[CURRENCY_FORMAT_KEY])
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            when (mode) {
                ThemeMode.SYSTEM -> prefs.remove(DARK_MODE_KEY)
                ThemeMode.LIGHT -> prefs[DARK_MODE_KEY] = false
                ThemeMode.DARK -> prefs[DARK_MODE_KEY] = true
            }
        }
    }

    suspend fun setCurrencyFormat(format: CurrencyFormat) {
        context.dataStore.edit { prefs ->
            prefs[CURRENCY_FORMAT_KEY] = format.name
        }
    }
}
