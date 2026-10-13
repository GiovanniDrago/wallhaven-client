package dev.giovannidrago.wallhaven.ui.albums

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.giovannidrago.wallhaven.data.db.AlbumItemEntity
import dev.giovannidrago.wallhaven.data.repository.AlbumRepository
import dev.giovannidrago.wallhaven.domain.Album
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface TransferState {
	data object Idle : TransferState

	data object Working : TransferState

	data object Exported : TransferState

	data class Imported(val name: String) : TransferState

	data object Failed : TransferState
}

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
	savedStateHandle: SavedStateHandle,
	private val repository: AlbumRepository
) : ViewModel() {
	private val albumId: String = checkNotNull(savedStateHandle["albumId"])

	val album: StateFlow<Album?> = repository.album(albumId)
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

	val items: Flow<PagingData<AlbumItemEntity>> = repository.albumItems(albumId)
		.cachedIn(viewModelScope)

	private val _transfer = MutableStateFlow<TransferState>(TransferState.Idle)
	val transfer: StateFlow<TransferState> = _transfer.asStateFlow()

	init {
		// Resolving placeholders and caching images happens when the album is opened (FR-016).
		viewModelScope.launch {
			runCatching { repository.syncAlbum(albumId) }
		}
	}

	fun removeWallpaper(wallpaperId: String) {
		viewModelScope.launch {
			repository.removeFromAlbum(albumId, wallpaperId)
		}
	}

	suspend fun buildExport(): String? = runCatching { repository.exportAlbumJson(albumId) }.getOrNull()

	fun importDocument(content: String) {
		_transfer.value = TransferState.Working
		viewModelScope.launch {
			repository.importAlbumJson(content)
				.onSuccess { imported ->
					_transfer.value = TransferState.Imported(imported.name)
					runCatching { repository.syncAlbum(imported.id) }
				}
				.onFailure { _transfer.value = TransferState.Failed }
		}
	}

	fun onExportResult(success: Boolean) {
		_transfer.value = if (success) TransferState.Exported else TransferState.Failed
	}

	fun deleteAlbum(onDeleted: () -> Unit) {
		viewModelScope.launch {
			repository.deleteAlbum(albumId)
			onDeleted()
		}
	}

	fun consumeTransfer() {
		_transfer.value = TransferState.Idle
	}
}

private const val STOP_TIMEOUT_MS = 5_000L
