package dev.giovannidrago.wallhaven.domain

enum class AlbumImageStatus {
	PENDING,
	DOWNLOADING,
	CACHED,
	FAILED;

	companion object {
		fun fromStorage(value: String?): AlbumImageStatus =
			entries.firstOrNull { it.name == value } ?: PENDING
	}
}

data class Album(
	val id: String,
	val name: String,
	val createdAt: Long,
	val updatedAt: Long,
	val itemCount: Int
)

data class AlbumSummary(
	val id: String,
	val name: String,
	val itemCount: Int,
	val previewThumbUrl: String?
)

data class AlbumItem(
	val wallpaperId: String,
	val position: Int,
	val previewUrl: String?,
	val cacheStatus: AlbumImageStatus,
	val localPath: String?,
	val metadataResolved: Boolean
)
