package dev.giovannidrago.wallhaven.domain

/** How the app picks between the light and the dark palette. */
enum class AppearanceMode {
	SYSTEM,
	LIGHT,
	DARK
}

const val DEFAULT_LIGHT_PRESET_ID = "default_light"
const val DEFAULT_DARK_PRESET_ID = "default_dark"

/**
 * Theme preferences: the appearance mode plus the theme preset used for each background.
 *
 * The preset ids are opaque strings owned by the theme repository; the defaults are stable so a
 * fresh install always resolves to the built-in light and dark themes.
 */
data class ThemeSettings(
	val mode: AppearanceMode = AppearanceMode.SYSTEM,
	val lightPresetId: String = DEFAULT_LIGHT_PRESET_ID,
	val darkPresetId: String = DEFAULT_DARK_PRESET_ID
) {
	/** Resolves the effective dark/light state given the system setting. */
	fun isDark(systemDark: Boolean): Boolean = when (mode) {
		AppearanceMode.DARK -> true
		AppearanceMode.LIGHT -> false
		AppearanceMode.SYSTEM -> systemDark
	}
}
