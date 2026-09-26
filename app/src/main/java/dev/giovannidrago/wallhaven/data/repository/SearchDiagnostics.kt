package dev.giovannidrago.wallhaven.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Keeps the last search request parameters and the purity breakdown of the response, so the
 * Settings screen can show what was actually asked and returned when a filter looks ineffective.
 */
@Singleton
class SearchDiagnostics @Inject constructor() {
	private val _lastRequest = MutableStateFlow<String?>(null)
	val lastRequest: StateFlow<String?> = _lastRequest.asStateFlow()

	private val _lastResponse = MutableStateFlow<String?>(null)
	val lastResponse: StateFlow<String?> = _lastResponse.asStateFlow()

	fun recordRequest(summary: String) {
		_lastRequest.value = summary
	}

	fun recordResponse(summary: String) {
		_lastResponse.value = summary
	}
}
