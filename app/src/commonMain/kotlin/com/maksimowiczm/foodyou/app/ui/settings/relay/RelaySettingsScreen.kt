package com.maksimowiczm.foodyou.app.ui.settings.relay

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

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
        modifier = (modifier).uiName("RelaySettingsScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "RelaySettingsScreen"),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(Res.string.headline_relay_settings), modifier = Modifier.uiName("RelaySettingsScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "RelaySettingsScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("RelaySettingsScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "RelaySettingsScreen")) },
                actions = {
                    TextButton(onClick = viewModel::save, modifier = Modifier.uiName("RelaySettingsScreen.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "RelaySettingsScreen")) {
                        Text(stringResource(Res.string.action_save), modifier = Modifier.uiName("RelaySettingsScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "RelaySettingsScreen"))
                    }
                },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("RelaySettingsScreen.LargeFlexibleTopAppBar.1", "androidx.compose.material3.LargeFlexibleTopAppBar", uiInspectionSource, "RelaySettingsScreen")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                (Modifier.fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("RelaySettingsScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "RelaySettingsScreen"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = paddingValues.add(vertical = 8.dp),
        ) {
            item {
                Text(
                    text = stringResource(Res.string.description_relay_settings_screen),
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("RelaySettingsScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "RelaySettingsScreen")
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
                    modifier = (Modifier.fillMaxWidth()).uiName("RelaySettingsScreen.OutlinedTextField.1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "RelaySettingsScreen"),
                    label = { Text(stringResource(Res.string.headline_relay_url), modifier = Modifier.uiName("RelaySettingsScreen.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "RelaySettingsScreen")) },
                    supportingText = errorText?.let { { Text(it, modifier = Modifier.uiName("RelaySettingsScreen.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "RelaySettingsScreen")) } },
                    isError = errorText != null,
                    lineLimits = TextFieldLineLimits.SingleLine,
                )
            }

            item { CheckRow(state = check, onCheck = viewModel::checkConnection, modifier = Modifier.uiName("RelaySettingsScreen.CheckRow.1", "com.maksimowiczm.foodyou.app.ui.settings.relay.CheckRow", uiInspectionSource, "RelaySettingsScreen")) }
        }
    }
}

@Composable
private fun CheckRow(state: RelayCheckUiState, onCheck: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = (modifier.fillMaxWidth()).uiName("CheckRow.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "CheckRow"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(onClick = onCheck, enabled = state != RelayCheckUiState.Checking, modifier = Modifier.uiName("CheckRow.OutlinedButton.1", "androidx.compose.material3.OutlinedButton", uiInspectionSource, "CheckRow")) {
            Text(stringResource(Res.string.action_check_relay_connection), modifier = Modifier.uiName("CheckRow.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "CheckRow"))
        }

        when (state) {
            RelayCheckUiState.Idle -> Unit
            RelayCheckUiState.Checking -> {
                CircularProgressIndicator(modifier = (Modifier.size(20.dp)).uiName("CheckRow.CircularProgressIndicator.1", "androidx.compose.material3.CircularProgressIndicator", uiInspectionSource, "CheckRow"), strokeWidth = 2.dp)
                StatusText(stringResource(Res.string.relay_checking))
            }
            RelayCheckUiState.Success -> {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = SuccessGreen, modifier = Modifier.uiName("CheckRow.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "CheckRow")
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
        modifier = (Modifier.weight(1f)).uiName("StatusText.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "StatusText"),
    )
}

// Explicit success green for the check tick; the Material scheme has no semantic success color.
private val SuccessGreen = Color(0xFF2E7D32)


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.settings.relay",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/settings/relay/RelaySettingsScreen.kt",
)
