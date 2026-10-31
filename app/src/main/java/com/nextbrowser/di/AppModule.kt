package com.nextbrowser.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.nextbrowser.data.local.BrowserLibraryDao
import com.nextbrowser.data.local.NextBrowserDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("next_browser_settings.preferences_pb") }
    )

    @Provides
    @Singleton
    fun provideBrowserDatabase(
        @ApplicationContext context: Context
    ): NextBrowserDatabase = Room.databaseBuilder(
        context,
        NextBrowserDatabase::class.java,
        "next_browser_library.db"
    ).build()

    @Provides
    fun provideBrowserLibraryDao(database: NextBrowserDatabase): BrowserLibraryDao =
        database.libraryDao()
}
