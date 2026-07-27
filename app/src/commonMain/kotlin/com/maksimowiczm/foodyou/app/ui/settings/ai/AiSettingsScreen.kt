package com.maksimowiczm.foodyou.app.ui.settings.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.common.compose.extension.add
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AiSettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: AiSettingsViewModel = koinViewModel()
    val validation by viewModel.validation.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(Res.string.headline_ai_settings)) },
                navigationIcon = { ArrowBackIconButton(onBack) },
                actions = {
                    TextButton(onClick = viewModel::save) {
                        Text(stringResource(Res.string.action_save))
                    }
                },
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
                    text = stringResource(Res.string.description_ai_settings_screen),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            item {
                AiField(
                    state = viewModel.apiKey,
                    label = stringResource(Res.string.headline_api_key),
                )
            }

            item {
                AiField(
                    state = viewModel.endpoint,
                    label = stringResource(Res.string.headline_ai_endpoint),
                    placeholder = viewModel.endpointPlaceholder,
                )
            }

            item {
                AiField(
                    state = viewModel.model,
                    label = stringResource(Res.string.headline_ai_model),
                    placeholder = viewModel.modelPlaceholder,
                )
            }

            item { ValidateRow(state = validation, onValidate = viewModel::validate) }

            item {
                AiField(
                    state = viewModel.systemPrompt,
                    label = stringResource(Res.string.headline_ai_system_prompt),
                    supportingText = stringResource(Res.string.description_ai_system_prompt),
                    singleLine = false,
                )
            }
        }
    }
}

@Composable
private fun AiField(
    state: TextFieldState,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        state = state,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = supportingText?.let { { Text(it) } },
        lineLimits =
            if (singleLine) TextFieldLineLimits.SingleLine
            else TextFieldLineLimits.MultiLine(minHeightInLines = 3),
    )
}

@Composable
private fun ValidateRow(
    state: AiValidationUiState,
    onValidate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onValidate,
            enabled = state != AiValidationUiState.Validating,
        ) {
            Text(stringResource(Res.string.action_validate))
        }

        when (state) {
            AiValidationUiState.Idle -> Unit
            AiValidationUiState.Validating -> {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Text(
                    text = stringResource(Res.string.ai_validating),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            AiValidationUiState.Success -> {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = SuccessGreen,
                )
                Text(
                    text = stringResource(Res.string.ai_validation_success),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SuccessGreen,
                )
            }
            is AiValidationUiState.Error ->
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
        }
    }
}

// Explicit success green for the validation tick; the Material scheme has no semantic success color.
private val SuccessGreen = Color(0xFF2E7D32)
