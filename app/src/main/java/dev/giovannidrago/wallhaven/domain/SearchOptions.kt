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
 * Wallhaven only accepts one color per search, so [color] is a single palette value.
 */
data class SearchOptions(
	val query: String = "",
	val sorting: SearchSorting = SearchSorting.RELEVANCE,
	val general: Boolean = true,
	val anime: Boolean = true,
	val people: Boolean = true,
	val sketchy: Boolean = false,
	val nsfw: Boolean = false,
	val color: String? = null
) {
	val categories: String
		get() = buildCategoryMask(general, anime, people)

	val categoryValues: List<String>
		get() = buildCategoryValues(general, anime, people)

	val purity: String
		get() = buildPurityMask(sketchy, nsfw)

	val purityValues: List<String>
		get() = buildPurityValues(sketchy, nsfw)

	val categoryCount: Int
		get() = listOf(general, anime, people).count { it }

	val activeFilterCount: Int
		get() = listOf(
			sorting != SearchSorting.RELEVANCE,
			!general || !anime || !people,
			sketchy,
			nsfw,
			color != null
		).count { it }

	val key: String
		get() = "search|${sorting.apiValue}|$categories|$purity|${color.orEmpty()}|${query.trim().lowercase()}"

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
		key = key
	)
}
