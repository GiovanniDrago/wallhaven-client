package dev.giovannidrago.wallhaven.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.giovannidrago.wallhaven.data.db.WallpaperEntity
import dev.giovannidrago.wallhaven.data.repository.WallpaperRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor(
	private val repository: WallpaperRepository
) : ViewModel() {
	val favorites: Flow<PagingData<WallpaperEntity>> = repository.favorites()
		.cachedIn(viewModelScope)

	fun toggleFavorite(wallpaper: WallpaperEntity, favorited: Boolean) {
		viewModelScope.launch {
			repository.setFavorite(wallpaper.id, favorited)
		}
	}
}
