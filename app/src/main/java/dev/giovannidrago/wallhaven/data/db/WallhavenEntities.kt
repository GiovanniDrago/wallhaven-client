package dev.giovannidrago.wallhaven.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "wallpapers")
data class WallpaperEntity(
	@PrimaryKey val id: String,
	val url: String?,
	val shortUrl: String?,
	val uploader: String?,
	val views: Int,
	val favorites: Int,
	val source: String?,
	val purity: String,
	val category: String,
	val dimensionX: Int?,
	val dimensionY: Int?,
	val resolution: String?,
	val ratio: String?,
	val fileSize: Long?,
	val fileType: String?,
	val createdAt: String?,
	val colorsJson: String?,
	val path: String,
	val thumbSmall: String?,
	val thumbOriginal: String?,
	val thumbLarge: String?,
	val tagsJson: String?,
	val tagsText: String?,
	val favorited: Boolean = false,
	val favoritedAt: Long? = null,
	// False marks a placeholder inserted for an imported album identifier that is not yet resolved.
	@ColumnInfo(defaultValue = "1")
	val metadataResolved: Boolean = true,
	val cachedAt: Long
) {
	val aspectRatio: Float
		get() {
			val width = dimensionX ?: return DEFAULT_ASPECT_RATIO
			val height = dimensionY ?: return DEFAULT_ASPECT_RATIO
			if (width <= 0 || height <= 0) {
				return DEFAULT_ASPECT_RATIO
			}
			return (width.toFloat() / height.toFloat()).coerceIn(MIN_ASPECT_RATIO, MAX_ASPECT_RATIO)
		}

	companion object {
		private const val DEFAULT_ASPECT_RATIO = 16f / 9f
		private const val MIN_ASPECT_RATIO = 0.55f
		private const val MAX_ASPECT_RATIO = 2.2f
	}
}

@Entity(
	tableName = "gallery_entries",
	primaryKeys = ["queryKey", "wallpaperId"],
	foreignKeys = [
		ForeignKey(
			entity = WallpaperEntity::class,
			parentColumns = ["id"],
			childColumns = ["wallpaperId"],
			onDelete = ForeignKey.CASCADE
		)
	],
	indices = [Index("wallpaperId")]
)
data class GalleryEntryEntity(
	val queryKey: String,
	val wallpaperId: String,
	val position: Int
)

@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
	@PrimaryKey val queryKey: String,
	val nextPage: Int?,
	val seed: String?
)
