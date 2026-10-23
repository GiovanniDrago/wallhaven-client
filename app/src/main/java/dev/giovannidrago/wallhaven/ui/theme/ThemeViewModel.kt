package dev.giovannidrago.wallhaven.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.giovannidrago.wallhaven.data.prefs.ThemePreferences
import dev.giovannidrago.wallhaven.domain.ThemeSettings
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Exposes the persisted theme settings so the activity can resolve the effective dark/light state. */
@HiltViewModel
class ThemeViewModel @Inject constructor(
	private val themePreferences: ThemePreferences
) : ViewModel() {
	val settings: StateFlow<ThemeSettings> = themePreferences.settings
		.stateIn(viewModelScope, SharingStarted.Eagerly, ThemeSettings())
}
