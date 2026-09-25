package dev.giovannidrago.wallhaven.ui.components

import androidx.compose.ui.graphics.Color

private val FALLBACK_COLOR = Color(0xFF888888)

fun String?.toComposeColor(): Color = runCatching {
	Color(android.graphics.Color.parseColor(this))
}.getOrDefault(FALLBACK_COLOR)
