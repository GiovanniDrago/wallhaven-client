package dev.giovannidrago.wallhaven.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

private const val DEFAULT_PER_PAGE = 24

@Serializable
data class SearchResponseDto(
	val data: List<WallpaperDto> = emptyList(),
	val meta: SearchMetaDto = SearchMetaDto()
)

@Serializable
data class SearchMetaDto(
	@SerialName("current_page") val currentPage: Int = 1,
	@SerialName("last_page") val lastPage: Int = 1,
	@SerialName("per_page") val perPage: Int = DEFAULT_PER_PAGE,
	val total: Int = 0,
	val query: JsonElement? = null,
	val seed: String? = null
)

@Serializable
data class WallpaperDetailResponseDto(val data: WallpaperDto)

@Serializable
data class UserSettingsResponseDto(val data: UserSettingsDto = UserSettingsDto())

@Serializable
	data class UserSettingsDto(
		@SerialName("thumb_size") val thumbSize: String? = null,
		@SerialName("per_page") val perPage: String? = null,
		val purity: List<String> = emptyList(),
		val categories: List<String> = emptyList(),
		@SerialName("tag_blacklist") val tagBlacklist: List<String> = emptyList(),
		@SerialName("user_blacklist") val userBlacklist: List<String> = emptyList()
	)

@Serializable
data class WallpaperDto(
	val id: String,
	val url: String? = null,
	@SerialName("short_url") val shortUrl: String? = null,
	val uploader: UploaderDto? = null,
	val views: Int = 0,
	val favorites: Int = 0,
	val source: String? = null,
	val purity: String? = null,
	val category: String? = null,
	@SerialName("dimension_x") val dimensionX: Int? = null,
	@SerialName("dimension_y") val dimensionY: Int? = null,
	val resolution: String? = null,
	val ratio: String? = null,
	@SerialName("file_size") val fileSize: Long? = null,
	@SerialName("file_type") val fileType: String? = null,
	@SerialName("created_at") val createdAt: String? = null,
	val colors: List<String> = emptyList(),
	val path: String,
	val thumbs: ThumbsDto? = null,
	val tags: List<TagDto> = emptyList()
)

@Serializable
data class UploaderDto(
	val username: String = "",
	val group: String? = null,
	val avatar: AvatarDto? = null
)

@Serializable
data class AvatarDto(
	@SerialName("200px") val large: String? = null,
	@SerialName("128px") val medium: String? = null,
	@SerialName("32px") val small: String? = null,
	@SerialName("20px") val tiny: String? = null
)

@Serializable
data class ThumbsDto(
	val large: String? = null,
	val original: String? = null,
	val small: String? = null
)

@Serializable
data class TagDto(
	val id: Int = 0,
	val name: String = "",
	val alias: String? = null,
	val category: String? = null,
	val purity: String? = null
)
