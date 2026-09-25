package dev.giovannidrago.wallhaven.ui.settings

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.data.prefs.ApiKeyStore
import dev.giovannidrago.wallhaven.data.prefs.FilterPreferences
import dev.giovannidrago.wallhaven.data.repository.WallpaperRepository
import dev.giovannidrago.wallhaven.domain.Sorting
import dev.giovannidrago.wallhaven.domain.WallhavenFilters
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class SettingsViewModel @Inject constructor(
	private val repository: WallpaperRepository,
	private val apiKeyStore: ApiKeyStore,
	private val filterPreferences: FilterPreferences
) : ViewModel() {
	val apiKeyPresent: StateFlow<Boolean> = apiKeyStore.apiKey
		.map { it.isNotBlank() }
		.stateIn(viewModelScope, SharingStarted.Eagerly, apiKeyStore.apiKey.value.isNotBlank())

	val filters: StateFlow<WallhavenFilters> = filterPreferences.filters
		.stateIn(viewModelScope, SharingStarted.Eagerly, WallhavenFilters())

	private val _saving = MutableStateFlow(false)
	val saving: StateFlow<Boolean> = _saving.asStateFlow()

	private val _message = MutableStateFlow<SettingsMessage?>(null)
	val message: StateFlow<SettingsMessage?> = _message.asStateFlow()

	/** Verifies the key against wallhaven first, so an invalid key is never stored. */
	fun saveApiKey(apiKey: String) {
		val trimmed = apiKey.trim()
		if (trimmed.isEmpty() || _saving.value) {
			return
		}

		viewModelScope.launch {
			_saving.value = true
			val result = runCatching { repository.verifyApiKey(trimmed) }
			_message.value = result.fold(
				onSuccess = {
					apiKeyStore.set(trimmed)
					SettingsMessage.VALID
				},
				onFailure = { error ->
					if (error is HttpException && error.code() == 401) {
						SettingsMessage.INVALID
					} else {
						SettingsMessage.SAVE_ERROR
					}
				}
			)
			_saving.value = false
		}
	}

	fun removeApiKey() {
		apiKeyStore.clear()
		_message.value = SettingsMessage.REMOVED
	}

	fun setSorting(sorting: Sorting) {
		viewModelScope.launch {
			filterPreferences.update(filters.value.copy(sorting = sorting))
		}
	}

	fun consumeMessage() {
		_message.value = null
	}
}

enum class SettingsMessage(@StringRes val textRes: Int) {
	VALID(R.string.settings_api_key_valid),
	INVALID(R.string.settings_api_key_invalid),
	SAVE_ERROR(R.string.settings_api_key_save_error),
	REMOVED(R.string.settings_api_key_removed)
}
