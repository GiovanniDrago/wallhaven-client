package dev.giovannidrago.wallhaven

import dev.giovannidrago.wallhaven.domain.CategoryFilter
import dev.giovannidrago.wallhaven.domain.RatioFormat
import dev.giovannidrago.wallhaven.domain.SearchOptions
import dev.giovannidrago.wallhaven.domain.SearchSorting
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchOptionsTest {
	@Test
	fun `defaults use latest, sfw and no color`() {
		val options = SearchOptions()

		assertEquals(SearchSorting.LATEST, options.sorting)
		assertNull(options.color)
		assertNull(options.atleast)
		assertEquals(0, options.activeFilterCount)
		assertEquals("search|date_added|111|100|||||", options.key)
	}

	@Test
	fun `relevance stays selected without a text`() {
		val options = SearchOptions(sorting = SearchSorting.RELEVANCE)

		assertEquals("search|relevance|111|100|||||", options.key)
		assertEquals("relevance", options.toQuery().sorting)
		assertEquals(1, options.activeFilterCount)
	}

	@Test
	fun `the query is trimmed and lowercased in the cache key`() {
		val options = SearchOptions(query = "  Landscape  ", color = "cc0000", sorting = SearchSorting.VIEWS)

		assertEquals("search|views|111|100|cc0000||||landscape", options.key)
	}

	@Test
	fun `sfw can be excluded`() {
		val options = SearchOptions(sfw = false, sketchy = true)

		assertEquals("010", options.purity)
		assertEquals(listOf("sketchy"), options.purityValues)
	}

	@Test
	fun `the resolutions key is stable regardless of order`() {
		val options = SearchOptions(resolutions = listOf("3840x2160", "1920x1080"))

		assertEquals("1920x1080,3840x2160", options.resolutionsValue)
		assertEquals("search|date_added|111|100|||1920x1080,3840x2160||", options.key)
		assertEquals(
			"search|date_added|111|100||1920x1080|1920x1080,3840x2160||",
			SearchOptions(atleast = "1920x1080", resolutions = listOf("3840x2160", "1920x1080")).key
		)
	}

	@Test
	fun `the ratios key is stable regardless of order`() {
		val options = SearchOptions(ratios = listOf("9x18", "9x16"))

		assertEquals("9x16,9x18", options.ratiosValue)
		assertEquals("search|date_added|111|100||||9x16,9x18|", options.key)
	}

	@Test
	fun `ratio presets match only when the whole set is selected`() {
		assertTrue(RatioFormat.PORTRAIT.matches(listOf("9x18", "9x16", "10x16")))
		assertFalse(RatioFormat.PORTRAIT.matches(listOf("9x16")))
		assertFalse(RatioFormat.LANDSCAPE.matches(RatioFormat.PORTRAIT.ratios))
	}

	@Test
	fun `toQuery maps everything wallhaven needs`() {
		val options = SearchOptions(
			query = " sunset ",
			sorting = SearchSorting.TOPLIST_MONTH,
			anime = false,
			sketchy = true,
			color = "0066cc",
			atleast = "1920x1080",
			resolutions = listOf("3840x2160", "1920x1080"),
			ratios = listOf("9x16")
		)

		val query = options.toQuery()

		assertEquals("sunset", query.query)
		assertEquals("101", query.categories)
		assertEquals("110", query.purity)
		assertEquals("toplist", query.sorting)
		assertEquals("1M", query.topRange)
		assertEquals("0066cc", query.color)
		assertEquals("1920x1080", query.atleast)
		assertEquals("1920x1080,3840x2160", query.resolutions)
		assertEquals("9x16", query.ratios)
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
			sfw = false,
			sketchy = true,
			nsfw = true,
			color = "ffffff",
			atleast = "2560x1440",
			resolutions = listOf("3840x2160"),
			ratios = RatioFormat.PORTRAIT.ratios
		)

		assertEquals(7, options.activeFilterCount)
	}

	@Test
	fun `with category toggles a single category`() {
		val options = SearchOptions().withCategory(CategoryFilter.GENERAL, false)

		assertEquals("011", options.categories)
		assertEquals(2, options.categoryCount)
	}
}
