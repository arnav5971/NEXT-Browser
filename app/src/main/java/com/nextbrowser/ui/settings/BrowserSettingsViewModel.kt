package com.nextbrowser.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextbrowser.domain.model.BrowserSettings
import com.nextbrowser.domain.repository.BrowserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class BrowserSettingsViewModel @Inject constructor(
    private val repository: BrowserSettingsRepository
) : ViewModel() {

    val settings: StateFlow<BrowserSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BrowserSettings()
    )

    fun setThemeMode(mode: String) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setDynamicColorEnabled(enabled) }
    }

    fun setAccentColor(hexColor: String) {
        viewModelScope.launch { repository.setAccentColor(hexColor) }
    }

    fun setSearchEngine(engine: String) {
        viewModelScope.launch { repository.setSearchEngine(engine) }
    }
}
