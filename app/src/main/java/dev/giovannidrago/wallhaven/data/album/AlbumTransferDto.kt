package dev.giovannidrago.wallhaven.data.album

import kotlinx.serialization.Serializable

/** Portable album document: the album name and the wallhaven identifiers of its images. */
@Serializable
data class AlbumExport(
	val version: Int = CURRENT_VERSION,
	val name: String,
	val wallpapers: List<String> = emptyList()
) {
	companion object {
		const val CURRENT_VERSION = 1
	}
}
