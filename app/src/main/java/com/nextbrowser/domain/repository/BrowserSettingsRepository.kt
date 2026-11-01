package com.nextbrowser.domain.repository

import com.nextbrowser.domain.model.BrowserSettings
import kotlinx.coroutines.flow.Flow

interface BrowserSettingsRepository {
    val settings: Flow<BrowserSettings>
    suspend fun setThemeMode(mode: String)
    suspend fun setDynamicColorEnabled(enabled: Boolean)
    suspend fun setAccentColor(hexColor: String)
    suspend fun setSearchEngine(engine: String)
    suspend fun setDesktopSiteEnabled(enabled: Boolean)
}
