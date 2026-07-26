package com.maksimowiczm.foodyou.app.ui.food.diary.placeholder

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
    onSearch: (epochDay: Long, mealId: Long) -> Unit,
    onQuickAdd: (epochDay: Long, mealId: Long, name: String) -> Unit,
    onAiQuery: (epochDay: Long, mealId: Long, query: String) -> Unit,
    onRemoved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PlaceholderMetaViewModel = koinViewModel { parametersOf(manualEntryId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val latestOnAiQuery by rememberUpdatedState(onAiQuery)
    val latestOnRemoved by rememberUpdatedState(onRemoved)
    LaunchedCollectWithLifecycle(viewModel.events) { event ->
        when (event) {
            is PlaceholderMetaEvent.QueryGenerated ->
                latestOnAiQuery(event.epochDay, event.mealId, event.query)

            PlaceholderMetaEvent.Deleted -> latestOnRemoved()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_resolve_placeholder)) },
                navigationIcon = { ArrowBackIconButton(onBack) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!state.loaded) return@Column

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = state.name, style = MaterialTheme.typography.titleLarge)
                    state.description?.let {
                        Text(text = it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Text(
                text = stringResource(Res.string.description_resolve_placeholder),
                style = MaterialTheme.typography.bodyMedium,
            )

            ResolutionOption(
                icon = Icons.Outlined.Search,
                label = stringResource(Res.string.action_search),
                enabled = true,
                onClick = { onSearch(state.epochDay, state.mealId) },
            )

            ResolutionOption(
                icon = Icons.Outlined.Bolt,
                label = stringResource(Res.string.headline_quick_add),
                enabled = true,
                onClick = { onQuickAdd(state.epochDay, state.mealId, state.name) },
            )

            ResolutionOption(
                icon = Icons.Outlined.SmartToy,
                label = stringResource(Res.string.action_ai_search),
                enabled = state.aiConfigured && !state.generatingQuery,
                onClick = viewModel::generateQuery,
            )

            if (state.generatingQuery) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.height(24.dp))
                    Text(stringResource(Res.string.neutral_generating_query))
                }
            }

            if (!state.aiConfigured) {
                Text(
                    text = stringResource(Res.string.neutral_ai_not_configured),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            state.errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = viewModel::removePlaceholder,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.action_remove_placeholder))
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
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Text(label)
    }
}
