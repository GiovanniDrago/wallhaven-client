package dev.giovannidrago.wallhaven

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dev.giovannidrago.wallhaven.data.api.GithubApi
import dev.giovannidrago.wallhaven.data.prefs.UpdatePreferences
import dev.giovannidrago.wallhaven.data.repository.UpdateCheckResult
import dev.giovannidrago.wallhaven.data.repository.UpdateRepository
import dev.giovannidrago.wallhaven.util.AppLogger
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class UpdateRepositoryTest {
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

	private fun api(): GithubApi = Retrofit.Builder()
		.baseUrl(server.url("/"))
		.addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
		.build()
		.create(GithubApi::class.java)

	private fun repository(preferences: UpdatePreferences = mockk(relaxed = true)) =
		UpdateRepository(api(), preferences, AppLogger())

	private fun nextPatch(version: String): String {
		val parts = version.substringBefore('-').split('.')
		val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
		val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
		val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
		return "$major.$minor.${patch + 1}"
	}

	@Test
	fun `reports an update when the release is newer and prefers the latest apk`() = runTest {
		val repository = repository()
		val next = nextPatch(repository.installedVersion)
		server.enqueue(
			MockResponse()
				.setResponseCode(200)
				.setBody(
					"""{"tag_name":"personal-WallhavenClient-$next-20","html_url":"https://example.com/release","assets":[{"name":"WallhavenClient-$next-20.apk","browser_download_url":"https://example.com/versioned.apk"},{"name":"WallhavenClient-latest.apk","browser_download_url":"https://example.com/latest.apk"}]}"""
				)
		)

		val result = repository.check()

		assertTrue(result is UpdateCheckResult.UpdateAvailable)
		result as UpdateCheckResult.UpdateAvailable
		assertEquals(next, result.version)
		assertEquals("https://example.com/latest.apk", result.downloadUrl)
	}

	@Test
	fun `reports up to date when the release is not newer`() = runTest {
		server.enqueue(
			MockResponse()
				.setResponseCode(200)
				.setBody("""{"tag_name":"personal-WallhavenClient-0.5.0-16","html_url":"https://example.com/release","assets":[]}""")
		)

		assertEquals(UpdateCheckResult.UpToDate, repository().check())
	}

	@Test
	fun `falls back to the release page when there is no apk asset`() = runTest {
		val repository = repository()
		val next = nextPatch(repository.installedVersion)
		server.enqueue(
			MockResponse()
				.setResponseCode(200)
				.setBody("""{"tag_name":"personal-WallhavenClient-$next-30","html_url":"https://example.com/release","assets":[{"name":"notes.txt","browser_download_url":"https://example.com/notes.txt"}]}""")
		)

		val result = repository.check()

		result as UpdateCheckResult.UpdateAvailable
		assertEquals("https://example.com/release", result.downloadUrl)
	}

	@Test
	fun `reports an error on a non-200 response`() = runTest {
		server.enqueue(MockResponse().setResponseCode(500))

		assertEquals(UpdateCheckResult.Error, repository().check())
	}

	@Test
	fun `silent check is throttled to once per calendar day`() = runTest {
		val preferences = mockk<UpdatePreferences>(relaxed = true)
		coEvery { preferences.lastCheckDate() } returns "2026-10-15"
		val repository = repository(preferences)

		assertFalse(repository.shouldRunSilentCheck("2026-10-15"))
		assertTrue(repository.shouldRunSilentCheck("2026-10-16"))
	}
}
