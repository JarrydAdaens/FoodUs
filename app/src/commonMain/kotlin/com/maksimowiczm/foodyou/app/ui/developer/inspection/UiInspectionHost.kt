package com.maksimowiczm.foodyou.app.ui.developer.inspection

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.theme.FoodYouTheme
import com.maksimowiczm.foodyou.common.compose.utility.LocalClipboardManager
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.settings.domain.entity.Settings
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/** Every application Activity uses this seam; no debug-only APIs or retained capture history. */
@Composable
internal fun UiInspectionHost(repository: UserPreferencesRepository<Settings>, content: @Composable () -> Unit) {
    val preferences by remember(repository) {
        repository.observe().map { it.uiNameBarEnabled to it.uiNameBarIncludeHierarchy }
    }.collectAsStateWithLifecycle(initialValue = false to false)
    val scope = rememberCoroutineScope()
    UiInspectionContent(
        enabled = preferences.first,
        includeHierarchy = preferences.second,
        onIncludeHierarchy = { value ->
            scope.launch { repository.update { copy(uiNameBarIncludeHierarchy = value) } }
        },
        content = content,
    )
}

@Composable
internal fun UiInspectionContent(
    enabled: Boolean,
    includeHierarchy: Boolean,
    onIncludeHierarchy: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val inspector = remember(enabled) { if (enabled) UiInspectorState(scope) else null }
    DisposableEffect(inspector) { onDispose { inspector?.clear() } }
    val configuration = LocalViewConfiguration.current
    val inspectionConfiguration = remember(configuration) {
        object : ViewConfiguration by configuration {
            // Enabled inspection owns holds. No native long press may fire before the inspector.
            override val longPressTimeoutMillis = 86_400_000L
        }
    }
    FoodYouTheme {
        Column(Modifier.fillMaxSize()) {
            if (inspector != null) {
                UiNameBar(inspector.snapshot, includeHierarchy, onIncludeHierarchy)
            }
            CompositionLocalProvider(
                LocalUiInspector provides inspector,
                LocalInspectionLongPressThreshold provides configuration.longPressTimeoutMillis,
                LocalViewConfiguration provides if (enabled) inspectionConfiguration else configuration,
            ) {
                val insets = if (enabled) Modifier.consumeWindowInsets(WindowInsets.statusBars) else Modifier
                Box(Modifier.weight(1f).fillMaxWidth().then(insets)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun UiNameBar(snapshot: UiSelectionSnapshot?, includeHierarchy: Boolean, onIncludeHierarchy: (Boolean) -> Unit) {
    val clipboard = LocalClipboardManager.current
    // Inspector chrome intentionally uses normal text-selection gestures and cannot select itself.
    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column(Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = snapshot?.selected?.descriptor?.name.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text(stringResource(Res.string.ui_name_bar_title)) },
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    enabled = snapshot != null,
                    onClick = { snapshot?.let { clipboard.copy("FoodUs UI locator", it.copyText(includeHierarchy)) } },
                ) {
                    Icon(Icons.Outlined.ContentCopy, stringResource(Res.string.ui_name_bar_copy))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = includeHierarchy, onCheckedChange = onIncludeHierarchy)
                Text(stringResource(Res.string.ui_name_bar_hierarchy), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
