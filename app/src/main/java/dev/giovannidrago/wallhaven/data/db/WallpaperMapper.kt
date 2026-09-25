package dev.giovannidrago.wallhaven.data.db

import dev.giovannidrago.wallhaven.data.api.dto.WallpaperDto
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun WallpaperDto.toEntity(
	cachedAt: Long,
	favorited: Boolean,
	favoritedAt: Long?
): WallpaperEntity = WallpaperEntity(
	id = id,
	url = url,
	shortUrl = shortUrl,
	uploader = uploader?.username?.takeIf { it.isNotBlank() },
	views = views,
	favorites = favorites,
	source = source?.takeIf { it.isNotBlank() },
	purity = purity ?: "sfw",
	category = category ?: "general",
	dimensionX = dimensionX,
	dimensionY = dimensionY,
	resolution = resolution,
	ratio = ratio,
	fileSize = fileSize,
	fileType = fileType,
	createdAt = createdAt,
	colorsJson = colors.takeIf { it.isNotEmpty() }?.let { json.encodeToString(it) },
	path = path,
	thumbSmall = thumbs?.small,
	thumbOriginal = thumbs?.original,
	thumbLarge = thumbs?.large,
	tagsJson = tags.takeIf { it.isNotEmpty() }?.let { json.encodeToString(it) },
	tagsText = buildTagsText(this),
	favorited = favorited,
	favoritedAt = favoritedAt,
	cachedAt = cachedAt
)

internal fun buildTagsText(wallpaper: WallpaperDto): String? {
	val words = wallpapersTags(wallpaper)
	return words.takeIf { it.isNotEmpty() }?.joinToString(" ")?.lowercase()
}

private fun wallpapersTags(wallpaper: WallpaperDto): List<String> =
	wallpaper.tags.flatMap { tag ->
		listOfNotNull(tag.name.takeIf { it.isNotBlank() }, tag.alias?.takeIf { it.isNotBlank() })
	}

fun WallpaperEntity.tags(): List<String> = tagsJson?.let { stored ->
	runCatching { json.decodeFromString<List<dev.giovannidrago.wallhaven.data.api.dto.TagDto>>(stored) }
		.getOrDefault(emptyList())
		.map { it.name }
		.filter { it.isNotBlank() }
}.orEmpty()

fun WallpaperEntity.colors(): List<String> = colorsJson?.let { stored ->
	runCatching { json.decodeFromString<List<String>>(stored) }.getOrDefault(emptyList())
}.orEmpty()
