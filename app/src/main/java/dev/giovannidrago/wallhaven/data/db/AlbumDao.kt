package dev.giovannidrago.wallhaven.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
	@Upsert
	suspend fun upsertAlbum(album: AlbumEntity)

	@Query("UPDATE albums SET updatedAt = :updatedAt WHERE id = :id")
	suspend fun touchAlbum(id: String, updatedAt: Long)

	@Query("DELETE FROM albums WHERE id = :id")
	suspend fun deleteAlbum(id: String)

	@Query("SELECT * FROM albums WHERE id = :id")
	suspend fun findAlbum(id: String): AlbumEntity?

	@Query("SELECT * FROM albums WHERE id = :id")
	fun observeAlbum(id: String): Flow<AlbumEntity?>

	@Query(
		"SELECT a.id AS id, a.name AS name, " +
			"(SELECT COUNT(*) FROM album_entries e WHERE e.albumId = a.id) AS itemCount, " +
			"(SELECT COALESCE(w.thumbLarge, w.thumbOriginal, w.path) " +
			"FROM album_entries e INNER JOIN wallpapers w ON w.id = e.wallpaperId " +
			"WHERE e.albumId = a.id ORDER BY e.position ASC LIMIT 1) AS previewThumbUrl " +
			"FROM albums a ORDER BY a.updatedAt DESC"
	)
	fun observeAlbumSummaries(): Flow<List<AlbumSummaryRow>>

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertAlbumEntry(entry: AlbumEntryEntity): Long

	@Query("SELECT COALESCE(MAX(position), -1) FROM album_entries WHERE albumId = :albumId")
	suspend fun maxPosition(albumId: String): Int

	@Query("DELETE FROM album_entries WHERE albumId = :albumId AND wallpaperId = :wallpaperId")
	suspend fun deleteAlbumEntry(albumId: String, wallpaperId: String)

	@Query("SELECT COUNT(*) FROM album_entries WHERE wallpaperId = :wallpaperId")
	suspend fun countAlbumsReferencing(wallpaperId: String): Int

	@Query("SELECT COUNT(*) FROM album_entries WHERE albumId = :albumId")
	fun observeAlbumItemCount(albumId: String): Flow<Int>

	@Query("SELECT wallpaperId FROM album_entries WHERE albumId = :albumId")
	suspend fun wallpaperIdsInAlbum(albumId: String): List<String>

	@Query(
		"SELECT w.*, e.position AS position, i.localPath AS localPath, i.status AS cacheStatus " +
			"FROM album_entries e " +
			"INNER JOIN wallpapers w ON w.id = e.wallpaperId " +
			"LEFT JOIN album_images i ON i.wallpaperId = e.wallpaperId " +
			"WHERE e.albumId = :albumId ORDER BY e.position ASC"
	)
	fun albumItemsPagingSource(albumId: String): PagingSource<Int, AlbumItemEntity>

	@Query(
		"SELECT w.*, e.position AS position, i.localPath AS localPath, i.status AS cacheStatus " +
			"FROM album_entries e " +
			"INNER JOIN wallpapers w ON w.id = e.wallpaperId " +
			"LEFT JOIN album_images i ON i.wallpaperId = e.wallpaperId " +
			"WHERE e.albumId = :albumId ORDER BY e.position ASC"
	)
	suspend fun albumItems(albumId: String): List<AlbumItemEntity>

	@Upsert
	suspend fun upsertAlbumImage(image: AlbumImageEntity)

	@Query("SELECT * FROM album_images WHERE wallpaperId = :wallpaperId")
	suspend fun findAlbumImage(wallpaperId: String): AlbumImageEntity?

	@Query("DELETE FROM album_images WHERE wallpaperId = :wallpaperId")
	suspend fun deleteAlbumImage(wallpaperId: String)

	@Query("SELECT wallpaperId FROM album_images WHERE status IN ('PENDING', 'FAILED')")
	suspend fun pendingAlbumImages(): List<String>

	@Query("SELECT id FROM wallpapers WHERE metadataResolved = 0")
	suspend fun unresolvedWallpaperIds(): List<String>

	@Query(
		"DELETE FROM wallpapers WHERE id = :id AND metadataResolved = 0 AND favorited = 0 " +
			"AND id NOT IN (SELECT wallpaperId FROM gallery_entries)"
	)
	suspend fun deleteOrphanPlaceholder(id: String)
}
