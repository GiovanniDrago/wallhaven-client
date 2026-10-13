package dev.giovannidrago.wallhaven.ui.detail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.giovannidrago.wallhaven.data.db.WallpaperEntity
import dev.giovannidrago.wallhaven.data.repository.AlbumRepository
import dev.giovannidrago.wallhaven.data.repository.WallpaperDownloader
import dev.giovannidrago.wallhaven.data.repository.WallpaperRepository
import dev.giovannidrago.wallhaven.domain.AlbumSummary
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DownloadState {
	data object Idle : DownloadState

	data class Downloading(val downloadedBytes: Long, val totalBytes: Long?) : DownloadState {
		val progress: Float?
			get() = totalBytes?.takeIf { it > 0 }?.let { total -> (downloadedBytes.toFloat() / total).coerceIn(0f, 1f) }
	}

	data class Success(val uri: Uri) : DownloadState

	data class Failed(val error: Throwable) : DownloadState
}

enum class AlbumEvent {
	Added,
	BlankName
}

@HiltViewModel
class DetailViewModel @Inject constructor(
	savedStateHandle: SavedStateHandle,
	private val repository: WallpaperRepository,
	private val albumRepository: AlbumRepository,
	private val downloader: WallpaperDownloader
) : ViewModel() {
	private val wallpaperId: String = checkNotNull(savedStateHandle["id"])

	val wallpaper: StateFlow<WallpaperEntity?> = repository.observeWallpaper(wallpaperId)
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

	private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
	val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

	private val _albumChoices = MutableStateFlow<List<AlbumSummary>>(emptyList())
	val albumChoices: StateFlow<List<AlbumSummary>> = _albumChoices.asStateFlow()

	private val _albumEvent = MutableStateFlow<AlbumEvent?>(null)
	val albumEvent: StateFlow<AlbumEvent?> = _albumEvent.asStateFlow()

	init {
		// Enriches the cached entry with uploader and colors; offline it simply keeps the cached data.
		viewModelScope.launch {
			runCatching { repository.refreshWallpaper(wallpaperId) }
		}
	}

	fun loadAlbumChoices() {
		viewModelScope.launch {
			_albumChoices.value = albumRepository.albums().first()
		}
	}

	fun addToAlbum(albumId: String) {
		viewModelScope.launch {
			albumRepository.addToAlbum(albumId, wallpaperId)
			_albumEvent.value = AlbumEvent.Added
		}
	}

	fun createAlbumAndAdd(name: String) {
		viewModelScope.launch {
			val album = albumRepository.createAlbum(name)
			if (album == null) {
				_albumEvent.value = AlbumEvent.BlankName
			} else {
				albumRepository.addToAlbum(album.id, wallpaperId)
				_albumEvent.value = AlbumEvent.Added
			}
		}
	}

	fun consumeAlbumEvent() {
		_albumEvent.value = null
	}

	fun toggleFavorite() {
		val current = wallpaper.value ?: return
		viewModelScope.launch {
			repository.setFavorite(current.id, !current.favorited)
		}
	}

	fun download() {
		val current = wallpaper.value ?: return
		if (_downloadState.value is DownloadState.Downloading) {
			return
		}

		_downloadState.value = DownloadState.Downloading(0L, null)
		viewModelScope.launch {
			runCatching {
				downloader.download(current) { downloadedBytes, totalBytes ->
					_downloadState.value = DownloadState.Downloading(downloadedBytes, totalBytes)
				}
			}.onSuccess { uri ->
				_downloadState.value = DownloadState.Success(uri)
			}.onFailure { error ->
				_downloadState.value = DownloadState.Failed(error)
			}
		}
	}

	fun consumeDownloadState() {
		_downloadState.value = DownloadState.Idle
	}
}

private const val STOP_TIMEOUT_MS = 5_000L
