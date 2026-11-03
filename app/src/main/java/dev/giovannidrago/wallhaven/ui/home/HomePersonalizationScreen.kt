package dev.giovannidrago.wallhaven.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.domain.ATLEAST_RESOLUTIONS
import dev.giovannidrago.wallhaven.domain.CategoryFilter
import dev.giovannidrago.wallhaven.domain.EXACT_RATIOS
import dev.giovannidrago.wallhaven.domain.EXACT_RESOLUTIONS
import dev.giovannidrago.wallhaven.domain.RatioFormat
import dev.giovannidrago.wallhaven.domain.Sorting
import dev.giovannidrago.wallhaven.domain.WALLHAVEN_COLORS
import dev.giovannidrago.wallhaven.ui.components.toComposeColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomePersonalizationScreen(
	onOpenDrawer: () -> Unit,
	viewModel: HomePersonalizationViewModel = hiltViewModel()
) {
	val options by viewModel.options.collectAsStateWithLifecycle()
	val apiKeyPresent by viewModel.apiKeyPresent.collectAsStateWithLifecycle()

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.home_personalization_title)) },
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
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
				.verticalScroll(rememberScrollState())
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Text(
				text = stringResource(R.string.home_personalization_description),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)

			FilterSection(title = stringResource(R.string.search_sorting)) {
				Sorting.entries.forEach { sorting ->
					FilterChip(
						selected = options.sorting == sorting,
						onClick = { viewModel.setSorting(sorting) },
						label = { Text(stringResource(sorting.labelRes())) }
					)
				}
			}

			FilterSection(title = stringResource(R.string.search_categories)) {
				CategoryFilter.entries.forEach { category ->
					val enabled = when (category) {
						CategoryFilter.GENERAL -> options.general
						CategoryFilter.ANIME -> options.anime
						CategoryFilter.PEOPLE -> options.people
					}
					FilterChip(
						selected = enabled,
						onClick = { viewModel.setCategory(category, !enabled) },
						label = { Text(stringResource(category.labelRes())) }
					)
				}
			}

			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Text(
					text = stringResource(R.string.search_purity),
					style = MaterialTheme.typography.titleSmall
				)
				if (!apiKeyPresent) {
					Text(
						text = stringResource(R.string.search_nsfw_requires_key),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
				FlowRow(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					FilterChip(
						selected = options.sfw,
						onClick = { viewModel.setSfw(!options.sfw) },
						label = { Text(stringResource(R.string.purity_sfw)) }
					)
					FilterChip(
						selected = options.sketchy,
						onClick = { viewModel.setSketchy(!options.sketchy) },
						label = { Text(stringResource(R.string.purity_sketchy)) }
					)
					FilterChip(
						selected = options.nsfw,
						onClick = { viewModel.setNsfw(!options.nsfw) },
						enabled = apiKeyPresent,
						label = { Text(stringResource(R.string.purity_nsfw)) }
					)
				}
			}

			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Text(
					text = stringResource(R.string.search_resolution),
					style = MaterialTheme.typography.titleSmall
				)
				Text(
					text = stringResource(R.string.search_resolution_atleast),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				FlowRow(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					FilterChip(
						selected = options.atleast == null,
						onClick = { viewModel.setAtleast(null) },
						label = { Text(stringResource(R.string.search_resolution_none)) }
					)
					ATLEAST_RESOLUTIONS.forEach { resolution ->
						FilterChip(
							selected = options.atleast == resolution,
							onClick = {
								viewModel.setAtleast(if (options.atleast == resolution) null else resolution)
							},
							label = { Text(resolution) }
						)
					}
				}
				Text(
					text = stringResource(R.string.search_resolution_exact),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				FlowRow(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					EXACT_RESOLUTIONS.forEach { resolution ->
						FilterChip(
							selected = resolution in options.resolutions,
							onClick = { viewModel.toggleResolution(resolution) },
							label = { Text(resolution) }
						)
					}
				}
			}

			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Text(
					text = stringResource(R.string.search_ratio),
					style = MaterialTheme.typography.titleSmall
				)
				FlowRow(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					FilterChip(
						selected = options.ratios.isEmpty(),
						onClick = { viewModel.setRatioPreset(null) },
						label = { Text(stringResource(R.string.search_ratio_none)) }
					)
					RatioFormat.entries.forEach { format ->
						FilterChip(
							selected = format.matches(options.ratios),
							onClick = { viewModel.setRatioPreset(format) },
							label = { Text(stringResource(format.labelRes())) }
						)
					}
				}
				Text(
					text = stringResource(R.string.search_ratio_exact),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				FlowRow(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					EXACT_RATIOS.forEach { ratio ->
						FilterChip(
							selected = options.ratios == listOf(ratio),
							onClick = { viewModel.setRatio(ratio) },
							label = { Text(ratio) }
						)
					}
				}
			}

			FilterSection(title = stringResource(R.string.search_color)) {
				FilterChip(
					selected = options.color == null,
					onClick = { viewModel.setColor(null) },
					label = { Text(stringResource(R.string.search_color_none)) }
				)
				WALLHAVEN_COLORS.forEach { hex ->
					val selected = options.color == hex
					Box(
						modifier = Modifier
							.size(34.dp)
							.clip(CircleShape)
							.background("#$hex".toComposeColor())
							.border(
								width = if (selected) 3.dp else 1.dp,
								color = if (selected) {
									MaterialTheme.colorScheme.primary
								} else {
									MaterialTheme.colorScheme.outlineVariant
								},
								shape = CircleShape
							)
							.clickable { viewModel.setColor(if (selected) null else hex) }
					)
				}
			}

			TextButton(
				onClick = viewModel::reset,
				modifier = Modifier.fillMaxWidth()
			) {
				Text(stringResource(R.string.home_personalization_reset))
			}
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(
	title: String,
	content: @Composable () -> Unit
) {
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleSmall
		)
		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalArrangement = Arrangement.spacedBy(4.dp)
		) {
			content()
		}
	}
}

private fun Sorting.labelRes(): Int = when (this) {
	Sorting.LATEST -> R.string.drawer_sorting_latest
	Sorting.TOPLIST_MONTH -> R.string.drawer_sorting_toplist
	Sorting.RANDOM -> R.string.drawer_sorting_random
}

private fun CategoryFilter.labelRes(): Int = when (this) {
	CategoryFilter.GENERAL -> R.string.drawer_category_general
	CategoryFilter.ANIME -> R.string.drawer_category_anime
	CategoryFilter.PEOPLE -> R.string.drawer_category_people
}

private fun RatioFormat.labelRes(): Int = when (this) {
	RatioFormat.LANDSCAPE -> R.string.search_ratio_landscape
	RatioFormat.PORTRAIT -> R.string.search_ratio_portrait
	RatioFormat.SQUARE -> R.string.search_ratio_square
}
