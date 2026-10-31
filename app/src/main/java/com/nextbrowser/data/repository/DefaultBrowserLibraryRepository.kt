package com.nextbrowser.data.repository

import com.nextbrowser.data.local.BookmarkEntity
import com.nextbrowser.data.local.BrowserLibraryDao
import com.nextbrowser.data.local.HistoryEntity
import com.nextbrowser.domain.repository.BrowserLibraryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class DefaultBrowserLibraryRepository @Inject constructor(
    private val dao: BrowserLibraryDao
) : BrowserLibraryRepository {
    override fun observeBookmarks(): Flow<List<BookmarkEntity>> = dao.observeBookmarks()
    override fun searchBookmarks(query: String): Flow<List<BookmarkEntity>> = dao.searchBookmarks(query)
    override suspend fun addBookmark(bookmark: BookmarkEntity) = dao.addBookmark(bookmark)
    override suspend fun removeBookmark(id: String) = dao.removeBookmark(id)

    override fun observeRecentHistory(limit: Int): Flow<List<HistoryEntity>> =
        dao.observeRecentHistory(limit)

    override fun searchHistory(query: String): Flow<List<HistoryEntity>> = dao.searchHistory(query)
    override suspend fun addHistory(entry: HistoryEntity) = dao.addHistory(entry)
    override suspend fun removeHistoryEntry(id: String) = dao.removeHistoryEntry(id)
    override suspend fun clearHistory() = dao.clearHistory()
    override suspend fun deleteHistoryBefore(timestamp: Long) = dao.deleteHistoryBefore(timestamp)
}
