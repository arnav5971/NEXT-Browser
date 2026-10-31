package com.nextbrowser.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextbrowser.data.local.BookmarkEntity
import com.nextbrowser.data.local.HistoryEntity
import com.nextbrowser.domain.repository.BrowserLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class BrowserLibraryViewModel @Inject constructor(
    private val repository: BrowserLibraryRepository
) : ViewModel() {
    private val query = MutableStateFlow("")

    val searchQuery: StateFlow<String> = query

    val bookmarks: StateFlow<List<BookmarkEntity>> = query
        .flatMapLatest { value ->
            if (value.isBlank()) repository.observeBookmarks()
            else repository.searchBookmarks(value.trim())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val history: StateFlow<List<HistoryEntity>> = query
        .flatMapLatest { value ->
            if (value.isBlank()) repository.observeRecentHistory()
            else repository.searchHistory(value.trim())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateSearchQuery(value: String) {
        query.value = value
    }

    fun addBookmark(bookmark: BookmarkEntity) {\n        viewModelScope.launch { repository.addBookmark(bookmark) }\n    }\n\n    fun addHistory(entry: HistoryEntity) {\n        viewModelScope.launch { repository.addHistory(entry) }\n    }\n\n    fun deleteBookmark(id: String) {
        viewModelScope.launch { repository.removeBookmark(id) }
    }

    fun deleteHistoryEntry(id: String) {
        viewModelScope.launch { repository.removeHistoryEntry(id) }
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clearHistory() }
    }
}
