package dev.giovannidrago.wallhaven.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dev.giovannidrago.wallhaven.domain.DEFAULT_DARK_PRESET_ID
import dev.giovannidrago.wallhaven.domain.DEFAULT_LIGHT_PRESET_ID

private val DefaultLightColors: ColorScheme = lightPreset(DEFAULT_LIGHT_PRESET_ID).colorScheme

private val DefaultDarkColors: ColorScheme = darkPreset(DEFAULT_DARK_PRESET_ID).colorScheme

@Composable
fun WallhavenTheme(
	colorScheme: ColorScheme? = null,
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit
) {
	val effective = colorScheme ?: if (darkTheme) DefaultDarkColors else DefaultLightColors

	val view = LocalView.current
	if (!view.isInEditMode) {
		SideEffect {
			val activity = view.context.findActivity() ?: return@SideEffect
			WindowCompat.getInsetsController(activity.window, view).apply {
				isAppearanceLightStatusBars = !darkTheme
				isAppearanceLightNavigationBars = !darkTheme
			}
		}
	}

	MaterialTheme(
		colorScheme = effective,
		content = content
	)
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
	is Activity -> this
	is ContextWrapper -> baseContext.findActivity()
	else -> null
}
