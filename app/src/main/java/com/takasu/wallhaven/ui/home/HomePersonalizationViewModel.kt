package com.takasu.wallhaven.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.takasu.wallhaven.data.prefs.ApiKeyStore
import com.takasu.wallhaven.data.repository.HomePersonalizationRepository
import com.takasu.wallhaven.domain.CategoryFilter
import com.takasu.wallhaven.domain.RatioFormat
import com.takasu.wallhaven.domain.Sorting
import com.takasu.wallhaven.domain.WallhavenFilters
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomePersonalizationViewModel @Inject constructor(
	private val repository: HomePersonalizationRepository,
	apiKeyStore: ApiKeyStore
) : ViewModel() {
	val options: StateFlow<WallhavenFilters> = repository.persisted
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), WallhavenFilters())

	val apiKeyPresent: StateFlow<Boolean> = apiKeyStore.apiKey
		.map { it.isNotBlank() }
		.stateIn(viewModelScope, SharingStarted.Eagerly, apiKeyStore.apiKey.value.isNotBlank())

	fun setSorting(sorting: Sorting) = persist { it.copy(sorting = sorting) }

	fun setCategory(category: CategoryFilter, enabled: Boolean) = persist { current ->
		val updated = current.withCategory(category, enabled)
		if (updated.categoryCount == 0) current else updated
	}

	fun setSfw(enabled: Boolean) = persistPurity { it.copy(sfw = enabled) }

	fun setSketchy(enabled: Boolean) = persistPurity { it.copy(sketchy = enabled) }

	fun setNsfw(enabled: Boolean) = persistPurity { it.copy(nsfw = enabled) }

	fun setColor(color: String?) = persist { it.copy(color = color) }

	fun setAtleast(value: String?) = persist { it.copy(atleast = value) }

	fun toggleResolution(resolution: String) = persist { current ->
		if (resolution in current.resolutions) {
			current.copy(resolutions = current.resolutions - resolution)
		} else {
			current.copy(resolutions = current.resolutions + resolution)
		}
	}

	fun setRatioPreset(format: RatioFormat?) = persist { current ->
		if (format == null || format.matches(current.ratios)) {
			current.copy(ratios = emptyList())
		} else {
			current.copy(ratios = format.ratios)
		}
	}

	fun setRatio(ratio: String) = persist { current ->
		if (current.ratios == listOf(ratio)) {
			current.copy(ratios = emptyList())
		} else {
			current.copy(ratios = listOf(ratio))
		}
	}

	fun reset() {
		viewModelScope.launch { repository.reset() }
	}

	private fun persistPurity(transform: (WallhavenFilters) -> WallhavenFilters) {
		persist { current ->
			val updated = transform(current)
			if (updated.purityCount == 0) current else updated
		}
	}

	private fun persist(transform: (WallhavenFilters) -> WallhavenFilters) {
		viewModelScope.launch { repository.updatePersisted(transform) }
	}
}

private const val STOP_TIMEOUT_MS = 5_000L
