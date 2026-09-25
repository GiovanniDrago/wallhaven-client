package dev.giovannidrago.wallhaven

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.giovannidrago.wallhaven.ui.MainScreen
import dev.giovannidrago.wallhaven.ui.theme.WallhavenTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			WallhavenTheme {
				MainScreen()
			}
		}
	}
}
