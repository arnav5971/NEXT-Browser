package com.nextbrowser.data.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.nextbrowser.domain.model.BrowserSettings
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DefaultBrowserSettingsRepositoryTest {

    @Test
    fun persistsAndEmitsBrowserSettings() = runBlocking {
        val file = File.createTempFile("next-browser-settings", ".preferences_pb")
        file.delete()
        val dataStore = PreferenceDataStoreFactory.create(produceFile = { file })
        val repository = DefaultBrowserSettingsRepository(dataStore)

        repository.setThemeMode(BrowserSettings.THEME_DARK)
        repository.setDynamicColorEnabled(false)
        repository.setAccentColor("#315CDB")
        repository.setSearchEngine(BrowserSettings.SEARCH_ENGINE_DUCKDUCKGO)

        val actual = repository.settings.first()

        assertEquals(BrowserSettings.THEME_DARK, actual.themeMode)
        assertEquals(false, actual.dynamicColor)
        assertEquals("#315CDB", actual.accentColorHex)
        assertEquals(BrowserSettings.SEARCH_ENGINE_DUCKDUCKGO, actual.searchEngine)

        file.delete()
    }

    @Test
    fun rejectsUnsupportedThemeMode() {
        val file = File.createTempFile("next-browser-settings-invalid", ".preferences_pb")
        file.delete()
        val repository = DefaultBrowserSettingsRepository(
            PreferenceDataStoreFactory.create(produceFile = { file })
        )

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.setThemeMode("sepia") }
        }
        file.delete()
    }

    @Test
    fun rejectsMalformedAccentColor() {
        val file = File.createTempFile("next-browser-accent-invalid", ".preferences_pb")
        file.delete()
        val repository = DefaultBrowserSettingsRepository(
            PreferenceDataStoreFactory.create(produceFile = { file })
        )

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.setAccentColor("blue") }
        }
        file.delete()
    }
}
