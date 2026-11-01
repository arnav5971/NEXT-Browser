package com.nextbrowser

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONArray
import org.json.JSONObject

private const val STORE = "next_browser"
private const val BOOKMARKS = "bookmarks"
private const val HISTORY = "history"

data class TabState(val id: Int, val url: String = "", val title: String = "New tab", val privateMode: Boolean = false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BrowserApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowserApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = remember { context.getSharedPreferences(STORE, Context.MODE_PRIVATE) }
    var tabs by remember { mutableStateOf(listOf(TabState(1))) }
    var activeId by remember { mutableIntStateOf(1) }
    var nextId by remember { mutableIntStateOf(2) }
    var address by remember { mutableStateOf("") }
    var currentUrl by remember { mutableStateOf("") }
    var currentTitle by remember { mutableStateOf("NEXT Browser") }
    var progress by remember { mutableIntStateOf(0) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var menu by remember { mutableStateOf(false) }
    var panel by remember { mutableStateOf("") }
    var dark by remember { mutableStateOf(store.getBoolean("dark", false)) }
    var desktop by remember { mutableStateOf(store.getBoolean("desktop", false)) }
    var bookmarks by remember { mutableStateOf(readEntries(store.getString(BOOKMARKS, "[]").orEmpty())) }
    var history by remember { mutableStateOf(readEntries(store.getString(HISTORY, "[]").orEmpty())) }
    val active = tabs.firstOrNull { it.id == activeId } ?: tabs.first()

    fun persist(key: String, entries: List<Pair<String, String>>) {
        store.edit().putString(key, JSONArray().apply {
            entries.forEach { (title, url) -> put(JSONObject().put("title", title).put("url", url)) }
        }.toString()).apply()
    }
    fun saveEntry(key: String, title: String, url: String) {
        if (url.isBlank() || active.privateMode) return
        val source = if (key == BOOKMARKS) bookmarks else history
        val updated = (listOf(title to url) + source.filterNot { it.second == url }).take(300)
        if (key == BOOKMARKS) bookmarks = updated else history = updated
        persist(key, updated)
    }
    fun navigate(input: String) {
        val q = input.trim()
        if (q.isBlank()) return
        val target = when {
            q.startsWith("https://", true) || q.startsWith("http://", true) -> q
            q.contains(".") && !q.contains(" ") -> "https://$q"
            else -> "https://www.google.com/search?q=" + Uri.encode(q)
        }
        address = target
        currentUrl = target
        currentTitle = target
        tabs = tabs.map { if (it.id == activeId) it.copy(url = target, title = target) else it }
        panel = ""
        webView?.loadUrl(target)
    }
    fun newTab(privateMode: Boolean = false) {
        val t = TabState(nextId++, privateMode = privateMode)
        tabs = tabs + t
        activeId = t.id
        address = ""
        currentUrl = ""
        currentTitle = "NEXT Browser"
        progress = 0
        webView = null
        panel = ""
    }
    fun switchTheme() {
        dark = !dark
        store.edit().putBoolean("dark", dark).apply()
    }
    fun switchDesktop() {
        desktop = !desktop
        store.edit().putBoolean("desktop", desktop).apply()
        // Recreate the WebView so the selected User-Agent is active before navigation.
        webView = null
        progress = 0
    }

    BackHandler(enabled = panel.isNotEmpty() || webView?.canGoBack() == true) {
        if (panel.isNotEmpty()) panel = "" else webView?.goBack()
    }

    val scheme = if (dark) darkColorScheme(
        primary = Color(0xFF9B8CFF),
        secondary = Color(0xFF64D8CB),
        surface = Color(0xFF171923),
        background = Color(0xFF10121A)
    ) else lightColorScheme(
        primary = Color(0xFF5546D7),
        secondary = Color(0xFF087F8C),
        surface = Color(0xFFFFFFFF),
        background = Color(0xFFF5F6FC)
    )

    MaterialTheme(colorScheme = scheme) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (panel.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { panel = "" }) { Icon(Icons.Default.ArrowBack, "Back") }
                        Text(panel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        if (panel == "History") TextButton(onClick = { history = emptyList(); persist(HISTORY, emptyList()) }) { Text("Clear") }
                    }
                } else {
                    Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                        Row(
                            Modifier.fillMaxWidth().padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(38.dp).clip(RoundedCornerShape(13.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF7667F5), Color(0xFF19B6B0)))),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.Language, null, tint = Color.White) }
                            Spacer(Modifier.width(10.dp))
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(18.dp),
                                placeholder = { Text("Search or enter address", fontSize = 14.sp) },
                                leadingIcon = { Icon(if (currentUrl.startsWith("https://")) Icons.Default.Lock else Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                                trailingIcon = {
                                    if (address.isNotBlank()) IconButton(onClick = { address = ""; if (currentUrl.isBlank()) Unit }) { Icon(Icons.Default.Close, "Clear") }
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                keyboardActions = KeyboardActions(onGo = { navigate(address) }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = .25f),
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Box {
                                IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "More options") }
                                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                    DropdownMenuItem(text = { Text(if (desktop) "Switch to mobile site" else "Desktop site") }, onClick = { menu = false; switchDesktop() }, leadingIcon = { Icon(Icons.Default.Computer, null) })
                                    DropdownMenuItem(text = { Text(if (dark) "Light appearance" else "Dark appearance") }, onClick = { menu = false; switchTheme() }, leadingIcon = { Icon(Icons.Default.DarkMode, null) })
                                    DropdownMenuItem(text = { Text("Add bookmark") }, onClick = { menu = false; saveEntry(BOOKMARKS, currentTitle, currentUrl); Toast.makeText(context, "Bookmark saved", Toast.LENGTH_SHORT).show() }, leadingIcon = { Icon(Icons.Default.BookmarkAdd, null) })
                                    DropdownMenuItem(text = { Text("Share page") }, onClick = { menu = false; if (currentUrl.isNotBlank()) context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, currentUrl), "Share page")) }, leadingIcon = { Icon(Icons.Default.Share, null) })
                                    DropdownMenuItem(text = { Text("Bookmarks") }, onClick = { menu = false; panel = "Bookmarks" }, leadingIcon = { Icon(Icons.Default.Bookmarks, null) })
                                    DropdownMenuItem(text = { Text("History") }, onClick = { menu = false; panel = "History" }, leadingIcon = { Icon(Icons.Default.History, null) })
                                    DropdownMenuItem(text = { Text("Downloads") }, onClick = { menu = false; panel = "Downloads" }, leadingIcon = { Icon(Icons.Default.Download, null) })
                                    DropdownMenuItem(text = { Text("Privacy") }, onClick = { menu = false; panel = "Privacy" }, leadingIcon = { Icon(Icons.Default.Shield, null) })
                                }
                            }
                        }
                        if (progress in 1..99) LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth().height(2.dp))
                    }
                }
            },
            bottomBar = {
                if (panel.isEmpty()) BrowserBottomBar(
                    canBack = webView?.canGoBack() == true,
                    canForward = webView?.canGoForward() == true,
                    tabCount = tabs.size,
                    isHome = currentUrl.isBlank(),
                    onBack = { webView?.goBack() },
                    onForward = { webView?.goForward() },
                    onReload = { if (currentUrl.isBlank()) { address = "" } else webView?.reload() },
                    onHome = { currentUrl = ""; address = ""; tabs = tabs.map { if (it.id == activeId) it.copy(url = "") else it }; webView = null },
                    onNewTab = { newTab() },
                    onTabs = { panel = "Tabs" }
                )
            }
        ) { padding ->
            when {
                panel.isNotEmpty() -> BrowserPanel(
                    panel, tabs, bookmarks, history,
                    modifier = Modifier.padding(padding),
                    onOpen = { navigate(it) },
                    onNewTab = { newTab() },
                    onSelectTab = { id ->
                        activeId = id
                        val t = tabs.first { it.id == id }
                        address = t.url
                        currentUrl = t.url
                        currentTitle = t.title
                        webView = null
                        panel = ""
                    },
                    onCloseTab = { id ->
                        val rest = tabs.filterNot { it.id == id }.ifEmpty { listOf(TabState(nextId++)) }
                        tabs = rest
                        if (activeId == id) {
                            val t = rest.first()
                            activeId = t.id
                            address = t.url
                            currentUrl = t.url
                            currentTitle = t.title
                            webView = null
                        }
                    },
                    onClearHistory = { history = emptyList(); persist(HISTORY, emptyList()) },
                    onNewPrivateTab = { newTab(true) }
                )
                currentUrl.isBlank() && active.url.isBlank() -> HomePage(
                    modifier = Modifier.padding(padding),
                    privateMode = active.privateMode,
                    bookmarks = bookmarks,
                    history = history,
                    onSearch = { navigate(it) },
                    onOpen = { navigate(it) },
                    onPanel = { panel = it }
                )
                else -> key(active.id, desktop) {
                    BrowserWebView(
                        url = active.url.ifBlank { currentUrl },
                        desktop = desktop,
                        modifier = Modifier.padding(padding),
                        onCreated = { webView = it },
                        onProgress = { progress = it },
                        onPage = { url, title ->
                            currentUrl = url
                            address = url
                            currentTitle = title
                            tabs = tabs.map { if (it.id == activeId) it.copy(url = url, title = title) else it }
                            if (!active.privateMode && url.startsWith("http")) saveEntry(HISTORY, title, url)
                        },
                        onDownload = { url, agent, disposition, mime ->
                            runCatching {
                                val filename = disposition?.substringAfter("filename=")?.trim('"')?.takeIf { it.isNotBlank() }
                                    ?: Uri.parse(url).lastPathSegment ?: "download"
                                val request = DownloadManager.Request(Uri.parse(url)).apply {
                                    setTitle(filename)
                                    setDescription("Downloading with NEXT Browser")
                                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
                                    if (!agent.isNullOrBlank()) addRequestHeader("User-Agent", agent)
                                    if (!mime.isNullOrBlank()) setMimeType(mime)
                                    CookieManager.getInstance().getCookie(url)?.let { addRequestHeader("Cookie", it) }
                                }
                                context.getSystemService(DownloadManager::class.java).enqueue(request)
                                Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
                            }.onFailure { Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show() }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BrowserBottomBar(
    canBack: Boolean, canForward: Boolean, tabCount: Int, isHome: Boolean,
    onBack: () -> Unit, onForward: () -> Unit, onReload: () -> Unit, onHome: () -> Unit, onNewTab: () -> Unit, onTabs: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 10.dp) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 9.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomAction(Icons.Default.ArrowBack, "Back", canBack, onBack)
            BottomAction(Icons.Default.ArrowForward, "Forward", canForward, onForward)
            BottomAction(if (isHome) Icons.Default.Home else Icons.Default.Refresh, if (isHome) "Home" else "Reload", true, if (isHome) onHome else onReload)
            BottomAction(Icons.Default.Add, "New tab", true, onNewTab)
            Box(contentAlignment = Alignment.Center) {
                IconButton(onClick = onTabs, modifier = Modifier.size(42.dp)) {
                    Icon(Icons.Default.Tab, null, tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(tabCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun BottomAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(62.dp).clickable(enabled = enabled, onClick = onClick).padding(vertical = 2.dp)) {
        Icon(icon, null, tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = .3f), modifier = Modifier.size(23.dp))
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HomePage(
    modifier: Modifier, privateMode: Boolean, bookmarks: List<Pair<String, String>>, history: List<Pair<String, String>>,
    onSearch: (String) -> Unit, onOpen: (String) -> Unit, onPanel: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 24.dp)) {
        item {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF30266F), Color(0xFF6755D9), Color(0xFF12A6A0))))
                    .padding(22.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, null, tint = Color.White, modifier = Modifier.size(27.dp))
                        Spacer(Modifier.width(9.dp))
                        Text("NEXT", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Spacer(Modifier.weight(1f))
                        Surface(color = Color.White.copy(alpha = .16f), shape = CircleShape) {
                            Text(if (privateMode) "PRIVATE" else "YOUR WEB, YOUR WAY", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("A little more room to explore.", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Search, discover and get things done.", color = Color.White.copy(alpha = .78f), style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp), singleLine = true,
                placeholder = { Text("Search Google or type a URL") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = { IconButton(onClick = { onSearch(query) }) { Icon(Icons.Default.ArrowForward, null) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(query) })
            )
            Spacer(Modifier.height(23.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Quick access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Tap to open", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(13.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickTile("Google", "G", Color(0xFF4285F4), Modifier.weight(1f)) { onOpen("https://www.google.com") }
                QuickTile("YouTube", "▶", Color(0xFFFF0033), Modifier.weight(1f)) { onOpen("https://m.youtube.com") }
                QuickTile("Gmail", "M", Color(0xFFDB4437), Modifier.weight(1f)) { onOpen("https://mail.google.com") }
                QuickTile("Instagram", "◎", Color(0xFFC13584), Modifier.weight(1f)) { onOpen("https://www.instagram.com") }
            }
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Your library", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = { onPanel("Bookmarks") }) { Text("See all") }
            }
            if (bookmarks.isEmpty()) {
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) {
                    Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BookmarkBorder, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Save pages for later", fontWeight = FontWeight.SemiBold)
                            Text("Your bookmarks will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                bookmarks.take(3).forEach { entry ->
                    LibraryRow(entry.first, entry.second, Icons.Default.Bookmark) { onOpen(entry.second) }
                }
            }
            Spacer(Modifier.height(15.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Recently visited", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = { onPanel("History") }) { Text("History") }
            }
        }
        if (history.isEmpty()) {
            item {
                Text("Pages you visit will show up here.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp))
            }
        } else {
            items(history.take(5)) { entry ->
                LibraryRow(entry.first, entry.second, Icons.Default.History) { onOpen(entry.second) }
            }
        }
        if (privateMode) {
            item {
                Spacer(Modifier.height(12.dp))
                Text("Private tab: pages are not added to local history. Cookies and site storage are not yet isolated from normal tabs.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun QuickTile(label: String, glyph: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(color.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
            Text(glyph, color = color, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(7.dp))
        Text(label, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun LibraryRow(title: String, url: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Return the installed Android System WebView's current Chromium version.
 * This keeps both browser identities aligned with the engine actually rendering pages.
 */
private fun chromiumVersion(context: Context): String {
    return Regex("Chrome/[0-9.]+")
        .find(WebSettings.getDefaultUserAgent(context))
        ?.value
        ?: "Chrome/120.0.0.0"
}

/** Mobile identity: preserve the device's real Android/model details, but remove WebView markers. */
private fun mobileModeUserAgent(context: Context): String {
    return WebSettings.getDefaultUserAgent(context)
        .replace("; wv)", ")")
        .replace("Version/4.0 ", "")
}

/** Desktop identity: request the full desktop site while still using the phone's WebView engine. */
private fun desktopModeUserAgent(context: Context): String {
    return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) ${chromiumVersion(context)} Safari/537.36"
}

/**
 * Apply one coherent configuration before the first request.
 * A mode change recreates the WebView, so UA and viewport are never mixed on one instance.
 */
private fun configureWebViewForMode(context: Context, webView: WebView, desktopMode: Boolean) {
    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        loadsImagesAutomatically = true
        javaScriptCanOpenWindowsAutomatically = true
        setSupportMultipleWindows(false)

        userAgentString = if (desktopMode) {
            desktopModeUserAgent(context)
        } else {
            mobileModeUserAgent(context)
        }

        useWideViewPort = true
        // Never shrink a desktop page to fit the phone screen. Desktop mode uses
        // a fixed 1024 CSS-pixel viewport and lets the user pan/zoom naturally.
        loadWithOverviewMode = false

        setSupportZoom(true)
        builtInZoomControls = true
        displayZoomControls = false
        textZoom = 100
        layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun BrowserWebView(
    url: String,
    desktop: Boolean,
    modifier: Modifier,
    onCreated: (WebView) -> Unit,
    onProgress: (Int) -> Unit,
    onPage: (String, String) -> Unit,
    onDownload: (String, String?, String?, String?) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(android.graphics.Color.WHITE)
                configureWebViewForMode(ctx, this, desktop)
                // 100% scale gives desktop pages a real desktop-sized canvas;
                // mobile mode keeps WebView's normal device-responsive scale.
                setInitialScale(if (desktop) 100 else 0)

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, value: Int) {
                        onProgress(value)
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val uri = request?.url ?: return false
                        if (uri.scheme == "http" || uri.scheme == "https") return false
                        return try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            true
                        } catch (_: Exception) {
                            true
                        }
                    }

                    override fun onPageFinished(view: WebView?, finished: String?) {
                        if (!finished.isNullOrBlank()) {
                            if (desktop && view != null) {
                                // Desktop-only viewport override. Mobile mode never
                                // touches the site's viewport metadata.
                                view.evaluateJavascript(
                                    """(function() {
                                        var meta = document.querySelector('meta[name="viewport"]');
                                        if (!meta) {
                                            meta = document.createElement('meta');
                                            meta.name = 'viewport';
                                            (document.head || document.documentElement).appendChild(meta);
                                        }
                                        meta.setAttribute('content',
                                            'width=1024, initial-scale=1.0, minimum-scale=0.25, maximum-scale=5.0, user-scalable=yes');
                                    })();""".trimIndent(),
                                    null
                                )
                            }
                            onPage(finished, view?.title?.takeIf { it.isNotBlank() } ?: finished)
                        }
                    }
                }

                setDownloadListener { downloadUrl, agent, disposition, mime, _ ->
                    onDownload(downloadUrl, agent, disposition, mime)
                }

                onCreated(this)
                if (url.isNotBlank()) loadUrl(url)
            }
        },
        update = { view ->
            // BrowserApp keys this view by tab id and mode. The factory therefore
            // applies the selected UA + viewport before this URL is requested.
            if (url.isNotBlank() && view.url != url) view.loadUrl(url)
        }
    )
}

@Composable
private fun BrowserPanel(
    panel: String, tabs: List<TabState>, bookmarks: List<Pair<String, String>>, history: List<Pair<String, String>>,
    modifier: Modifier, onOpen: (String) -> Unit, onNewTab: () -> Unit,
    onSelectTab: (Int) -> Unit, onCloseTab: (Int) -> Unit, onClearHistory: () -> Unit, onNewPrivateTab: () -> Unit
) {
    Column(modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        when (panel) {
            "Tabs" -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Your tabs", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("${tabs.size} open", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    FilledTonalButton(onClick = onNewTab) { Icon(Icons.Default.Add, null); Text(" New") }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onNewPrivateTab, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Lock, null); Text("  Open private tab") }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
                    items(tabs, key = { it.id }) { tab ->
                        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
                            Row(Modifier.fillMaxWidth().clickable { onSelectTab(tab.id) }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (tab.privateMode) Icons.Default.Lock else Icons.Default.Language, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(tab.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(if (tab.privateMode) "Private tab" else tab.url.ifBlank { "New tab" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                IconButton(onClick = { onCloseTab(tab.id) }) { Icon(Icons.Default.Close, "Close tab") }
                            }
                        }
                    }
                }
            }
            "Privacy" -> InfoPanel("Privacy & security", "NEXT uses Android System WebView for website rendering. Private tabs are excluded from local history, but cookies, cache and site storage are currently shared with regular tabs. Fully isolated private browsing is not implemented yet.", Icons.Default.Shield)
            "Downloads" -> InfoPanel("Your downloads", "Files downloaded through NEXT are saved to the device Downloads folder. Open the Android Downloads app to view and manage them.", Icons.Default.Download)
            else -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(if (panel == "Bookmarks") "Saved pages" else "Browsing history", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(if (panel == "Bookmarks") "${bookmarks.size} saved" else "${history.size} recent pages", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    if (panel == "History") TextButton(onClick = onClearHistory) { Text("Clear all") }
                }
                Spacer(Modifier.height(12.dp))
                val entries = if (panel == "Bookmarks") bookmarks else history
                if (entries.isEmpty()) InfoPanel("Nothing here yet", "When you save or visit pages, they will appear here.", if (panel == "Bookmarks") Icons.Default.BookmarkBorder else Icons.Default.History)
                else LazyColumn {
                    items(entries, key = { it.second }) { entry ->
                        LibraryRow(entry.first, entry.second, if (panel == "Bookmarks") Icons.Default.Bookmark else Icons.Default.History) { onOpen(entry.second) }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPanel(title: String, body: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(Modifier.fillMaxWidth().padding(top = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(74.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun readEntries(raw: String): List<Pair<String, String>> = runCatching {
    val array = JSONArray(raw)
    (0 until array.length()).mapNotNull { i ->
        val obj = array.optJSONObject(i) ?: return@mapNotNull null
        obj.optString("title") to obj.optString("url")
    }
}.getOrDefault(emptyList())
