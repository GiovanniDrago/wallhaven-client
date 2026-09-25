package dev.giovannidrago.wallhaven

import dev.giovannidrago.wallhaven.domain.CategoryFilter
import dev.giovannidrago.wallhaven.domain.SearchOptions
import dev.giovannidrago.wallhaven.domain.SearchSorting
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchOptionsTest {
	@Test
	fun `defaults use relevance and no color`() {
		val options = SearchOptions()

		assertEquals(SearchSorting.RELEVANCE, options.sorting)
		assertNull(options.color)
		assertEquals(0, options.activeFilterCount)
		assertEquals("search|relevance|111|100||", options.key)
	}

	@Test
	fun `the query is trimmed and lowercased in the cache key`() {
		val options = SearchOptions(query = "  Landscape  ", color = "cc0000", sorting = SearchSorting.VIEWS)

		assertEquals("search|views|111|100|cc0000|landscape", options.key)
	}

	@Test
	fun `toQuery maps everything wallhaven needs`() {
		val options = SearchOptions(
			query = " sunset ",
			sorting = SearchSorting.TOPLIST_MONTH,
			anime = false,
			sketchy = true,
			color = "0066cc"
		)

		val query = options.toQuery()

		assertEquals("sunset", query.query)
		assertEquals("101", query.categories)
		assertEquals("110", query.purity)
		assertEquals("toplist", query.sorting)
		assertEquals("1M", query.topRange)
		assertEquals("0066cc", query.color)
		assertEquals("desc", query.order)
	}

	@Test
	fun `a blank query becomes null`() {
		assertNull(SearchOptions(query = "   ").toQuery().query)
	}

	@Test
	fun `active filters are counted for the badge`() {
		val options = SearchOptions(
			sorting = SearchSorting.RANDOM,
			people = false,
			sketchy = true,
			nsfw = true,
			color = "ffffff"
		)

		assertEquals(5, options.activeFilterCount)
	}

	@Test
	fun `with category toggles a single category`() {
		val options = SearchOptions().withCategory(CategoryFilter.GENERAL, false)

		assertEquals("011", options.categories)
		assertEquals(2, options.categoryCount)
	}
}
