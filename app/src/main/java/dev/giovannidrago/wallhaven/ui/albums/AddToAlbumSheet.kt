package dev.giovannidrago.wallhaven.ui.albums

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.domain.AlbumSummary

/**
 * Lets the user add the current wallpaper to an existing album or to a new one created on the spot.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToAlbumSheet(
	albums: List<AlbumSummary>,
	onAddExisting: (String) -> Unit,
	onCreateAndAdd: (String) -> Unit,
	onDismiss: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState()
	var creating by remember { mutableStateOf(false) }
	var name by remember { mutableStateOf("") }

	ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
		Text(
			text = stringResource(R.string.album_add_to_title),
			style = MaterialTheme.typography.titleMedium,
			modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
		)

		if (creating) {
			Column(modifier = Modifier.padding(horizontal = 24.dp)) {
				OutlinedTextField(
					value = name,
					onValueChange = { name = it },
					label = { Text(stringResource(R.string.album_name_hint)) },
					singleLine = true,
					modifier = Modifier.fillMaxWidth()
				)
				Button(
					onClick = { onCreateAndAdd(name) },
					enabled = name.isNotBlank(),
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = 12.dp)
				) {
					Text(stringResource(R.string.album_create))
				}
				TextButton(
					onClick = { creating = false },
					modifier = Modifier.fillMaxWidth()
				) {
					Text(stringResource(R.string.album_cancel))
				}
			}
		} else {
			LazyColumn(modifier = Modifier.fillMaxWidth()) {
				items(albums, key = { it.id }) { album ->
					ListItem(
						headlineContent = { Text(album.name) },
						supportingContent = {
							Text(stringResource(R.string.album_item_count, album.itemCount))
						},
						leadingContent = {
							Icon(
								imageVector = Icons.Outlined.CollectionsBookmark,
								contentDescription = null
							)
						},
						modifier = Modifier.clickable { onAddExisting(album.id) }
					)
				}
				item {
					HorizontalDivider()
					ListItem(
						headlineContent = { Text(stringResource(R.string.album_add_new)) },
						leadingContent = {
							Icon(imageVector = Icons.Filled.Add, contentDescription = null)
						},
						modifier = Modifier.clickable { creating = true }
					)
				}
			}
		}
	}
}
