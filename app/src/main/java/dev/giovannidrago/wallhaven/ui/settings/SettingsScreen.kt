package dev.giovannidrago.wallhaven.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.giovannidrago.wallhaven.BuildConfig
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.domain.Sorting
import dev.giovannidrago.wallhaven.ui.search.purityLabelRes
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	onOpenDrawer: () -> Unit,
	viewModel: SettingsViewModel = hiltViewModel()
) {
	val apiKeyPresent by viewModel.apiKeyPresent.collectAsStateWithLifecycle()
	val filters by viewModel.filters.collectAsStateWithLifecycle()
	val saving by viewModel.saving.collectAsStateWithLifecycle()
	val message by viewModel.message.collectAsStateWithLifecycle()
	val accountPurity by viewModel.accountPurity.collectAsStateWithLifecycle()
	val accountBlacklistCount by viewModel.accountBlacklistCount.collectAsStateWithLifecycle()
	val apiKeyInvalid by viewModel.apiKeyInvalid.collectAsStateWithLifecycle()
	val lastRequest by viewModel.lastRequest.collectAsStateWithLifecycle()
	val lastResponse by viewModel.lastResponse.collectAsStateWithLifecycle()
	val snackbarHostState = remember { SnackbarHostState() }
	var apiKeyInput by rememberSaveable { mutableStateOf("") }

	val messageText = message?.let { stringResource(it.textRes) }
	LaunchedEffect(message, messageText) {
		if (messageText != null) {
			snackbarHostState.showSnackbar(messageText)
			viewModel.consumeMessage()
		}
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.settings_title)) },
				navigationIcon = {
					IconButton(onClick = onOpenDrawer) {
						Icon(
							imageVector = Icons.Default.Menu,
							contentDescription = stringResource(R.string.cd_open_menu)
						)
					}
				}
			)
		},
		snackbarHost = { SnackbarHost(snackbarHostState) }
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
				text = stringResource(R.string.settings_api_key_title),
				style = MaterialTheme.typography.titleMedium
			)
			Surface(
				shape = RoundedCornerShape(16.dp),
				color = MaterialTheme.colorScheme.surfaceVariant,
				modifier = Modifier.fillMaxWidth()
			) {
				Column(
					modifier = Modifier.padding(16.dp),
					verticalArrangement = Arrangement.spacedBy(12.dp)
				) {
					Text(
						text = stringResource(R.string.settings_api_key_description),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
					Text(
						text = stringResource(
							if (apiKeyPresent) {
								R.string.settings_api_key_configured
							} else {
								R.string.settings_api_key_not_configured
							}
						),
						style = MaterialTheme.typography.bodyMedium
					)
					if (apiKeyInvalid) {
						Text(
							text = stringResource(R.string.settings_api_key_invalid_saved),
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.error
						)
					}
					if (accountPurity.isNotEmpty()) {
						val labels = listOf("sfw", "sketchy", "nsfw")
							.filter { it in accountPurity }
							.map { stringResource(purityLabelRes(it)) }
						Text(
							text = stringResource(R.string.settings_account_purity, labels.joinToString(", ")),
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
					if (accountBlacklistCount > 0) {
						Text(
							text = stringResource(R.string.settings_account_blacklist, accountBlacklistCount),
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
					OutlinedTextField(
						value = apiKeyInput,
						onValueChange = { apiKeyInput = it },
						label = { Text(stringResource(R.string.settings_api_key_hint)) },
						singleLine = true,
						visualTransformation = PasswordVisualTransformation(),
						modifier = Modifier.fillMaxWidth()
					)
					Row(
						horizontalArrangement = Arrangement.spacedBy(12.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Button(
							onClick = { viewModel.saveApiKey(apiKeyInput) },
							enabled = apiKeyInput.isNotBlank() && !saving
						) {
							if (saving) {
								CircularProgressIndicator(
									modifier = Modifier.size(18.dp),
									strokeWidth = 2.dp
								)
								Spacer(modifier = Modifier.width(8.dp))
							}
							Text(stringResource(R.string.settings_api_key_save))
						}
						OutlinedButton(
							onClick = viewModel::removeApiKey,
							enabled = apiKeyPresent
						) {
							Text(stringResource(R.string.settings_api_key_remove))
						}
					}
				}
			}

			Text(
				text = stringResource(R.string.settings_sorting_title),
				style = MaterialTheme.typography.titleMedium
			)
			Surface(
				shape = RoundedCornerShape(16.dp),
				color = MaterialTheme.colorScheme.surfaceVariant,
				modifier = Modifier.fillMaxWidth()
			) {
				Column(modifier = Modifier.padding(vertical = 8.dp)) {
					Sorting.entries.forEach { sorting ->
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.selectable(
									selected = filters.sorting == sorting,
									onClick = { viewModel.setSorting(sorting) }
								)
								.padding(horizontal = 16.dp, vertical = 8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							RadioButton(
								selected = filters.sorting == sorting,
								onClick = { viewModel.setSorting(sorting) }
							)
							Text(
								text = stringResource(sorting.labelRes()),
								modifier = Modifier.padding(start = 8.dp)
							)
						}
					}
				}
			}

			Text(
				text = stringResource(R.string.settings_diagnostics_title),
				style = MaterialTheme.typography.titleMedium
			)
			Surface(
				shape = RoundedCornerShape(16.dp),
				color = MaterialTheme.colorScheme.surfaceVariant,
				modifier = Modifier.fillMaxWidth()
			) {
				Column(
					modifier = Modifier.padding(16.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					if (lastRequest == null && lastResponse == null) {
						Text(
							text = stringResource(R.string.settings_diagnostics_empty),
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					} else {
						lastRequest?.let { summary ->
							Text(
								text = stringResource(R.string.settings_diagnostics_last_request, summary),
								style = MaterialTheme.typography.bodySmall
							)
						}
						lastResponse?.let { summary ->
							Text(
								text = stringResource(R.string.settings_diagnostics_last_response, summary),
								style = MaterialTheme.typography.bodySmall
							)
						}
					}
				}
			}

			Text(
				text = stringResource(R.string.settings_about_title),
				style = MaterialTheme.typography.titleMedium
			)
			Surface(
				shape = RoundedCornerShape(16.dp),
				color = MaterialTheme.colorScheme.surfaceVariant,
				modifier = Modifier.fillMaxWidth()
			) {
				Column(
					modifier = Modifier.padding(16.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					Text(
						text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
						style = MaterialTheme.typography.bodyMedium
					)
					Text(
						text = stringResource(R.string.settings_privacy_note),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
		}
	}
}

private fun Sorting.labelRes(): Int = when (this) {
	Sorting.LATEST -> R.string.settings_sorting_latest
	Sorting.TOPLIST_MONTH -> R.string.settings_sorting_toplist
	Sorting.RANDOM -> R.string.settings_sorting_random
}
