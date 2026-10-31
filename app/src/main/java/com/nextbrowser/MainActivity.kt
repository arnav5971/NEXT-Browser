package com.nextbrowser

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextbrowser.data.local.BrowserSessionStore
import com.nextbrowser.data.local.NextBrowserDatabase
import com.nextbrowser.data.local.BookmarkEntity
import com.nextbrowser.data.local.HistoryEntity
import com.nextbrowser.domain.model.BrowserTab
import com.nextbrowser.ui.screens.browser.BrowserScreen
import com.nextbrowser.ui.screens.home.HomeScreen
import com.nextbrowser.ui.screens.library.LibraryDialog
import com.nextbrowser.ui.theme.NextBrowserTheme
import java.util.UUID
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NextBrowserTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    NextBrowserApp()
                }
            }
        }
    }
}

@Composable
private fun NextBrowserApp() {
    val context = LocalContext.current
    val dao = remember { NextBrowserDatabase.getInstance(context).libraryDao() }
    val scope = rememberCoroutineScope()
    var libraryMode by remember { mutableStateOf<String?>(null) }
    val restored = remember { BrowserSessionStore.restore(context) }
    val tabs = remember {
        mutableStateListOf<BrowserTab>().apply {
            addAll(restored.first)
            if (isEmpty()) add(BrowserTab(id = UUID.randomUUID().toString()))
        }
    }
    var activeTabId by remember {
        mutableStateOf(restored.second?.takeIf { id -> tabs.any { it.id == id } } ?: tabs.first().id)
    }
    var showTabSwitcher by remember { mutableStateOf(false) }
    val activeTab = tabs.firstOrNull { it.id == activeTabId } ?: tabs.first()

    SideEffect { BrowserSessionStore.save(context, tabs, activeTabId) }

    fun createTab(isPrivate: Boolean = false) {
        val tab = BrowserTab(id = UUID.randomUUID().toString(), title = if (isPrivate) "Private tab" else "New tab", isPrivate = isPrivate)
        tabs.add(tab)
        activeTabId = tab.id
        showTabSwitcher = false
    }

    fun closeTab(tabId: String) {
        val index = tabs.indexOfFirst { it.id == tabId }
        if (index == -1) return
        tabs.removeAt(index)
        if (tabs.isEmpty()) createTab()
        else if (activeTabId == tabId) activeTabId = tabs[(index - 1).coerceAtLeast(0).coerceAtMost(tabs.lastIndex)].id
    }

    if (activeTab.url.isNullOrBlank()) {
        HomeScreen(
            onSearch = { query ->
                val index = tabs.indexOfFirst { it.id == activeTab.id }
                if (index >= 0) tabs[index] = tabs[index].copy(url = query)
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
                val index = tabs.indexOfFirst { it.id == activeTab.id }
                if (index >= 0) tabs[index] = tabs[index].copy(url = null, title = "New tab")
            },
            onShowTabs = { showTabSwitcher = true },
            tabCount = tabs.size,
            isPrivateTab = activeTab.isPrivate,
            onUrlChanged = { url ->
                val index = tabs.indexOfFirst { it.id == activeTab.id }
                if (index >= 0 && tabs[index].url != url) tabs[index] = tabs[index].copy(url = url)
            },
            onTitleChanged = { title ->
                val index = tabs.indexOfFirst { it.id == activeTab.id }
                if (index >= 0 && tabs[index].title != title) tabs[index] = tabs[index].copy(title = title)
            },
            onPageVisited = { url, title ->
                if (!activeTab.isPrivate && url.startsWith("http")) {
                    scope.launch { dao.addHistory(HistoryEntity(id = UUID.randomUUID().toString(), title = title, url = url)) }
                }
            },
            onAddBookmark = { url, title ->
                if (!activeTab.isPrivate && url.startsWith("http")) {
                    scope.launch { dao.addBookmark(BookmarkEntity(id = UUID.randomUUID().toString(), title = title.ifBlank { url }, url = url)) }
                }
            }
        )
    }

    if (libraryMode != null) {
        LibraryDialog(
            dao = dao,
            showBookmarks = libraryMode == "bookmarks",
            onOpenUrl = { url ->
                val index = tabs.indexOfFirst { it.id == activeTab.id }
                if (index >= 0) tabs[index] = tabs[index].copy(url = url)
                libraryMode = null
            },
            onDeleteBookmark = { id -> scope.launch { dao.removeBookmark(id) } },
            onClearHistory = { scope.launch { dao.clearHistory() } },
            onDismiss = { libraryMode = null }
        )
    }

    if (showTabSwitcher) {
        TabSwitcherDialog(
            tabs = tabs, activeTabId = activeTabId,
            onSelect = { activeTabId = it; showTabSwitcher = false },
            onClose = ::closeTab,
            onNewTab = { createTab() },
            onNewPrivateTab = { createTab(isPrivate = true) },
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