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
 * Filters of the home gallery, edited from the drawer quick toggles and the Home personalization
 * screen.
 *
 * Wallhaven encodes categories and purity as three-digit bitmasks (general/anime/people and
 * sfw/sketchy/nsfw); the same values are also stored as plain words so that Room can filter locally.
 * The refinement fields mirror the search filters (color, minimum/exact resolution, format).
 */
data class WallhavenFilters(
	val sketchy: Boolean = false,
	val nsfw: Boolean = false,
	val general: Boolean = true,
	val anime: Boolean = true,
	val people: Boolean = true,
	val sorting: Sorting = Sorting.LATEST,
	val sfw: Boolean = true,
	val color: String? = null,
	val atleast: String? = null,
	val resolutions: List<String> = emptyList(),
	val ratios: List<String> = emptyList()
) {
	val categories: String
		get() = buildCategoryMask(general, anime, people)

	val purity: String
		get() = buildPurityMask(sketchy, nsfw, sfw)

	val categoryValues: List<String>
		get() = buildCategoryValues(general, anime, people)

	val purityValues: List<String>
		get() = buildPurityValues(sketchy, nsfw, sfw)

	val sortingValue: String
		get() = sorting.apiValue

	val topRange: String?
		get() = sorting.topRange

	val categoryCount: Int
		get() = listOf(general, anime, people).count { it }

	val purityCount: Int
		get() = listOf(sfw, sketchy, nsfw).count { it }

	val resolutionsValue: String
		get() = resolutions.sorted().joinToString(",")

	val ratiosValue: String
		get() = ratios.sorted().joinToString(",")

	/**
	 * Cache key for the home pages. The base is unchanged for the default configuration; refinement
	 * segments are appended only when set, so existing cached pages stay valid until personalization.
	 */
	val key: String
		get() = buildString {
			append(sortingValue).append('|').append(categories).append('|').append(purity)
			color?.takeIf { it.isNotBlank() }?.let { append('|').append(it) }
			atleast?.takeIf { it.isNotBlank() }?.let { append('|').append(it) }
			if (resolutions.isNotEmpty()) {
				append('|').append(resolutionsValue)
			}
			if (ratios.isNotEmpty()) {
				append('|').append(ratiosValue)
			}
		}

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
		color = color?.takeIf { it.isNotBlank() },
		atleast = atleast?.takeIf { it.isNotBlank() },
		resolutions = resolutionsValue.ifBlank { null },
		ratios = ratiosValue.ifBlank { null },
		key = key
	)
}
