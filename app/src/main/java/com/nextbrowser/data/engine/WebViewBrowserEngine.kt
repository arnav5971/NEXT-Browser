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
    override val currentUrl: String?
        get() = webView.url

    override val canGoBack: Boolean
        get() = webView.canGoBack()

    override val canGoForward: Boolean
        get() = webView.canGoForward()

    override fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    override fun goBack() {
        if (webView.canGoBack()) webView.goBack()
    }

    override fun goForward() {
        if (webView.canGoForward()) webView.goForward()
    }

    override fun reload() {
        webView.reload()
    }

    override fun stopLoading() {
        webView.stopLoading()
    }

    override fun destroy() {
        webView.stopLoading()
        webView.webChromeClient = null
        webView.webViewClient = WebViewClient()
        webView.destroy()
    }
}
