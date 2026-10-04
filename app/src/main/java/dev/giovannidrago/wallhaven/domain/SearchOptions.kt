package dev.giovannidrago.wallhaven.domain

enum class SearchSorting(val apiValue: String, val topRange: String? = null) {
	RELEVANCE("relevance"),
	LATEST("date_added"),
	VIEWS("views"),
	FAVORITES("favorites"),
	TOPLIST_MONTH("toplist", "1M"),
	RANDOM("random")
}

/**
 * Search-only filters, independent from the drawer toggles and persisted separately.
 *
 * Wallhaven accepts a single color and a single minimum resolution per search, plus a list of exact
 * resolutions.
 */
data class SearchOptions(
	val query: String = "",
	val sorting: SearchSorting = SearchSorting.LATEST,
	val general: Boolean = true,
	val anime: Boolean = true,
	val people: Boolean = true,
	val sfw: Boolean = true,
	val sketchy: Boolean = false,
	val nsfw: Boolean = false,
	val color: String? = null,
	val atleast: String? = null,
	val resolutions: List<String> = emptyList(),
	val ratios: List<String> = emptyList()
) {
	val categories: String
		get() = buildCategoryMask(general, anime, people)

	val categoryValues: List<String>
		get() = buildCategoryValues(general, anime, people)

	val purity: String
		get() = buildPurityMask(sketchy, nsfw, sfw)

	val purityValues: List<String>
		get() = buildPurityValues(sketchy, nsfw, sfw)

	val categoryCount: Int
		get() = listOf(general, anime, people).count { it }

	val purityCount: Int
		get() = listOf(sfw, sketchy, nsfw).count { it }

	val resolutionsValue: String
		get() = resolutions.sorted().joinToString(",")

	val ratiosValue: String
		get() = ratios.sorted().joinToString(",")

	val activeFilterCount: Int
		get() = listOf(
			sorting != SearchSorting.LATEST,
			!general || !anime || !people,
			!sfw || sketchy || nsfw,
			color != null,
			atleast != null,
			resolutions.isNotEmpty(),
			ratios.isNotEmpty()
		).count { it }

	val key: String
		get() = "search|${sorting.apiValue}|$categories|$purity|${color.orEmpty()}|" +
			"${atleast.orEmpty()}|$resolutionsValue|$ratiosValue|${query.trim().lowercase()}"

	fun withCategory(category: CategoryFilter, enabled: Boolean): SearchOptions = when (category) {
		CategoryFilter.GENERAL -> copy(general = enabled)
		CategoryFilter.ANIME -> copy(anime = enabled)
		CategoryFilter.PEOPLE -> copy(people = enabled)
	}

	fun toQuery(): WallhavenQuery = WallhavenQuery(
		query = query.trim().ifBlank { null },
		categories = categories,
		purity = purity,
		sorting = sorting.apiValue,
		order = ORDER_DESC,
		topRange = sorting.topRange,
		color = color,
		atleast = atleast,
		resolutions = resolutionsValue.ifBlank { null },
		ratios = ratiosValue.ifBlank { null },
		key = key
	)
}
