package dev.giovannidrago.wallhaven.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.giovannidrago.wallhaven.domain.AppearanceMode
import dev.giovannidrago.wallhaven.domain.DEFAULT_DARK_PRESET_ID
import dev.giovannidrago.wallhaven.domain.DEFAULT_LIGHT_PRESET_ID
import dev.giovannidrago.wallhaven.domain.ThemeSettings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists the appearance mode and the selected theme preset ids. Unknown or blank stored values
 * resolve to the defaults, so a stale preference never breaks the theme.
 */
@Singleton
class ThemePreferences @Inject constructor(
	private val dataStore: DataStore<Preferences>
) {
	val settings: Flow<ThemeSettings> = dataStore.data.map { preferences -> preferences.toThemeSettings() }

	suspend fun setMode(mode: AppearanceMode) {
		dataStore.edit { preferences ->
			preferences[KEY_APPEARANCE_MODE] = mode.name
		}
	}

	suspend fun setLightPreset(id: String) {
		dataStore.edit { preferences ->
			preferences[KEY_LIGHT_PRESET] = id
		}
	}

	suspend fun setDarkPreset(id: String) {
		dataStore.edit { preferences ->
			preferences[KEY_DARK_PRESET] = id
		}
	}
}

private fun Preferences.toThemeSettings(): ThemeSettings = ThemeSettings(
	mode = this[KEY_APPEARANCE_MODE]?.toAppearanceMode() ?: AppearanceMode.SYSTEM,
	lightPresetId = this[KEY_LIGHT_PRESET]?.takeIf { it.isNotBlank() } ?: DEFAULT_LIGHT_PRESET_ID,
	darkPresetId = this[KEY_DARK_PRESET]?.takeIf { it.isNotBlank() } ?: DEFAULT_DARK_PRESET_ID
)

private fun String.toAppearanceMode(): AppearanceMode? = AppearanceMode.entries.firstOrNull { it.name == this }

private val KEY_APPEARANCE_MODE = stringPreferencesKey("appearance_mode")
private val KEY_LIGHT_PRESET = stringPreferencesKey("theme_light_preset")
private val KEY_DARK_PRESET = stringPreferencesKey("theme_dark_preset")
