package com.nextbrowser.data.repository

import android.content.Context
import com.nextbrowser.data.local.BrowserSessionStore
import com.nextbrowser.domain.model.BrowserTab
import com.nextbrowser.domain.repository.BrowserSessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultBrowserSessionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : BrowserSessionRepository {
    override fun restore(): Pair<List<BrowserTab>, String?> =
        BrowserSessionStore.restore(context)

    override fun save(tabs: List<BrowserTab>, activeTabId: String) {
        BrowserSessionStore.save(context, tabs, activeTabId)
    }
}
