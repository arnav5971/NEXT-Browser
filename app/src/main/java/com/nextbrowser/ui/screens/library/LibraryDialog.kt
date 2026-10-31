package com.nextbrowser.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextbrowser.domain.repository.BrowserLibraryRepository

@Composable
fun LibraryDialog(repository: BrowserLibraryRepository, showBookmarks: Boolean, onOpenUrl: (String) -> Unit, onDeleteBookmark: (String) -> Unit, onClearHistory: () -> Unit, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var confirmClear by remember { mutableStateOf(false) }
    val bookmarks by (if (query.isBlank()) repository.observeBookmarks() else repository.searchBookmarks(query)).collectAsState(initial = emptyList())
    val history by (if (query.isBlank()) repository.observeRecentHistory() else repository.searchHistory(query)).collectAsState(initial = emptyList())
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (showBookmarks) "Bookmarks" else "History") },
        text = {
            Column {
                OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text(if (showBookmarks) "Search bookmarks" else "Search history") })
                LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                    if (showBookmarks) {
                        items(bookmarks, key = { it.id }) { item ->
                            Row(modifier = Modifier.fillMaxWidth().clickable { onOpenUrl(item.url) }.padding(vertical = 9.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(item.url, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (!item.folder.isNullOrBlank()) Text(item.folder, style = MaterialTheme.typography.labelSmall)
                                }
                                IconButton(onClick = { onDeleteBookmark(item.id) }) { Icon(Icons.Default.Delete, contentDescription = "Delete bookmark") }
                            }
                        }
                    } else {
                        items(history, key = { it.id }) { item ->
                            Column(modifier = Modifier.fillMaxWidth().clickable { onOpenUrl(item.url) }.padding(vertical = 9.dp)) {
                                Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(item.url, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(java.text.DateFormat.getDateTimeInstance().format(java.util.Date(item.visitedAt)), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { if (!showBookmarks) TextButton(onClick = { confirmClear = true }) { Text("Clear history") } else TextButton(onClick = onDismiss) { Text("Done") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear browsing history?") },
            text = { Text("This permanently deletes all saved browsing history. Bookmarks will remain.") },
            confirmButton = {
                Button(onClick = {
                    onClearHistory()
                    confirmClear = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            }
        )
    }
}