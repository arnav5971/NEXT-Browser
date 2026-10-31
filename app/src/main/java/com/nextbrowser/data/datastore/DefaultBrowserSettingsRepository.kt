package com.nextbrowser.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nextbrowser.domain.model.BrowserSettings
import com.nextbrowser.domain.repository.BrowserSettingsRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DefaultBrowserSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : BrowserSettingsRepository {

    override val settings: Flow<BrowserSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            BrowserSettings(
                themeMode = preferences[Keys.THEME_MODE] ?: BrowserSettings.THEME_SYSTEM,
                dynamicColor = preferences[Keys.DYNAMIC_COLOR] ?: true,
                accentColorHex = preferences[Keys.ACCENT_COLOR].orEmpty(),
                searchEngine = preferences[Keys.SEARCH_ENGINE] ?: BrowserSettings.SEARCH_ENGINE_GOOGLE
            )
        }

    override suspend fun setThemeMode(mode: String) {
        require(mode in setOf(
            BrowserSettings.THEME_SYSTEM,
            BrowserSettings.THEME_LIGHT,
            BrowserSettings.THEME_DARK
        )) { "Unsupported theme mode: $mode" }
        dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    override suspend fun setAccentColor(hexColor: String) {
        require(hexColor.isEmpty() || HEX_COLOR.matches(hexColor)) {
            "Accent color must be an empty string or a #RRGGBB/#AARRGGBB value"
        }
        dataStore.edit { it[Keys.ACCENT_COLOR] = hexColor.uppercase() }
    }

    override suspend fun setSearchEngine(engine: String) {
        require(engine in setOf(
            BrowserSettings.SEARCH_ENGINE_GOOGLE,
            BrowserSettings.SEARCH_ENGINE_BING,
            BrowserSettings.SEARCH_ENGINE_DUCKDUCKGO
        )) { "Unsupported search engine: $engine" }
        dataStore.edit { it[Keys.SEARCH_ENGINE] = engine }
    }

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
    }

    private companion object {
        val HEX_COLOR = Regex("^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$")
    }
}
