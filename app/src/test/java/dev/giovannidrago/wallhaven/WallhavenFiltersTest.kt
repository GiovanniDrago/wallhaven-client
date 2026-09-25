package dev.giovannidrago.wallhaven

import dev.giovannidrago.wallhaven.domain.CategoryFilter
import dev.giovannidrago.wallhaven.domain.Sorting
import dev.giovannidrago.wallhaven.domain.WallhavenFilters
import org.junit.Assert.assertEquals
import org.junit.Test

class WallhavenFiltersTest {
	@Test
	fun `defaults allow every category and only sfw purity`() {
		val filters = WallhavenFilters()

		assertEquals("111", filters.categories)
		assertEquals("100", filters.purity)
		assertEquals(listOf("sfw"), filters.purityValues)
	}

	@Test
	fun `enabling nsfw grants sketchy and nsfw too`() {
		val filters = WallhavenFilters(allowNsfw = true)

		assertEquals("111", filters.purity)
		assertEquals(listOf("sfw", "sketchy", "nsfw"), filters.purityValues)
	}

	@Test
	fun `disabling categories builds the expected mask`() {
		val filters = WallhavenFilters(general = false, anime = true, people = false)

		assertEquals("010", filters.categories)
		assertEquals(listOf("anime"), filters.categoryValues)
	}

	@Test
	fun `an empty category selection falls back to general`() {
		val filters = WallhavenFilters(general = false, anime = false, people = false)

		assertEquals(listOf("general"), filters.categoryValues)
		assertEquals(0, filters.categoryCount)
	}

	@Test
	fun `with category toggles a single category`() {
		val filters = WallhavenFilters().withCategory(CategoryFilter.PEOPLE, false)

		assertEquals("110", filters.categories)
		assertEquals(2, filters.categoryCount)
	}

	@Test
	fun `query key changes with sorting and filters`() {
		assertEquals("date_added|111|100", WallhavenFilters().key)
		assertEquals(
			"toplist|101|111",
			WallhavenFilters(
				allowNsfw = true,
				anime = false,
				sorting = Sorting.TOPLIST_MONTH
			).key
		)
	}
}
