package dev.giovannidrago.wallhaven.ui.favorites

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.ui.components.WallpaperGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
	onOpenDrawer: () -> Unit,
	onWallpaperClick: (String) -> Unit,
	viewModel: FavoritesViewModel = hiltViewModel()
) {
	val items = viewModel.favorites.collectAsLazyPagingItems()

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.favorites_title)) },
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
		WallpaperGrid(
			items = items,
			emptyText = stringResource(R.string.empty_favorites),
			onWallpaperClick = onWallpaperClick,
			onToggleFavorite = viewModel::toggleFavorite,
			modifier = Modifier.padding(padding)
		)
	}
}
