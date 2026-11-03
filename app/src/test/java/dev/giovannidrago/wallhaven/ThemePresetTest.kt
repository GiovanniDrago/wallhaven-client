package dev.giovannidrago.wallhaven

import androidx.compose.ui.graphics.Color
import dev.giovannidrago.wallhaven.ui.theme.DARK_PRESETS
import dev.giovannidrago.wallhaven.ui.theme.LIGHT_PRESETS
import kotlin.math.pow
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePresetTest {

	@Test
	fun `exposes at least two light and two dark presets`() {
		assertTrue(LIGHT_PRESETS.size >= 2)
		assertTrue(DARK_PRESETS.size >= 2)
	}

	@Test
	fun `every preset keeps body text readable`() {
		(LIGHT_PRESETS + DARK_PRESETS).forEach { preset ->
			val scheme = preset.colorScheme
			assertContrast(
				preset.id,
				"onBackground/background",
				scheme.onBackground,
				scheme.background,
				BODY_TEXT_MIN_CONTRAST
			)
			assertContrast(
				preset.id,
				"onSurface/surface",
				scheme.onSurface,
				scheme.surface,
				BODY_TEXT_MIN_CONTRAST
			)
			assertContrast(
				preset.id,
				"onSurfaceVariant/surfaceVariant",
				scheme.onSurfaceVariant,
				scheme.surfaceVariant,
				BODY_TEXT_MIN_CONTRAST
			)
			assertContrast(
				preset.id,
				"error/surfaceVariant",
				scheme.error,
				scheme.surfaceVariant,
				BODY_TEXT_MIN_CONTRAST
			)
			assertContrast(
				preset.id,
				"onSecondaryContainer/secondaryContainer",
				scheme.onSecondaryContainer,
				scheme.secondaryContainer,
				BODY_TEXT_MIN_CONTRAST
			)
			assertContrast(
				preset.id,
				"onSurface/surfaceContainerHigh",
				scheme.onSurface,
				scheme.surfaceContainerHigh,
				BODY_TEXT_MIN_CONTRAST
			)
			assertContrast(
				preset.id,
				"inversePrimary/inverseSurface",
				scheme.inversePrimary,
				scheme.inverseSurface,
				BODY_TEXT_MIN_CONTRAST
			)
		}
	}

	@Test
	fun `every preset keeps primary content readable`() {
		(LIGHT_PRESETS + DARK_PRESETS).forEach { preset ->
			val scheme = preset.colorScheme
			assertContrast(
				preset.id,
				"onPrimary/primary",
				scheme.onPrimary,
				scheme.primary,
				LARGE_TEXT_MIN_CONTRAST
			)
		}
	}

	private fun assertContrast(
		presetId: String,
		role: String,
		foreground: Color,
		background: Color,
		minimum: Double
	) {
		val ratio = contrastRatio(foreground, background)
		assertTrue("$presetId: $role contrast $ratio (minimum $minimum)", ratio >= minimum)
	}

	private fun contrastRatio(foreground: Color, background: Color): Double {
		val foregroundLuminance = relativeLuminance(foreground)
		val backgroundLuminance = relativeLuminance(background)
		val lighter = maxOf(foregroundLuminance, backgroundLuminance)
		val darker = minOf(foregroundLuminance, backgroundLuminance)
		return (lighter + 0.05) / (darker + 0.05)
	}

	private fun relativeLuminance(color: Color): Double =
		0.2126 * linearize(color.red.toDouble()) +
			0.7152 * linearize(color.green.toDouble()) +
			0.0722 * linearize(color.blue.toDouble())

	private fun linearize(channel: Double): Double =
		if (channel <= 0.03928) {
			channel / 12.92
		} else {
			((channel + 0.055) / 1.055).pow(2.4)
		}

	private companion object {
		const val BODY_TEXT_MIN_CONTRAST = 4.5
		const val LARGE_TEXT_MIN_CONTRAST = 3.0
	}
}
