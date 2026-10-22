package dev.giovannidrago.wallhaven.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.giovannidrago.wallhaven.data.prefs.ApiKeyStore
import dev.giovannidrago.wallhaven.data.prefs.FilterPreferences
import dev.giovannidrago.wallhaven.data.repository.AlbumRepository
import dev.giovannidrago.wallhaven.data.repository.UpdateCheckResult
import dev.giovannidrago.wallhaven.data.repository.UpdateRepository
import dev.giovannidrago.wallhaven.domain.CategoryFilter
import dev.giovannidrago.wallhaven.domain.Sorting
import dev.giovannidrago.wallhaven.domain.WallhavenFilters
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
	private val filterPreferences: FilterPreferences,
	private val apiKeyStore: ApiKeyStore,
	private val albumRepository: AlbumRepository,
	private val updateRepository: UpdateRepository
) : ViewModel() {
	val filters: StateFlow<WallhavenFilters> = filterPreferences.filters
		.stateIn(viewModelScope, SharingStarted.Eagerly, WallhavenFilters())

	val apiKeyPresent: StateFlow<Boolean> = apiKeyStore.apiKey
		.map { it.isNotBlank() }
		.stateIn(viewModelScope, SharingStarted.Eagerly, apiKeyStore.apiKey.value.isNotBlank())

	private val _updateAvailable = MutableStateFlow<UpdateCheckResult.UpdateAvailable?>(null)
	val updateAvailable: StateFlow<UpdateCheckResult.UpdateAvailable?> = _updateAvailable.asStateFlow()

	init {
		// Resolves imported album placeholders and caches missing album images on app start (FR-016).
		viewModelScope.launch {
			runCatching { albumRepository.syncPendingWork() }
		}
		// Silent update check: at most once per calendar day, never blocking the UI.
		viewModelScope.launch {
			val today = LocalDate.now().toString()
			if (!updateRepository.shouldRunSilentCheck(today)) {
				return@launch
			}
			when (val result = updateRepository.check()) {
				is UpdateCheckResult.UpdateAvailable -> {
					_updateAvailable.value = result
					updateRepository.markChecked(today)
				}
				UpdateCheckResult.UpToDate -> updateRepository.markChecked(today)
				UpdateCheckResult.Error -> Unit
			}
		}
	}

	fun consumeUpdateAvailable() {
		_updateAvailable.value = null
	}

	fun setSketchy(sketchy: Boolean) {
		viewModelScope.launch {
			filterPreferences.update { current -> current.copy(sketchy = sketchy) }
		}
	}

	fun setNsfw(nsfw: Boolean) {
		viewModelScope.launch {
			filterPreferences.update { current -> current.copy(nsfw = nsfw) }
		}
	}

	fun setCategory(category: CategoryFilter, enabled: Boolean) {
		viewModelScope.launch {
			filterPreferences.update { current ->
				val updated = current.withCategory(category, enabled)
				// Turning every category off would make wallhaven return nothing, so the last one stays on.
				if (updated.categoryCount == 0) {
					current
				} else {
					updated
				}
			}
		}
	}

	fun setSorting(sorting: Sorting) {
		viewModelScope.launch {
			filterPreferences.update { current -> current.copy(sorting = sorting) }
		}
	}
}
