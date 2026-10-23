package dev.giovannidrago.wallhaven.ui.theme

import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.domain.DEFAULT_DARK_PRESET_ID
import dev.giovannidrago.wallhaven.domain.DEFAULT_LIGHT_PRESET_ID

/**
 * A selectable theme: a stable id persisted in preferences, a localized label and the Material 3
 * color scheme it applies.
 */
data class ThemePreset(
	val id: String,
	@StringRes val labelRes: Int,
	val dark: Boolean,
	val colorScheme: ColorScheme
)

/** Available light themes. The first entry is the default. */
val LIGHT_PRESETS: List<ThemePreset> = listOf(
	ThemePreset(
		DEFAULT_LIGHT_PRESET_ID,
		R.string.theme_default_light,
		false,
		lightColorScheme(
			primary = Color(0xFF4C5BD4),
			secondary = Color(0xFF5A5F7D),
			tertiary = Color(0xFF7B4E8C)
		)
	),
	ThemePreset(
		"latte",
		R.string.theme_latte,
		false,
		lightColorScheme(
			primary = Color(0xFF1E66F5),
			onPrimary = Color(0xFFEFF1F5),
			secondary = Color(0xFF8839EF),
			onSecondary = Color(0xFFEFF1F5),
			tertiary = Color(0xFF179299),
			onTertiary = Color(0xFFEFF1F5),
			background = Color(0xFFEFF1F5),
			onBackground = Color(0xFF4C4F69),
			surface = Color(0xFFE6E9EF),
			onSurface = Color(0xFF4C4F69),
			surfaceVariant = Color(0xFFCCD0DA),
			onSurfaceVariant = Color(0xFF4C4F69),
			error = Color(0xFFB00020),
			onError = Color(0xFFEFF1F5),
			surfaceContainerLowest = Color(0xFFFFFFFF),
			surfaceContainerLow = Color(0xFFE6E9EF),
			surfaceContainer = Color(0xFFDCE0E8),
			surfaceContainerHigh = Color(0xFFCCD0DA),
			surfaceContainerHighest = Color(0xFFBCC0CC),
			secondaryContainer = Color(0xFFCCD0DA),
			onSecondaryContainer = Color(0xFF4C4F69),
			outline = Color(0xFF6C6F85),
			outlineVariant = Color(0xFFACB0BE),
			inverseSurface = Color(0xFF4C4F69),
			inverseOnSurface = Color(0xFFEFF1F5),
			inversePrimary = Color(0xFFB4C7FF)
		)
	)
)

/** Available dark themes. The first entry is the default. */
val DARK_PRESETS: List<ThemePreset> = listOf(
	ThemePreset(
		DEFAULT_DARK_PRESET_ID,
		R.string.theme_default_dark,
		true,
		darkColorScheme(
			primary = Color(0xFFB9C3FF),
			secondary = Color(0xFFC3C6DD),
			tertiary = Color(0xFFEAB8FF)
		)
	),
	ThemePreset(
		"mocha",
		R.string.theme_mocha,
		true,
		darkColorScheme(
			primary = Color(0xFF89B4FA),
			onPrimary = Color(0xFF1E1E2E),
			secondary = Color(0xFFCBA6F7),
			onSecondary = Color(0xFF1E1E2E),
			tertiary = Color(0xFF94E2D5),
			onTertiary = Color(0xFF1E1E2E),
			background = Color(0xFF1E1E2E),
			onBackground = Color(0xFFCDD6F4),
			surface = Color(0xFF313244),
			onSurface = Color(0xFFCDD6F4),
			surfaceVariant = Color(0xFF45475A),
			onSurfaceVariant = Color(0xFFBAC2DE),
			// Lightened from Catppuccin red #F38BA8 to keep body-text contrast on surfaceVariant.
			error = Color(0xFFF5A9B8),
			onError = Color(0xFF1E1E2E),
			surfaceContainerLowest = Color(0xFF11111B),
			surfaceContainerLow = Color(0xFF181825),
			surfaceContainer = Color(0xFF1E1E2E),
			surfaceContainerHigh = Color(0xFF313244),
			surfaceContainerHighest = Color(0xFF45475A),
			secondaryContainer = Color(0xFF45475A),
			onSecondaryContainer = Color(0xFFCDD6F4),
			outline = Color(0xFF7F849C),
			outlineVariant = Color(0xFF585B70),
			inverseSurface = Color(0xFFCDD6F4),
			inverseOnSurface = Color(0xFF1E1E2E),
			inversePrimary = Color(0xFF164CC4)
		)
	)
)

/** Resolves a light preset by id, falling back to the default light theme. */
fun lightPreset(id: String?): ThemePreset =
	LIGHT_PRESETS.firstOrNull { it.id == id }
		?: LIGHT_PRESETS.firstOrNull { it.id == DEFAULT_LIGHT_PRESET_ID }
		?: LIGHT_PRESETS.first()

/** Resolves a dark preset by id, falling back to the default dark theme. */
fun darkPreset(id: String?): ThemePreset =
	DARK_PRESETS.firstOrNull { it.id == id }
		?: DARK_PRESETS.firstOrNull { it.id == DEFAULT_DARK_PRESET_ID }
		?: DARK_PRESETS.first()
