package dev.giovannidrago.wallhaven.domain

fun contentDigit(enabled: Boolean): String = if (enabled) {
	"1"
} else {
	"0"
}

fun buildCategoryMask(general: Boolean, anime: Boolean, people: Boolean): String =
	"${contentDigit(general)}${contentDigit(anime)}${contentDigit(people)}"

fun buildCategoryValues(general: Boolean, anime: Boolean, people: Boolean): List<String> = buildList {
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

/** Purity is a three-digit mask sfw/sketchy/nsfw where each tier can be toggled on its own. */
fun buildPurityMask(sketchy: Boolean, nsfw: Boolean, sfw: Boolean = true): String =
	"${contentDigit(sfw)}${contentDigit(sketchy)}${contentDigit(nsfw)}"

fun buildPurityValues(sketchy: Boolean, nsfw: Boolean, sfw: Boolean = true): List<String> = buildList {
	if (sfw) {
		add("sfw")
	}
	if (sketchy) {
		add("sketchy")
	}
	if (nsfw) {
		add("nsfw")
	}
}

fun hasAnyPurity(sfw: Boolean, sketchy: Boolean, nsfw: Boolean): Boolean = sfw || sketchy || nsfw
