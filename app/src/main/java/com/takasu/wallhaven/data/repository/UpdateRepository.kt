package com.takasu.wallhaven.data.repository

import com.takasu.wallhaven.BuildConfig
import com.takasu.wallhaven.data.api.GithubApi
import com.takasu.wallhaven.data.api.dto.GithubReleaseDto
import com.takasu.wallhaven.data.prefs.UpdatePreferences
import com.takasu.wallhaven.domain.extractVersionFromTag
import com.takasu.wallhaven.domain.isNewerVersion
import com.takasu.wallhaven.util.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

sealed interface UpdateCheckResult {
	data object UpToDate : UpdateCheckResult

	data class UpdateAvailable(val version: String, val downloadUrl: String) : UpdateCheckResult

	data object Error : UpdateCheckResult
}

/**
 * Checks the app's GitHub "latest release" and compares it with the installed version. Mirrors the
 * reference app's criteria: no authentication, numeric version comparison, first APK asset (else the
 * release page), and a silent check throttled to once per calendar day. It never sends or logs a
 * secret (Principle III).
 */
@Singleton
class UpdateRepository @Inject constructor(
	private val api: GithubApi,
	private val preferences: UpdatePreferences,
	private val logger: AppLogger
) {
	val installedVersion: String = BuildConfig.VERSION_NAME

	suspend fun check(): UpdateCheckResult = try {
		val release = api.latestRelease(ACCEPT, "wallhaven-client/$installedVersion")
		val version = release.tagName?.let { extractVersionFromTag(it) }
		if (version.isNullOrBlank() || !isNewerVersion(version, installedVersion)) {
			UpdateCheckResult.UpToDate
		} else {
			val downloadUrl = pickDownloadUrl(release) ?: release.htmlUrl
			if (downloadUrl.isNullOrBlank()) {
				UpdateCheckResult.Error
			} else {
				UpdateCheckResult.UpdateAvailable(version, downloadUrl)
			}
		}
	} catch (exception: Exception) {
		logger.debug(LOG_TAG, "update check failed: ${exception.message}")
		UpdateCheckResult.Error
	}

	suspend fun shouldRunSilentCheck(today: String): Boolean = preferences.lastCheckDate() != today

	suspend fun markChecked(today: String) {
		preferences.markChecked(today)
	}

	private fun pickDownloadUrl(release: GithubReleaseDto): String? {
		val apks = release.assets.filter { asset ->
			asset.name?.endsWith(".apk", ignoreCase = true) == true && !asset.downloadUrl.isNullOrBlank()
		}
		val latest = apks.firstOrNull { it.name?.equals(LATEST_APK, ignoreCase = true) == true }
		return (latest ?: apks.firstOrNull())?.downloadUrl
	}

	companion object {
		private const val ACCEPT = "application/vnd.github+json"
		private const val LATEST_APK = "WallhavenClient-latest.apk"
		private const val LOG_TAG = "UpdateRepository"
	}
}
