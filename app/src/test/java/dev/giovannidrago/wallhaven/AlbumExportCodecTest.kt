package dev.giovannidrago.wallhaven

import dev.giovannidrago.wallhaven.data.album.AlbumExport
import dev.giovannidrago.wallhaven.data.album.AlbumExportCodec
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlbumExportCodecTest {
	private val codec = AlbumExportCodec(
		Json {
			ignoreUnknownKeys = true
			explicitNulls = false
			coerceInputValues = true
		}
	)

	@Test
	fun `round trip preserves the name and ordered identifiers`() {
		val encoded = codec.encode(AlbumExport(name = "Nature", wallpapers = listOf("a", "b", "c")))
		val decoded = codec.decode(encoded).getOrThrow()

		assertEquals("Nature", decoded.name)
		assertEquals(listOf("a", "b", "c"), decoded.wallpapers)
		assertEquals(1, decoded.version)
	}

	@Test
	fun `deduplicates identifiers preserving order`() {
		val decoded = codec.decode("""{"version":1,"name":"N","wallpapers":["a","b","a"]}""").getOrThrow()

		assertEquals(listOf("a", "b"), decoded.wallpapers)
	}

	@Test
	fun `tolerates unknown fields`() {
		val result = codec.decode("""{"version":1,"name":"N","wallpapers":["a"],"future":true}""")

		assertTrue(result.isSuccess)
	}

	@Test
	fun `rejects a blank name`() {
		assertTrue(codec.decode("""{"version":1,"name":"  ","wallpapers":[]}""").isFailure)
	}

	@Test
	fun `rejects an unsupported version`() {
		assertTrue(codec.decode("""{"version":99,"name":"N","wallpapers":[]}""").isFailure)
	}

	@Test
	fun `rejects malformed json`() {
		assertTrue(codec.decode("not json").isFailure)
	}

	@Test
	fun `the encoded document never contains the api key`() {
		val encoded = codec.encode(AlbumExport(name = "Nature", wallpapers = listOf("abc")))

		assertFalse(encoded.contains("api", ignoreCase = true))
		assertFalse(encoded.contains("X-API-Key", ignoreCase = true))
	}
}
