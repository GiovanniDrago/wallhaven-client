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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.domain.ATLEAST_RESOLUTIONS
import dev.giovannidrago.wallhaven.domain.CategoryFilter
import dev.giovannidrago.wallhaven.domain.EXACT_RATIOS
import dev.giovannidrago.wallhaven.domain.EXACT_RESOLUTIONS
import dev.giovannidrago.wallhaven.domain.RatioFormat
import dev.giovannidrago.wallhaven.domain.SearchOptions
import dev.giovannidrago.wallhaven.domain.SearchSorting
import dev.giovannidrago.wallhaven.domain.WALLHAVEN_COLORS
import dev.giovannidrago.wallhaven.ui.components.openUrl
import dev.giovannidrago.wallhaven.ui.components.toComposeColor

private const val ACCOUNT_SETTINGS_URL = "https://wallhaven.cc/settings/browsing"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchFilterSheet(
	options: SearchOptions,
	apiKeyPresent: Boolean,
	accountPurity: Set<String>,
	queryIsBlank: Boolean,
	onDismiss: () -> Unit,
	onSortingChange: (SearchSorting) -> Unit,
	onCategoryChange: (CategoryFilter, Boolean) -> Unit,
	onSfwChange: (Boolean) -> Unit,
	onSketchyChange: (Boolean) -> Unit,
	onNsfwChange: (Boolean) -> Unit,
	onColorChange: (String?) -> Unit,
	onAtleastChange: (String?) -> Unit,
	onResolutionToggle: (String) -> Unit,
	onRatioPresetChange: (RatioFormat?) -> Unit,
	onRatioChange: (String) -> Unit,
	onReset: () -> Unit
) {
	val context = LocalContext.current
	val missingPurity = options.purityValues.filter { it !in accountPurity }
	val hasAccountLimits = accountPurity.isNotEmpty() && missingPurity.isNotEmpty()

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

			FilterSection(
				title = stringResource(R.string.search_sorting),
				subtitle = if (queryIsBlank && options.sorting == SearchSorting.RELEVANCE) {
					stringResource(R.string.search_relevance_requires_text)
				} else {
					null
				}
			) {
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
						onClick = { onSfwChange(!options.sfw) },
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
				if (hasAccountLimits) {
					Surface(
						shape = RoundedCornerShape(12.dp),
						color = MaterialTheme.colorScheme.errorContainer,
						contentColor = MaterialTheme.colorScheme.onErrorContainer,
						modifier = Modifier.fillMaxWidth()
					) {
						Column(modifier = Modifier.padding(12.dp)) {
							val labels = missingPurity.map { stringResource(purityLabelRes(it)) }
							Text(
								text = stringResource(R.string.search_account_missing_purity, labels.joinToString(", ")),
								style = MaterialTheme.typography.bodySmall
							)
							TextButton(onClick = { openUrl(context, ACCOUNT_SETTINGS_URL) }) {
								Text(stringResource(R.string.search_open_account_settings))
							}
						}
					}
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
						onClick = { onAtleastChange(null) },
						label = { Text(stringResource(R.string.search_resolution_none)) }
					)
					ATLEAST_RESOLUTIONS.forEach { resolution ->
						FilterChip(
							selected = options.atleast == resolution,
							onClick = { onAtleastChange(if (options.atleast == resolution) null else resolution) },
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
							onClick = { onResolutionToggle(resolution) },
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
						onClick = { onRatioPresetChange(null) },
						label = { Text(stringResource(R.string.search_ratio_none)) }
					)
					RatioFormat.entries.forEach { format ->
						FilterChip(
							selected = format.matches(options.ratios),
							onClick = { onRatioPresetChange(format) },
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
							onClick = { onRatioChange(ratio) },
							label = { Text(ratio) }
						)
					}
				}
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

internal fun SearchSorting.labelRes(): Int = when (this) {
	SearchSorting.RELEVANCE -> R.string.search_sorting_relevance
	SearchSorting.LATEST -> R.string.search_sorting_latest
	SearchSorting.VIEWS -> R.string.search_sorting_views
	SearchSorting.FAVORITES -> R.string.search_sorting_favorites
	SearchSorting.TOPLIST_MONTH -> R.string.search_sorting_toplist
	SearchSorting.RANDOM -> R.string.search_sorting_random
}

internal fun CategoryFilter.labelRes(): Int = when (this) {
	CategoryFilter.GENERAL -> R.string.drawer_category_general
	CategoryFilter.ANIME -> R.string.drawer_category_anime
	CategoryFilter.PEOPLE -> R.string.drawer_category_people
}

internal fun purityLabelRes(purity: String): Int = when (purity) {
	"sketchy" -> R.string.purity_sketchy
	"nsfw" -> R.string.purity_nsfw
	else -> R.string.purity_sfw
}

internal fun RatioFormat.labelRes(): Int = when (this) {
	RatioFormat.LANDSCAPE -> R.string.search_ratio_landscape
	RatioFormat.PORTRAIT -> R.string.search_ratio_portrait
	RatioFormat.SQUARE -> R.string.search_ratio_square
}
