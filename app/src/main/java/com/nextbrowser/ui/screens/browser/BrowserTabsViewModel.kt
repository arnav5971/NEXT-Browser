package com.nextbrowser.ui.screens.browser

import androidx.lifecycle.ViewModel
import com.nextbrowser.domain.model.BrowserTab
import com.nextbrowser.domain.repository.BrowserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class BrowserTabsViewModel @Inject constructor(
    private val sessionRepository: BrowserSessionRepository
) : ViewModel() {
    private val restoredSession = sessionRepository.restore()
    private val initialTabs = restoredSession.first.ifEmpty {
        listOf(BrowserTab(id = UUID.randomUUID().toString()))
    }

    private val _tabs = MutableStateFlow(initialTabs)
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow(
        restoredSession.second?.takeIf { id -> initialTabs.any { it.id == id } }
            ?: initialTabs.first().id
    )
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    init {
        persistSession()
    }

    fun createTab(isPrivate: Boolean = false) {
        val tab = BrowserTab(
            id = UUID.randomUUID().toString(),
            title = if (isPrivate) "Private tab" else "New tab",
            isPrivate = isPrivate
        )
        _tabs.value = _tabs.value + tab
        _activeTabId.value = tab.id
        persistSession()
    }

    fun closeTab(tabId: String) {
        val currentTabs = _tabs.value
        val index = currentTabs.indexOfFirst { it.id == tabId }
        if (index == -1) return

        val updatedTabs = currentTabs.toMutableList().apply { removeAt(index) }
        if (updatedTabs.isEmpty()) {
            val replacement = BrowserTab(id = UUID.randomUUID().toString())
            _tabs.value = listOf(replacement)
            _activeTabId.value = replacement.id
        } else {
            _tabs.value = updatedTabs
            if (_activeTabId.value == tabId) {
                _activeTabId.value = updatedTabs[
                    (index - 1).coerceAtLeast(0).coerceAtMost(updatedTabs.lastIndex)
                ].id
            }
        }
        persistSession()
    }

    fun selectTab(tabId: String) {
        if (_tabs.value.none { it.id == tabId }) return
        _activeTabId.value = tabId
        persistSession()
    }

    fun updateTab(tabId: String, transform: (BrowserTab) -> BrowserTab) {
        val currentTabs = _tabs.value
        val index = currentTabs.indexOfFirst { it.id == tabId }
        if (index == -1) return

        val updatedTab = transform(currentTabs[index])
        if (updatedTab == currentTabs[index]) return
        _tabs.value = currentTabs.toMutableList().apply { this[index] = updatedTab }
        persistSession()
    }

    private fun persistSession() {
        sessionRepository.save(_tabs.value, _activeTabId.value)
    }
}
