package dev.giovannidrago.wallhaven.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.domain.CategoryFilter
import dev.giovannidrago.wallhaven.domain.SearchOptions
import dev.giovannidrago.wallhaven.domain.SearchSorting
import dev.giovannidrago.wallhaven.domain.WALLHAVEN_COLORS
import dev.giovannidrago.wallhaven.ui.components.toComposeColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchFilterSheet(
	options: SearchOptions,
	apiKeyPresent: Boolean,
	onDismiss: () -> Unit,
	onSortingChange: (SearchSorting) -> Unit,
	onCategoryChange: (CategoryFilter, Boolean) -> Unit,
	onSketchyChange: (Boolean) -> Unit,
	onNsfwChange: (Boolean) -> Unit,
	onColorChange: (String?) -> Unit,
	onReset: () -> Unit
) {
	ModalBottomSheet(onDismissRequest = onDismiss) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp)
				.padding(bottom = 32.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = stringResource(R.string.search_filters),
					style = MaterialTheme.typography.titleMedium,
					modifier = Modifier.weight(1f)
				)
				TextButton(onClick = onReset) {
					Text(stringResource(R.string.search_reset))
				}
			}

			FilterSection(title = stringResource(R.string.search_sorting)) {
				SearchSorting.entries.forEach { sorting ->
					FilterChip(
						selected = options.sorting == sorting,
						onClick = { onSortingChange(sorting) },
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
						onClick = { onCategoryChange(category, !enabled) },
						label = { Text(stringResource(category.labelRes())) }
					)
				}
			}

			FilterSection(
				title = stringResource(R.string.search_purity),
				subtitle = if (apiKeyPresent) {
					null
				} else {
					stringResource(R.string.search_nsfw_requires_key)
				}
			) {
				FilterChip(
					selected = true,
					onClick = {},
					enabled = false,
					label = { Text(stringResource(R.string.purity_sfw)) }
				)
				FilterChip(
					selected = options.sketchy,
					onClick = { onSketchyChange(!options.sketchy) },
					label = { Text(stringResource(R.string.purity_sketchy)) }
				)
				FilterChip(
					selected = options.nsfw,
					onClick = { onNsfwChange(!options.nsfw) },
					enabled = apiKeyPresent,
					label = { Text(stringResource(R.string.purity_nsfw)) }
				)
			}

			FilterSection(title = stringResource(R.string.search_color)) {
				FilterChip(
					selected = options.color == null,
					onClick = { onColorChange(null) },
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
								width = if (selected) {
									3.dp
								} else {
									1.dp
								},
								color = if (selected) {
									MaterialTheme.colorScheme.primary
								} else {
									MaterialTheme.colorScheme.outlineVariant
								},
								shape = CircleShape
							)
							.clickable { onColorChange(if (selected) null else hex) }
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(
	title: String,
	subtitle: String? = null,
	content: @Composable () -> Unit
) {
	Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleSmall
		)
		if (subtitle != null) {
			Text(
				text = subtitle,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalArrangement = Arrangement.spacedBy(4.dp)
		) {
			content()
		}
	}
}

private fun SearchSorting.labelRes(): Int = when (this) {
	SearchSorting.RELEVANCE -> R.string.search_sorting_relevance
	SearchSorting.LATEST -> R.string.search_sorting_latest
	SearchSorting.VIEWS -> R.string.search_sorting_views
	SearchSorting.FAVORITES -> R.string.search_sorting_favorites
	SearchSorting.TOPLIST_MONTH -> R.string.search_sorting_toplist
	SearchSorting.RANDOM -> R.string.search_sorting_random
}

private fun CategoryFilter.labelRes(): Int = when (this) {
	CategoryFilter.GENERAL -> R.string.drawer_category_general
	CategoryFilter.ANIME -> R.string.drawer_category_anime
	CategoryFilter.PEOPLE -> R.string.drawer_category_people
}
