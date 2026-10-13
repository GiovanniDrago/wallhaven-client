package dev.giovannidrago.wallhaven.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
	@PrimaryKey val id: String,
	val name: String,
	val createdAt: Long,
	val updatedAt: Long
)

@Entity(
	tableName = "album_entries",
	primaryKeys = ["albumId", "wallpaperId"],
	foreignKeys = [
		ForeignKey(
			entity = AlbumEntity::class,
			parentColumns = ["id"],
			childColumns = ["albumId"],
			onDelete = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = WallpaperEntity::class,
			parentColumns = ["id"],
			childColumns = ["wallpaperId"],
			onDelete = ForeignKey.CASCADE
		)
	],
	indices = [Index("wallpaperId")]
)
data class AlbumEntryEntity(
	val albumId: String,
	val wallpaperId: String,
	val position: Int,
	val addedAt: Long
)

@Entity(
	tableName = "album_images",
	foreignKeys = [
		ForeignKey(
			entity = WallpaperEntity::class,
			parentColumns = ["id"],
			childColumns = ["wallpaperId"],
			onDelete = ForeignKey.CASCADE
		)
	]
)
data class AlbumImageEntity(
	@PrimaryKey val wallpaperId: String,
	val localPath: String?,
	val status: String,
	val bytes: Long?,
	val cachedAt: Long?,
	val lastError: String?
)

/** Read projection for the album grid: the wallpaper row plus its membership and cache state. */
data class AlbumItemEntity(
	@Embedded val wallpaper: WallpaperEntity,
	val position: Int,
	val localPath: String?,
	val cacheStatus: String?
)

/** Read projection for the album list: name, member count and a single first-member thumbnail. */
data class AlbumSummaryRow(
	val id: String,
	val name: String,
	val itemCount: Int,
	val previewThumbUrl: String?
)
