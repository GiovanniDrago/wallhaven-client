package dev.giovannidrago.wallhaven

import dev.giovannidrago.wallhaven.domain.extractVersionFromTag
import dev.giovannidrago.wallhaven.domain.isNewerVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparisonTest {

	@Test
	fun `extracts the version from a release tag`() {
		assertEquals("0.5.0", extractVersionFromTag("personal-WallhavenClient-0.5.0-16"))
		assertEquals("1.2.3", extractVersionFromTag("v1.2.3"))
		assertNull(extractVersionFromTag("latest"))
	}

	@Test
	fun `compares numeric components`() {
		assertTrue(isNewerVersion("0.5.1", "0.5.0"))
		assertTrue(isNewerVersion("1.10.0", "1.9.9"))
		assertFalse(isNewerVersion("0.4.9", "0.5.0"))
		assertFalse(isNewerVersion("0.5.0", "0.5.0"))
	}

	@Test
	fun `treats missing components as zero`() {
		assertFalse(isNewerVersion("1.2", "1.2.0"))
		assertTrue(isNewerVersion("1.2.1", "1.2"))
	}

	@Test
	fun `ignores build metadata`() {
		assertFalse(isNewerVersion("1.3.0+build", "1.3.0"))
		assertTrue(isNewerVersion("1.3.1+abc", "1.3.0"))
	}
}
