package dev.giovannidrago.wallhaven.data.album

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.giovannidrago.wallhaven.data.db.AlbumDao
import dev.giovannidrago.wallhaven.data.db.AlbumImageEntity
import dev.giovannidrago.wallhaven.data.db.WallpaperEntity
import dev.giovannidrago.wallhaven.util.AppLogger
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

private const val DIRECTORY = "album_images"
private const val LOG_TAG = "AlbumImageCache"

/**
 * Keeps the full-resolution image of every album wallpaper in app-private storage so albums stay
 * viewable offline. The download carries no API key: the image URL is public and the key is only
 * ever sent through the `X-API-Key` header on API calls (constitution Principle III).
 */
@Singleton
class AlbumImageCache @Inject constructor(
	@ApplicationContext private val context: Context,
	private val okHttpClient: OkHttpClient,
	private val albumDao: AlbumDao,
	private val logger: AppLogger
) {
	suspend fun cache(wallpaper: WallpaperEntity) = withContext(Dispatchers.IO) {
		val url = wallpaper.path
		if (url.isBlank()) {
			return@withContext
		}

		albumDao.upsertAlbumImage(
			AlbumImageEntity(wallpaper.id, null, STATUS_DOWNLOADING, null, null, null)
		)

		try {
			val request = Request.Builder().url(url).build()
			okHttpClient.newCall(request).execute().use { response ->
				if (!response.isSuccessful) {
					throw IOException("HTTP ${response.code}")
				}

				val directory = File(context.filesDir, DIRECTORY).apply { mkdirs() }
				val file = File(directory, "${wallpaper.id}.${extensionFor(wallpaper, url)}")
				response.body.byteStream().use { input ->
					file.outputStream().use { output -> input.copyTo(output) }
				}

				albumDao.upsertAlbumImage(
					AlbumImageEntity(
						wallpaperId = wallpaper.id,
						localPath = file.absolutePath,
						status = STATUS_CACHED,
						bytes = file.length(),
						cachedAt = System.currentTimeMillis(),
						lastError = null
					)
				)
			}
		} catch (exception: Exception) {
			logger.debug(LOG_TAG, "cache failed for ${wallpaper.id}: ${exception.message}")
			albumDao.upsertAlbumImage(
				AlbumImageEntity(
					wallpaperId = wallpaper.id,
					localPath = null,
					status = STATUS_FAILED,
					bytes = null,
					cachedAt = null,
					lastError = exception.message?.take(200)
				)
			)
		}
	}

	suspend fun delete(localPath: String?) = withContext(Dispatchers.IO) {
		if (localPath.isNullOrBlank()) {
			return@withContext
		}
		runCatching { File(localPath).delete() }
	}

	suspend fun deleteCachedFile(wallpaperId: String) = withContext(Dispatchers.IO) {
		val directory = File(context.filesDir, DIRECTORY)
		directory.listFiles()
			.orEmpty()
			.filter { it.name.startsWith("$wallpaperId.") }
			.forEach { runCatching { it.delete() } }
	}

	private fun extensionFor(wallpaper: WallpaperEntity, url: String): String {
		val fromType = when (wallpaper.fileType) {
			"image/png" -> "png"
			"image/webp" -> "webp"
			"image/gif" -> "gif"
			"image/jpeg" -> "jpg"
			null -> null
			else -> null
		}
		if (fromType != null) {
			return fromType
		}
		val fromUrl = url.substringAfterLast('.', "").substringBefore('?').lowercase()
		return fromUrl.takeIf { it in setOf("jpg", "jpeg", "png", "webp", "gif") } ?: "jpg"
	}

	companion object {
		const val STATUS_PENDING = "PENDING"
		const val STATUS_DOWNLOADING = "DOWNLOADING"
		const val STATUS_CACHED = "CACHED"
		const val STATUS_FAILED = "FAILED"
	}
}
