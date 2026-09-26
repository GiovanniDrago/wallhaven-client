package dev.giovannidrago.wallhaven.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Caches the account settings returned by the wallhaven settings endpoint when the API key is
 * verified, so the UI can warn when a requested purity is not allowed by the account.
 */
@Singleton
class AccountPreferences @Inject constructor(
	private val dataStore: DataStore<Preferences>
) {
	val accountPurity: Flow<Set<String>> = dataStore.data.map { preferences ->
		preferences[KEY_ACCOUNT_PURITY] ?: emptySet()
	}

	val accountBlacklistCount: Flow<Int> = dataStore.data.map { preferences ->
		preferences[KEY_ACCOUNT_BLACKLIST_COUNT] ?: 0
	}

	suspend fun update(purity: List<String>, blacklistCount: Int) {
		dataStore.edit { preferences ->
			preferences[KEY_ACCOUNT_PURITY] = purity.toSet()
			preferences[KEY_ACCOUNT_BLACKLIST_COUNT] = blacklistCount
		}
	}

	suspend fun clear() {
		dataStore.edit { preferences ->
			preferences.remove(KEY_ACCOUNT_PURITY)
			preferences.remove(KEY_ACCOUNT_BLACKLIST_COUNT)
		}
	}
}

private val KEY_ACCOUNT_PURITY = stringSetPreferencesKey("account_purity")
private val KEY_ACCOUNT_BLACKLIST_COUNT = intPreferencesKey("account_blacklist_count")
