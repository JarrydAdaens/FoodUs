package com.maksimowiczm.foodyou.app.ui.database.australianfoodcompositiondatabase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.common.compose.extension.add
import com.maksimowiczm.foodyou.importexport.australianfoodcompositiondatabase.AustralianFoodCompositionDatabaseConfig
import foodyou.app.generated.resources.*
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AustralianFoodCompositionDatabaseScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: AustralianFoodCompositionDatabaseViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AustralianFoodCompositionDatabaseScreen(
        uiState = uiState,
        onBack = onBack,
        onEnabledChange = viewModel::setEnabled,
        onImport = viewModel::import,
        modifier = modifier,
    )
}

@Composable
private fun AustralianFoodCompositionDatabaseScreen(
    uiState: AustralianFoodCompositionDatabaseUiState,
    onBack: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val importing =
        uiState.phase is AustralianFoodCompositionDatabaseUiState.Phase.Downloading ||
            uiState.phase is AustralianFoodCompositionDatabaseUiState.Phase.Importing

    Scaffold(
        modifier = modifier,
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(stringResource(Res.string.headline_australian_food_composition_database))
                },
                navigationIcon = { ArrowBackIconButton(onClick = onBack, enabled = !importing) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = paddingValues.add(vertical = 8.dp),
        ) {
            item {
                Text(
                    text =
                        stringResource(
                            Res.string.description_australian_food_composition_database
                        ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LabelledSwitch(
                        label = stringResource(Res.string.action_enable_provider_search),
                        checked = uiState.enabled,
                        enabled = !importing,
                        onCheckedChange = onEnabledChange,
                    )
                }
            }

            item { HorizontalDivider() }

            item { StateSection(uiState) }

            item {
                Button(
                    onClick = onImport,
                    enabled = !importing,
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val label =
                        if (uiState.metadata?.installed == true) {
                            stringResource(Res.string.action_update_dataset)
                        } else {
                            stringResource(Res.string.action_import)
                        }
                    Text(label)
                }
            }

            if (importing) {
                item {
                    val fraction =
                        (uiState.phase as? AustralianFoodCompositionDatabaseUiState.Phase.Importing)
                            ?.fraction
                    if (fraction == null) {
                        LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
                    } else {
                        LinearWavyProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            item {
                Text(
                    text = AustralianFoodCompositionDatabaseConfig.ATTRIBUTION,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StateSection(uiState: AustralianFoodCompositionDatabaseUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val metadata = uiState.metadata

        InfoRow(
            label = stringResource(Res.string.headline_import_state),
            value =
                if (metadata?.installed == true) {
                    stringResource(
                        Res.string.neutral_installed_version,
                        metadata.installedVersion.orEmpty(),
                    )
                } else {
                    stringResource(Res.string.neutral_not_installed)
                },
        )

        InfoRow(
            label = stringResource(Res.string.headline_last_import),
            value = metadata?.importedAtEpochSeconds.toDateLabel(),
        )

        InfoRow(
            label = stringResource(Res.string.headline_last_successful_check),
            value = metadata?.lastSuccessfulCheckEpochSeconds.toDateLabel(),
        )

        val error = (uiState.phase as? AustralianFoodCompositionDatabaseUiState.Phase.Error)?.message
            ?: metadata?.lastError
        if (error != null) {
            InfoRow(
                label = stringResource(Res.string.headline_last_error),
                value = error,
                valueColor = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = valueColor)
    }
}

@Composable
private fun LabelledSwitch(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.titleSmall)
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun Long?.toDateLabel(): String =
    if (this == null) {
        stringResource(Res.string.neutral_never)
    } else {
        Instant.fromEpochSeconds(this)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString()
    }
