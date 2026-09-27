package dev.giovannidrago.wallhaven.domain

/** The wallhaven search palette, without the leading '#'. */
val WALLHAVEN_COLORS: List<String> = listOf(
	"660000",
	"990000",
	"cc0000",
	"cc3333",
	"ea4c88",
	"993399",
	"663399",
	"333399",
	"0066cc",
	"0099cc",
	"66cccc",
	"77cc33",
	"669900",
	"336600",
	"666600",
	"999900",
	"cccc33",
	"ffff00",
	"ffcc33",
	"ff9900",
	"ff6600",
	"cc6633",
	"996633",
	"663300",
	"000000",
	"999999",
	"cccccc",
	"ffffff",
	"424153"
)

/** Common minimum resolutions offered by the "at least" filter. */
val ATLEAST_RESOLUTIONS: List<String> = listOf(
	"1280x720",
	"1920x1080",
	"2560x1440",
	"3840x2160"
)

/** Common exact resolutions offered by the "exactly" filter (multi-select). */
val EXACT_RESOLUTIONS: List<String> = listOf(
	"1280x720",
	"1600x900",
	"1920x1080",
	"1920x1200",
	"2560x1080",
	"2560x1440",
	"3440x1440",
	"3840x2160"
)

/**
 * Orientation presets for the "format" filter.
 *
 * Wallhaven applies the ratio list only when all the values share the same orientation (mixing
 * landscape and portrait returns just the first group), so the UI keeps a single selection at a time.
 */
enum class RatioFormat(val ratios: List<String>) {
	LANDSCAPE(listOf("16x9", "16x10", "3x2", "4x3", "21x9", "32x9", "48x9", "5x4")),
	PORTRAIT(listOf("9x16", "10x16", "9x18")),
	SQUARE(listOf("1x1"));

	fun matches(ratios: List<String>): Boolean = ratios.toSet() == this.ratios.toSet()
}

/** Single aspect ratios offered alongside the orientation presets (single selection). */
val EXACT_RATIOS: List<String> = listOf(
	"16x9",
	"16x10",
	"21x9",
	"3x2",
	"4x3",
	"9x16",
	"10x16",
	"9x18",
	"1x1"
)
