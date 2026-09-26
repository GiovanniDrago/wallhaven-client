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
