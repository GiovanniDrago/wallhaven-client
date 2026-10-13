package dev.giovannidrago.wallhaven.data.album

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Encodes and validates the album export/import document. The API key is never part of this model,
 * so it can never end up in an exported file (constitution Principle III).
 */
@Singleton
class AlbumExportCodec @Inject constructor(
	private val json: Json
) {
	fun encode(export: AlbumExport): String = json.encodeToString(
		export.copy(
			version = AlbumExport.CURRENT_VERSION,
			name = export.name.trim(),
			wallpapers = export.wallpapers.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
		)
	)

	fun decode(content: String): Result<AlbumExport> = runCatching {
		val parsed = json.decodeFromString<AlbumExport>(content)
		require(parsed.version == AlbumExport.CURRENT_VERSION) {
			"Unsupported album export version: ${parsed.version}"
		}
		val name = parsed.name.trim()
		require(name.isNotEmpty()) { "Album name must not be blank" }
		require(parsed.wallpapers.all { it.isNotBlank() }) { "Album contains a blank identifier" }
		val identifiers = parsed.wallpapers.map { it.trim() }.distinct()
		parsed.copy(name = name, wallpapers = identifiers)
	}
}
