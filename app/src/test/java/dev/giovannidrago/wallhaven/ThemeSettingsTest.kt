package dev.giovannidrago.wallhaven

import dev.giovannidrago.wallhaven.domain.AppearanceMode
import dev.giovannidrago.wallhaven.domain.DEFAULT_DARK_PRESET_ID
import dev.giovannidrago.wallhaven.domain.DEFAULT_LIGHT_PRESET_ID
import dev.giovannidrago.wallhaven.domain.ThemeSettings
import dev.giovannidrago.wallhaven.ui.theme.DARK_PRESETS
import dev.giovannidrago.wallhaven.ui.theme.LIGHT_PRESETS
import dev.giovannidrago.wallhaven.ui.theme.darkPreset
import dev.giovannidrago.wallhaven.ui.theme.lightPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeSettingsTest {

	@Test
	fun `defaults use the system mode and default presets`() {
		val settings = ThemeSettings()

		assertEquals(AppearanceMode.SYSTEM, settings.mode)
		assertEquals("default_light", settings.lightPresetId)
		assertEquals("default_dark", settings.darkPresetId)
	}

	@Test
	fun `isDark follows the mode`() {
		val system = ThemeSettings(mode = AppearanceMode.SYSTEM)
		assertTrue(system.isDark(true))
		assertFalse(system.isDark(false))

		val light = ThemeSettings(mode = AppearanceMode.LIGHT)
		assertFalse(light.isDark(true))
		assertFalse(light.isDark(false))

		val dark = ThemeSettings(mode = AppearanceMode.DARK)
		assertTrue(dark.isDark(false))
		assertTrue(dark.isDark(true))
	}

	@Test
	fun `unknown preset ids fall back to the defaults`() {
		assertEquals("default_light", lightPreset(null).id)
		assertEquals("default_light", lightPreset("does-not-exist").id)
		assertEquals("default_dark", darkPreset(null).id)
		assertEquals("default_dark", darkPreset("nope").id)
	}

	@Test
	fun `known preset ids resolve`() {
		assertEquals("latte", lightPreset("latte").id)
		assertFalse(lightPreset("latte").dark)
		assertEquals("mocha", darkPreset("mocha").id)
		assertTrue(darkPreset("mocha").dark)
	}

	@Test
	fun `preset lists are consistent`() {
		assertEquals(LIGHT_PRESETS.size, LIGHT_PRESETS.map { it.id }.toSet().size)
		assertEquals(DARK_PRESETS.size, DARK_PRESETS.map { it.id }.toSet().size)

		LIGHT_PRESETS.forEach { preset ->
			assertTrue("${preset.id}: missing label", preset.labelRes != 0)
			assertFalse("${preset.id}: flagged as dark", preset.dark)
		}
		DARK_PRESETS.forEach { preset ->
			assertTrue("${preset.id}: missing label", preset.labelRes != 0)
			assertTrue("${preset.id}: not flagged as dark", preset.dark)
		}

		assertEquals(DEFAULT_LIGHT_PRESET_ID, lightPreset("unknown").id)
		assertEquals(DEFAULT_DARK_PRESET_ID, darkPreset("unknown").id)
	}
}
