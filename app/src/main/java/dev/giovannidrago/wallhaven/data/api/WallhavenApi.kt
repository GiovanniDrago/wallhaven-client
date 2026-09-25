package dev.giovannidrago.wallhaven.data.api

import dev.giovannidrago.wallhaven.data.api.dto.SearchResponseDto
import dev.giovannidrago.wallhaven.data.api.dto.UserSettingsResponseDto
import dev.giovannidrago.wallhaven.data.api.dto.WallpaperDetailResponseDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface WallhavenApi {
	@GET("search")
	suspend fun search(
		@Query("q") query: String?,
		@Query("categories") categories: String,
		@Query("purity") purity: String,
		@Query("sorting") sorting: String,
		@Query("order") order: String,
		@Query("topRange") topRange: String?,
		@Query("seed") seed: String?,
		@Query("page") page: Int,
		// Sent as a header so the key never lands in the URL, where HTTP logs would show it.
		@Header("X-API-Key") apiKey: String?
	): SearchResponseDto

	@GET("w/{id}")
	suspend fun wallpaper(
		@Path("id") id: String,
		@Header("X-API-Key") apiKey: String?
	): WallpaperDetailResponseDto

	@GET("settings")
	suspend fun settings(
		@Header("X-API-Key") apiKey: String
	): UserSettingsResponseDto
}
