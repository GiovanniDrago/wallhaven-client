package dev.giovannidrago.wallhaven

import dev.giovannidrago.wallhaven.domain.WallhavenQuery
import org.junit.Assert.assertEquals
import org.junit.Test

class WallhavenQueryTest {
	private fun query(purity: String) = WallhavenQuery(
		query = "test",
		categories = "111",
		purity = purity,
		sorting = "relevance",
		order = "desc",
		topRange = null,
		color = null,
		key = "k"
	)

	@Test
	fun `without nsfw keeps sketchy enabled`() {
		assertEquals("110", query("111").withoutNsfw().purity)
		assertEquals("110", query("110").withoutNsfw().purity)
	}

	@Test
	fun `without nsfw clears the nsfw bit alone`() {
		assertEquals("100", query("101").withoutNsfw().purity)
		assertEquals("100", query("100").withoutNsfw().purity)
	}
}
