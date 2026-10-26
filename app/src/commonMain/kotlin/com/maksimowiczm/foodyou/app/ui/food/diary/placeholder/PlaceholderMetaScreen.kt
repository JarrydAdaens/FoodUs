package com.maksimowiczm.foodyou.app.ui.food.diary.placeholder

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.common.compose.extension.LaunchedCollectWithLifecycle
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Placeholder resolution meta screen (Milestone 2, Story 9). Opened instead of the regular editor
 * when a fast-text placeholder is edited. It shows the saved name/description as context and offers
 * three resolution routes — search, quick add, and a text-only AI that generates a better search
 * query — plus an explicit affordance to remove the placeholder once the real entry is logged.
 */
@Composable
fun PlaceholderMetaScreen(
    manualEntryId: Long,
    onBack: () -> Unit,
    onSearch: (epochDay: Long, diarySlotId: Long) -> Unit,
    onQuickAdd: (epochDay: Long, diarySlotId: Long, name: String) -> Unit,
    onAiQuery: (epochDay: Long, diarySlotId: Long, query: String) -> Unit,
    onRemoved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PlaceholderMetaViewModel = koinViewModel { parametersOf(manualEntryId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val readFailed by viewModel.readFailed.collectAsStateWithLifecycle()

    val latestOnAiQuery by rememberUpdatedState(onAiQuery)
    val latestOnRemoved by rememberUpdatedState(onRemoved)
    LaunchedCollectWithLifecycle(viewModel.events) { event ->
        when (event) {
            is PlaceholderMetaEvent.QueryGenerated ->
                latestOnAiQuery(event.epochDay, event.diarySlotId, event.query)

            PlaceholderMetaEvent.Deleted -> latestOnRemoved()
        }
    }

    Scaffold(
        modifier = (modifier).uiName("PlaceholderMetaScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "PlaceholderMetaScreen"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_resolve_placeholder), modifier = Modifier.uiName("PlaceholderMetaScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "PlaceholderMetaScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("PlaceholderMetaScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "PlaceholderMetaScreen")) }, modifier = Modifier.uiName("PlaceholderMetaScreen.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "PlaceholderMetaScreen")
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                (Modifier.fillMaxWidth()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)).uiName("PlaceholderMetaScreen.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "PlaceholderMetaScreen"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (readFailed) {
                Text(stringResource(Res.string.meals_diary_unavailable), modifier = Modifier.uiName("PlaceholderMetaScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "PlaceholderMetaScreen"))
                TextButton(onClick = viewModel::retryRead, modifier = Modifier.uiName("PlaceholderMetaScreen.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "PlaceholderMetaScreen")) { Text(stringResource(Res.string.meals_retry), modifier = Modifier.uiName("PlaceholderMetaScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "PlaceholderMetaScreen")) }
                return@Column
            }
            if (!state.loaded) return@Column

            ElevatedCard(modifier = (Modifier.fillMaxWidth()).uiName("PlaceholderMetaScreen.ElevatedCard.1", "androidx.compose.material3.ElevatedCard", uiInspectionSource, "PlaceholderMetaScreen")) {
                Column(
                    modifier = (Modifier.fillMaxWidth().padding(16.dp)).uiName("PlaceholderMetaScreen.Column.2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "PlaceholderMetaScreen"),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = state.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.uiName("PlaceholderMetaScreen.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "PlaceholderMetaScreen"))
                    state.description?.let {
                        Text(text = it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("PlaceholderMetaScreen.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "PlaceholderMetaScreen"))
                    }
                }
            }

            Text(
                text = stringResource(Res.string.description_resolve_placeholder),
                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("PlaceholderMetaScreen.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "PlaceholderMetaScreen")
            )

            ResolutionOption(
                icon = Icons.Outlined.Search,
                label = stringResource(Res.string.action_search),
                enabled = true,
                onClick = { onSearch(state.epochDay, state.diarySlotId) }, modifier = Modifier.uiName("PlaceholderMetaScreen.ResolutionOption.1", "com.maksimowiczm.foodyou.app.ui.food.diary.placeholder.ResolutionOption", uiInspectionSource, "PlaceholderMetaScreen")
            )

            ResolutionOption(
                icon = Icons.Outlined.Bolt,
                label = stringResource(Res.string.headline_quick_add),
                enabled = true,
                onClick = { onQuickAdd(state.epochDay, state.diarySlotId, state.name) }, modifier = Modifier.uiName("PlaceholderMetaScreen.ResolutionOption.2", "com.maksimowiczm.foodyou.app.ui.food.diary.placeholder.ResolutionOption", uiInspectionSource, "PlaceholderMetaScreen")
            )

            ResolutionOption(
                icon = Icons.Outlined.SmartToy,
                label = stringResource(Res.string.action_ai_search),
                enabled = state.aiConfigured && !state.generatingQuery,
                onClick = viewModel::generateQuery, modifier = Modifier.uiName("PlaceholderMetaScreen.ResolutionOption.3", "com.maksimowiczm.foodyou.app.ui.food.diary.placeholder.ResolutionOption", uiInspectionSource, "PlaceholderMetaScreen")
            )

            if (state.generatingQuery) {
                Row(
                    modifier = (Modifier.fillMaxWidth()).uiName("PlaceholderMetaScreen.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "PlaceholderMetaScreen"),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = (Modifier.height(24.dp)).uiName("PlaceholderMetaScreen.CircularProgressIndicator.1", "androidx.compose.material3.CircularProgressIndicator", uiInspectionSource, "PlaceholderMetaScreen"))
                    Text(stringResource(Res.string.neutral_generating_query), modifier = Modifier.uiName("PlaceholderMetaScreen.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "PlaceholderMetaScreen"))
                }
            }

            if (!state.aiConfigured) {
                Text(
                    text = stringResource(Res.string.neutral_ai_not_configured),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error, modifier = Modifier.uiName("PlaceholderMetaScreen.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "PlaceholderMetaScreen")
                )
            }

            state.errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error, modifier = Modifier.uiName("PlaceholderMetaScreen.Text.9", "androidx.compose.material3.Text", uiInspectionSource, "PlaceholderMetaScreen")
                )
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = viewModel::removePlaceholder,
                modifier = (Modifier.fillMaxWidth()).uiName("PlaceholderMetaScreen.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "PlaceholderMetaScreen"),
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.uiName("PlaceholderMetaScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "PlaceholderMetaScreen"))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.action_remove_placeholder), modifier = Modifier.uiName("PlaceholderMetaScreen.Text.10", "androidx.compose.material3.Text", uiInspectionSource, "PlaceholderMetaScreen"))
            }
        }
    }
}

@Composable
private fun ResolutionOption(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = (modifier.fillMaxWidth()).uiName("ResolutionOption.OutlinedButton.1", "androidx.compose.material3.OutlinedButton", uiInspectionSource, "ResolutionOption")) {
        Icon(icon, contentDescription = null, modifier = Modifier.uiName("ResolutionOption.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "ResolutionOption"))
        Spacer(Modifier.width(12.dp))
        Text(label, modifier = Modifier.uiName("ResolutionOption.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ResolutionOption"))
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.diary.placeholder",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/placeholder/PlaceholderMetaScreen.kt",
)
