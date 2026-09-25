package dev.giovannidrago.wallhaven.ui.navigation

import kotlinx.serialization.Serializable

enum class AppSection {
	GALLERY,
	SEARCH,
	FAVORITES,
	SETTINGS
}

@Serializable
data object GalleryRoute

@Serializable
data object FavoritesRoute

@Serializable
data class SearchRoute(val initialQuery: String? = null)

@Serializable
data object SettingsRoute

@Serializable
data class DetailRoute(val id: String)
