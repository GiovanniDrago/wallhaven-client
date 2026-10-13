package dev.giovannidrago.wallhaven.ui.albums

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.data.album.AlbumImageCache
import dev.giovannidrago.wallhaven.data.db.AlbumItemEntity
import dev.giovannidrago.wallhaven.ui.components.EmptyState
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
	onBack: () -> Unit,
	onWallpaperClick: (String) -> Unit,
	viewModel: AlbumDetailViewModel = hiltViewModel()
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val album by viewModel.album.collectAsStateWithLifecycle()
	val transfer by viewModel.transfer.collectAsStateWithLifecycle()
	val items = viewModel.items.collectAsLazyPagingItems()
	val snackbarHostState = remember { SnackbarHostState() }

	var showDeleteDialog by remember { mutableStateOf(false) }

	val exportSuccess = stringResource(R.string.album_export_success)
	val importSuccess = stringResource(R.string.album_import_success)
	val importError = stringResource(R.string.album_import_error)
	val defaultFileName = stringResource(R.string.album_export_file_name)

	val exportLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.CreateDocument("application/json")
	) { uri ->
		if (uri == null) {
			viewModel.consumeTransfer()
			return@rememberLauncherForActivityResult
		}
		scope.launch {
			val content = viewModel.buildExport()
			val ok = content != null && runCatching {
				withContext(Dispatchers.IO) {
					context.contentResolver.openOutputStream(uri)?.use { stream ->
						stream.write(content.toByteArray())
					} ?: error("no output stream")
				}
			}.isSuccess
			viewModel.onExportResult(ok)
		}
	}

	val importLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.OpenDocument()
	) { uri ->
		if (uri == null) {
			return@rememberLauncherForActivityResult
		}
		scope.launch {
			val content = runCatching {
				withContext(Dispatchers.IO) {
					context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
				}
			}.getOrNull()
			if (content.isNullOrBlank()) {
				viewModel.onExportResult(false)
			} else {
				viewModel.importDocument(content)
			}
		}
	}

	LaunchedEffect(transfer) {
		when (transfer) {
			TransferState.Exported -> {
				snackbarHostState.showSnackbar(exportSuccess)
				viewModel.consumeTransfer()
			}
			is TransferState.Imported -> {
				snackbarHostState.showSnackbar(importSuccess)
				viewModel.consumeTransfer()
			}
			TransferState.Failed -> {
				snackbarHostState.showSnackbar(importError)
				viewModel.consumeTransfer()
			}
			TransferState.Idle, TransferState.Working -> Unit
		}
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(album?.name ?: stringResource(R.string.albums_title)) },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = stringResource(R.string.cd_back)
						)
					}
				},
				actions = {
					IconButton(
						onClick = { exportLauncher.launch(defaultFileName) },
						enabled = transfer !is TransferState.Working
					) {
						Icon(
							imageVector = Icons.Outlined.FileUpload,
							contentDescription = stringResource(R.string.album_export)
						)
					}
					IconButton(
						onClick = { importLauncher.launch(arrayOf("application/json")) },
						enabled = transfer !is TransferState.Working
					) {
						Icon(
							imageVector = Icons.Outlined.FileDownload,
							contentDescription = stringResource(R.string.album_import)
						)
					}
					IconButton(onClick = { showDeleteDialog = true }) {
						Icon(
							imageVector = Icons.Outlined.Delete,
							contentDescription = stringResource(R.string.cd_album_delete)
						)
					}
				}
			)
		},
		snackbarHost = { SnackbarHost(snackbarHostState) }
	) { padding ->
		val refresh = items.loadState.refresh
		Box(modifier = Modifier.padding(padding)) {
			when {
				items.itemCount == 0 && refresh is LoadState.Loading -> {
					Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
						CircularProgressIndicator()
					}
				}
				items.itemCount == 0 -> {
					EmptyState(
						text = stringResource(R.string.album_detail_empty),
						modifier = Modifier.fillMaxSize()
					)
				}
				else -> {
					LazyVerticalGrid(
						columns = GridCells.Adaptive(minSize = 132.dp),
						modifier = Modifier.fillMaxSize(),
						contentPadding = PaddingValues(12.dp),
						verticalArrangement = Arrangement.spacedBy(12.dp),
						horizontalArrangement = Arrangement.spacedBy(12.dp)
					) {
						items(
							count = items.itemCount,
							key = { index -> items.peek(index)?.wallpaper?.id ?: index }
						) { index ->
							val item = items[index]
							if (item != null) {
								AlbumItemCard(
									item = item,
									onClick = { onWallpaperClick(item.wallpaper.id) },
									onRemove = { viewModel.removeWallpaper(item.wallpaper.id) }
								)
							}
						}
					}
				}
			}
		}
	}

	if (showDeleteDialog) {
		AlertDialog(
			onDismissRequest = { showDeleteDialog = false },
			title = { Text(stringResource(R.string.album_delete)) },
			text = { Text(stringResource(R.string.album_delete_confirm)) },
			confirmButton = {
				TextButton(
					onClick = {
						showDeleteDialog = false
						viewModel.deleteAlbum(onDeleted = onBack)
					}
				) {
					Text(stringResource(R.string.album_delete_action))
				}
			},
			dismissButton = {
				TextButton(onClick = { showDeleteDialog = false }) {
					Text(stringResource(R.string.album_cancel))
				}
			}
		)
	}
}

@Composable
private fun AlbumItemCard(
	item: AlbumItemEntity,
	onClick: () -> Unit,
	onRemove: () -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.aspectRatio(item.wallpaper.aspectRatio)
			.clip(RoundedCornerShape(14.dp))
			.background(MaterialTheme.colorScheme.surfaceVariant)
	) {
		val model: Any? = when {
			item.cacheStatus == AlbumImageCache.STATUS_CACHED && !item.localPath.isNullOrBlank() ->
				File(item.localPath)
			else -> item.wallpaper.thumbLarge ?: item.wallpaper.thumbOriginal ?: item.wallpaper.path
		}

		val hasModel = (model is File) || (!(model as? String).isNullOrBlank())
		if (hasModel) {
			AsyncImage(
				model = ImageRequest.Builder(LocalContext.current)
					.data(model)
					.crossfade(true)
					.build(),
				contentDescription = stringResource(R.string.cd_thumbnail),
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.fillMaxSize()
					.clickable(onClick = onClick)
			)
		} else {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.clickable(onClick = onClick),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center
			) {
				Icon(
					imageVector = Icons.Outlined.ImageNotSupported,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
				Text(
					text = stringResource(R.string.album_cache_pending),
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}

		if (item.cacheStatus == AlbumImageCache.STATUS_FAILED) {
			Text(
				text = stringResource(R.string.album_cache_failed),
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.error,
				modifier = Modifier
					.align(Alignment.BottomStart)
					.padding(6.dp)
			)
		}

		IconButton(
			onClick = onRemove,
			modifier = Modifier
				.align(Alignment.TopEnd)
				.padding(2.dp)
				.size(32.dp)
				.background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f), CircleShape)
		) {
			Icon(
				imageVector = Icons.Outlined.Delete,
				contentDescription = stringResource(R.string.album_remove),
				tint = MaterialTheme.colorScheme.onPrimary,
				modifier = Modifier.size(18.dp)
			)
		}
	}
}
