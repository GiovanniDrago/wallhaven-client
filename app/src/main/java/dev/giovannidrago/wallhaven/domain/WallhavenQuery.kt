package dev.giovannidrago.wallhaven.domain

const val ORDER_DESC = "desc"

/**
 * Everything the wallhaven search endpoint needs, plus the key that identifies the cached pages.
 *
 * Both the gallery feed and the search screen build one of these, so they share the same remote
 * mediator, cache tables and pagination behaviour.
 */
data class WallhavenQuery(
	val query: String?,
	val categories: String,
	val purity: String,
	val sorting: String,
	val order: String,
	val topRange: String?,
	val color: String?,
	val atleast: String?,
	val resolutions: String?,
	val ratios: String?,
	val key: String
) {
	/** Wallhaven rejects nsfw results without a key, while sketchy stays available. */
	fun withoutNsfw(): WallhavenQuery {
		val nsfwDigit = purity.getOrNull(2) ?: return this
		if (nsfwDigit == '0') {
			return this
		}

		val withoutNsfw = purity.substring(0, 2) + "0"
		// When nsfw was the only enabled tier, fall back to sfw so the request stays valid.
		val effective = if (withoutNsfw == "000") {
			"100"
		} else {
			withoutNsfw
		}
		return copy(purity = effective)
	}
}
