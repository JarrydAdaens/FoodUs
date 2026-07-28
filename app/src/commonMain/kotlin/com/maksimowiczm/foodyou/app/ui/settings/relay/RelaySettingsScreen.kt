package com.maksimowiczm.foodyou.app.ui.settings.relay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.TextFieldLineLimits
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
import com.maksimowiczm.foodyou.relay.domain.RelayUrlValidationResult
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RelaySettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: RelaySettingsViewModel = koinViewModel()
    val urlError by viewModel.urlError.collectAsStateWithLifecycle()
    val check by viewModel.check.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(Res.string.headline_relay_settings)) },
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
                    text = stringResource(Res.string.description_relay_settings_screen),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            item {
                val errorText =
                    when (urlError) {
                        RelayUrlValidationResult.NotHttps ->
                            stringResource(Res.string.error_relay_url_not_https)
                        RelayUrlValidationResult.Malformed ->
                            stringResource(Res.string.error_relay_url_malformed)
                        else -> null
                    }

                OutlinedTextField(
                    state = viewModel.url,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.headline_relay_url)) },
                    supportingText = errorText?.let { { Text(it) } },
                    isError = errorText != null,
                    lineLimits = TextFieldLineLimits.SingleLine,
                )
            }

            item { CheckRow(state = check, onCheck = viewModel::checkConnection) }
        }
    }
}

@Composable
private fun CheckRow(state: RelayCheckUiState, onCheck: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(onClick = onCheck, enabled = state != RelayCheckUiState.Checking) {
            Text(stringResource(Res.string.action_check_relay_connection))
        }

        when (state) {
            RelayCheckUiState.Idle -> Unit
            RelayCheckUiState.Checking -> {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                StatusText(stringResource(Res.string.relay_checking))
            }
            RelayCheckUiState.Success -> {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = SuccessGreen,
                )
                StatusText(stringResource(Res.string.relay_check_success), color = SuccessGreen)
            }
            is RelayCheckUiState.Unreachable ->
                StatusText(
                    text = state.message ?: stringResource(Res.string.relay_check_unreachable),
                    color = MaterialTheme.colorScheme.error,
                )
            RelayCheckUiState.Unknown ->
                StatusText(stringResource(Res.string.relay_check_unknown))
        }
    }
}

@Composable
private fun RowScope.StatusText(text: String, color: Color = Color.Unspecified) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        modifier = Modifier.weight(1f),
    )
}

// Explicit success green for the check tick; the Material scheme has no semantic success color.
private val SuccessGreen = Color(0xFF2E7D32)
