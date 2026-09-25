package dev.giovannidrago.wallhaven

import dev.giovannidrago.wallhaven.data.api.dto.TagDto
import dev.giovannidrago.wallhaven.data.api.dto.ThumbsDto
import dev.giovannidrago.wallhaven.data.api.dto.UploaderDto
import dev.giovannidrago.wallhaven.data.api.dto.WallpaperDto
import dev.giovannidrago.wallhaven.data.db.toEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WallpaperMapperTest {
	@Test
	fun `maps dto fields and builds the like index`() {
		val dto = WallpaperDto(
			id = "abc123",
			url = "https://wallhaven.cc/w/abc123",
			uploader = UploaderDto(username = "tester"),
			views = 10,
			favorites = 2,
			purity = "nsfw",
			category = "anime",
			dimensionX = 1920,
			dimensionY = 1080,
			resolution = "1920x1080",
			ratio = "1.78",
			fileSize = 1234,
			fileType = "image/png",
			createdAt = "2024-01-02 03:04:05",
			colors = listOf("#000000"),
			path = "https://w.wallhaven.cc/full/ab/wallhaven-abc123.png",
			thumbs = ThumbsDto(large = "large", original = "orig", small = "small"),
			tags = listOf(TagDto(id = 1, name = "Landscape", alias = "Scenery"))
		)

		val entity = dto.toEntity(cachedAt = 42L, favorited = true, favoritedAt = 7L)

		assertEquals("abc123", entity.id)
		assertEquals("tester", entity.uploader)
		assertEquals("nsfw", entity.purity)
		assertEquals("anime", entity.category)
		assertEquals("large", entity.thumbLarge)
		assertEquals("landscape scenery", entity.tagsText)
		assertTrue(entity.colorsJson?.contains("#000000") == true)
		assertTrue(entity.favorited)
		assertEquals(7L, entity.favoritedAt)
		assertEquals(42L, entity.cachedAt)
	}

	@Test
	fun `blank uploader and source become null`() {
		val dto = WallpaperDto(
			id = "abc123",
			path = "https://w.wallhaven.cc/full/ab/wallhaven-abc123.jpg",
			uploader = UploaderDto(username = ""),
			source = ""
		)

		val entity = dto.toEntity(cachedAt = 1L, favorited = false, favoritedAt = null)

		assertEquals(null, entity.uploader)
		assertEquals(null, entity.source)
		assertEquals(null, entity.tagsText)
	}

	@Test
	fun `aspect ratio falls back and clamps extreme values`() {
		val square = WallpaperDto(id = "a", path = "p", dimensionX = 1000, dimensionY = 1000)
			.toEntity(cachedAt = 1L, favorited = false, favoritedAt = null)
		val panorama = WallpaperDto(id = "b", path = "p", dimensionX = 9000, dimensionY = 1000)
			.toEntity(cachedAt = 1L, favorited = false, favoritedAt = null)
		val unknown = WallpaperDto(id = "c", path = "p")
			.toEntity(cachedAt = 1L, favorited = false, favoritedAt = null)

		assertEquals(1f, square.aspectRatio, 0.001f)
		assertEquals(2.2f, panorama.aspectRatio, 0.001f)
		assertEquals(16f / 9f, unknown.aspectRatio, 0.001f)
	}
}
