package dev.giovannidrago.wallhaven

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dev.giovannidrago.wallhaven.data.album.AlbumExportCodec
import dev.giovannidrago.wallhaven.data.album.AlbumImageCache
import dev.giovannidrago.wallhaven.data.api.WallhavenApi
import dev.giovannidrago.wallhaven.data.db.AlbumDao
import dev.giovannidrago.wallhaven.data.db.WallhavenDao
import dev.giovannidrago.wallhaven.data.db.WallpaperEntity
import dev.giovannidrago.wallhaven.data.prefs.ApiKeyStore
import dev.giovannidrago.wallhaven.data.repository.AlbumRepository
import dev.giovannidrago.wallhaven.util.AppLogger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class AlbumImportResolutionTest {
	private lateinit var server: MockWebServer
	private val json = Json {
		ignoreUnknownKeys = true
		explicitNulls = false
		coerceInputValues = true
	}

	@Before
	fun setUp() {
		server = MockWebServer()
		server.start()
	}

	@After
	fun tearDown() {
		server.shutdown()
	}

	private fun api(): WallhavenApi = Retrofit.Builder()
		.baseUrl(server.url("/"))
		.addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
		.build()
		.create(WallhavenApi::class.java)

	private fun repository(
		api: WallhavenApi,
		albumDao: AlbumDao,
		wallpaperDao: WallhavenDao
	): AlbumRepository {
		val apiKeyStore = mockk<ApiKeyStore>().apply {
			every { apiKey } returns MutableStateFlow("")
		}
		return AlbumRepository(
			albumDao = albumDao,
			wallpaperDao = wallpaperDao,
			api = api,
			apiKeyStore = apiKeyStore,
			imageCache = mockk<AlbumImageCache>(relaxed = true),
			codec = AlbumExportCodec(json),
			logger = AppLogger()
		)
	}

	@Test
	fun `import creates placeholders immediately without modifying existing albums`() = runTest {
		val albumDao = mockk<AlbumDao>(relaxed = true)
		val wallpaperDao = mockk<WallhavenDao>(relaxed = true)
		coEvery { wallpaperDao.findWallpaper(any()) } returns null

		repository(api(), albumDao, wallpaperDao)
			.importAlbumJson("""{"version":1,"name":"Imported","wallpapers":["abc","def"]}""")
			.getOrThrow()

		coVerify {
			wallpaperDao.upsertWallpapers(
				match { it.size == 1 && it.first().id == "abc" && !it.first().metadataResolved }
			)
		}
		coVerify {
			wallpaperDao.upsertWallpapers(match { it.first().id == "def" && !it.first().metadataResolved })
		}
		coVerify {
			albumDao.insertAlbumEntry(match { it.wallpaperId == "def" && it.position == 1 })
		}
	}

	@Test
	fun `resolution fetches metadata and marks the wallpaper resolved`() = runTest {
		server.enqueue(
			MockResponse()
				.setResponseCode(200)
				.setHeader("Content-Type", "application/json")
				.setBody(
					"""{"data":{"id":"abc","path":"https://example.com/abc.jpg","purity":"sfw","category":"general"}}"""
				)
		)
		val albumDao = mockk<AlbumDao>(relaxed = true)
		val wallpaperDao = mockk<WallhavenDao>(relaxed = true)
		coEvery { albumDao.wallpaperIdsInAlbum("a1") } returns listOf("abc")
		coEvery { wallpaperDao.findWallpaper("abc") } returns placeholder("abc")
		coEvery { albumDao.albumItems("a1") } returns emptyList()

		repository(api(), albumDao, wallpaperDao).syncAlbum("a1")

		coVerify {
			wallpaperDao.upsertWallpapers(
				match {
					it.first().metadataResolved && it.first().path == "https://example.com/abc.jpg"
				}
			)
		}
	}

	private fun placeholder(id: String) = WallpaperEntity(
		id = id,
		url = null,
		shortUrl = null,
		uploader = null,
		views = 0,
		favorites = 0,
		source = null,
		purity = "sfw",
		category = "general",
		dimensionX = null,
		dimensionY = null,
		resolution = null,
		ratio = null,
		fileSize = null,
		fileType = null,
		createdAt = null,
		colorsJson = null,
		path = "",
		thumbSmall = null,
		thumbOriginal = null,
		thumbLarge = null,
		tagsJson = null,
		tagsText = null,
		favorited = false,
		favoritedAt = null,
		metadataResolved = false,
		cachedAt = 0L
	)
}
