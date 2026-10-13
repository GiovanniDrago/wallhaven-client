package dev.giovannidrago.wallhaven.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dev.giovannidrago.wallhaven.data.album.AlbumExport
import dev.giovannidrago.wallhaven.data.album.AlbumExportCodec
import dev.giovannidrago.wallhaven.data.album.AlbumImageCache
import dev.giovannidrago.wallhaven.data.api.WallhavenApi
import dev.giovannidrago.wallhaven.data.db.AlbumDao
import dev.giovannidrago.wallhaven.data.db.AlbumEntity
import dev.giovannidrago.wallhaven.data.db.AlbumEntryEntity
import dev.giovannidrago.wallhaven.data.db.AlbumImageEntity
import dev.giovannidrago.wallhaven.data.db.AlbumItemEntity
import dev.giovannidrago.wallhaven.data.db.WallhavenDao
import dev.giovannidrago.wallhaven.data.db.WallpaperEntity
import dev.giovannidrago.wallhaven.data.db.toEntity
import dev.giovannidrago.wallhaven.data.prefs.ApiKeyStore
import dev.giovannidrago.wallhaven.di.ApplicationScope
import dev.giovannidrago.wallhaven.domain.Album
import dev.giovannidrago.wallhaven.domain.AlbumSummary
import dev.giovannidrago.wallhaven.domain.LOCAL_PAGE_SIZE
import dev.giovannidrago.wallhaven.util.AppLogger
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Singleton
class AlbumRepository @Inject constructor(
	private val albumDao: AlbumDao,
	private val wallpaperDao: WallhavenDao,
	private val api: WallhavenApi,
	private val apiKeyStore: ApiKeyStore,
	private val imageCache: AlbumImageCache,
	private val codec: AlbumExportCodec,
	private val logger: AppLogger,
	@ApplicationScope private val applicationScope: CoroutineScope
) {
	fun albums(): Flow<List<AlbumSummary>> = albumDao.observeAlbumSummaries().map { rows ->
		rows.map { row ->
			AlbumSummary(
				id = row.id,
				name = row.name,
				itemCount = row.itemCount,
				previewThumbUrl = row.previewThumbUrl?.takeIf { it.isNotBlank() }
			)
		}
	}

	fun album(id: String): Flow<Album?> = combine(
		albumDao.observeAlbum(id),
		albumDao.observeAlbumItemCount(id)
	) { entity, count ->
		entity?.let { Album(it.id, it.name, it.createdAt, it.updatedAt, count) }
	}

	fun albumItems(id: String): Flow<PagingData<AlbumItemEntity>> = Pager(
		config = PagingConfig(pageSize = LOCAL_PAGE_SIZE, enablePlaceholders = false),
		pagingSourceFactory = { albumDao.albumItemsPagingSource(id) }
	).flow

	suspend fun createAlbum(name: String): Album? {
		val trimmed = name.trim()
		if (trimmed.isEmpty()) {
			return null
		}
		val now = System.currentTimeMillis()
		val entity = AlbumEntity(
			id = UUID.randomUUID().toString(),
			name = trimmed,
			createdAt = now,
			updatedAt = now
		)
		albumDao.upsertAlbum(entity)
		return Album(entity.id, entity.name, entity.createdAt, entity.updatedAt, 0)
	}

	suspend fun deleteAlbum(id: String) {
		val wallpaperIds = albumDao.wallpaperIdsInAlbum(id)
		albumDao.deleteAlbum(id)
		wallpaperIds.forEach { cleanupIfUnreferenced(it) }
	}

	suspend fun addToAlbum(albumId: String, wallpaperId: String) {
		val now = System.currentTimeMillis()
		val position = albumDao.maxPosition(albumId) + 1
		albumDao.insertAlbumEntry(
			AlbumEntryEntity(albumId = albumId, wallpaperId = wallpaperId, position = position, addedAt = now)
		)
		albumDao.touchAlbum(albumId, now)

		val existing = albumDao.findAlbumImage(wallpaperId)
		if (existing?.status != AlbumImageCache.STATUS_CACHED) {
			albumDao.upsertAlbumImage(
				AlbumImageEntity(wallpaperId, null, AlbumImageCache.STATUS_PENDING, null, null, null)
			)
			// The full-resolution download must not block the add confirmation: run it in the
			// background and let app-start / album-open sync retry it if it fails or is interrupted.
			wallpaperDao.findWallpaper(wallpaperId)?.let { wallpaper ->
				applicationScope.launch { imageCache.cache(wallpaper) }
			}
		}
	}

	suspend fun removeFromAlbum(albumId: String, wallpaperId: String) {
		albumDao.deleteAlbumEntry(albumId, wallpaperId)
		albumDao.touchAlbum(albumId, System.currentTimeMillis())
		cleanupIfUnreferenced(wallpaperId)
	}

	suspend fun exportAlbumJson(id: String): String? {
		val album = albumDao.findAlbum(id) ?: return null
		val identifiers = albumDao.albumItems(id).map { it.wallpaper.id }
		return codec.encode(AlbumExport(name = album.name, wallpapers = identifiers))
	}

	suspend fun importAlbumJson(content: String): Result<Album> =
		codec.decode(content).map { export -> importAlbum(export) }

	/** Resolves placeholders and caches missing images for every album. Called on app start. */
	suspend fun syncPendingWork() {
		resolveUnresolved(albumDao.unresolvedWallpaperIds())
		cachePending(albumDao.pendingAlbumImages())
	}

	/** Resolves placeholders and caches missing images for a single album. Called when it opens. */
	suspend fun syncAlbum(albumId: String) {
		val wallpaperIds = albumDao.wallpaperIdsInAlbum(albumId)
		resolveUnresolved(wallpaperIds)
		val pending = albumDao.albumItems(albumId)
			.filter { it.wallpaper.metadataResolved && it.cacheStatus != AlbumImageCache.STATUS_CACHED }
			.map { it.wallpaper.id }
		cachePending(pending)
	}

	private suspend fun importAlbum(export: AlbumExport): Album {
		val now = System.currentTimeMillis()
		val entity = AlbumEntity(
			id = UUID.randomUUID().toString(),
			name = export.name,
			createdAt = now,
			updatedAt = now
		)
		albumDao.upsertAlbum(entity)

		var position = 0
		for (identifier in export.wallpapers) {
			if (wallpaperDao.findWallpaper(identifier) == null) {
				wallpaperDao.upsertWallpapers(listOf(placeholder(identifier, now)))
			}
			albumDao.insertAlbumEntry(
				AlbumEntryEntity(entity.id, identifier, position, now)
			)
			position++
		}

		return Album(entity.id, entity.name, entity.createdAt, entity.updatedAt, export.wallpapers.size)
	}

	private suspend fun resolveUnresolved(wallpaperIds: List<String>) {
		if (wallpaperIds.isEmpty()) {
			return
		}
		val apiKey = apiKeyStore.apiKey.value.takeIf { it.isNotBlank() }
		for (id in wallpaperIds) {
			val existing = wallpaperDao.findWallpaper(id) ?: continue
			if (existing.metadataResolved || existing.path.isNotBlank()) {
				continue
			}
			runCatching { api.wallpaper(id, apiKey) }
				.onSuccess { response ->
					val resolved = response.data.toEntity(
						cachedAt = System.currentTimeMillis(),
						favorited = existing.favorited,
						favoritedAt = existing.favoritedAt
					)
					wallpaperDao.upsertWallpapers(listOf(resolved))
					albumDao.upsertAlbumImage(
						AlbumImageEntity(id, null, AlbumImageCache.STATUS_PENDING, null, null, null)
					)
				}
				.onFailure { error ->
					logger.debug(LOG_TAG, "resolve failed for $id: ${error.message}")
				}
		}
	}

	private suspend fun cachePending(wallpaperIds: List<String>) {
		for (id in wallpaperIds) {
			val wallpaper = wallpaperDao.findWallpaper(id) ?: continue
			if (wallpaper.path.isBlank()) {
				continue
			}
			imageCache.cache(wallpaper)
		}
	}

	private suspend fun cleanupIfUnreferenced(wallpaperId: String) {
		if (albumDao.countAlbumsReferencing(wallpaperId) > 0) {
			return
		}
		val image = albumDao.findAlbumImage(wallpaperId)
		albumDao.deleteAlbumImage(wallpaperId)
		imageCache.delete(image?.localPath)
		imageCache.deleteCachedFile(wallpaperId)
		// Deletes only an unresolved placeholder that is neither favorited nor browsed (FR-017).
		albumDao.deleteOrphanPlaceholder(wallpaperId)
	}

	private fun placeholder(id: String, now: Long): WallpaperEntity = WallpaperEntity(
		id = id,
		url = null,
		shortUrl = null,
		uploader = null,
		views = 0,
		favorites = 0,
		source = null,
		purity = "sfw",
		category = "general",
		dimensionX = null,
		dimensionY = null,
		resolution = null,
		ratio = null,
		fileSize = null,
		fileType = null,
		createdAt = null,
		colorsJson = null,
		path = "",
		thumbSmall = null,
		thumbOriginal = null,
		thumbLarge = null,
		tagsJson = null,
		tagsText = null,
		favorited = false,
		favoritedAt = null,
		metadataResolved = false,
		cachedAt = now
	)

	companion object {
		private const val LOG_TAG = "AlbumRepository"
	}
}
