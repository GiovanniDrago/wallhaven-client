package dev.giovannidrago.wallhaven

import dev.giovannidrago.wallhaven.data.album.AlbumExportCodec
import dev.giovannidrago.wallhaven.data.album.AlbumImageCache
import dev.giovannidrago.wallhaven.data.api.WallhavenApi
import dev.giovannidrago.wallhaven.data.db.AlbumDao
import dev.giovannidrago.wallhaven.data.db.AlbumImageEntity
import dev.giovannidrago.wallhaven.data.db.WallhavenDao
import dev.giovannidrago.wallhaven.data.prefs.ApiKeyStore
import dev.giovannidrago.wallhaven.data.repository.AlbumRepository
import dev.giovannidrago.wallhaven.util.AppLogger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AlbumRepositoryTest {
	private val albumDao = mockk<AlbumDao>(relaxed = true)
	private val wallpaperDao = mockk<WallhavenDao>(relaxed = true)
	private val api = mockk<WallhavenApi>(relaxed = true)
	private val apiKeyStore = mockk<ApiKeyStore>().apply {
		every { apiKey } returns MutableStateFlow("")
	}
	private val imageCache = mockk<AlbumImageCache>(relaxed = true)
	private val codec = AlbumExportCodec(
		Json {
			ignoreUnknownKeys = true
			explicitNulls = false
			coerceInputValues = true
		}
	)
	private val repository = AlbumRepository(
		albumDao = albumDao,
		wallpaperDao = wallpaperDao,
		api = api,
		apiKeyStore = apiKeyStore,
		imageCache = imageCache,
		codec = codec,
		logger = AppLogger(),
		applicationScope = CoroutineScope(Dispatchers.Unconfined)
	)

	@Test
	fun `create album trims the name and rejects blank`() = runTest {
		assertNull(repository.createAlbum("   "))

		val album = repository.createAlbum("  Nature ")

		assertEquals("Nature", album?.name)
		coVerify { albumDao.upsertAlbum(match { it.name == "Nature" }) }
	}

	@Test
	fun `add to album inserts one membership and skips caching when already cached`() = runTest {
		coEvery { albumDao.findAlbumImage("w1") } returns
			AlbumImageEntity("w1", "/p/w1.jpg", "CACHED", 1L, 1L, null)

		repository.addToAlbum("a1", "w1")

		coVerify(exactly = 1) {
			albumDao.insertAlbumEntry(match { it.albumId == "a1" && it.wallpaperId == "w1" })
		}
		coVerify(exactly = 0) { imageCache.cache(any()) }
	}

	@Test
	fun `add to album marks a pending image when nothing is cached yet`() = runTest {
		coEvery { albumDao.findAlbumImage("w2") } returns null

		repository.addToAlbum("a1", "w2")

		coVerify { albumDao.upsertAlbumImage(match { it.status == "PENDING" && it.wallpaperId == "w2" }) }
	}

	@Test
	fun `removing the last reference deletes the cached image and the orphan placeholder`() = runTest {
		coEvery { albumDao.countAlbumsReferencing("w1") } returns 0
		coEvery { albumDao.findAlbumImage("w1") } returns
			AlbumImageEntity("w1", "/p/w1.jpg", "CACHED", 1L, 1L, null)

		repository.removeFromAlbum("a1", "w1")

		coVerify { albumDao.deleteAlbumImage("w1") }
		coVerify { imageCache.delete("/p/w1.jpg") }
		coVerify { albumDao.deleteOrphanPlaceholder("w1") }
	}

	@Test
	fun `removing a wallpaper still referenced elsewhere keeps its cache`() = runTest {
		coEvery { albumDao.countAlbumsReferencing("w2") } returns 1

		repository.removeFromAlbum("a1", "w2")

		coVerify(exactly = 0) { albumDao.deleteAlbumImage("w2") }
		coVerify(exactly = 0) { albumDao.deleteOrphanPlaceholder("w2") }
	}
}
