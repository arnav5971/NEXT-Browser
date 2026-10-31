package com.nextbrowser.data.local

import android.content.Context
import com.nextbrowser.domain.model.BrowserTab
import org.json.JSONArray
import org.json.JSONObject

object BrowserSessionStore {
    private const val PREFS = "next_browser_session"
    private const val KEY_TABS = "regular_tabs"
    private const val KEY_ACTIVE = "active_tab_id"

    fun restore(context: Context): Pair<List<BrowserTab>, String?> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_TABS, null) ?: return emptyList<BrowserTab>() to null
        return try {
            val array = JSONArray(raw)
            val tabs = buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val id = item.optString("id").takeIf { it.isNotBlank() } ?: continue
                    add(BrowserTab(id = id, url = item.optString("url").takeIf { it.isNotBlank() }, title = item.optString("title", "New tab"), isPrivate = false))
                }
            }
            tabs to prefs.getString(KEY_ACTIVE, null)
        } catch (_: Exception) {
            emptyList<BrowserTab>() to null
        }
    }

    fun save(context: Context, tabs: List<BrowserTab>, activeTabId: String) {
        val regularTabs = tabs.filterNot { it.isPrivate }
        val array = JSONArray()
        regularTabs.forEach { tab ->
            array.put(JSONObject().apply {
                put("id", tab.id)
                put("url", tab.url ?: "")
                put("title", tab.title)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_TABS, array.toString())
            .putString(KEY_ACTIVE, activeTabId.takeIf { id -> regularTabs.any { it.id == id } } ?: regularTabs.firstOrNull()?.id)
            .apply()
    }
}