package dev.giovannidrago.wallhaven.util

data class DownloadFile(
	val fileName: String,
	val mimeType: String
)

fun downloadFile(id: String, fileType: String?): DownloadFile {
	val mimeType = fileType.takeIf { !it.isNullOrBlank() } ?: "image/jpeg"
	val extension = when (mimeType) {
		"image/jpeg" -> "jpg"
		"image/png" -> "png"
		"image/webp" -> "webp"
		"image/gif" -> "gif"
		else -> "jpg"
	}
	return DownloadFile(fileName = "wallhaven-$id.$extension", mimeType = mimeType)
}
