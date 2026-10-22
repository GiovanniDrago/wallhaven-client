package dev.giovannidrago.wallhaven.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/** Remembers the calendar day of the last automatic update check (throttling). */
@Singleton
class UpdatePreferences @Inject constructor(
	private val dataStore: DataStore<Preferences>
) {
	suspend fun lastCheckDate(): String? = dataStore.data.first()[KEY_LAST_CHECK]

	suspend fun markChecked(date: String) {
		dataStore.edit { preferences ->
			preferences[KEY_LAST_CHECK] = date
		}
	}
}

private val KEY_LAST_CHECK = stringPreferencesKey("last_update_check")
