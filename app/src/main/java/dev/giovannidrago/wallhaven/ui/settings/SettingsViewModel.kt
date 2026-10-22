package dev.giovannidrago.wallhaven.ui.settings

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.data.api.dto.UserSettingsDto
import dev.giovannidrago.wallhaven.data.prefs.AccountPreferences
import dev.giovannidrago.wallhaven.data.prefs.ApiKeyStore
import dev.giovannidrago.wallhaven.data.prefs.FilterPreferences
import dev.giovannidrago.wallhaven.data.repository.SearchDiagnostics
import dev.giovannidrago.wallhaven.data.repository.UpdateCheckResult
import dev.giovannidrago.wallhaven.data.repository.UpdateRepository
import dev.giovannidrago.wallhaven.data.repository.WallpaperRepository
import dev.giovannidrago.wallhaven.domain.Sorting
import dev.giovannidrago.wallhaven.domain.WallhavenFilters
import java.time.LocalDate
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
	private val filterPreferences: FilterPreferences,
	private val accountPreferences: AccountPreferences,
	private val updateRepository: UpdateRepository,
	diagnostics: SearchDiagnostics
) : ViewModel() {
	val apiKeyPresent: StateFlow<Boolean> = apiKeyStore.apiKey
		.map { it.isNotBlank() }
		.stateIn(viewModelScope, SharingStarted.Eagerly, apiKeyStore.apiKey.value.isNotBlank())

	val filters: StateFlow<WallhavenFilters> = filterPreferences.filters
		.stateIn(viewModelScope, SharingStarted.Eagerly, WallhavenFilters())

	val accountPurity: StateFlow<Set<String>> = accountPreferences.accountPurity
		.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

	val accountBlacklistCount: StateFlow<Int> = accountPreferences.accountBlacklistCount
		.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

	val lastRequest: StateFlow<String?> = diagnostics.lastRequest
	val lastResponse: StateFlow<String?> = diagnostics.lastResponse

	val installedVersion: String = updateRepository.installedVersion

	private val _update = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
	val update: StateFlow<UpdateUiState> = _update.asStateFlow()

	private val _apiKeyInvalid = MutableStateFlow(false)
	val apiKeyInvalid: StateFlow<Boolean> = _apiKeyInvalid.asStateFlow()

	private val _saving = MutableStateFlow(false)
	val saving: StateFlow<Boolean> = _saving.asStateFlow()

	private val _message = MutableStateFlow<SettingsMessage?>(null)
	val message: StateFlow<SettingsMessage?> = _message.asStateFlow()

	init {
		// Picks up keys regenerated on wallhaven since they were saved, and refreshes the account limits.
		val storedKey = apiKeyStore.apiKey.value
		if (storedKey.isNotBlank()) {
			viewModelScope.launch {
				runCatching { repository.verifyApiKey(storedKey) }
					.onSuccess { settings ->
						_apiKeyInvalid.value = false
						storeAccountSettings(settings)
					}
					.onFailure {
						_apiKeyInvalid.value = true
					}
			}
		}
	}

	/** Verifies the key against wallhaven first, so an invalid key is never stored. */
	fun saveApiKey(apiKey: String) {
		val trimmed = apiKey.trim()
		if (trimmed.isEmpty() || _saving.value) {
			return
		}

		viewModelScope.launch {
			_saving.value = true
			runCatching { repository.verifyApiKey(trimmed) }
				.onSuccess { settings ->
					apiKeyStore.set(trimmed)
					_apiKeyInvalid.value = false
					storeAccountSettings(settings)
					_message.value = SettingsMessage.VALID
				}
				.onFailure { error ->
					_message.value = if (error is HttpException && error.code() == 401) {
						SettingsMessage.INVALID
					} else {
						SettingsMessage.SAVE_ERROR
					}
				}
			_saving.value = false
		}
	}

	fun removeApiKey() {
		apiKeyStore.clear()
		_apiKeyInvalid.value = false
		viewModelScope.launch {
			accountPreferences.clear()
		}
		_message.value = SettingsMessage.REMOVED
	}

	fun setSorting(sorting: Sorting) {
		viewModelScope.launch {
			filterPreferences.update { current -> current.copy(sorting = sorting) }
		}
	}

	fun checkForUpdates() {
		if (_update.value is UpdateUiState.Checking) {
			return
		}
		_update.value = UpdateUiState.Checking
		viewModelScope.launch {
			val today = LocalDate.now().toString()
			when (val result = updateRepository.check()) {
				is UpdateCheckResult.UpdateAvailable -> {
					_update.value = UpdateUiState.Available(result.version, result.downloadUrl)
					updateRepository.markChecked(today)
				}
				UpdateCheckResult.UpToDate -> {
					_update.value = UpdateUiState.NoUpdate
					updateRepository.markChecked(today)
				}
				UpdateCheckResult.Error -> _update.value = UpdateUiState.CheckError
			}
		}
	}

	fun consumeUpdate() {
		_update.value = UpdateUiState.Idle
	}

	fun consumeMessage() {
		_message.value = null
	}

	private suspend fun storeAccountSettings(settings: UserSettingsDto) {
		accountPreferences.update(
			purity = settings.purity,
			blacklistCount = settings.tagBlacklist.size + settings.userBlacklist.size
		)
	}
}

enum class SettingsMessage(@StringRes val textRes: Int) {
	VALID(R.string.settings_api_key_valid),
	INVALID(R.string.settings_api_key_invalid),
	SAVE_ERROR(R.string.settings_api_key_save_error),
	REMOVED(R.string.settings_api_key_removed)
}

sealed interface UpdateUiState {
	data object Idle : UpdateUiState

	data object Checking : UpdateUiState

	data object NoUpdate : UpdateUiState

	data class Available(val version: String, val downloadUrl: String) : UpdateUiState

	data object CheckError : UpdateUiState
}
