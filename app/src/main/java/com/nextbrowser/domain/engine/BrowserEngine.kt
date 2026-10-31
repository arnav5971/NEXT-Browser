package com.nextbrowser.domain.engine

/**
 * Platform-independent navigation contract for a single browser tab.
 * Implementations must be scoped to one WebView/tab instance.
 */
interface BrowserEngine {
    val currentUrl: String?
    val canGoBack: Boolean
    val canGoForward: Boolean

    fun loadUrl(url: String)
    fun goBack()
    fun goForward()
    fun reload()
    fun stopLoading()
    fun destroy()
}
