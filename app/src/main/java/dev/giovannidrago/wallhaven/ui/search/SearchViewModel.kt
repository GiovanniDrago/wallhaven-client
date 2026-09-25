package dev.giovannidrago.wallhaven.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.giovannidrago.wallhaven.data.db.WallpaperEntity
import dev.giovannidrago.wallhaven.data.prefs.FilterPreferences
import dev.giovannidrago.wallhaven.data.repository.WallpaperRepository
import dev.giovannidrago.wallhaven.domain.CategoryFilter
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MS = 450L

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
	savedStateHandle: SavedStateHandle,
	private val repository: WallpaperRepository,
	private val filterPreferences: FilterPreferences
) : ViewModel() {
	private val _query = MutableStateFlow(savedStateHandle.get<String>("initialQuery").orEmpty())
	val query: StateFlow<String> = _query.asStateFlow()

	val options: StateFlow<SearchOptions> = filterPreferences.searchOptions
		.stateIn(viewModelScope, SharingStarted.Eagerly, SearchOptions())

	val apiKeyPresent: StateFlow<Boolean> = repository.apiKey
		.map { it.isNotBlank() }
		.stateIn(viewModelScope, SharingStarted.Eagerly, repository.apiKey.value.isNotBlank())

	val results: Flow<PagingData<WallpaperEntity>> = combine(
		_query
			.debounce(SEARCH_DEBOUNCE_MS)
			.map { it.trim() }
			.distinctUntilChanged(),
		filterPreferences.searchOptions
	) { text, storedOptions -> storedOptions.copy(query = text) to text }
		.flatMapLatest { (searchOptions, text) ->
			if (text.isBlank()) {
				flowOf(PagingData.empty<WallpaperEntity>())
			} else {
				repository.search(searchOptions)
			}
		}
		.cachedIn(viewModelScope)

	fun onQueryChange(value: String) {
		_query.value = value
	}

	fun setSorting(sorting: SearchSorting) {
		updateOptions(options.value.copy(sorting = sorting))
	}

	fun setCategory(category: CategoryFilter, enabled: Boolean) {
		val updated = options.value.withCategory(category, enabled)
		// Turning every category off would make wallhaven return nothing, so the last one stays on.
		if (updated.categoryCount == 0) {
			return
		}

		updateOptions(updated)
	}

	fun setSketchy(sketchy: Boolean) {
		updateOptions(options.value.copy(sketchy = sketchy))
	}

	fun setNsfw(nsfw: Boolean) {
		updateOptions(options.value.copy(nsfw = nsfw))
	}

	fun setColor(color: String?) {
		updateOptions(options.value.copy(color = color))
	}

	fun resetFilters() {
		updateOptions(SearchOptions())
	}

	fun toggleFavorite(wallpaper: WallpaperEntity, favorited: Boolean) {
		viewModelScope.launch {
			repository.setFavorite(wallpaper.id, favorited)
		}
	}

	private fun updateOptions(updated: SearchOptions) {
		viewModelScope.launch {
			filterPreferences.updateSearch(updated)
		}
	}
}
