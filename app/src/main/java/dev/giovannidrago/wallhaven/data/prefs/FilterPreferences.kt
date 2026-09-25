package dev.giovannidrago.wallhaven.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.giovannidrago.wallhaven.domain.SearchOptions
import dev.giovannidrago.wallhaven.domain.SearchSorting
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
		// Before sketchy and nsfw were split, one toggle enabled both: the old value seeds each new flag.
		val legacyNsfw = preferences[KEY_LEGACY_NSFW]
		WallhavenFilters(
			sketchy = preferences[KEY_SKETCHY] ?: legacyNsfw ?: false,
			nsfw = preferences[KEY_NSFW] ?: legacyNsfw ?: false,
			general = preferences[KEY_GENERAL] ?: true,
			anime = preferences[KEY_ANIME] ?: true,
			people = preferences[KEY_PEOPLE] ?: true,
			sorting = preferences[KEY_SORTING]?.toSorting() ?: Sorting.LATEST
		)
	}

	val searchOptions: Flow<SearchOptions> = dataStore.data.map { preferences ->
		SearchOptions(
			sorting = preferences[KEY_SEARCH_SORTING]?.toSearchSorting() ?: SearchSorting.RELEVANCE,
			general = preferences[KEY_SEARCH_GENERAL] ?: true,
			anime = preferences[KEY_SEARCH_ANIME] ?: true,
			people = preferences[KEY_SEARCH_PEOPLE] ?: true,
			sketchy = preferences[KEY_SEARCH_SKETCHY] ?: false,
			nsfw = preferences[KEY_SEARCH_NSFW] ?: false,
			color = preferences[KEY_SEARCH_COLOR]?.takeIf { it.isNotBlank() }
		)
	}

	suspend fun update(filters: WallhavenFilters) {
		dataStore.edit { preferences ->
			preferences.remove(KEY_LEGACY_NSFW)
			preferences[KEY_SKETCHY] = filters.sketchy
			preferences[KEY_NSFW] = filters.nsfw
			preferences[KEY_GENERAL] = filters.general
			preferences[KEY_ANIME] = filters.anime
			preferences[KEY_PEOPLE] = filters.people
			preferences[KEY_SORTING] = filters.sorting.name
		}
	}

	suspend fun updateSearch(options: SearchOptions) {
		dataStore.edit { preferences ->
			preferences[KEY_SEARCH_SORTING] = options.sorting.name
			preferences[KEY_SEARCH_GENERAL] = options.general
			preferences[KEY_SEARCH_ANIME] = options.anime
			preferences[KEY_SEARCH_PEOPLE] = options.people
			preferences[KEY_SEARCH_SKETCHY] = options.sketchy
			preferences[KEY_SEARCH_NSFW] = options.nsfw
			if (options.color == null) {
				preferences.remove(KEY_SEARCH_COLOR)
			} else {
				preferences[KEY_SEARCH_COLOR] = options.color
			}
		}
	}
}

private fun String.toSorting(): Sorting? = Sorting.entries.firstOrNull { it.name == this }

private fun String.toSearchSorting(): SearchSorting? = SearchSorting.entries.firstOrNull { it.name == this }

private val KEY_LEGACY_NSFW = booleanPreferencesKey("allow_nsfw")
private val KEY_SKETCHY = booleanPreferencesKey("sketchy")
private val KEY_NSFW = booleanPreferencesKey("nsfw")
private val KEY_GENERAL = booleanPreferencesKey("category_general")
private val KEY_ANIME = booleanPreferencesKey("category_anime")
private val KEY_PEOPLE = booleanPreferencesKey("category_people")
private val KEY_SORTING = stringPreferencesKey("sorting")
private val KEY_SEARCH_SORTING = stringPreferencesKey("search_sorting")
private val KEY_SEARCH_GENERAL = booleanPreferencesKey("search_category_general")
private val KEY_SEARCH_ANIME = booleanPreferencesKey("search_category_anime")
private val KEY_SEARCH_PEOPLE = booleanPreferencesKey("search_category_people")
private val KEY_SEARCH_SKETCHY = booleanPreferencesKey("search_sketchy")
private val KEY_SEARCH_NSFW = booleanPreferencesKey("search_nsfw")
private val KEY_SEARCH_COLOR = stringPreferencesKey("search_color")
