package com.nextbrowser.ui.screens.browser

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import com.nextbrowser.data.engine.WebViewBrowserEngine
import com.nextbrowser.domain.engine.BrowserEngine

@Composable
fun BrowserScreen(
    initialUrl: String,
    searchEngine: String = com.nextbrowser.domain.model.BrowserSettings.SEARCH_ENGINE_GOOGLE,
    desktopMode: Boolean,
    onToggleDesktopMode: () -> Unit,
    onGoHome: () -> Unit,
    onShowTabs: () -> Unit,
    tabCount: Int,
    isPrivateTab: Boolean,
    onUrlChanged: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onPageVisited: (String, String) -> Unit,
    onAddBookmark: (String, String) -> Unit,
    onDownloadRequested: (String, String?, String?, String?, Long) -> Unit,
    onShowDownloads: () -> Unit,
    savedWebViewState: Bundle? = null,
    onSaveWebViewState: (Bundle, String?, Boolean) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val currentIsPrivateTab by rememberUpdatedState(isPrivateTab)
    val currentDownloadHandler by rememberUpdatedState(onDownloadRequested)
    val currentUrlChangeHandler by rememberUpdatedState(onUrlChanged)
    val currentTitleChangeHandler by rememberUpdatedState(onTitleChanged)
    val currentPageVisitedHandler by rememberUpdatedState(onPageVisited)
    val currentSaveWebViewStateHandler by rememberUpdatedState(onSaveWebViewState)
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var addressText by remember { mutableStateOf(initialUrl) }
    var pageTitle by remember { mutableStateOf("New tab") }
    var progress by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var pageError by remember { mutableStateOf<String?>(null) }
    var rendererGeneration by remember { mutableStateOf(0) }
    var browserEngine by remember { mutableStateOf<BrowserEngine?>(null) }

    // A tab switch can change initialUrl while this composable stays in place.
    // Load the selected tab's URL without reloading the page currently navigating.
    LaunchedEffect(initialUrl) {
        val targetUrl = resolveAddress(initialUrl, searchEngine)
        val engine = browserEngine
        if (engine != null && engine.currentUrl != targetUrl) {
            addressText = targetUrl
            engine.loadUrl(targetUrl)
        }
    }

    // One watchdog per actual loading attempt. It is cancelled when loading ends
    // and restarted when the address or loading state changes.
    LaunchedEffect(isLoading, addressText) {
        if (isLoading && addressText.isNotBlank() && pageError == null) {
            delay(25_000)
            if (isLoading && pageError == null) {
                pageError = "The page is taking too long to respond. Check your connection and tap Reload."
                isLoading = false
                browserEngine?.stopLoading()
            }
        }
    }

    fun navigate(rawInput: String) {
        val target = resolveAddress(rawInput, searchEngine)
        addressText = target
        pageError = null
        browserEngine?.loadUrl(target)
    }

    BackHandler(enabled = canGoBack) {
        browserEngine?.goBack()
    }

    DisposableEffect(browserEngine) {
        val engine = browserEngine
        onDispose {
            if (engine != null) {
                webViewInstance?.let { view ->
                    val state = Bundle()
                    view.saveState(state)
                    currentSaveWebViewStateHandler(state, view.url, desktopMode)
                }
                engine.destroy()
                webViewInstance = null
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onGoHome) {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }

            OutlinedTextField(
                value = addressText,
                onValueChange = { addressText = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                placeholder = { Text("Search or enter address") },
                leadingIcon = {
                    Icon(
                        imageVector = if (addressText.startsWith("https://")) {
                            Icons.Default.Lock
                        } else {
                            Icons.Default.Search
                        },
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(
                    onGo = { navigate(addressText) }
                )
            )
            IconButton(
                onClick = { navigate(addressText) },
                enabled = addressText.isNotBlank()
            ) {
                Icon(Icons.Default.Search, contentDescription = "Go")
            }
            IconButton(onClick = { browserEngine?.currentUrl?.let { onAddBookmark(it, pageTitle) } }, enabled = browserEngine?.currentUrl?.startsWith("http") == true && !isPrivateTab) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = if (isPrivateTab) "Bookmarks unavailable in private tabs" else "Add bookmark")
            }
            IconButton(onClick = onToggleDesktopMode) {
                Icon(
                    imageVector = if (desktopMode) Icons.Default.Smartphone else Icons.Default.Computer,
                    contentDescription = if (desktopMode) "Switch to mobile site" else "Request desktop site"
                )
            }
            IconButton(onClick = onShowTabs) {
                Icon(Icons.Default.Tab, contentDescription = "Open tabs")
            }
            Text(
                text = tabCount.toString() + if (isPrivateTab) " · Private" else "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isLoading) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )
        }

        key(rendererGeneration) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            factory = {
                WebView(context).apply {
                    webViewInstance = this
                    val engine = WebViewBrowserEngine(this)
                    browserEngine = engine
                    // Apply the selected site identity before restoring state or loading a URL.
                    val defaultUserAgent = WebSettings.getDefaultUserAgent(context)
                    settings.userAgentString = browserModeUserAgent(defaultUserAgent, desktopMode)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadsImagesAutomatically = true
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    settings.setSupportMultipleWindows(false)
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    settings.safeBrowsingEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = desktopMode
                    settings.textZoom = 100
                    settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean = false

                        override fun onPageStarted(
                            view: WebView?,
                            url: String?,
                            favicon: Bitmap?
                        ) {
                            super.onPageStarted(view, url, favicon)
                            if (!url.isNullOrBlank()) {
                                addressText = url
                                currentUrlChangeHandler(url)
                            }
                            pageError = null
                            progress = 0
                            isLoading = true
                        }

                        override fun onPageCommitVisible(view: WebView?, url: String?) {
                            super.onPageCommitVisible(view, url)
                            // The main document has started rendering; avoid a stale loading indicator.
                            if (!url.isNullOrBlank()) {
                                addressText = url
                                currentUrlChangeHandler(url)
                            }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            if (!url.isNullOrBlank()) addressText = url
                            // Errors reported before onPageFinished (HTTP/SSL/main-frame failures)
                            // must remain visible until a new navigation successfully commits.
                            pageTitle = view?.title?.takeIf { it.isNotBlank() } ?: "Web page"
                            currentTitleChangeHandler(pageTitle)
                            // onPageFinished may fire for subframes too. Only the top-level
                            // document belongs in the browser's address bar and history.
                            if (!url.isNullOrBlank() && view?.url == url) {
                                currentPageVisitedHandler(url, pageTitle)
                            }
                            progress = 100
                            isLoading = false
                            canGoBack = browserEngine?.canGoBack == true
                            canGoForward = browserEngine?.canGoForward == true
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                pageError = "Network error: ${error?.description ?: "The page could not be loaded."}"
                                isLoading = false
                            }
                        }

                        override fun onReceivedHttpError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            errorResponse: android.webkit.WebResourceResponse?
                        ) {
                            super.onReceivedHttpError(view, request, errorResponse)
                            if (request?.isForMainFrame == true) {
                                pageError = "Website returned HTTP ${errorResponse?.statusCode ?: "error"}."
                                isLoading = false
                            }
                        }

                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: android.webkit.SslErrorHandler?,
                            error: android.net.http.SslError?
                        ) {
                            handler?.cancel()
                            pageError = "Secure connection failed. Check your device date, time, and network."
                            isLoading = false
                        }

                        override fun onRenderProcessGone(
                            view: WebView?,
                            detail: android.webkit.RenderProcessGoneDetail?
                        ): Boolean {
                            pageError = "The web page renderer stopped. Reopening the page…"
                            isLoading = false
                            // A WebView whose renderer has exited cannot be reliably reused.
                            // Remove it and let Compose create a fresh WebView for this tab.
                            webViewInstance = null
                            browserEngine = null
                            rendererGeneration += 1
                            return true
                        }
                    }

                    setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
                        if (url.startsWith("https://", ignoreCase = true) || url.startsWith("http://", ignoreCase = true)) {
                            if (!currentIsPrivateTab) currentDownloadHandler(url, userAgent, contentDisposition, mimeType, contentLength)
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        private var customView: View? = null
                        private var customViewCallback: CustomViewCallback? = null

                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress
                            if (pageError == null) isLoading = newProgress < 100
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            if (!title.isNullOrBlank()) {
                                pageTitle = title
                                currentTitleChangeHandler(title)
                            }
                        }

                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            val host = activity?.window?.decorView as? FrameLayout
                            if (view == null || host == null || customView != null) {
                                callback?.onCustomViewHidden()
                                return
                            }
                            customView = view
                            customViewCallback = callback
                            host.addView(view, FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            ))
                            webViewInstance?.visibility = View.GONE
                            activity?.window?.setFlags(
                                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                WindowManager.LayoutParams.FLAG_FULLSCREEN
                            )
                        }

                        override fun onHideCustomView() {
                            val host = activity?.window?.decorView as? FrameLayout
                            customView?.let { host?.removeView(it) }
                            customView = null
                            webViewInstance?.visibility = View.VISIBLE
                            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                            customViewCallback?.onCustomViewHidden()
                            customViewCallback = null
                        }
                    }

                    val restoredState = savedWebViewState?.let { restoreState(it) }
                    if (restoredState == null) {
                        engine.loadUrl(resolveAddress(initialUrl, searchEngine))
                    } else {
                        url?.let { restoredUrl ->
                            addressText = restoredUrl
                            currentUrlChangeHandler(restoredUrl)
                        }
                        title?.takeIf { it.isNotBlank() }?.let { restoredTitle ->
                            pageTitle = restoredTitle
                            currentTitleChangeHandler(restoredTitle)
                        }
                        canGoBack = canGoBack()
                        canGoForward = canGoForward()
                    }
                }
            }
        )
        }

        if (pageError != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Couldn't load page: $pageError",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { browserEngine?.goBack() },
                    enabled = canGoBack
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                IconButton(
                    onClick = { browserEngine?.goForward() },
                    enabled = canGoForward
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward")
                }
                IconButton(
                    onClick = {
                        if (isLoading) {
                            browserEngine?.stopLoading()
                            isLoading = false
                        } else {
                            pageError = null
                            browserEngine?.reload()
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isLoading) Icons.Default.Stop else Icons.Default.Refresh,
                        contentDescription = if (isLoading) "Stop loading" else "Reload"
                    )
                }
                IconButton(onClick = onShowDownloads, enabled = !isPrivateTab) {
                    Icon(Icons.Default.Download, contentDescription = if (isPrivateTab) "Downloads unavailable in private tabs" else "Downloads")
                }
                Text(
                    text = pageTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

internal fun browserModeUserAgent(defaultUserAgent: String, desktopMode: Boolean): String {
    if (!desktopMode) {
        return defaultUserAgent
            .replace("; wv)", ")")
            .replace("Version/4.0 ", "")
    }
    val chromeVersion = Regex("Chrome/([0-9.]+)")
        .find(defaultUserAgent)
        ?.groupValues
        ?.getOrNull(1)
        ?: return defaultUserAgent
    return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVersion Safari/537.36"
}
