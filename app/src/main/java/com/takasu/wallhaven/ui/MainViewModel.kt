package com.takasu.wallhaven.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.takasu.wallhaven.data.prefs.ApiKeyStore
import com.takasu.wallhaven.data.repository.AlbumRepository
import com.takasu.wallhaven.data.repository.HomePersonalizationRepository
import com.takasu.wallhaven.data.repository.UpdateCheckResult
import com.takasu.wallhaven.data.repository.UpdateRepository
import com.takasu.wallhaven.domain.CategoryFilter
import com.takasu.wallhaven.domain.Sorting
import com.takasu.wallhaven.domain.WallhavenFilters
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
	private val homePersonalizationRepository: HomePersonalizationRepository,
	private val apiKeyStore: ApiKeyStore,
	private val albumRepository: AlbumRepository,
	private val updateRepository: UpdateRepository
) : ViewModel() {
	val filters: StateFlow<WallhavenFilters> = homePersonalizationRepository.effective

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

	fun setSketchy(sketchy: Boolean) = homePersonalizationRepository.setQuickSketchy(sketchy)

	fun setNsfw(nsfw: Boolean) = homePersonalizationRepository.setQuickNsfw(nsfw)

	fun setCategory(category: CategoryFilter, enabled: Boolean) =
		homePersonalizationRepository.setQuickCategory(category, enabled)

	fun setSorting(sorting: Sorting) = homePersonalizationRepository.setQuickSorting(sorting)
}
