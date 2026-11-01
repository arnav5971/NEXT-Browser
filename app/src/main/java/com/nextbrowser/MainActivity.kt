package com.nextbrowser

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    val context = LocalContext.current
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
    var dark by remember { mutableStateOf(false) }
    var desktop by remember { mutableStateOf(false) }
    var bookmarks by remember { mutableStateOf(readEntries(store.getString(BOOKMARKS, "[]").orEmpty())) }
    var history by remember { mutableStateOf(readEntries(store.getString(HISTORY, "[]").orEmpty())) }
    val active = tabs.firstOrNull { it.id == activeId } ?: tabs.first()

    fun navigate(input: String) {
        val q = input.trim()
        if (q.isBlank()) return
        val target = when {
            q.startsWith("https://") || q.startsWith("http://") -> q
            q.contains(".") && !q.contains(" ") -> "https://$q"
            else -> "https://www.google.com/search?q=" + Uri.encode(q)
        }
        address = target
        currentUrl = target
        tabs = tabs.map { if (it.id == activeId) it.copy(url = target) else it }
        webView?.loadUrl(target)
    }
    fun persist(key: String, entries: List<Pair<String, String>>) {
        store.edit().putString(key, JSONArray().apply {
            entries.forEach { (title, url) -> put(JSONObject().put("title", title).put("url", url)) }
        }.toString()).apply()
    }
    fun saveEntry(key: String, title: String, url: String) {
        if (url.isBlank() || active.privateMode) return
        val source = if (key == BOOKMARKS) bookmarks else history
        val updated = (listOf(title to url) + source.filterNot { it.second == url }).take(500)
        if (key == BOOKMARKS) bookmarks = updated else history = updated
        persist(key, updated)
    }
    fun newTab(privateMode: Boolean = false) {
        val t = TabState(nextId++, privateMode = privateMode)
        tabs = tabs + t
        activeId = t.id
        address = ""
        currentUrl = ""
        currentTitle = "New tab"
        webView = null
    }

    BackHandler(enabled = panel.isNotEmpty() || webView?.canGoBack() == true) {
        if (panel.isNotEmpty()) panel = "" else webView?.goBack()
    }

    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        Scaffold(
            topBar = {
                Column {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(6.dp))
                        OutlinedTextField(
                            value = address, onValueChange = { address = it },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            singleLine = true, shape = RoundedCornerShape(25.dp),
                            placeholder = { Text("Search or enter website") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = { navigate(address) })
                        )
                        IconButton(onClick = { saveEntry(BOOKMARKS, currentTitle, currentUrl); Toast.makeText(context, "Bookmark saved", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.BookmarkAdd, "Bookmark")
                        }
                        Box {
                            IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Menu") }
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                DropdownMenuItem(text = { Text("Bookmarks") }, onClick = { panel = "Bookmarks"; menu = false }, leadingIcon = { Icon(Icons.Default.Bookmarks, null) })
                                DropdownMenuItem(text = { Text("History") }, onClick = { panel = "History"; menu = false }, leadingIcon = { Icon(Icons.Default.History, null) })
                                DropdownMenuItem(text = { Text("Downloads") }, onClick = { panel = "Downloads"; menu = false }, leadingIcon = { Icon(Icons.Default.Download, null) })
                                DropdownMenuItem(text = { Text(if (desktop) "Mobile site" else "Desktop site") }, onClick = { desktop = !desktop; menu = false; webView?.settings?.useWideViewPort = desktop; webView?.settings?.loadWithOverviewMode = desktop }, leadingIcon = { Icon(Icons.Default.Computer, null) })
                                DropdownMenuItem(text = { Text(if (dark) "Light theme" else "Dark theme") }, onClick = { dark = !dark; menu = false }, leadingIcon = { Icon(Icons.Default.DarkMode, null) })
                                DropdownMenuItem(text = { Text("Share page") }, onClick = {
                                    menu = false
                                    if (currentUrl.isNotBlank()) context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, currentUrl), "Share page"))
                                }, leadingIcon = { Icon(Icons.Default.Share, null) })
                                DropdownMenuItem(text = { Text("Privacy details") }, onClick = { panel = "Privacy"; menu = false }, leadingIcon = { Icon(Icons.Default.Security, null) })
                            }
                        }
                    }
                    if (progress in 1..99) LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth())
                }
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(selected = false, onClick = { webView?.goBack() }, icon = { Icon(Icons.Default.ArrowBack, null) }, label = { Text("Back") })
                    NavigationBarItem(selected = false, onClick = { webView?.goForward() }, icon = { Icon(Icons.Default.ArrowForward, null) }, label = { Text("Next") })
                    NavigationBarItem(selected = false, onClick = { webView?.reload() }, icon = { Icon(Icons.Default.Refresh, null) }, label = { Text("Reload") })
                    NavigationBarItem(selected = false, onClick = { newTab() }, icon = { Icon(Icons.Default.Add, null) }, label = { Text("New") })
                    NavigationBarItem(selected = false, onClick = { panel = "Tabs" }, icon = { Icon(Icons.Default.Tab, null) }, label = { Text("${tabs.size} tabs") })
                }
            }
        ) { padding ->
            when {
                panel.isNotEmpty() -> BrowserPanel(panel, tabs, bookmarks, history,
                    onClose = { panel = "" },
                    onOpen = { url -> panel = ""; navigate(url) },
                    onNewTab = { newTab(); panel = "" },
                    onSelectTab = { id -> activeId = id; val t = tabs.first { it.id == id }; address = t.url; currentUrl = t.url; panel = ""; webView = null },
                    onCloseTab = { id ->
                        val rest = tabs.filterNot { it.id == id }.ifEmpty { listOf(TabState(nextId++)) }
                        tabs = rest
                        if (activeId == id) { activeId = rest.first().id; address = rest.first().url; currentUrl = rest.first().url; webView = null }
                    },
                    onClearHistory = { history = emptyList(); persist(HISTORY, emptyList()) }
                )
                currentUrl.isBlank() && active.url.isBlank() -> HomePage(
                    Modifier.padding(padding), active.privateMode,
                    onSearch = { navigate(it) },
                    onPanel = { panel = it }
                )
                else -> key(active.id) {
                    BrowserWebView(
                        url = active.url.ifBlank { currentUrl }, desktop = desktop,
                        onCreated = { webView = it },
                        onProgress = { progress = it },
                        onPage = { url, title ->
                            currentUrl = url; address = url; currentTitle = title
                            tabs = tabs.map { if (it.id == activeId) it.copy(url = url, title = title) else it }
                            if (!active.privateMode && url.startsWith("http")) saveEntry(HISTORY, title, url)
                        },
                        onDownload = { url, agent, disposition, mime ->
                            runCatching {
                                val name = disposition?.substringAfter("filename=")?.trim('"')?.takeIf { it.isNotBlank() } ?: Uri.parse(url).lastPathSegment ?: "download"
                                val request = DownloadManager.Request(Uri.parse(url))
                                    .setTitle(name).setDescription("NEXT Browser download")
                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)
                                if (!agent.isNullOrBlank()) request.addRequestHeader("User-Agent", agent)
                                if (!mime.isNullOrBlank()) request.setMimeType(mime)
                                CookieManager.getInstance().getCookie(url)?.let { request.addRequestHeader("Cookie", it) }
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
private fun HomePage(modifier: Modifier, privateMode: Boolean, onSearch: (String) -> Unit, onPanel: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    Column(modifier.fillMaxSize().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(76.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Language, null, modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(16.dp))
        Text("NEXT", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
        Text(if (privateMode) "Private tab" else "Browse beyond limits", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(22.dp))
        OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Search the web") }, trailingIcon = { IconButton(onClick = { onSearch(query) }) { Icon(Icons.Default.ArrowForward, null) } }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { onSearch(query) }))
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(onClick = { onPanel("Bookmarks") }) { Icon(Icons.Default.Bookmarks, null); Text(" Bookmarks") }
            FilledTonalButton(onClick = { onPanel("History") }) { Icon(Icons.Default.History, null); Text(" History") }
        }
        if (privateMode) Text("Private tabs are omitted from history, but web storage is not isolated yet.", modifier = Modifier.padding(top = 18.dp), style = MaterialTheme.typography.bodySmall)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun BrowserWebView(
    url: String, desktop: Boolean, onCreated: (WebView) -> Unit,
    onProgress: (Int) -> Unit, onPage: (String, String) -> Unit,
    onDownload: (String, String?, String?, String?) -> Unit
) {
    val context = LocalContext.current
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true
                settings.useWideViewPort = desktop
                settings.loadWithOverviewMode = desktop
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, value: Int) { onProgress(value) }
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val uri = request?.url ?: return false
                        if (uri.scheme == "http" || uri.scheme == "https") return false
                        return try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)); true } catch (_: Exception) { true }
                    }
                    override fun onPageFinished(view: WebView?, finished: String?) {
                        if (!finished.isNullOrBlank()) onPage(finished, view?.title ?: finished)
                    }
                }
                setDownloadListener { downloadUrl, agent, disposition, mime, _ -> onDownload(downloadUrl, agent, disposition, mime) }
                onCreated(this)
            }
        },
        update = { view ->
            if (url.isNotBlank() && view.url != url) view.loadUrl(url)
            view.settings.useWideViewPort = desktop
            view.settings.loadWithOverviewMode = desktop
        }
    )
}

@Composable
private fun BrowserPanel(
    panel: String, tabs: List<TabState>, bookmarks: List<Pair<String, String>>, history: List<Pair<String, String>>,
    onClose: () -> Unit, onOpen: (String) -> Unit, onNewTab: () -> Unit,
    onSelectTab: (Int) -> Unit, onCloseTab: (Int) -> Unit, onClearHistory: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(panel, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = onClose) { Text("Close") }
        }
        when (panel) {
            "Tabs" -> {
                Button(onClick = onNewTab) { Icon(Icons.Default.Add, null); Text(" New tab") }
                LazyColumn {
                    items(tabs, key = { it.id }) { tab ->
                        ListItem(
                            headlineContent = { Text(tab.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            supportingContent = { Text(if (tab.privateMode) "Private tab" else tab.url.ifBlank { "Start page" }, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingContent = { Icon(if (tab.privateMode) Icons.Default.Lock else Icons.Default.Language, null) },
                            trailingContent = { IconButton(onClick = { onCloseTab(tab.id) }) { Icon(Icons.Default.Close, null) } },
                            modifier = Modifier.clickable { onSelectTab(tab.id) }
                        )
                    }
                }
            }
            "Privacy" -> Text("NEXT Browser uses Android System WebView. Private tabs are excluded from local history, but cookies, cache and web storage are currently shared with regular tabs. Fully isolated private browsing is not implemented yet.")
            "Downloads" -> Text("Downloads are saved in the device Downloads folder. Use the Android Downloads app to view or manage files.")
            else -> {
                val entries = if (panel == "Bookmarks") bookmarks else history
                if (panel == "History") TextButton(onClick = onClearHistory) { Text("Clear history") }
                if (entries.isEmpty()) Text("Nothing here yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyColumn {
                    items(entries, key = { it.second }) { item ->
                        ListItem(
                            headlineContent = { Text(item.first, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            supportingContent = { Text(item.second, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingContent = { Icon(if (panel == "Bookmarks") Icons.Default.Bookmark else Icons.Default.History, null) },
                            modifier = Modifier.clickable { onOpen(item.second) }
                        )
                    }
                }
            }
        }
    }
}

private fun readEntries(raw: String): List<Pair<String, String>> = runCatching {
    val array = JSONArray(raw)
    (0 until array.length()).mapNotNull { i ->
        val obj = array.optJSONObject(i) ?: return@mapNotNull null
        obj.optString("title") to obj.optString("url")
    }
}.getOrDefault(emptyList())
