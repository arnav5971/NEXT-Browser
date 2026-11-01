package com.nextbrowser.ui.screens.browser

import android.graphics.Bitmap
import android.os.Bundle
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
import java.net.URLEncoder
import kotlinx.coroutines.delay
import com.nextbrowser.data.engine.WebViewBrowserEngine
import com.nextbrowser.domain.engine.BrowserEngine

@Composable
fun BrowserScreen(
    initialUrl: String,
    searchEngine: String = com.nextbrowser.domain.model.BrowserSettings.SEARCH_ENGINE_GOOGLE,
    onGoHome: () -> Unit,
    onShowTabs: () -> Unit,
    tabCount: Int,
    isPrivateTab: Boolean,
    desktopSite: Boolean = false,
    onUrlChanged: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onPageVisited: (String, String) -> Unit,
    onAddBookmark: (String, String) -> Unit,
    onDownloadRequested: (String, String?, String?, String?, Long) -> Unit,
    onShowDownloads: () -> Unit,
    savedWebViewState: Bundle? = null,
    onSaveWebViewState: (Bundle, String?) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
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
                    state.putBoolean("next_browser_desktop_site", desktopSite)
                    currentSaveWebViewStateHandler(state, view.url)
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

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            factory = {
                WebView(context).apply {
                    webViewInstance = this
                    val engine = WebViewBrowserEngine(this)
                    browserEngine = engine
                    val defaultAgent = WebSettings.getDefaultUserAgent(context)
                    val chromeVersion = Regex("Chrome/([0-9.]+)")
                        .find(defaultAgent)?.groupValues?.getOrNull(1) ?: "120.0.0.0"
                    settings.userAgentString = if (desktopSite) {
                        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVersion Safari/537.36"
                    } else {
                        defaultAgent.replace("; wv", "").replace("Version/4.0 ", "")
                    }
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = desktopSite
                    settings.textZoom = 100
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadsImagesAutomatically = true
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    settings.setSupportMultipleWindows(false)
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.mixedContentMode =
                        android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    settings.safeBrowsingEnabled = true
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false

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
                            currentPageVisitedHandler(url ?: addressText, pageTitle)
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
                            pageError = "The web page renderer stopped. Reload the page to try again."
                            isLoading = false
                            return true
                        }
                    }

                    setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
                        if (url.startsWith("https://", ignoreCase = true) || url.startsWith("http://", ignoreCase = true)) {
                            if (!currentIsPrivateTab) currentDownloadHandler(url, userAgent, contentDisposition, mimeType, contentLength)
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress
                            // Do not let late progress callbacks undo a terminal load error.
                            if (pageError == null) {
                                isLoading = newProgress < 100
                            }
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            if (!title.isNullOrBlank()) {
                                pageTitle = title
                                currentTitleChangeHandler(title)
                            }
                        }
                    }

                    val compatibleState = savedWebViewState?.takeIf {
                        it.getBoolean("next_browser_desktop_site", false) == desktopSite
                    }
                    val restoredState = compatibleState?.let { restoreState(it) }
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

internal fun resolveAddress(
    input: String,
    searchEngine: String = com.nextbrowser.domain.model.BrowserSettings.SEARCH_ENGINE_GOOGLE
): String {
    val value = input.trim()
    if (value.isEmpty()) return "https://www.google.com"

    if (value.startsWith("https://", ignoreCase = true) ||
        value.startsWith("http://", ignoreCase = true)
    ) return value

    // Local development endpoints commonly do not provide TLS certificates.
    val isLocalHost = value.equals("localhost", ignoreCase = true) ||
        value.startsWith("localhost:", ignoreCase = true) ||
        value.equals("10.0.2.2", ignoreCase = true) ||
        value.startsWith("10.0.2.2:", ignoreCase = true)
    val looksLikeHost = !value.contains(" ") &&
        (isLocalHost || HOST_WITH_OPTIONAL_PORT.matches(value))

    if (looksLikeHost) {
        return "${if (isLocalHost) "http" else "https"}://$value"
    }

    val encodedQuery = URLEncoder.encode(value, "UTF-8").replace("+", "%20")
    val query = when (searchEngine.lowercase()) {
        com.nextbrowser.domain.model.BrowserSettings.SEARCH_ENGINE_BING ->
            "https://www.bing.com/search?q=$encodedQuery"
        com.nextbrowser.domain.model.BrowserSettings.SEARCH_ENGINE_DUCKDUCKGO ->
            "https://duckduckgo.com/?q=$encodedQuery"
        else -> "https://www.google.com/search?q=$encodedQuery"
    }
    return query
}

private val HOST_WITH_OPTIONAL_PORT = Regex(
    "^(?=.{1,253}$)(?:" +
        // Accept valid dotted IPv4 addresses as hosts, not search terms.
        "(?:25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])" +
        "(?:\\.(?:25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}" +
        "|" +
        // Or a conventional domain name with a valid-looking alphabetic TLD.
        "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}" +
        ")(?::[0-9]{1,5})?(?:/[^\\s]*)?$"
)
