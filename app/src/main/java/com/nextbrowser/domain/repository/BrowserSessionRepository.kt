package com.nextbrowser.domain.repository

import com.nextbrowser.domain.model.BrowserTab

interface BrowserSessionRepository {
    fun restore(): Pair<List<BrowserTab>, String?>
    fun save(tabs: List<BrowserTab>, activeTabId: String)
}
