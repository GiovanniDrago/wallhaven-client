package dev.giovannidrago.wallhaven.util

import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

fun formatFileSize(bytes: Long?): String? {
	if (bytes == null || bytes <= 0) {
		return null
	}

	val units = listOf("B", "KB", "MB", "GB")
	var value = bytes.toDouble()
	var unitIndex = 0
	while (value >= 1024 && unitIndex < units.lastIndex) {
		value /= 1024
		unitIndex++
	}

	val locale = Locale.getDefault()
	return if (unitIndex == 0) {
		"${value.toLong()} ${units[unitIndex]}"
	} else {
		String.format(locale, "%.1f %s", value, units[unitIndex])
	}
}

fun formatDate(createdAt: String?): String? {
	if (createdAt.isNullOrBlank()) {
		return null
	}

	return try {
		val dateTime = LocalDateTime.parse(createdAt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
		dateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.getDefault()))
	} catch (exception: DateTimeParseException) {
		createdAt
	}
}

fun formatCount(value: Int?): String? {
	if (value == null) {
		return null
	}

	return NumberFormat.getIntegerInstance().format(value)
}

fun formatFileType(mimeType: String?): String? {
	if (mimeType.isNullOrBlank()) {
		return null
	}

	return mimeType.substringAfter('/', missingDelimiterValue = mimeType).uppercase(Locale.getDefault())
}

fun formatPurity(purity: String): String = when (purity) {
	"sketchy" -> "Sketchy"
	"nsfw" -> "NSFW"
	else -> "SFW"
}
