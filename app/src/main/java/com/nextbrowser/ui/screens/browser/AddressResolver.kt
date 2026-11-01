package com.nextbrowser.ui.screens.browser

import com.nextbrowser.domain.model.BrowserSettings
import java.net.URLEncoder
import java.util.Locale

/**
 * Resolves address-bar input consistently for both the New Tab search fields
 * and the browser toolbar. Plain text is sent to the user's selected search
 * provider; URL-like input is opened directly.
 */
internal fun resolveAddress(
    input: String,
    searchEngine: String = BrowserSettings.SEARCH_ENGINE_GOOGLE
): String {
    val value = input.trim()
    if (value.isEmpty()) return GOOGLE_HOME

    if (value.startsWith("https://", ignoreCase = true) ||
        value.startsWith("http://", ignoreCase = true)
    ) {
        return value
    }

    val isLocalHost = LOCAL_HOST.matches(value)
    val looksLikeHost = !value.any { it.isWhitespace() } &&
        (isLocalHost || HOST_WITH_OPTIONAL_PORT.matches(value))

    if (looksLikeHost) {
        return "${if (isLocalHost) "http" else "https"}://$value"
    }

    val encodedQuery = URLEncoder.encode(value, Charsets.UTF_8.name())
        .replace("+", "%20")
    val provider = when (searchEngine.lowercase(Locale.ROOT)) {
        BrowserSettings.SEARCH_ENGINE_BING -> BING_SEARCH
        BrowserSettings.SEARCH_ENGINE_DUCKDUCKGO -> DUCKDUCKGO_SEARCH
        else -> GOOGLE_SEARCH
    }
    return "$provider$encodedQuery"
}

private const val GOOGLE_HOME = "https://www.google.com"
private const val GOOGLE_SEARCH = "https://www.google.com/search?q="
private const val BING_SEARCH = "https://www.bing.com/search?q="
private const val DUCKDUCKGO_SEARCH = "https://duckduckgo.com/?q="

private val LOCAL_HOST = Regex(
    "^(?:localhost|10\\.0\\.2\\.2)(?::[0-9]{1,5})?(?:[/?#][^\\s]*)?$",
    RegexOption.IGNORE_CASE
)

private val HOST_WITH_OPTIONAL_PORT = Regex(
    "^(?=.{1,253}$)(?:" +
        "(?:25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])" +
        "(?:\\.(?:25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}" +
        "|" +
        "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}" +
        ")(?::[0-9]{1,5})?(?:[/?#][^\\s]*)?$"
)
