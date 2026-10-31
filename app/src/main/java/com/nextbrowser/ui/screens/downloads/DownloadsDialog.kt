package com.nextbrowser.ui.screens.downloads

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private data class DownloadRow(
    val id: Long,
    val title: String,
    val status: String,
    val progress: String,
    val mimeType: String?
)

@Composable
fun DownloadsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }
    val downloads = remember { mutableStateListOf<DownloadRow>() }

    fun refresh() {
        val rows = mutableListOf<DownloadRow>()
        val cursor = manager.query(DownloadManager.Query())
        cursor.use {
            val idColumn = it.getColumnIndex(DownloadManager.COLUMN_ID)
            val titleColumn = it.getColumnIndex(DownloadManager.COLUMN_TITLE)
            val statusColumn = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val totalColumn = it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            val downloadedColumn = it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val mimeColumn = it.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val statusCode = it.getInt(statusColumn)
                val total = if (totalColumn >= 0) it.getLong(totalColumn) else -1L
                val downloaded = if (downloadedColumn >= 0) it.getLong(downloadedColumn) else 0L
                val status = when (statusCode) {
                    DownloadManager.STATUS_PENDING -> "Waiting"
                    DownloadManager.STATUS_RUNNING -> "Downloading"
                    DownloadManager.STATUS_PAUSED -> "Paused"
                    DownloadManager.STATUS_SUCCESSFUL -> "Completed"
                    DownloadManager.STATUS_FAILED -> "Failed"
                    else -> "Unknown"
                }
                val progress = if (total > 0) "${(downloaded * 100 / total).coerceIn(0, 100)}%" else if (statusCode == DownloadManager.STATUS_SUCCESSFUL) "100%" else ""
                rows.add(DownloadRow(
                    id = id,
                    title = if (titleColumn >= 0) it.getString(titleColumn).orEmpty().ifBlank { "Downloaded file" } else "Downloaded file",
                    status = status,
                    progress = progress,
                    mimeType = if (mimeColumn >= 0) it.getString(mimeColumn) else null
                ))
            }
        }
        downloads.clear()
        downloads.addAll(rows.reversed())
    }

    LaunchedEffect(manager) {
        while (isActive) {
            refresh()
            delay(1200)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Downloads")
                IconButton(onClick = ::refresh) { Icon(Icons.Default.Refresh, contentDescription = "Refresh downloads") }
            }
        },
        text = {
            if (downloads.isEmpty()) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("No downloads yet", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(downloads, key = { it.id }) { item ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f).clickable(enabled = item.status == "Completed") {
                                openDownloadedFile(context, manager, item.id, item.mimeType)
                            }.padding(vertical = 6.dp)) {
                                Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                                Text(listOfNotNull(item.status, item.progress.takeIf { it.isNotBlank() }).joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                manager.remove(item.id)
                                refresh()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove download")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}

private fun openDownloadedFile(context: Context, manager: DownloadManager, id: Long, mimeType: String?) {
    val uri: Uri = manager.getUriForDownloadedFile(id) ?: return
    val resolvedType = mimeType ?: context.contentResolver.getType(uri) ?: "*/*"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, resolvedType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(intent) }
}
