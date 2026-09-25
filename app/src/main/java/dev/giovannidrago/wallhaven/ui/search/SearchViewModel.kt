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
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MS = 300L

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
	savedStateHandle: SavedStateHandle,
	private val repository: WallpaperRepository,
	private val filterPreferences: FilterPreferences
) : ViewModel() {
	private val _query = MutableStateFlow(savedStateHandle.get<String>("initialQuery").orEmpty())
	val query: StateFlow<String> = _query.asStateFlow()

	val results: Flow<PagingData<WallpaperEntity>> = combine(
		_query
			.debounce(SEARCH_DEBOUNCE_MS)
			.map { it.trim() }
			.distinctUntilChanged(),
		filterPreferences.filters
	) { query, filters -> query to filters }
		.flatMapLatest { (query, filters) ->
			if (query.isBlank()) {
				flowOf(PagingData.empty<WallpaperEntity>())
			} else {
				repository.search(query, filters)
			}
		}
		.cachedIn(viewModelScope)

	fun onQueryChange(value: String) {
		_query.value = value
	}

	fun toggleFavorite(wallpaper: WallpaperEntity, favorited: Boolean) {
		viewModelScope.launch {
			repository.setFavorite(wallpaper.id, favorited)
		}
	}
}
