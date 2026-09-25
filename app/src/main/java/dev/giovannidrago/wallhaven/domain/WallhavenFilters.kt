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
 * Content filters shared by the gallery feed and the local search.
 *
 * Wallhaven encodes categories and purity as three-digit bitmasks (general/anime/people and
 * sfw/sketchy/nsfw); the same values are also stored as plain words so that Room can filter locally.
 */
data class WallhavenFilters(
	val allowNsfw: Boolean = false,
	val general: Boolean = true,
	val anime: Boolean = true,
	val people: Boolean = true,
	val sorting: Sorting = Sorting.LATEST
) {
	val categories: String
		get() = "${flag(general)}${flag(anime)}${flag(people)}"

	val purity: String
		get() = if (allowNsfw) {
			"111"
		} else {
			"100"
		}

	val categoryValues: List<String>
		get() = buildList {
			if (general) {
				add("general")
			}
			if (anime) {
				add("anime")
			}
			if (people) {
				add("people")
			}
		}.ifEmpty { listOf("general") }

	val purityValues: List<String>
		get() = if (allowNsfw) {
			listOf("sfw", "sketchy", "nsfw")
		} else {
			listOf("sfw")
		}

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

	private fun flag(enabled: Boolean) = if (enabled) {
		"1"
	} else {
		"0"
	}
}
