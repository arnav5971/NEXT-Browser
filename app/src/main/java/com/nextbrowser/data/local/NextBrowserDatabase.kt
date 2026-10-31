package com.nextbrowser.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BookmarkEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NextBrowserDatabase : RoomDatabase() {
    abstract fun libraryDao(): BrowserLibraryDao

    companion object {
        @Volatile private var INSTANCE: NextBrowserDatabase? = null

        fun getInstance(context: Context): NextBrowserDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NextBrowserDatabase::class.java,
                    "next_browser_library.db"
                ).build().also { INSTANCE = it }
            }
    }
}
