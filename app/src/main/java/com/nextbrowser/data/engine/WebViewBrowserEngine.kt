package com.nextbrowser.data.engine

import android.webkit.WebView
import android.webkit.WebViewClient
import com.nextbrowser.domain.engine.BrowserEngine

/**
 * Android System WebView adapter. Keep one instance per browser tab;
 * do not register this adapter as an application-wide singleton.
 */
class WebViewBrowserEngine(
    private val webView: WebView
) : BrowserEngine {
    private var destroyed = false

    override val currentUrl: String?
        get() = if (destroyed) null else runCatching { webView.url }.getOrNull()

    override val canGoBack: Boolean
        get() = !destroyed && runCatching { webView.canGoBack() }.getOrDefault(false)

    override val canGoForward: Boolean
        get() = !destroyed && runCatching { webView.canGoForward() }.getOrDefault(false)

    override fun loadUrl(url: String) {
        if (!destroyed) runCatching { webView.loadUrl(url) }
    }

    override fun goBack() {
        if (canGoBack) runCatching { webView.goBack() }
    }

    override fun goForward() {
        if (canGoForward) runCatching { webView.goForward() }
    }

    override fun reload() {
        if (!destroyed) runCatching { webView.reload() }
    }

    override fun stopLoading() {
        if (!destroyed) runCatching { webView.stopLoading() }
    }

    override fun destroy() {
        if (destroyed) return
        destroyed = true
        // Renderer shutdown can race with Activity/Compose disposal. Cleanup must
        // remain idempotent and must not crash the host app if WebView is already gone.
        runCatching { webView.stopLoading() }
        runCatching { webView.webChromeClient = null }
        runCatching { webView.webViewClient = WebViewClient() }
        runCatching { webView.destroy() }
    }
}
