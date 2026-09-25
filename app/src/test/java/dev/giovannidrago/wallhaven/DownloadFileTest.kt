package dev.giovannidrago.wallhaven

import dev.giovannidrago.wallhaven.util.DownloadFile
import dev.giovannidrago.wallhaven.util.downloadFile
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadFileTest {
	@Test
	fun `jpeg keeps the jpg extension`() {
		assertEquals(
			DownloadFile(fileName = "wallhaven-abc123.jpg", mimeType = "image/jpeg"),
			downloadFile("abc123", "image/jpeg")
		)
	}

	@Test
	fun `png keeps its own extension`() {
		assertEquals(
			DownloadFile(fileName = "wallhaven-abc123.png", mimeType = "image/png"),
			downloadFile("abc123", "image/png")
		)
	}

	@Test
	fun `a missing file type falls back to jpeg`() {
		assertEquals(
			DownloadFile(fileName = "wallhaven-abc123.jpg", mimeType = "image/jpeg"),
			downloadFile("abc123", null)
		)
	}

	@Test
	fun `an unknown file type also falls back to jpeg`() {
		assertEquals(
			DownloadFile(fileName = "wallhaven-abc123.jpg", mimeType = "image/bmp"),
			downloadFile("abc123", "image/bmp")
		)
	}
}
