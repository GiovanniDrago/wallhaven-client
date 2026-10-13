package dev.giovannidrago.wallhaven.ui.albums

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.domain.AlbumSummary
import dev.giovannidrago.wallhaven.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
	onOpenDrawer: () -> Unit,
	onOpenAlbum: (String) -> Unit,
	viewModel: AlbumsViewModel = hiltViewModel()
) {
	val albums by viewModel.albums.collectAsStateWithLifecycle()
	val event by viewModel.event.collectAsStateWithLifecycle()

	var showCreateDialog by remember { mutableStateOf(false) }
	var pendingDelete by remember { mutableStateOf<AlbumSummary?>(null) }

	LaunchedEffect(event) {
		when (event) {
			AlbumsEvent.Created -> {
				showCreateDialog = false
				viewModel.consumeEvent()
			}
			AlbumsEvent.BlankName, AlbumsEvent.Deleted -> viewModel.consumeEvent()
			null -> Unit
		}
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.albums_title)) },
				navigationIcon = {
					IconButton(onClick = onOpenDrawer) {
						Icon(
							imageVector = Icons.Default.Menu,
							contentDescription = stringResource(R.string.cd_open_menu)
						)
					}
				},
				actions = {
					IconButton(onClick = { showCreateDialog = true }) {
						Icon(
							imageVector = Icons.Default.Add,
							contentDescription = stringResource(R.string.album_new)
						)
					}
				}
			)
		}
	) { padding ->
		if (albums.isEmpty()) {
			EmptyState(
				text = stringResource(R.string.album_empty),
				modifier = Modifier.padding(padding)
			)
		} else {
			LazyColumn(
				modifier = Modifier
					.fillMaxSize()
					.padding(padding),
				contentPadding = PaddingValues(12.dp),
				verticalArrangement = Arrangement.spacedBy(12.dp)
			) {
				items(albums, key = { it.id }) { album ->
					AlbumRow(
						album = album,
						onClick = { onOpenAlbum(album.id) },
						onDelete = { pendingDelete = album }
					)
				}
			}
		}
	}

	if (showCreateDialog) {
		CreateAlbumDialog(
			onDismiss = { showCreateDialog = false },
			onConfirm = { name -> viewModel.createAlbum(name) }
		)
	}

	pendingDelete?.let { album ->
		AlertDialog(
			onDismissRequest = { pendingDelete = null },
			title = { Text(stringResource(R.string.album_delete)) },
			text = { Text(stringResource(R.string.album_delete_confirm)) },
			confirmButton = {
				TextButton(
					onClick = {
						viewModel.deleteAlbum(album.id)
						pendingDelete = null
					}
				) {
					Text(stringResource(R.string.album_delete_action))
				}
			},
			dismissButton = {
				TextButton(onClick = { pendingDelete = null }) {
					Text(stringResource(R.string.album_cancel))
				}
			}
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumRow(
	album: AlbumSummary,
	onClick: () -> Unit,
	onDelete: () -> Unit
) {
	Card(
		onClick = onClick,
		shape = RoundedCornerShape(14.dp),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
		modifier = Modifier.fillMaxWidth()
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			AlbumPreview(album.previewThumbUrl)
			Column(
				modifier = Modifier
					.weight(1f)
					.padding(horizontal = 12.dp)
			) {
				Text(
					text = album.name,
					style = MaterialTheme.typography.titleMedium,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
				Text(
					text = stringResource(R.string.album_item_count, album.itemCount),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			IconButton(onClick = onDelete) {
				Icon(
					imageVector = Icons.Outlined.Delete,
					contentDescription = stringResource(R.string.cd_album_delete)
				)
			}
		}
	}
}

@Composable
private fun AlbumPreview(url: String?) {
	Box(
		modifier = Modifier
			.size(56.dp)
			.clip(RoundedCornerShape(10.dp))
			.background(MaterialTheme.colorScheme.surface),
		contentAlignment = Alignment.Center
	) {
		if (url.isNullOrBlank()) {
			Icon(
				imageVector = Icons.Outlined.CollectionsBookmark,
				contentDescription = stringResource(R.string.cd_album_preview),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
		} else {
			AsyncImage(
				model = ImageRequest.Builder(LocalContext.current)
					.data(url)
					.crossfade(true)
					.build(),
				contentDescription = stringResource(R.string.cd_album_preview),
				contentScale = ContentScale.Crop,
				modifier = Modifier.fillMaxSize()
			)
		}
	}
}

@Composable
private fun CreateAlbumDialog(
	onDismiss: () -> Unit,
	onConfirm: (String) -> Unit
) {
	var name by remember { mutableStateOf("") }
	var showError by remember { mutableStateOf(false) }
	val blankError = stringResource(R.string.album_name_hint)

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(stringResource(R.string.album_new)) },
		text = {
			OutlinedTextField(
				value = name,
				onValueChange = {
					name = it
					showError = false
				},
				label = { Text(stringResource(R.string.album_name_hint)) },
				isError = showError,
				singleLine = true
			)
			if (showError) {
				Text(
					text = blankError,
					color = MaterialTheme.colorScheme.error,
					style = MaterialTheme.typography.bodySmall,
					modifier = Modifier.padding(top = 4.dp)
				)
			}
		},
		confirmButton = {
			TextButton(
				onClick = {
					if (name.isBlank()) {
						showError = true
					} else {
						onConfirm(name)
					}
				}
			) {
				Text(stringResource(R.string.album_create))
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(R.string.album_cancel))
			}
		}
	)
}
