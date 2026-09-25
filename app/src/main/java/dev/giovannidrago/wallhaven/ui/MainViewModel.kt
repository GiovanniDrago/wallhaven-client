package dev.giovannidrago.wallhaven.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.giovannidrago.wallhaven.data.prefs.ApiKeyStore
import dev.giovannidrago.wallhaven.data.prefs.FilterPreferences
import dev.giovannidrago.wallhaven.domain.CategoryFilter
import dev.giovannidrago.wallhaven.domain.Sorting
import dev.giovannidrago.wallhaven.domain.WallhavenFilters
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
	private val filterPreferences: FilterPreferences,
	private val apiKeyStore: ApiKeyStore
) : ViewModel() {
	val filters: StateFlow<WallhavenFilters> = filterPreferences.filters
		.stateIn(viewModelScope, SharingStarted.Eagerly, WallhavenFilters())

	val apiKeyPresent: StateFlow<Boolean> = apiKeyStore.apiKey
		.map { it.isNotBlank() }
		.stateIn(viewModelScope, SharingStarted.Eagerly, apiKeyStore.apiKey.value.isNotBlank())

	fun setSketchy(sketchy: Boolean) {
		viewModelScope.launch {
			filterPreferences.update(filters.value.copy(sketchy = sketchy))
		}
	}

	fun setNsfw(nsfw: Boolean) {
		viewModelScope.launch {
			filterPreferences.update(filters.value.copy(nsfw = nsfw))
		}
	}

	fun setCategory(category: CategoryFilter, enabled: Boolean) {
		val updated = filters.value.withCategory(category, enabled)
		// Turning every category off would make wallhaven return nothing, so the last one stays on.
		if (updated.categoryCount == 0) {
			return
		}

		viewModelScope.launch {
			filterPreferences.update(updated)
		}
	}

	fun setSorting(sorting: Sorting) {
		viewModelScope.launch {
			filterPreferences.update(filters.value.copy(sorting = sorting))
		}
	}
}
