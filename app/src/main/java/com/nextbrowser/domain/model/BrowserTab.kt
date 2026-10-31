package com.nextbrowser.domain.model

data class BrowserTab(
    val id: String,
    val url: String? = null,
    val title: String = "New tab",
    val isPrivate: Boolean = false
)
