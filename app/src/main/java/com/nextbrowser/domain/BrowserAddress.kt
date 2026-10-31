package com.nextbrowser.domain

import java.net.URLEncoder

/** Pure address resolver: explicit schemes are preserved, hosts default to HTTPS, text is searched. */
object BrowserAddress {
    fun resolve(input: String, provider: SearchProvider = SearchProvider.GOOGLE): String {
        val value = input.trim()
        if (value.isEmpty()) return ""
        if (value.startsWith("https://", true) || value.startsWith("http://", true)) return value
        if (looksLikeHost(value)) return "https://$value"
        val query = URLEncoder.encode(value, Charsets.UTF_8.name())
        return when (provider) {
            SearchProvider.GOOGLE -> "https://www.google.com/search?q=$query"
            SearchProvider.BING -> "https://www.bing.com/search?q=$query"
            SearchProvider.DUCKDUCKGO -> "https://duckduckgo.com/?q=$query"
        }
    }

    private fun looksLikeHost(value: String): Boolean {
        if (value.any(Char::isWhitespace)) return false
        val authority = value.substringBefore('/').substringBefore('?').substringBefore('#')
        val host = if (authority.count { it == ':' } == 1) authority.substringBeforeLast(':') else authority
        if (host.equals("localhost", true)) return true
        if (host.matches(Regex("""(?:[0-9]{1,3}\.){3}[0-9]{1,3}"""))) {
            return host.split('.').all { octet -> octet.toIntOrNull()?.let { it in 0..255 } == true }
        }
        return host.contains('.') && host.split('.').all { label ->
            label.isNotEmpty() && label.length <= 63 &&
                label.first().isLetterOrDigit() && label.last().isLetterOrDigit() &&
                label.all { it.isLetterOrDigit() || it == '-' }
        }
    }
}

enum class SearchProvider { GOOGLE, BING, DUCKDUCKGO }
