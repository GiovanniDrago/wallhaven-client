package dev.giovannidrago.wallhaven.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.giovannidrago.wallhaven.data.repository.AlbumRepository
import dev.giovannidrago.wallhaven.domain.AlbumSummary
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AlbumsEvent {
	data object Created : AlbumsEvent

	data object BlankName : AlbumsEvent

	data object Deleted : AlbumsEvent
}

@HiltViewModel
class AlbumsViewModel @Inject constructor(
	private val repository: AlbumRepository
) : ViewModel() {
	val albums: StateFlow<List<AlbumSummary>> = repository.albums()
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

	private val _event = MutableStateFlow<AlbumsEvent?>(null)
	val event: StateFlow<AlbumsEvent?> = _event.asStateFlow()

	fun createAlbum(name: String) {
		viewModelScope.launch {
			val album = repository.createAlbum(name)
			_event.value = if (album == null) AlbumsEvent.BlankName else AlbumsEvent.Created
		}
	}

	fun deleteAlbum(id: String) {
		viewModelScope.launch {
			repository.deleteAlbum(id)
			_event.value = AlbumsEvent.Deleted
		}
	}

	fun consumeEvent() {
		_event.value = null
	}
}

private const val STOP_TIMEOUT_MS = 5_000L
