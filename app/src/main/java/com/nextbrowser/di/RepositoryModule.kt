package com.nextbrowser.di

import com.nextbrowser.data.datastore.DefaultBrowserSettingsRepository
import com.nextbrowser.data.repository.DefaultBrowserLibraryRepository
import com.nextbrowser.data.repository.DefaultBrowserSessionRepository
import com.nextbrowser.domain.repository.BrowserLibraryRepository
import com.nextbrowser.domain.repository.BrowserSessionRepository
import com.nextbrowser.domain.repository.BrowserSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBrowserSettingsRepository(
        implementation: DefaultBrowserSettingsRepository
    ): BrowserSettingsRepository

    @Binds
    @Singleton
    abstract fun bindBrowserLibraryRepository(
        implementation: DefaultBrowserLibraryRepository
    ): BrowserLibraryRepository

    @Binds
    @Singleton
    abstract fun bindBrowserSessionRepository(
        implementation: DefaultBrowserSessionRepository
    ): BrowserSessionRepository
}
