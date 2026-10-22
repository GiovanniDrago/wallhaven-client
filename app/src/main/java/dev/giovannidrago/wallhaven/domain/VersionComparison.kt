package dev.giovannidrago.wallhaven.domain

private val VERSION_GROUP = Regex("""\d+(\.\d+){0,2}""")

/** Extracts the first semver-like numeric group from a release tag, e.g. `...-0.5.0-16` → `0.5.0`. */
fun extractVersionFromTag(tag: String): String? = VERSION_GROUP.find(tag)?.value

/** Numeric major/minor/patch comparison, ignoring a leading `v` and build metadata. */
fun isNewerVersion(latest: String, current: String): Boolean {
	val latestParts = versionParts(latest)
	val currentParts = versionParts(current)
	for (index in 0 until 3) {
		val latestPart = latestParts.getOrElse(index) { 0 }
		val currentPart = currentParts.getOrElse(index) { 0 }
		if (latestPart > currentPart) {
			return true
		}
		if (latestPart < currentPart) {
			return false
		}
	}
	return false
}

private fun versionParts(version: String): List<Int> =
	version.substringBefore('+')
		.trim()
		.removePrefix("v")
		.removePrefix("V")
		.split('.')
		.map { it.toIntOrNull() ?: 0 }
