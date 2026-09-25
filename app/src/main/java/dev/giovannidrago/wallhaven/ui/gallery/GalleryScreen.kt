package dev.giovannidrago.wallhaven.ui.gallery

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.ui.components.WallpaperGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
	onOpenDrawer: () -> Unit,
	onWallpaperClick: (String) -> Unit,
	viewModel: GalleryViewModel = hiltViewModel()
) {
	val items = viewModel.gallery.collectAsLazyPagingItems()

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.gallery_title)) },
				navigationIcon = {
					IconButton(onClick = onOpenDrawer) {
						Icon(
							imageVector = Icons.Default.Menu,
							contentDescription = stringResource(R.string.cd_open_menu)
						)
					}
				}
			)
		}
	) { padding ->
		PullToRefreshBox(
			isRefreshing = items.loadState.refresh is LoadState.Loading,
			onRefresh = { items.refresh() },
			modifier = Modifier.padding(padding)
		) {
			WallpaperGrid(
				items = items,
				emptyText = stringResource(R.string.empty_gallery),
				onWallpaperClick = onWallpaperClick,
				onToggleFavorite = viewModel::toggleFavorite
			)
		}
	}
}
