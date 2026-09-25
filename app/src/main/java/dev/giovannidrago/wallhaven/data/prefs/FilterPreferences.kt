package dev.giovannidrago.wallhaven.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.giovannidrago.wallhaven.domain.Sorting
import dev.giovannidrago.wallhaven.domain.WallhavenFilters
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class FilterPreferences @Inject constructor(
	private val dataStore: DataStore<Preferences>
) {
	val filters: Flow<WallhavenFilters> = dataStore.data.map { preferences ->
		WallhavenFilters(
			allowNsfw = preferences[KEY_NSFW] ?: false,
			general = preferences[KEY_GENERAL] ?: true,
			anime = preferences[KEY_ANIME] ?: true,
			people = preferences[KEY_PEOPLE] ?: true,
			sorting = preferences[KEY_SORTING]?.toSorting() ?: Sorting.LATEST
		)
	}

	suspend fun update(filters: WallhavenFilters) {
		dataStore.edit { preferences ->
			preferences[KEY_NSFW] = filters.allowNsfw
			preferences[KEY_GENERAL] = filters.general
			preferences[KEY_ANIME] = filters.anime
			preferences[KEY_PEOPLE] = filters.people
			preferences[KEY_SORTING] = filters.sorting.name
		}
	}
}

private fun String.toSorting(): Sorting? = Sorting.entries.firstOrNull { it.name == this }

private val KEY_NSFW = booleanPreferencesKey("allow_nsfw")
private val KEY_GENERAL = booleanPreferencesKey("category_general")
private val KEY_ANIME = booleanPreferencesKey("category_anime")
private val KEY_PEOPLE = booleanPreferencesKey("category_people")
private val KEY_SORTING = stringPreferencesKey("sorting")
