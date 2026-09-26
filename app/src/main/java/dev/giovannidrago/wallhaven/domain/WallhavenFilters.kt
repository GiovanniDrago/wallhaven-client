package dev.giovannidrago.wallhaven.domain

const val WALLPAPERS_PER_PAGE = 24
const val LOCAL_PAGE_SIZE = 60

enum class Sorting {
	LATEST,
	TOPLIST_MONTH,
	RANDOM;

	val apiValue: String
		get() = when (this) {
			LATEST -> "date_added"
			TOPLIST_MONTH -> "toplist"
			RANDOM -> "random"
		}

	val topRange: String?
		get() = if (this == TOPLIST_MONTH) {
			"1M"
		} else {
			null
		}
}

enum class CategoryFilter {
	GENERAL,
	ANIME,
	PEOPLE
}

/**
 * Content filters of the main gallery, edited from the drawer.
 *
 * Wallhaven encodes categories and purity as three-digit bitmasks (general/anime/people and
 * sfw/sketchy/nsfw); the same values are also stored as plain words so that Room can filter locally.
 */
data class WallhavenFilters(
	val sketchy: Boolean = false,
	val nsfw: Boolean = false,
	val general: Boolean = true,
	val anime: Boolean = true,
	val people: Boolean = true,
	val sorting: Sorting = Sorting.LATEST
) {
	val categories: String
		get() = buildCategoryMask(general, anime, people)

	val purity: String
		get() = buildPurityMask(sketchy, nsfw)

	val categoryValues: List<String>
		get() = buildCategoryValues(general, anime, people)

	val purityValues: List<String>
		get() = buildPurityValues(sketchy, nsfw)

	val sortingValue: String
		get() = sorting.apiValue

	val topRange: String?
		get() = sorting.topRange

	val categoryCount: Int
		get() = listOf(general, anime, people).count { it }

	val key: String
		get() = "$sortingValue|$categories|$purity"

	fun withCategory(category: CategoryFilter, enabled: Boolean): WallhavenFilters = when (category) {
		CategoryFilter.GENERAL -> copy(general = enabled)
		CategoryFilter.ANIME -> copy(anime = enabled)
		CategoryFilter.PEOPLE -> copy(people = enabled)
	}

	fun toQuery(): WallhavenQuery = WallhavenQuery(
		query = null,
		categories = categories,
		purity = purity,
		sorting = sortingValue,
		order = ORDER_DESC,
		topRange = topRange,
		color = null,
		atleast = null,
		resolutions = null,
		key = key
	)
}
