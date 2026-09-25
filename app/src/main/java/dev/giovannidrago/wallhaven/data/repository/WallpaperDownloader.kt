package dev.giovannidrago.wallhaven.data.repository

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.giovannidrago.wallhaven.data.db.WallpaperEntity
import dev.giovannidrago.wallhaven.util.downloadFile
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

private const val PICTURES_FOLDER = "Wallhaven"

@Singleton
class WallpaperDownloader @Inject constructor(
	@ApplicationContext private val context: Context,
	private val okHttpClient: OkHttpClient
) {
	/**
	 * Streams the full resolution wallpaper into the system gallery under Pictures/Wallhaven.
	 *
	 * On Android 10+ the row is inserted with IS_PENDING so the file only becomes visible once it is
	 * complete; a failure deletes the partial row instead of leaving a broken image behind.
	 */
	suspend fun download(
		wallpaper: WallpaperEntity,
		onProgress: (downloadedBytes: Long, totalBytes: Long?) -> Unit = { _, _ -> }
	): Uri = withContext(Dispatchers.IO) {
		val resolver = context.contentResolver
		val downloadFile = downloadFile(wallpaper.id, wallpaper.fileType)
		val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
		val values = ContentValues().apply {
			put(MediaStore.MediaColumns.DISPLAY_NAME, downloadFile.fileName)
			put(MediaStore.MediaColumns.MIME_TYPE, downloadFile.mimeType)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$PICTURES_FOLDER")
				put(MediaStore.MediaColumns.IS_PENDING, 1)
			} else {
				@Suppress("DEPRECATION")
				val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), PICTURES_FOLDER)
				if (!directory.exists() && !directory.mkdirs()) {
					throw IOException("Cannot create $directory")
				}
				@Suppress("DEPRECATION")
				put(MediaStore.MediaColumns.DATA, File(directory, downloadFile.fileName).absolutePath)
			}
		}

		val uri = resolver.insert(collection, values) ?: throw IOException("MediaStore insert failed")

		try {
			val request = Request.Builder()
				.url(wallpaper.path)
				.build()
			okHttpClient.newCall(request).execute().use { response ->
				if (!response.isSuccessful) {
					throw IOException("HTTP ${response.code}")
				}

				val body = response.body
				val totalBytes = body.contentLength().takeIf { it > 0 }
				resolver.openOutputStream(uri)?.use { output ->
					body.byteStream().use { input ->
						val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
						var downloadedBytes = 0L
						while (true) {
							val read = input.read(buffer)
							if (read < 0) {
								break
							}
							output.write(buffer, 0, read)
							downloadedBytes += read
							onProgress(downloadedBytes, totalBytes)
						}
					}
				} ?: throw IOException("Cannot open the destination stream")
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				val completed = ContentValues().apply {
					put(MediaStore.MediaColumns.IS_PENDING, 0)
				}
				resolver.update(uri, completed, null, null)
			} else {
				MediaScannerConnection.scanFile(context, arrayOf(uri.path), arrayOf(downloadFile.mimeType), null)
			}

			uri
		} catch (exception: Exception) {
			resolver.delete(uri, null, null)
			throw exception
		}
	}
}
