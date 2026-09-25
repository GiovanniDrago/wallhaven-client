package dev.giovannidrago.wallhaven.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WallhavenDao {
	@Upsert
	suspend fun upsertWallpapers(wallpapers: List<WallpaperEntity>)

	@Query("SELECT * FROM wallpapers WHERE id = :id")
	suspend fun findWallpaper(id: String): WallpaperEntity?

	@Query("SELECT * FROM wallpapers WHERE id = :id")
	fun observeWallpaper(id: String): Flow<WallpaperEntity?>

	@Query("SELECT * FROM wallpapers WHERE favorited = 1 AND id IN (:ids)")
	suspend fun favoriteStates(ids: List<String>): List<WallpaperEntity>

	@Query("UPDATE wallpapers SET favorited = :favorited, favoritedAt = :favoritedAt WHERE id = :id")
	suspend fun updateFavorite(id: String, favorited: Boolean, favoritedAt: Long?)

	@Query(
		"SELECT w.* FROM wallpapers AS w " +
			"INNER JOIN gallery_entries AS e ON e.wallpaperId = w.id " +
			"WHERE e.queryKey = :queryKey ORDER BY e.position ASC"
	)
	fun galleryPagingSource(queryKey: String): PagingSource<Int, WallpaperEntity>

	@Query("SELECT * FROM wallpapers WHERE favorited = 1 ORDER BY favoritedAt DESC")
	fun favoritesPagingSource(): PagingSource<Int, WallpaperEntity>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertGalleryEntries(entries: List<GalleryEntryEntity>)

	@Query("DELETE FROM gallery_entries WHERE queryKey = :queryKey")
	suspend fun deleteGalleryEntries(queryKey: String)

	@Query("SELECT * FROM remote_keys WHERE queryKey = :queryKey")
	suspend fun findRemoteKey(queryKey: String): RemoteKeyEntity?

	@Upsert
	suspend fun upsertRemoteKey(remoteKey: RemoteKeyEntity)
}
