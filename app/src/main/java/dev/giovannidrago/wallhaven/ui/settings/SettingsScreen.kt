package dev.giovannidrago.wallhaven.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.giovannidrago.wallhaven.BuildConfig
import dev.giovannidrago.wallhaven.R
import dev.giovannidrago.wallhaven.domain.AppearanceMode
import dev.giovannidrago.wallhaven.domain.Sorting
import dev.giovannidrago.wallhaven.ui.components.UpdateAvailableDialog
import dev.giovannidrago.wallhaven.ui.components.openUrl
import dev.giovannidrago.wallhaven.ui.search.purityLabelRes
import dev.giovannidrago.wallhaven.ui.theme.DARK_PRESETS
import dev.giovannidrago.wallhaven.ui.theme.LIGHT_PRESETS
import dev.giovannidrago.wallhaven.ui.theme.darkPreset
import dev.giovannidrago.wallhaven.ui.theme.lightPreset
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
	val installedVersion = viewModel.installedVersion
	val update by viewModel.update.collectAsStateWithLifecycle()
	val themeSettings by viewModel.themeSettings.collectAsStateWithLifecycle()
	val snackbarHostState = remember { SnackbarHostState() }
	val scope = rememberCoroutineScope()
	val context = LocalContext.current
	val errorOpenLink = stringResource(R.string.error_open_link)
	var apiKeyInput by rememberSaveable { mutableStateOf("") }

	val noUpdatesText = stringResource(R.string.settings_no_updates)
	val updateErrorText = stringResource(R.string.settings_update_error)
	val downloadErrorText = stringResource(R.string.settings_download_error)

	val messageText = message?.let { stringResource(it.textRes) }
	LaunchedEffect(message, messageText) {
		if (messageText != null) {
			snackbarHostState.showSnackbar(messageText)
			viewModel.consumeMessage()
		}
	}

	LaunchedEffect(update) {
		when (update) {
			UpdateUiState.NoUpdate -> {
				snackbarHostState.showSnackbar(noUpdatesText)
				viewModel.consumeUpdate()
			}
			UpdateUiState.CheckError -> {
				snackbarHostState.showSnackbar(updateErrorText)
				viewModel.consumeUpdate()
			}
			else -> Unit
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
				text = stringResource(R.string.settings_appearance_title),
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
						text = stringResource(R.string.settings_appearance_description),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
					Text(
						text = stringResource(R.string.settings_appearance_mode),
						style = MaterialTheme.typography.titleSmall
					)
					FlowRow(
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						verticalArrangement = Arrangement.spacedBy(8.dp)
					) {
						AppearanceMode.entries.forEach { mode ->
							FilterChip(
								selected = themeSettings.mode == mode,
								onClick = { viewModel.setAppearanceMode(mode) },
								label = { Text(stringResource(mode.labelRes())) }
							)
						}
					}
					Text(
						text = stringResource(R.string.settings_appearance_light_theme),
						style = MaterialTheme.typography.titleSmall
					)
					FlowRow(
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						verticalArrangement = Arrangement.spacedBy(8.dp)
					) {
						LIGHT_PRESETS.forEach { preset ->
							FilterChip(
								selected = lightPreset(themeSettings.lightPresetId).id == preset.id,
								onClick = { viewModel.setLightPreset(preset.id) },
								label = { Text(stringResource(preset.labelRes)) }
							)
						}
					}
					Text(
						text = stringResource(R.string.settings_appearance_dark_theme),
						style = MaterialTheme.typography.titleSmall
					)
					FlowRow(
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						verticalArrangement = Arrangement.spacedBy(8.dp)
					) {
						DARK_PRESETS.forEach { preset ->
							FilterChip(
								selected = darkPreset(themeSettings.darkPresetId).id == preset.id,
								onClick = { viewModel.setDarkPreset(preset.id) },
								label = { Text(stringResource(preset.labelRes)) }
							)
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
						text = stringResource(R.string.settings_version, installedVersion),
						style = MaterialTheme.typography.bodyMedium
					)
					FilledTonalButton(
						onClick = viewModel::checkForUpdates,
						enabled = update !is UpdateUiState.Checking
					) {
						if (update is UpdateUiState.Checking) {
							CircularProgressIndicator(
								modifier = Modifier.size(18.dp),
								strokeWidth = 2.dp
							)
							Spacer(modifier = Modifier.width(8.dp))
						}
						Text(stringResource(R.string.settings_check_updates))
					}
					Text(
						text = stringResource(R.string.settings_privacy_note),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
					FilledTonalButton(
						onClick = {
							if (!openUrl(context, BuildConfig.DONATION_URL)) {
								scope.launch { snackbarHostState.showSnackbar(errorOpenLink) }
							}
						}
					) {
						Icon(
							imageVector = Icons.Outlined.LocalCafe,
							contentDescription = null
						)
						Spacer(modifier = Modifier.width(8.dp))
						Text(stringResource(R.string.settings_donate_button))
					}
				}
			}
		}
	}

	val available = update
	if (available is UpdateUiState.Available) {
		UpdateAvailableDialog(
			version = available.version,
			onDismiss = viewModel::consumeUpdate,
			onDownload = {
				if (!openUrl(context, available.downloadUrl)) {
					scope.launch { snackbarHostState.showSnackbar(downloadErrorText) }
				}
				viewModel.consumeUpdate()
			}
		)
	}
}

private fun Sorting.labelRes(): Int = when (this) {
	Sorting.LATEST -> R.string.settings_sorting_latest
	Sorting.TOPLIST_MONTH -> R.string.settings_sorting_toplist
	Sorting.RANDOM -> R.string.settings_sorting_random
}

private fun AppearanceMode.labelRes(): Int = when (this) {
	AppearanceMode.SYSTEM -> R.string.settings_appearance_system
	AppearanceMode.LIGHT -> R.string.settings_appearance_light
	AppearanceMode.DARK -> R.string.settings_appearance_dark
}
