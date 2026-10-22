package dev.giovannidrago.wallhaven.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.giovannidrago.wallhaven.R

/** Shared "update available" dialog used by the manual and the automatic checks. */
@Composable
fun UpdateAvailableDialog(
	version: String,
	onDismiss: () -> Unit,
	onDownload: () -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(stringResource(R.string.settings_update_available_title, version)) },
		confirmButton = {
			TextButton(onClick = onDownload) {
				Text(stringResource(R.string.settings_update_download))
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(R.string.settings_update_later))
			}
		}
	)
}
