package dev.giovannidrago.wallhaven.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.giovannidrago.wallhaven.R

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
	Box(
		modifier = modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = text,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(32.dp)
		)
	}
}

@Composable
fun ErrorState(error: Throwable?, onRetry: () -> Unit, modifier: Modifier = Modifier) {
	Column(
		modifier = modifier
			.fillMaxSize()
			.padding(32.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Icon(
			imageVector = Icons.Outlined.CloudOff,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.size(48.dp)
		)
		Text(
			text = errorMessage(error),
			style = MaterialTheme.typography.bodyLarge,
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
		)
		Button(onClick = onRetry) {
			Text(stringResource(R.string.action_retry))
		}
	}
}

@Composable
fun AppendError(error: Throwable?, onRetry: () -> Unit) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 8.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Center
	) {
		Text(
			text = errorMessage(error),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
		TextButton(onClick = onRetry) {
			Text(stringResource(R.string.action_retry))
		}
	}
}
