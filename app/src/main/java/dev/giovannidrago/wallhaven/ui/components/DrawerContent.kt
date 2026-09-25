package dev.giovannidrago.wallhaven.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.domain.CategoryFilter
import dev.giovannidrago.wallhaven.domain.WallhavenFilters
import dev.giovannidrago.wallhaven.ui.navigation.AppSection

@Composable
fun DrawerContent(
	filters: WallhavenFilters,
	apiKeyPresent: Boolean,
	selectedSection: AppSection,
	onNavigate: (AppSection) -> Unit,
	onNsfwChange: (Boolean) -> Unit,
	onSketchyChange: (Boolean) -> Unit,
	onCategoryChange: (CategoryFilter, Boolean) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.verticalScroll(rememberScrollState())
	) {
		Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp)) {
			Text(
				text = stringResource(R.string.app_name),
				style = MaterialTheme.typography.titleLarge
			)
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = stringResource(
					if (apiKeyPresent) {
						R.string.drawer_api_key_configured
					} else {
						R.string.drawer_api_key_missing
					}
				),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}

		AppSection.entries.forEach { section ->
			NavigationDrawerItem(
				label = { Text(stringResource(section.labelRes())) },
				selected = selectedSection == section,
				onClick = { onNavigate(section) },
				icon = { Icon(section.icon(), contentDescription = null) },
				modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
			)
		}

		HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
		Text(
			text = stringResource(R.string.drawer_filters),
			style = MaterialTheme.typography.labelLarge,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
		)

		FilterToggle(
			label = stringResource(R.string.drawer_nsfw),
			subtitle = if (apiKeyPresent) {
				null
			} else {
				stringResource(R.string.drawer_nsfw_requires_key)
			},
			checked = filters.nsfw,
			enabled = apiKeyPresent,
			onCheckedChange = onNsfwChange,
			onClick = if (apiKeyPresent) {
				null
			} else {
				{ onNavigate(AppSection.SETTINGS) }
			}
		)
		FilterToggle(
			label = stringResource(R.string.drawer_sketchy),
			checked = filters.sketchy,
			onCheckedChange = onSketchyChange
		)
		FilterToggle(
			label = stringResource(R.string.drawer_category_people),
			checked = filters.people,
			onCheckedChange = { onCategoryChange(CategoryFilter.PEOPLE, it) }
		)
		FilterToggle(
			label = stringResource(R.string.drawer_category_anime),
			checked = filters.anime,
			onCheckedChange = { onCategoryChange(CategoryFilter.ANIME, it) }
		)
		FilterToggle(
			label = stringResource(R.string.drawer_category_general),
			checked = filters.general,
			onCheckedChange = { onCategoryChange(CategoryFilter.GENERAL, it) }
		)

		Spacer(modifier = Modifier.height(16.dp))
	}
}

@Composable
private fun FilterToggle(
	label: String,
	subtitle: String? = null,
	checked: Boolean,
	enabled: Boolean = true,
	onCheckedChange: (Boolean) -> Unit,
	onClick: (() -> Unit)? = null
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.then(
				if (onClick != null) {
					Modifier.clickable(onClick = onClick)
				} else {
					Modifier
				}
			)
			.padding(horizontal = 28.dp, vertical = 6.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = label,
				style = MaterialTheme.typography.bodyLarge
			)
			if (subtitle != null) {
				Text(
					text = subtitle,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
		Spacer(modifier = Modifier.width(12.dp))
		Switch(
			checked = checked,
			onCheckedChange = onCheckedChange,
			enabled = enabled
		)
	}
}

private fun AppSection.labelRes(): Int = when (this) {
	AppSection.GALLERY -> R.string.drawer_gallery
	AppSection.SEARCH -> R.string.drawer_search
	AppSection.FAVORITES -> R.string.drawer_favorites
	AppSection.SETTINGS -> R.string.drawer_settings
}

private fun AppSection.icon(): ImageVector = when (this) {
	AppSection.GALLERY -> Icons.Outlined.PhotoLibrary
	AppSection.SEARCH -> Icons.Outlined.Search
	AppSection.FAVORITES -> Icons.Outlined.FavoriteBorder
	AppSection.SETTINGS -> Icons.Outlined.Settings
}
