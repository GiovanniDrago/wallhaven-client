package dev.giovannidrago.wallhaven.ui.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.data.db.WallpaperEntity
import dev.giovannidrago.wallhaven.data.db.colors
import dev.giovannidrago.wallhaven.data.db.tags
import dev.giovannidrago.wallhaven.ui.albums.AddToAlbumSheet
import dev.giovannidrago.wallhaven.ui.components.FULLSCREEN_MAX_SCALE
import dev.giovannidrago.wallhaven.ui.components.ZoomableImage
import dev.giovannidrago.wallhaven.ui.components.openUrl
import dev.giovannidrago.wallhaven.ui.components.toComposeColor
import dev.giovannidrago.wallhaven.util.formatCount
import dev.giovannidrago.wallhaven.util.formatDate
import dev.giovannidrago.wallhaven.util.formatFileSize
import dev.giovannidrago.wallhaven.util.formatFileType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
	onBack: () -> Unit,
	onTagClick: (String) -> Unit,
	viewModel: DetailViewModel = hiltViewModel()
) {
	val wallpaper by viewModel.wallpaper.collectAsStateWithLifecycle()
	val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
	val snackbarHostState = remember { SnackbarHostState() }
	val context = LocalContext.current
	val downloadSuccessMessage = stringResource(R.string.download_success)
	val downloadFailedMessage = stringResource(R.string.download_failed)
	val actionOpenLabel = stringResource(R.string.action_open)
	val currentWallpaper = wallpaper
	val albumChoices by viewModel.albumChoices.collectAsStateWithLifecycle()
	val albumEvent by viewModel.albumEvent.collectAsStateWithLifecycle()
	val albumAddedMessage = stringResource(R.string.album_added)
	var showAlbumSheet by remember { mutableStateOf(false) }
	var fullScreen by remember { mutableStateOf(false) }

	LaunchedEffect(downloadState) {
		when (val state = downloadState) {
			is DownloadState.Success -> {
				val result = snackbarHostState.showSnackbar(
					message = downloadSuccessMessage,
					actionLabel = actionOpenLabel,
					withDismissAction = true
				)
				if (result == SnackbarResult.ActionPerformed) {
					openUri(context, state.uri)
				}
				viewModel.consumeDownloadState()
			}
			is DownloadState.Failed -> {
				snackbarHostState.showSnackbar(downloadFailedMessage)
				viewModel.consumeDownloadState()
			}
			DownloadState.Idle, is DownloadState.Downloading -> Unit
		}
	}

	LaunchedEffect(albumEvent) {
		when (albumEvent) {
			AlbumEvent.Added -> {
				showAlbumSheet = false
				snackbarHostState.showSnackbar(albumAddedMessage)
				viewModel.consumeAlbumEvent()
			}
			AlbumEvent.BlankName -> viewModel.consumeAlbumEvent()
			null -> Unit
		}
	}

	BackHandler(enabled = fullScreen) { fullScreen = false }

	Box(modifier = Modifier.fillMaxSize()) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(currentWallpaper?.id ?: stringResource(R.string.detail_title)) },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = stringResource(R.string.cd_back)
						)
					}
				},
				actions = {
					if (currentWallpaper != null) {
						IconButton(onClick = viewModel::toggleFavorite) {
							Icon(
								imageVector = if (currentWallpaper.favorited) {
									Icons.Filled.Favorite
								} else {
									Icons.Outlined.FavoriteBorder
								},
								contentDescription = stringResource(R.string.cd_favorite),
								tint = if (currentWallpaper.favorited) {
									MaterialTheme.colorScheme.primary
								} else {
									LocalContentColor.current
								}
							)
						}
					}
				}
			)
		},
		snackbarHost = { SnackbarHost(snackbarHostState) }
	) { padding ->
		if (currentWallpaper == null) {
			Box(
				modifier = Modifier
					.padding(padding)
					.fillMaxSize(),
				contentAlignment = Alignment.Center
			) {
				CircularProgressIndicator()
			}
		} else {
			WallpaperDetailContent(
				wallpaper = currentWallpaper,
				downloadState = downloadState,
				onDownload = viewModel::download,
				onToggleFavorite = viewModel::toggleFavorite,
				onAddToAlbum = {
					viewModel.loadAlbumChoices()
					showAlbumSheet = true
				},
				onTagClick = onTagClick,
				onOpenUrl = { url -> openUrl(context, url) },
				onImageTap = { fullScreen = true },
				resetKey = fullScreen,
				modifier = Modifier.padding(padding)
			)
		}
	}

		if (fullScreen && currentWallpaper != null) {
			FullScreenImage(url = currentWallpaper.path, onClose = { fullScreen = false })
		}
	}

	if (showAlbumSheet) {
		AddToAlbumSheet(
			albums = albumChoices,
			onAddExisting = viewModel::addToAlbum,
			onCreateAndAdd = viewModel::createAlbumAndAdd,
			onDismiss = { showAlbumSheet = false }
		)
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WallpaperDetailContent(
	wallpaper: WallpaperEntity,
	downloadState: DownloadState,
	onDownload: () -> Unit,
	onToggleFavorite: () -> Unit,
	onAddToAlbum: () -> Unit,
	onTagClick: (String) -> Unit,
	onOpenUrl: (String) -> Unit,
	onImageTap: () -> Unit,
	resetKey: Any,
	modifier: Modifier = Modifier
) {
	LazyColumn(
		modifier = modifier.fillMaxSize(),
		contentPadding = PaddingValues(bottom = 32.dp)
	) {
		item {
			key(resetKey) {
				ZoomableImage(
					url = wallpaper.path,
					onSingleTap = onImageTap,
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 8.dp)
				)
			}
		}
		item {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp),
				horizontalArrangement = Arrangement.spacedBy(12.dp)
			) {
				Button(
					onClick = onDownload,
					enabled = downloadState !is DownloadState.Downloading,
					modifier = Modifier.weight(1f)
				) {
					Icon(
						imageVector = Icons.Default.Download,
						contentDescription = null
					)
					Spacer(modifier = Modifier.width(8.dp))
					Text(
						text = stringResource(
							if (downloadState is DownloadState.Downloading) {
								R.string.downloading
							} else {
								R.string.download
							}
						),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
				}
				FilledTonalButton(
					onClick = onToggleFavorite,
					modifier = Modifier.weight(1f)
				) {
					Icon(
						imageVector = if (wallpaper.favorited) {
							Icons.Filled.Favorite
						} else {
							Icons.Outlined.FavoriteBorder
						},
						contentDescription = null
					)
					Spacer(modifier = Modifier.width(8.dp))
					Text(
						text = stringResource(
							if (wallpaper.favorited) {
								R.string.favorite_remove
							} else {
								R.string.favorite_add
							}
						),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
				}
			}
		}
		item {
			FilledTonalButton(
				onClick = onAddToAlbum,
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp)
			) {
				Icon(
					imageVector = Icons.Default.LibraryAdd,
					contentDescription = null
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(stringResource(R.string.album_add_to))
			}
		}
		if (downloadState is DownloadState.Downloading) {
			item {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp)
				) {
					val progress = downloadState.progress
					if (progress == null) {
						LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
					} else {
						LinearProgressIndicator(
							progress = { progress },
							modifier = Modifier.fillMaxWidth()
						)
					}
					Text(
						text = stringResource(R.string.downloading),
						style = MaterialTheme.typography.bodySmall,
						modifier = Modifier.padding(top = 4.dp)
					)
				}
			}
		}
		item {
			InfoSection(
				wallpaper = wallpaper,
				onTagClick = onTagClick,
				onOpenUrl = onOpenUrl
			)
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InfoSection(
	wallpaper: WallpaperEntity,
	onTagClick: (String) -> Unit,
	onOpenUrl: (String) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		Surface(
			shape = RoundedCornerShape(16.dp),
			color = MaterialTheme.colorScheme.surfaceVariant,
			modifier = Modifier.fillMaxWidth()
		) {
			Column(
				modifier = Modifier.padding(16.dp),
				verticalArrangement = Arrangement.spacedBy(10.dp)
			) {
				InfoRow(labelRes = R.string.info_resolution, value = wallpaper.resolution)
				InfoRow(labelRes = R.string.info_ratio, value = wallpaper.ratio)
				InfoRow(labelRes = R.string.info_type, value = formatFileType(wallpaper.fileType))
				InfoRow(labelRes = R.string.info_size, value = formatFileSize(wallpaper.fileSize))
				InfoRow(labelRes = R.string.info_views, value = formatCount(wallpaper.views))
				InfoRow(labelRes = R.string.info_favorites, value = formatCount(wallpaper.favorites))
				InfoRow(labelRes = R.string.info_uploaded, value = formatDate(wallpaper.createdAt))
				InfoRow(labelRes = R.string.info_uploader, value = wallpaper.uploader)
				InfoRow(labelRes = R.string.info_id, value = wallpaper.id)
				InfoRow(
					labelRes = R.string.info_source,
					value = wallpaper.source,
					onClick = { onOpenUrl(wallpaper.source.orEmpty()) }
				)
			}
		}

		val tags = wallpaper.tags()
		if (tags.isNotEmpty()) {
			Text(
				text = stringResource(R.string.info_tags),
				style = MaterialTheme.typography.titleSmall
			)
			FlowRow(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				tags.forEach { tag ->
					AssistChip(
						onClick = { onTagClick(tag) },
						label = { Text(tag) }
					)
				}
			}
		}

		val colors = wallpaper.colors()
		if (colors.isNotEmpty()) {
			Text(
				text = stringResource(R.string.info_colors),
				style = MaterialTheme.typography.titleSmall
			)
			FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				colors.forEach { hex ->
					Box(
						modifier = Modifier
							.size(28.dp)
							.clip(CircleShape)
							.background(hex.toComposeColor())
					)
				}
			}
		}

		if (!wallpaper.url.isNullOrBlank()) {
			TextButton(onClick = { onOpenUrl(wallpaper.url) }) {
				Icon(
					imageVector = Icons.Default.OpenInNew,
					contentDescription = null,
					modifier = Modifier.size(18.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(stringResource(R.string.action_open_in_browser))
			}
		}

		Spacer(modifier = Modifier.height(8.dp))
	}
}

@Composable
private fun InfoRow(
	labelRes: Int,
	value: String?,
	onClick: (() -> Unit)? = null
) {
	if (value.isNullOrBlank()) {
		return
	}

	Row(modifier = Modifier.fillMaxWidth()) {
		Text(
			text = stringResource(labelRes),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.width(120.dp)
		)
		Text(
			text = value,
			style = MaterialTheme.typography.bodyMedium,
			modifier = Modifier
				.weight(1f)
				.then(
					if (onClick != null) {
						Modifier.clickable(onClick = onClick)
					} else {
						Modifier
					}
				)
		)
	}
}

/**
 * Immersive full-screen image viewer hosted by the detail screen: black background, free zoom up to
 * [FULLSCREEN_MAX_SCALE], single-finger panning, and exit on a single tap.
 */
@Composable
private fun FullScreenImage(url: String, onClose: () -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.Black)
	) {
		ZoomableImage(
			url = url,
			maxScale = FULLSCREEN_MAX_SCALE,
			singleFingerPan = true,
			fillParent = true,
			onSingleTap = onClose,
			modifier = Modifier.fillMaxSize()
		)
	}
}

private fun openUri(context: Context, uri: Uri) {
	runCatching {
		context.startActivity(
			Intent(Intent.ACTION_VIEW).apply {
				setDataAndType(uri, "image/*")
				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			}
		)
	}
}


