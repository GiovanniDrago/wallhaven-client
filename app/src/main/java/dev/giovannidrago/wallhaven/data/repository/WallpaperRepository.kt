package dev.giovannidrago.wallhaven.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dev.giovannidrago.wallhaven.data.api.WallhavenApi
import dev.giovannidrago.wallhaven.data.api.dto.UserSettingsDto
import dev.giovannidrago.wallhaven.data.db.WallhavenDao
import dev.giovannidrago.wallhaven.data.db.WallhavenDatabase
import dev.giovannidrago.wallhaven.data.db.WallpaperEntity
import dev.giovannidrago.wallhaven.data.db.toEntity
import dev.giovannidrago.wallhaven.data.prefs.ApiKeyStore
import dev.giovannidrago.wallhaven.util.AppLogger
import dev.giovannidrago.wallhaven.domain.LOCAL_PAGE_SIZE
import dev.giovannidrago.wallhaven.domain.SearchOptions
import dev.giovannidrago.wallhaven.domain.WALLPAPERS_PER_PAGE
import dev.giovannidrago.wallhaven.domain.WallhavenFilters
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class WallpaperRepository @Inject constructor(
	private val api: WallhavenApi,
	private val database: WallhavenDatabase,
	private val dao: WallhavenDao,
	private val apiKeyStore: ApiKeyStore,
	private val diagnostics: SearchDiagnostics,
	private val logger: AppLogger
) {
	val apiKey: StateFlow<String> = apiKeyStore.apiKey

	@OptIn(ExperimentalPagingApi::class)
	fun gallery(filters: WallhavenFilters): Flow<PagingData<WallpaperEntity>> = Pager(
		config = PagingConfig(
			pageSize = WALLPAPERS_PER_PAGE,
			prefetchDistance = WALLPAPERS_PER_PAGE,
			enablePlaceholders = false
		),
		remoteMediator = WallhavenRemoteMediator(filters.toQuery(), api, database, dao, apiKeyStore, diagnostics, logger),
		pagingSourceFactory = { dao.galleryPagingSource(filters.key) }
	).flow

	@OptIn(ExperimentalPagingApi::class)
	fun search(options: SearchOptions): Flow<PagingData<WallpaperEntity>> = Pager(
		config = PagingConfig(
			pageSize = WALLPAPERS_PER_PAGE,
			prefetchDistance = WALLPAPERS_PER_PAGE,
			enablePlaceholders = false
		),
		remoteMediator = WallhavenRemoteMediator(options.toQuery(), api, database, dao, apiKeyStore, diagnostics, logger),
		pagingSourceFactory = { dao.galleryPagingSource(options.key) }
	).flow

	fun favorites(): Flow<PagingData<WallpaperEntity>> = Pager(
		config = PagingConfig(pageSize = LOCAL_PAGE_SIZE, enablePlaceholders = false),
		pagingSourceFactory = { dao.favoritesPagingSource() }
	).flow

	fun observeWallpaper(id: String): Flow<WallpaperEntity?> = dao.observeWallpaper(id)

	suspend fun setFavorite(id: String, favorited: Boolean) {
		dao.updateFavorite(id, favorited, if (favorited) System.currentTimeMillis() else null)
	}

	/** Refreshes a cached wallpaper with the full detail payload, keeping the local favorite state. */
	suspend fun refreshWallpaper(id: String) {
		val apiKey = apiKeyStore.apiKey.value.takeIf { it.isNotBlank() }
		val response = api.wallpaper(id, apiKey)
		val existing = dao.findWallpaper(id)
		val favorited = existing?.favorited == true
		dao.upsertWallpapers(
			listOf(
				response.data.toEntity(
					cachedAt = System.currentTimeMillis(),
					favorited = favorited,
					favoritedAt = if (favorited) {
						existing?.favoritedAt
					} else {
						null
					}
				)
			)
		)
	}

	/** Validates the key against the account settings endpoint and returns the account settings. */
	suspend fun verifyApiKey(apiKey: String): UserSettingsDto = api.settings(apiKey).data
}
