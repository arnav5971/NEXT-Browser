package com.nextbrowser.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks", indices = [Index(value = ["url"], unique = true)])
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val title: String,
    val url: String,
    val folder: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
