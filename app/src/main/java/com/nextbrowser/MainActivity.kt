package com.nextbrowser

import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextbrowser.data.local.BookmarkEntity
import com.nextbrowser.data.local.HistoryEntity
import com.nextbrowser.domain.model.BrowserTab
import com.nextbrowser.ui.screens.browser.BrowserTabsViewModel
import com.nextbrowser.ui.screens.browser.BrowserScreen
import com.nextbrowser.ui.screens.home.HomeScreen
import com.nextbrowser.ui.screens.downloads.DownloadsDialog
import com.nextbrowser.ui.screens.library.LibraryDialog
import com.nextbrowser.ui.theme.NextBrowserTheme
import java.util.UUID
import dagger.hilt.android.AndroidEntryPoint
import com.nextbrowser.ui.settings.BrowserSettingsViewModel
import com.nextbrowser.ui.screens.library.BrowserLibraryViewModel
import com.nextbrowser.domain.model.BrowserSettings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: BrowserSettingsViewModel by viewModels()
    private val libraryViewModel: BrowserLibraryViewModel by viewModels()
    private val tabsViewModel: BrowserTabsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by settingsViewModel.settings.collectAsState()
            val darkTheme = when (settings.themeMode) {
                BrowserSettings.THEME_DARK -> true
                BrowserSettings.THEME_LIGHT -> false
                else -> isSystemInDarkTheme()
            }
            val accentColor = settings.accentColorHex.takeIf { it.isNotBlank() }?.let { hex ->
                runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull()
            }
            NextBrowserTheme(
                darkTheme = darkTheme,
                dynamicColor = settings.dynamicColor,
                accentColor = accentColor
            ) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    NextBrowserApp(libraryViewModel, tabsViewModel)
                }
            }
        }
    }
}

@Composable
private fun NextBrowserApp(
    libraryViewModel: BrowserLibraryViewModel,
    tabsViewModel: BrowserTabsViewModel
) {
    val context = LocalContext.current
    var libraryMode by remember { mutableStateOf<String?>(null) }
    val tabs by tabsViewModel.tabs.collectAsState()
    val activeTabId by tabsViewModel.activeTabId.collectAsState()
    var showTabSwitcher by remember { mutableStateOf(false) }
    var showDownloadsDialog by remember { mutableStateOf(false) }
    val activeTab = tabs.firstOrNull { it.id == activeTabId } ?: tabs.first()

    if (activeTab.url.isNullOrBlank()) {
        HomeScreen(
            onSearch = { query ->
                tabsViewModel.updateTab(activeTab.id) { it.copy(url = query) }
            },
            onShowTabs = { showTabSwitcher = true },
            tabCount = tabs.size,
            onShowBookmarks = { libraryMode = "bookmarks" },
            onShowHistory = { libraryMode = "history" }
        )
    } else {
        BrowserScreen(
            initialUrl = activeTab.url.orEmpty(),
            onGoHome = {
                tabsViewModel.updateTab(activeTab.id) { it.copy(url = null, title = "New tab") }
            },
            onShowTabs = { showTabSwitcher = true },
            tabCount = tabs.size,
            isPrivateTab = activeTab.isPrivate,
            onUrlChanged = { url ->
                tabsViewModel.updateTab(activeTab.id) { tab ->
                    if (tab.url == url) tab else tab.copy(url = url)
                }
            },
            onTitleChanged = { title ->
                tabsViewModel.updateTab(activeTab.id) { tab ->
                    if (tab.title == title) tab else tab.copy(title = title)
                }
            },
            onPageVisited = { url, title ->
                if (!activeTab.isPrivate && url.startsWith("http")) {
                    libraryViewModel.addHistory(HistoryEntity(id = UUID.randomUUID().toString(), title = title, url = url))
                }
            },
            onAddBookmark = { url, title ->
                if (!activeTab.isPrivate && url.startsWith("http")) {
                    libraryViewModel.addBookmark(BookmarkEntity(id = UUID.randomUUID().toString(), title = title.ifBlank { url }, url = url))
                }
            },
            onDownloadRequested = { url, userAgent, contentDisposition, mimeType, _ ->
                runCatching {
                    val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
                        .replace(Regex("[\\\\/:*?\\\"<>|]"), "_")
                        .ifBlank { "download" }
                    val request = DownloadManager.Request(Uri.parse(url)).apply {
                        setTitle(fileName)
                        setDescription("Downloading with NEXT Browser")
                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                        setAllowedOverMetered(true)
                        setAllowedOverRoaming(false)
                        if (!mimeType.isNullOrBlank()) setMimeType(mimeType)
                        if (!userAgent.isNullOrBlank()) addRequestHeader("User-Agent", userAgent)
                        CookieManager.getInstance().getCookie(url)?.takeIf { it.isNotBlank() }?.let {
                            addRequestHeader("Cookie", it)
                        }
                    }
                    val manager = context.getSystemService(DownloadManager::class.java)
                    manager.enqueue(request)
                }.onFailure {
                    Toast.makeText(context, "Could not start download", Toast.LENGTH_SHORT).show()
                }
            },
            onShowDownloads = { showDownloadsDialog = true }
        )
    }

    if (libraryMode != null) {
        LibraryDialog(
            viewModel = libraryViewModel,
            showBookmarks = libraryMode == "bookmarks",
            onOpenUrl = { url ->
                tabsViewModel.updateTab(activeTab.id) { it.copy(url = url) }
                libraryMode = null
            },
            onDismiss = { libraryMode = null }
        )
    }

    if (showDownloadsDialog) {
        DownloadsDialog(onDismiss = { showDownloadsDialog = false })
    }

    if (showTabSwitcher) {
        TabSwitcherDialog(
            tabs = tabs, activeTabId = activeTabId,
            onSelect = { tabsViewModel.selectTab(it); showTabSwitcher = false },
            onClose = tabsViewModel::closeTab,
            onNewTab = { tabsViewModel.createTab(); showTabSwitcher = false },
            onNewPrivateTab = { tabsViewModel.createTab(isPrivate = true); showTabSwitcher = false },
            onDismiss = { showTabSwitcher = false }
        )
    }
}

@Composable
private fun TabSwitcherDialog(tabs: List<BrowserTab>, activeTabId: String, onSelect: (String) -> Unit, onClose: (String) -> Unit, onNewTab: () -> Unit, onNewPrivateTab: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) { Text("Tabs (${tabs.size})"); Icon(Icons.Default.Tab, contentDescription = null) } },
        text = {
            Column {
                LazyColumn {
                    items(tabs, key = { it.id }) { tab ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onSelect(tab.id) }, colors = CardDefaults.cardColors(containerColor = if (tab.id == activeTabId) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (tab.isPrivate) Icons.Default.Lock else Icons.Default.Language, contentDescription = null)
                                Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                    Text(tab.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(if (tab.isPrivate) "Private session" else tab.url ?: "Start page", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                IconButton(onClick = { onClose(tab.id) }) { Icon(Icons.Default.Close, contentDescription = "Close tab") }
                            }
                        }
                    }
                }
                Text("Regular tabs are restored after restarting the app. Private tabs are not saved. Private cookie/storage isolation is not implemented yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = onNewPrivateTab) { Icon(Icons.Default.Lock, contentDescription = null); Text(" Private") }; Button(onClick = onNewTab) { Icon(Icons.Default.Add, contentDescription = null); Text(" New tab") } } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}