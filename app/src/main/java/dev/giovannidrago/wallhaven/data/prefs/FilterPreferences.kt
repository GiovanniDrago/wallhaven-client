package dev.giovannidrago.wallhaven.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dev.giovannidrago.wallhaven.domain.SearchOptions
import dev.giovannidrago.wallhaven.domain.SearchSorting
import dev.giovannidrago.wallhaven.domain.Sorting
import dev.giovannidrago.wallhaven.domain.WallhavenFilters
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Reads and writes both filter sets atomically: writers apply a transform to the value currently
 * stored in DataStore, so rapid toggles can never clobber each other with a stale snapshot.
 */
@Singleton
class FilterPreferences @Inject constructor(
	private val dataStore: DataStore<Preferences>
) {
	val filters: Flow<WallhavenFilters> = dataStore.data.map { preferences -> preferences.toFilters() }

	val searchOptions: Flow<SearchOptions> = dataStore.data.map { preferences -> preferences.toSearchOptions() }

	suspend fun update(transform: (WallhavenFilters) -> WallhavenFilters) {
		dataStore.edit { preferences ->
			val updated = transform(preferences.toFilters())
			preferences.remove(KEY_LEGACY_NSFW)
			preferences[KEY_SKETCHY] = updated.sketchy
			preferences[KEY_NSFW] = updated.nsfw
			preferences[KEY_GENERAL] = updated.general
			preferences[KEY_ANIME] = updated.anime
			preferences[KEY_PEOPLE] = updated.people
			preferences[KEY_SORTING] = updated.sorting.name
		}
	}

	suspend fun updateSearch(transform: (SearchOptions) -> SearchOptions) {
		dataStore.edit { preferences ->
			val updated = transform(preferences.toSearchOptions())
			preferences[KEY_SEARCH_SORTING] = updated.sorting.name
			preferences[KEY_SEARCH_GENERAL] = updated.general
			preferences[KEY_SEARCH_ANIME] = updated.anime
			preferences[KEY_SEARCH_PEOPLE] = updated.people
			preferences[KEY_SEARCH_SFW] = updated.sfw
			preferences[KEY_SEARCH_SKETCHY] = updated.sketchy
			preferences[KEY_SEARCH_NSFW] = updated.nsfw
			if (updated.color == null) {
				preferences.remove(KEY_SEARCH_COLOR)
			} else {
				preferences[KEY_SEARCH_COLOR] = updated.color
			}
			if (updated.atleast == null) {
				preferences.remove(KEY_SEARCH_ATLEAST)
			} else {
				preferences[KEY_SEARCH_ATLEAST] = updated.atleast
			}
			if (updated.resolutions.isEmpty()) {
				preferences.remove(KEY_SEARCH_RESOLUTIONS)
			} else {
				preferences[KEY_SEARCH_RESOLUTIONS] = updated.resolutions.toSet()
			}
			if (updated.ratios.isEmpty()) {
				preferences.remove(KEY_SEARCH_RATIOS)
			} else {
				preferences[KEY_SEARCH_RATIOS] = updated.ratios.toSet()
			}
		}
	}
}

private fun Preferences.toFilters(): WallhavenFilters {
	// Before sketchy and nsfw were split, one toggle enabled both: the old value seeds each new flag.
	val legacyNsfw = this[KEY_LEGACY_NSFW]
	return WallhavenFilters(
		sketchy = this[KEY_SKETCHY] ?: legacyNsfw ?: false,
		nsfw = this[KEY_NSFW] ?: legacyNsfw ?: false,
		general = this[KEY_GENERAL] ?: true,
		anime = this[KEY_ANIME] ?: true,
		people = this[KEY_PEOPLE] ?: true,
		sorting = this[KEY_SORTING]?.toSorting() ?: Sorting.LATEST
	)
}

private fun Preferences.toSearchOptions(): SearchOptions = SearchOptions(
	sorting = this[KEY_SEARCH_SORTING]?.toSearchSorting() ?: SearchSorting.LATEST,
	general = this[KEY_SEARCH_GENERAL] ?: true,
	anime = this[KEY_SEARCH_ANIME] ?: true,
	people = this[KEY_SEARCH_PEOPLE] ?: true,
	sfw = this[KEY_SEARCH_SFW] ?: true,
	sketchy = this[KEY_SEARCH_SKETCHY] ?: false,
	nsfw = this[KEY_SEARCH_NSFW] ?: false,
	color = this[KEY_SEARCH_COLOR]?.takeIf { it.isNotBlank() },
	atleast = this[KEY_SEARCH_ATLEAST]?.takeIf { it.isNotBlank() },
	resolutions = this[KEY_SEARCH_RESOLUTIONS]?.toList().orEmpty(),
	ratios = this[KEY_SEARCH_RATIOS]?.toList().orEmpty()
)

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
private val KEY_SEARCH_SFW = booleanPreferencesKey("search_sfw")
private val KEY_SEARCH_SKETCHY = booleanPreferencesKey("search_sketchy")
private val KEY_SEARCH_NSFW = booleanPreferencesKey("search_nsfw")
private val KEY_SEARCH_COLOR = stringPreferencesKey("search_color")
private val KEY_SEARCH_ATLEAST = stringPreferencesKey("search_atleast")
private val KEY_SEARCH_RESOLUTIONS = stringSetPreferencesKey("search_resolutions")
private val KEY_SEARCH_RATIOS = stringSetPreferencesKey("search_ratios")
