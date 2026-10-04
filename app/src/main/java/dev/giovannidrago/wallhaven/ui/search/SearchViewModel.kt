package dev.giovannidrago.wallhaven.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.giovannidrago.wallhaven.data.db.WallpaperEntity
import dev.giovannidrago.wallhaven.data.prefs.AccountPreferences
import dev.giovannidrago.wallhaven.data.prefs.FilterPreferences
import dev.giovannidrago.wallhaven.data.repository.WallpaperRepository
import dev.giovannidrago.wallhaven.domain.CategoryFilter
import dev.giovannidrago.wallhaven.domain.RatioFormat
import dev.giovannidrago.wallhaven.domain.SearchOptions
import dev.giovannidrago.wallhaven.domain.SearchSorting
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MS = 450L

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
	savedStateHandle: SavedStateHandle,
	private val repository: WallpaperRepository,
	private val filterPreferences: FilterPreferences,
	accountPreferences: AccountPreferences
) : ViewModel() {
	private val _query = MutableStateFlow(savedStateHandle.get<String>("initialQuery").orEmpty())
	val query: StateFlow<String> = _query.asStateFlow()

	val options: StateFlow<SearchOptions> = filterPreferences.searchOptions
		.stateIn(viewModelScope, SharingStarted.Eagerly, SearchOptions())

	val apiKeyPresent: StateFlow<Boolean> = repository.apiKey
		.map { it.isNotBlank() }
		.stateIn(viewModelScope, SharingStarted.Eagerly, repository.apiKey.value.isNotBlank())

	val accountPurity: StateFlow<Set<String>> = accountPreferences.accountPurity
		.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

	val results: Flow<PagingData<WallpaperEntity>> = combine(
		_query
			.debounce(SEARCH_DEBOUNCE_MS)
			.map { it.trim() }
			.distinctUntilChanged(),
		filterPreferences.searchOptions
	) { text, storedOptions -> storedOptions.copy(query = text) }
		// The search runs even with a blank text: wallhaven accepts filter-only listings.
		.flatMapLatest { searchOptions -> repository.search(searchOptions) }
		.cachedIn(viewModelScope)

	fun onQueryChange(value: String) {
		_query.value = value
	}

	fun setSorting(sorting: SearchSorting) {
		updateOptions { it.copy(sorting = sorting) }
	}

	fun setCategory(category: CategoryFilter, enabled: Boolean) {
		updateOptions { current ->
			val updated = current.withCategory(category, enabled)
			// Turning every category off would make wallhaven return nothing, so the last one stays on.
			if (updated.categoryCount == 0) {
				current
			} else {
				updated
			}
		}
	}

	fun setSfw(enabled: Boolean) {
		updatePurity { it.copy(sfw = enabled) }
	}

	fun setSketchy(sketchy: Boolean) {
		updatePurity { it.copy(sketchy = sketchy) }
	}

	fun setNsfw(nsfw: Boolean) {
		updatePurity { it.copy(nsfw = nsfw) }
	}

	fun setColor(color: String?) {
		updateOptions { it.copy(color = color) }
	}

	fun setAtleast(atleast: String?) {
		updateOptions { it.copy(atleast = atleast) }
	}

	fun toggleResolution(resolution: String) {
		updateOptions { current ->
			val resolutions = if (resolution in current.resolutions) {
				current.resolutions - resolution
			} else {
				current.resolutions + resolution
			}
			current.copy(resolutions = resolutions)
		}
	}

	/** Selecting a format replaces the current one; selecting the active one (or null) clears it. */
	fun setRatioPreset(format: RatioFormat?) {
		updateOptions { current ->
			if (format == null || format.matches(current.ratios)) {
				current.copy(ratios = emptyList())
			} else {
				current.copy(ratios = format.ratios)
			}
		}
	}

	fun setRatio(ratio: String) {
		updateOptions { current ->
			if (current.ratios == listOf(ratio)) {
				current.copy(ratios = emptyList())
			} else {
				current.copy(ratios = listOf(ratio))
			}
		}
	}

	fun resetFilters() {
		updateOptions { SearchOptions() }
	}

	fun toggleFavorite(wallpaper: WallpaperEntity, favorited: Boolean) {
		viewModelScope.launch {
			repository.setFavorite(wallpaper.id, favorited)
		}
	}

	/** At least one purity tier must stay enabled, exactly like the wallhaven search page. */
	private fun updatePurity(transform: (SearchOptions) -> SearchOptions) {
		updateOptions { current ->
			val updated = transform(current)
			if (updated.purityCount == 0) {
				current
			} else {
				updated
			}
		}
	}

	private fun updateOptions(transform: (SearchOptions) -> SearchOptions) {
		viewModelScope.launch {
			filterPreferences.updateSearch(transform)
		}
	}
}
