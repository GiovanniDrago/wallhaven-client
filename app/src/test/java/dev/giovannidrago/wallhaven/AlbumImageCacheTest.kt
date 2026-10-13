package dev.giovannidrago.wallhaven

import android.content.Context
import dev.giovannidrago.wallhaven.data.album.AlbumImageCache
import dev.giovannidrago.wallhaven.data.db.AlbumDao
import dev.giovannidrago.wallhaven.data.db.WallpaperEntity
import dev.giovannidrago.wallhaven.util.AppLogger
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AlbumImageCacheTest {
	private lateinit var server: MockWebServer
	private lateinit var filesDir: File

	@Before
	fun setUp() {
		server = MockWebServer()
		server.start()
		filesDir = createTempDirectory("album-images").toFile()
	}

	@After
	fun tearDown() {
		server.shutdown()
		filesDir.deleteRecursively()
	}

	private fun cache(dao: AlbumDao): AlbumImageCache {
		val context = mockk<Context>()
		every { context.filesDir } returns filesDir
		return AlbumImageCache(context, OkHttpClient(), dao, AppLogger())
	}

	@Test
	fun `downloads the full resolution image and records the local path`() = runTest {
		server.enqueue(MockResponse().setResponseCode(200).setBody("image-bytes"))
		val dao = mockk<AlbumDao>(relaxed = true)

		cache(dao).cache(wallpaper("w1", server.url("/full/w1.jpg").toString(), "image/jpeg"))

		val file = File(File(filesDir, "album_images"), "w1.jpg")
		assertTrue(file.exists())
		coVerify { dao.upsertAlbumImage(match { it.status == "CACHED" && it.localPath == file.absolutePath }) }
		assertNull(server.takeRequest().getHeader("X-API-Key"))
	}

	@Test
	fun `marks the entry failed on a server error`() = runTest {
		server.enqueue(MockResponse().setResponseCode(500))
		val dao = mockk<AlbumDao>(relaxed = true)

		cache(dao).cache(wallpaper("w2", server.url("/full/w2.jpg").toString(), "image/jpeg"))

		coVerify { dao.upsertAlbumImage(match { it.status == "FAILED" && it.wallpaperId == "w2" }) }
	}

	private fun wallpaper(id: String, path: String, fileType: String?): WallpaperEntity = WallpaperEntity(
		id = id,
		url = null,
		shortUrl = null,
		uploader = null,
		views = 0,
		favorites = 0,
		source = null,
		purity = "sfw",
		category = "general",
		dimensionX = 1920,
		dimensionY = 1080,
		resolution = "1920x1080",
		ratio = "1.78",
		fileSize = 10L,
		fileType = fileType,
		createdAt = null,
		colorsJson = null,
		path = path,
		thumbSmall = null,
		thumbOriginal = null,
		thumbLarge = null,
		tagsJson = null,
		tagsText = null,
		favorited = false,
		favoritedAt = null,
		metadataResolved = true,
		cachedAt = 0L
	)
}
