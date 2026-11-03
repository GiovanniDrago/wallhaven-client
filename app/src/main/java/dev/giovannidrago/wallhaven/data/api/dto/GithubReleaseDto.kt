package dev.giovannidrago.wallhaven.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubReleaseDto(
	@SerialName("tag_name") val tagName: String? = null,
	@SerialName("html_url") val htmlUrl: String? = null,
	val assets: List<GithubAssetDto> = emptyList()
)

@Serializable
data class GithubAssetDto(
	val name: String? = null,
	@SerialName("browser_download_url") val downloadUrl: String? = null
)
