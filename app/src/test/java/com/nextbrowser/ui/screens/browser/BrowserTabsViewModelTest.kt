package com.nextbrowser.ui.screens.browser

import com.nextbrowser.domain.model.BrowserTab
import com.nextbrowser.domain.repository.BrowserSessionRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserTabsViewModelTest {

    @Test
    fun restoresExistingTabsAndActiveTab() {
        val first = BrowserTab(id = "one", url = "https://example.com")
        val second = BrowserTab(id = "two", title = "Second")
        val repository = FakeBrowserSessionRepository(first to second.id, listOf(first, second))

        val viewModel = BrowserTabsViewModel(repository)

        assertEquals(listOf(first, second), viewModel.tabs.value)
        assertEquals(second.id, viewModel.activeTabId.value)
    }

    @Test
    fun createsSelectsUpdatesAndPersistsTabs() {
        val repository = FakeBrowserSessionRepository()
        val viewModel = BrowserTabsViewModel(repository)

        viewModel.createTab()
        val created = viewModel.tabs.value.last()

        assertEquals(created.id, viewModel.activeTabId.value)
        viewModel.updateTab(created.id) { it.copy(url = "https://example.com", title = "Example") }
        assertEquals("https://example.com", viewModel.tabs.value.last().url)

        viewModel.selectTab(viewModel.tabs.value.first().id)
        assertEquals(viewModel.tabs.value.first().id, viewModel.activeTabId.value)
        assertTrue(repository.savedTabs.isNotEmpty())
    }

    @Test
    fun privateTabsAreMarkedAndClosingLastTabCreatesReplacement() {
        val repository = FakeBrowserSessionRepository()
        val viewModel = BrowserTabsViewModel(repository)

        viewModel.createTab(isPrivate = true)
        val privateTab = viewModel.tabs.value.last()
        assertTrue(privateTab.isPrivate)

        viewModel.closeTab(privateTab.id)
        assertFalse(viewModel.tabs.value.isEmpty())

        val remainingId = viewModel.tabs.value.single().id
        viewModel.closeTab(remainingId)
        assertEquals(1, viewModel.tabs.value.size)
        assertNotEquals(remainingId, viewModel.tabs.value.single().id)
        assertEquals(viewModel.tabs.value.single().id, viewModel.activeTabId.value)
    }

    private class FakeBrowserSessionRepository(
        private val restored: Pair<BrowserTab, String>? = null,
        private val initialTabs: List<BrowserTab> = emptyList()
    ) : BrowserSessionRepository {
        var savedTabs: List<BrowserTab> = emptyList()
            private set
        var savedActiveId: String? = null
            private set

        override fun restore(): Pair<List<BrowserTab>, String?> =
            if (restored == null) initialTabs to null else listOf(restored.first) + initialTabs.drop(1) to restored.second

        override fun save(tabs: List<BrowserTab>, activeTabId: String) {
            savedTabs = tabs.toList()
            savedActiveId = activeTabId
        }
    }
}
