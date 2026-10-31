package com.nextbrowser.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "history", indices = [Index(value = ["visitedAt"])])
data class HistoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val url: String,
    val visitedAt: Long = System.currentTimeMillis()
)
