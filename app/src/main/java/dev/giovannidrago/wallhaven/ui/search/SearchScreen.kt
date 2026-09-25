package dev.giovannidrago.wallhaven.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.ui.components.WallpaperGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
	onOpenDrawer: () -> Unit,
	onWallpaperClick: (String) -> Unit,
	viewModel: SearchViewModel = hiltViewModel()
) {
	val query by viewModel.query.collectAsStateWithLifecycle()
	val options by viewModel.options.collectAsStateWithLifecycle()
	val apiKeyPresent by viewModel.apiKeyPresent.collectAsStateWithLifecycle()
	val items = viewModel.results.collectAsLazyPagingItems()
	var showFilters by rememberSaveable { mutableStateOf(false) }

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.search_title)) },
				navigationIcon = {
					IconButton(onClick = onOpenDrawer) {
						Icon(
							imageVector = Icons.Default.Menu,
							contentDescription = stringResource(R.string.cd_open_menu)
						)
					}
				},
				actions = {
					IconButton(onClick = { showFilters = true }) {
						BadgedBox(
							badge = {
								if (options.activeFilterCount > 0) {
									Badge {
										Text(options.activeFilterCount.toString())
									}
								}
							}
						) {
							Icon(
								imageVector = Icons.Default.FilterList,
								contentDescription = stringResource(R.string.cd_search_filters)
							)
						}
					}
				}
			)
		}
	) { padding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
		) {
			OutlinedTextField(
				value = query,
				onValueChange = viewModel::onQueryChange,
				placeholder = { Text(stringResource(R.string.search_hint)) },
				leadingIcon = {
					Icon(
						imageVector = Icons.Outlined.Search,
						contentDescription = null
					)
				},
				trailingIcon = {
					if (query.isNotEmpty()) {
						IconButton(onClick = { viewModel.onQueryChange("") }) {
							Icon(
								imageVector = Icons.Default.Close,
								contentDescription = null
							)
						}
					}
				},
				singleLine = true,
				keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 8.dp)
			)
			WallpaperGrid(
				items = items,
				emptyText = stringResource(
					if (query.isBlank()) {
						R.string.empty_search
					} else {
						R.string.empty_search_no_results
					}
				),
				onWallpaperClick = onWallpaperClick,
				onToggleFavorite = viewModel::toggleFavorite,
				modifier = Modifier.weight(1f)
			)
		}
	}

	if (showFilters) {
		SearchFilterSheet(
			options = options,
			apiKeyPresent = apiKeyPresent,
			onDismiss = { showFilters = false },
			onSortingChange = viewModel::setSorting,
			onCategoryChange = viewModel::setCategory,
			onSketchyChange = viewModel::setSketchy,
			onNsfwChange = viewModel::setNsfw,
			onColorChange = viewModel::setColor,
			onReset = viewModel::resetFilters
		)
	}
}
