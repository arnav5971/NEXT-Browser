package com.nextbrowser.ui.screens.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.SafeBrowsingResponse
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun BrowserScreen(
    initialUrl: String,
    onGoHome: () -> Unit,
    onShowTabs: () -> Unit,
    tabCount: Int,
    isPrivateTab: Boolean,
    onUrlChanged: (String) -> Unit,
    onTitleChanged: (String) -> Unit
) {
    val context = LocalContext.current
    var addressText by remember { mutableStateOf(initialUrl) }
    var pageTitle by remember { mutableStateOf("New tab") }
    var progress by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var pageError by remember { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    fun navigate(rawInput: String) {
        val target = resolveAddress(rawInput)
        addressText = target
        pageError = null
        webView?.loadUrl(target)
    }

    BackHandler(enabled = canGoBack) {
        webView?.goBack()
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                stopLoading()
                webChromeClient = null
                webViewClient = WebViewClient()
                destroy()
            }
            webView = null
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
                    webView = this
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
                                onUrlChanged(url)
                            }
                            pageError = null
                            progress = 0
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            if (!url.isNullOrBlank()) addressText = url
                            pageTitle = view?.title?.takeIf { it.isNotBlank() } ?: "Web page"
                            onTitleChanged(pageTitle)
                            progress = 100
                            isLoading = false
                            canGoBack = view?.canGoBack() == true
                            canGoForward = view?.canGoForward() == true
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                pageError = error?.description?.toString()
                                    ?: "The page could not be loaded."
                                isLoading = false
                            }
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress
                            isLoading = newProgress < 100
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            if (!title.isNullOrBlank()) {
                                pageTitle = title
                                onTitleChanged(title)
                            }
                        }
                    }

                    loadUrl(resolveAddress(initialUrl))
                }
            },
            update = { view ->
                webView = view
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
                    onClick = { webView?.goBack() },
                    enabled = canGoBack
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                IconButton(
                    onClick = { webView?.goForward() },
                    enabled = canGoForward
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Forward")
                }
                IconButton(
                    onClick = {
                        pageError = null
                        webView?.reload()
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reload")
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

private fun resolveAddress(input: String): String {
    val value = input.trim()
    if (value.isEmpty()) return "https://www.google.com"

    val hasScheme = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://").containsMatchIn(value)
    if (hasScheme) return value

    val looksLikeHost = !value.contains(" ") &&
        (value == "localhost" || value.contains(".") || value.startsWith("127.0.0.1"))

    return if (looksLikeHost) {
        "https://$value"
    } else {
        "https://www.google.com/search?q=${Uri.encode(value)}"
    }
}
