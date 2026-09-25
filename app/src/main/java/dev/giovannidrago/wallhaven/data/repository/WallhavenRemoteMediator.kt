package dev.giovannidrago.wallhaven.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import dev.giovannidrago.wallhaven.data.api.WallhavenApi
import dev.giovannidrago.wallhaven.data.db.GalleryEntryEntity
import dev.giovannidrago.wallhaven.data.db.RemoteKeyEntity
import dev.giovannidrago.wallhaven.data.db.WallhavenDao
import dev.giovannidrago.wallhaven.data.db.WallhavenDatabase
import dev.giovannidrago.wallhaven.data.db.WallpaperEntity
import dev.giovannidrago.wallhaven.data.db.toEntity
import dev.giovannidrago.wallhaven.data.prefs.ApiKeyStore
import dev.giovannidrago.wallhaven.domain.WallhavenQuery
import java.io.IOException
import retrofit2.HttpException

private const val FIRST_PAGE = 1

/**
 * Fetches a wallhaven listing into Room so the gallery and the search screen share the same local
 * cache, and previously browsed wallpapers stay available offline.
 */
@OptIn(ExperimentalPagingApi::class)
class WallhavenRemoteMediator(
	private val query: WallhavenQuery,
	private val api: WallhavenApi,
	private val database: WallhavenDatabase,
	private val dao: WallhavenDao,
	private val apiKeyStore: ApiKeyStore
) : RemoteMediator<Int, WallpaperEntity>() {

	override suspend fun load(loadType: LoadType, state: PagingState<Int, WallpaperEntity>): MediatorResult {
		val queryKey = query.key
		val page = when (loadType) {
			LoadType.REFRESH -> FIRST_PAGE
			LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
			LoadType.APPEND -> {
				val nextPage = dao.findRemoteKey(queryKey)?.nextPage
					?: return MediatorResult.Success(endOfPaginationReached = true)
				nextPage
			}
		}

		val apiKey = apiKeyStore.apiKey.value.takeIf { it.isNotBlank() }
		// Without a key wallhaven rejects the nsfw purity bit, so only that one is forced off.
		val effectiveQuery = if (apiKey == null) {
			query.withoutNsfw()
		} else {
			query
		}

		return try {
			val response = api.search(
				query = effectiveQuery.query,
				categories = effectiveQuery.categories,
				purity = effectiveQuery.purity,
				sorting = effectiveQuery.sorting,
				order = effectiveQuery.order,
				topRange = effectiveQuery.topRange,
				seed = if (loadType == LoadType.REFRESH) {
					null
				} else {
					dao.findRemoteKey(queryKey)?.seed
				},
				color = effectiveQuery.color,
				page = page,
				apiKey = apiKey
			)

			database.withTransaction {
				if (loadType == LoadType.REFRESH) {
					dao.deleteGalleryEntries(queryKey)
				}

				val favoriteStates = if (response.data.isEmpty()) {
					emptyMap()
				} else {
					dao.favoriteStates(response.data.map { it.id }).associateBy { it.id }
				}
				val cachedAt = System.currentTimeMillis()
				dao.upsertWallpapers(
					response.data.map { dto ->
						val favorite = favoriteStates[dto.id]
						dto.toEntity(
							cachedAt = cachedAt,
							favorited = favorite != null,
							favoritedAt = favorite?.favoritedAt
						)
					}
				)

				val basePosition = (page - FIRST_PAGE) * response.meta.perPage
				dao.insertGalleryEntries(
					response.data.mapIndexed { index, dto ->
						GalleryEntryEntity(
							queryKey = queryKey,
							wallpaperId = dto.id,
							position = basePosition + index
						)
					}
				)

				val endOfPagination = response.data.isEmpty() || page >= response.meta.lastPage
				dao.upsertRemoteKey(
					RemoteKeyEntity(
						queryKey = queryKey,
						nextPage = if (endOfPagination) {
							null
						} else {
							page + 1
						},
						seed = response.meta.seed
					)
				)
			}

			MediatorResult.Success(endOfPaginationReached = response.data.isEmpty() || page >= response.meta.lastPage)
		} catch (exception: IOException) {
			MediatorResult.Error(exception)
		} catch (exception: HttpException) {
			MediatorResult.Error(exception)
		}
	}
}
