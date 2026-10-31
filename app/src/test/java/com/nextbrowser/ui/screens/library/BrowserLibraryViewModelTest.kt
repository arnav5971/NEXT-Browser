package com.nextbrowser.ui.screens.library

import com.nextbrowser.data.local.BookmarkEntity
import com.nextbrowser.data.local.HistoryEntity
import com.nextbrowser.domain.repository.BrowserLibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BrowserLibraryViewModelTest {

    @Test
    fun updateSearchQueryPublishesNewValue() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val viewModel = BrowserLibraryViewModel(FakeBrowserLibraryRepository())

            viewModel.updateSearchQuery("  example  ")

            assertEquals("  example  ", viewModel.searchQuery.value)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun libraryActionsAreDelegatedToRepository() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val repository = FakeBrowserLibraryRepository()
            val viewModel = BrowserLibraryViewModel(repository)
            val bookmark = BookmarkEntity(
                id = "bookmark-1",
                title = "Example",
                url = "https://example.com"
            )
            val history = HistoryEntity(
                id = "history-1",
                title = "Example",
                url = "https://example.com"
            )

            viewModel.addBookmark(bookmark)
            viewModel.addHistory(history)
            viewModel.deleteBookmark(bookmark.id)
            viewModel.deleteHistoryEntry(history.id)
            viewModel.clearHistory()
            advanceUntilIdle()

            assertEquals(listOf(bookmark), repository.addedBookmarks)
            assertEquals(listOf(history), repository.addedHistory)
            assertEquals(listOf(bookmark.id), repository.removedBookmarkIds)
            assertEquals(listOf(history.id), repository.removedHistoryIds)
            assertEquals(1, repository.clearHistoryCalls)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class FakeBrowserLibraryRepository : BrowserLibraryRepository {
        private val bookmarks = MutableStateFlow(emptyList<BookmarkEntity>())
        private val history = MutableStateFlow(emptyList<HistoryEntity>())

        val addedBookmarks = mutableListOf<BookmarkEntity>()
        val addedHistory = mutableListOf<HistoryEntity>()
        val removedBookmarkIds = mutableListOf<String>()
        val removedHistoryIds = mutableListOf<String>()
        var clearHistoryCalls = 0

        override fun observeBookmarks(): Flow<List<BookmarkEntity>> = bookmarks

        override fun searchBookmarks(query: String): Flow<List<BookmarkEntity>> = bookmarks

        override suspend fun addBookmark(bookmark: BookmarkEntity) {
            addedBookmarks += bookmark
        }

        override suspend fun removeBookmark(id: String) {
            removedBookmarkIds += id
        }

        override fun observeRecentHistory(limit: Int): Flow<List<HistoryEntity>> = history

        override fun searchHistory(query: String): Flow<List<HistoryEntity>> = history

        override suspend fun addHistory(entry: HistoryEntity) {
            addedHistory += entry
        }

        override suspend fun removeHistoryEntry(id: String) {
            removedHistoryIds += id
        }

        override suspend fun clearHistory() {
            clearHistoryCalls++
        }

        override suspend fun deleteHistoryBefore(timestamp: Long) = Unit
    }
}
