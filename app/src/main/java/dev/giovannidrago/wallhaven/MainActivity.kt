package dev.giovannidrago.wallhaven

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.giovannidrago.wallhaven.ui.MainScreen
import dev.giovannidrago.wallhaven.ui.theme.ThemeViewModel
import dev.giovannidrago.wallhaven.ui.theme.WallhavenTheme
import dev.giovannidrago.wallhaven.ui.theme.darkPreset
import dev.giovannidrago.wallhaven.ui.theme.lightPreset

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	private val themeViewModel: ThemeViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			val settings by themeViewModel.settings.collectAsStateWithLifecycle()
			val systemDark = isSystemInDarkTheme()
			val darkTheme = settings.isDark(systemDark)
			val preset = if (darkTheme) {
				darkPreset(settings.darkPresetId)
			} else {
				lightPreset(settings.lightPresetId)
			}
			WallhavenTheme(colorScheme = preset.colorScheme, darkTheme = darkTheme) {
				MainScreen()
			}
		}
	}
}
