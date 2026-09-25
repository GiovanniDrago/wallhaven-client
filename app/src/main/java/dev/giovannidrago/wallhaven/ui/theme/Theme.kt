package dev.giovannidrago.wallhaven.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
	primary = Color(0xFF4C5BD4),
	secondary = Color(0xFF5A5F7D),
	tertiary = Color(0xFF7B4E8C)
)

private val DarkColors = darkColorScheme(
	primary = Color(0xFFB9C3FF),
	secondary = Color(0xFFC3C6DD),
	tertiary = Color(0xFFEAB8FF)
)

@Composable
fun WallhavenTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	dynamicColor: Boolean = true,
	content: @Composable () -> Unit
) {
	val colorScheme = when {
		dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
			val context = LocalContext.current
			if (darkTheme) {
				dynamicDarkColorScheme(context)
			} else {
				dynamicLightColorScheme(context)
			}
		}
		darkTheme -> DarkColors
		else -> LightColors
	}

	MaterialTheme(
		colorScheme = colorScheme,
		content = content
	)
}
