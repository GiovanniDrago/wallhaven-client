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

/** The first purity digit is always sfw; sketchy and nsfw can be toggled independently. */
fun buildPurityMask(sketchy: Boolean, nsfw: Boolean): String =
	"1${contentDigit(sketchy)}${contentDigit(nsfw)}"

fun buildPurityValues(sketchy: Boolean, nsfw: Boolean): List<String> = buildList {
	add("sfw")
	if (sketchy) {
		add("sketchy")
	}
	if (nsfw) {
		add("nsfw")
	}
}
