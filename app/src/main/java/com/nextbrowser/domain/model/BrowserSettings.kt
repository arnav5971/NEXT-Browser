package com.nextbrowser.domain.model

data class BrowserSettings(
    val themeMode: String = THEME_SYSTEM,
    val dynamicColor: Boolean = true,
    val accentColorHex: String = "",
    val searchEngine: String = SEARCH_ENGINE_GOOGLE
) {
    companion object {
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"

        const val SEARCH_ENGINE_GOOGLE = "google"
        const val SEARCH_ENGINE_BING = "bing"
        const val SEARCH_ENGINE_DUCKDUCKGO = "duckduckgo"
    }
}
