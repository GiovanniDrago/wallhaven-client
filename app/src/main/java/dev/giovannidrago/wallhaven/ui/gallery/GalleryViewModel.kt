package dev.giovannidrago.wallhaven.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.giovannidrago.wallhaven.data.db.WallpaperEntity
import dev.giovannidrago.wallhaven.data.prefs.FilterPreferences
import dev.giovannidrago.wallhaven.data.repository.WallpaperRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalPagingApi::class)
@HiltViewModel
class GalleryViewModel @Inject constructor(
	private val repository: WallpaperRepository,
	filterPreferences: FilterPreferences
) : ViewModel() {
	val gallery: Flow<PagingData<WallpaperEntity>> = filterPreferences.filters
		.flatMapLatest { filters -> repository.gallery(filters) }
		.cachedIn(viewModelScope)

	fun toggleFavorite(wallpaper: WallpaperEntity, favorited: Boolean) {
		viewModelScope.launch {
			repository.setFavorite(wallpaper.id, favorited)
		}
	}
}
