package com.nextbrowser.domain.repository

import com.nextbrowser.data.local.BookmarkEntity
import com.nextbrowser.data.local.HistoryEntity
import kotlinx.coroutines.flow.Flow

interface BrowserLibraryRepository {
    fun observeBookmarks(): Flow<List<BookmarkEntity>>
    fun searchBookmarks(query: String): Flow<List<BookmarkEntity>>
    suspend fun addBookmark(bookmark: BookmarkEntity)
    suspend fun removeBookmark(id: String)

    fun observeRecentHistory(limit: Int = 200): Flow<List<HistoryEntity>>
    fun searchHistory(query: String): Flow<List<HistoryEntity>>
    suspend fun addHistory(entry: HistoryEntity)
    suspend fun removeHistoryEntry(id: String)
    suspend fun clearHistory()
    suspend fun deleteHistoryBefore(timestamp: Long)
}
