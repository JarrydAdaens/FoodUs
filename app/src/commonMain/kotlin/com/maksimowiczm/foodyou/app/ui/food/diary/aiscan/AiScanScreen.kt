package com.maksimowiczm.foodyou.app.ui.food.diary.aiscan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.maksimowiczm.foodyou.ai.domain.AiFoodEstimate
import com.maksimowiczm.foodyou.ai.domain.toNutritionFacts
import com.maksimowiczm.foodyou.app.ui.food.shared.component.KnownNutrientList
import com.maksimowiczm.foodyou.common.compose.utility.formatClipZeros
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.food.domain.entity.FoodId
import foodyou.app.generated.resources.*
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * AI scanning sub-screen (Milestone 2, Story 6): capture a photo, ask the AI to identify the food,
 * then either save the guess to Quick Add (tick) or align it with a locally saved custom food.
 */
@Composable
fun AiScanScreen(
    onBack: () -> Unit,
    onSaveToQuickAdd: (AiFoodEstimate, String) -> Unit,
    onAlignPickFood: (FoodId) -> Unit,
    onCreateCustomFood: () -> Unit,
    modifier: Modifier = Modifier,
    savedScanItemId: String? = null,
    onSavedScanItemConsumed: () -> Unit = {},
) {
    val viewModel: AiScanViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val configured by viewModel.aiConfigured.collectAsStateWithLifecycle()

    var alignQuery by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(savedScanItemId) {
        savedScanItemId?.let { id ->
            val next = viewModel.acknowledgeSaved(id)
            onSavedScanItemConsumed()
            next?.let { onSaveToQuickAdd(it.estimate, it.id) }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_ai_scanning)) },
                navigationIcon = { ArrowBackIconButton(onBack) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AiScanCameraSection(
                jpeg = state.jpeg,
                onPhotoCaptured = viewModel::onPhotoCaptured,
                onDiscardRetry = viewModel::onDiscard,
            )

            when (val current = state) {
                is AiScanUiState.NoPhoto,
                is AiScanUiState.Captured,
                is AiScanUiState.Failed -> {
                    if (current is AiScanUiState.Failed) {
                        Text(
                            text = current.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    SubmitSection(
                        hint = viewModel.hint,
                        configured = configured,
                        photoCaptured = current.jpeg != null,
                        onSubmit = viewModel::submit,
                    )
                }

                is AiScanUiState.Scanning -> ScanningIndicator()

                is AiScanUiState.Result -> {
                    Text(stringResource(Res.string.neutral_ai_saved_count, current.review.savedIds.size))
                    if (current.rejectedCount > 0) {
                        Text(stringResource(Res.string.neutral_ai_omitted_count, current.rejectedCount))
                    }
                    current.review.items.forEach { item ->
                        key(item.id) {
                            AiResultCard(
                                estimate = item.estimate,
                                selected = item.id in current.review.selectedIds,
                                saved = item.id in current.review.savedIds,
                                single = current.review.items.size == 1,
                                onSelected = { viewModel.select(item.id, it) },
                                onAlign = { alignQuery = item.estimate.name },
                            )
                        }
                    }
                    Button(
                        enabled = current.review.selectedCount > 0,
                        onClick = {
                            viewModel.startSelected()?.let { onSaveToQuickAdd(it.estimate, it.id) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (current.review.items.size == 1) stringResource(Res.string.action_save)
                            else stringResource(Res.string.action_ai_edit_selected, current.review.selectedCount))
                    }
                }
            }
        }
    }

    alignQuery?.let { query ->
        AlignBottomSheet(
            query = query,
            viewModel = viewModel,
            onPickFood = { foodId ->
                alignQuery = null
                onAlignPickFood(foodId)
            },
            onCreateCustomFood = {
                alignQuery = null
                onCreateCustomFood()
            },
            onDismiss = { alignQuery = null },
        )
    }
}

/**
 * Optional per-scan hint field (layer 3) plus the primary Submit button. Submit needs a captured
 * photo and a configured AI (the hint is always optional); the not-configured helper shows when no
 * usable key resolves.
 */
@Composable
private fun SubmitSection(
    hint: TextFieldState,
    configured: Boolean,
    photoCaptured: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bringHintIntoView = remember { BringIntoViewRequester() }
    var hintFocused by remember { mutableStateOf(false) }
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(hintFocused, imeBottom) {
        if (hintFocused) {
            withFrameNanos { }
            bringHintIntoView.bringIntoView()
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            state = hint,
            modifier =
                Modifier.fillMaxWidth()
                    .bringIntoViewRequester(bringHintIntoView)
                    .onFocusChanged { hintFocused = it.isFocused },
            label = { Text(stringResource(Res.string.label_ai_scan_hint)) },
            supportingText = { Text(stringResource(Res.string.description_ai_scan_hint)) },
            lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 3, maxHeightInLines = 3),
        )
        Button(
            onClick = onSubmit,
            enabled = photoCaptured && configured,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.action_submit))
        }
        if (!configured) {
            Text(
                text = stringResource(Res.string.neutral_ai_not_configured),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun ScanningIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(modifier = Modifier.height(24.dp))
        Text(stringResource(Res.string.neutral_ai_analysing))
    }
}

@Composable
private fun AiResultCard(
    estimate: AiFoodEstimate,
    selected: Boolean,
    saved: Boolean,
    single: Boolean,
    onSelected: (Boolean) -> Unit,
    onAlign: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(single) }
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = selected, onCheckedChange = onSelected, enabled = !saved)
                Text(text = estimate.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            }
            if (saved) Text(stringResource(Res.string.neutral_ai_item_saved))
            OutlinedButton(onClick = { expanded = !expanded }) {
                Text(stringResource(if (expanded) Res.string.action_ai_hide_details else Res.string.action_ai_show_details))
            }
            if (expanded) {
                estimate.certainty?.let {
                    Text(
                        text =
                            stringResource(
                                Res.string.neutral_ai_certainty,
                                "${(it * 100).roundToInt()}%",
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                estimate.description?.let { Text(it) }
                estimate.servingCount?.let {
                    Text("${stringResource(Res.string.label_number_of_servings)}: ${it.formatClipZeros()}")
                }
                estimate.weightGrams?.let {
                    Text("${stringResource(Res.string.weight)}: ${it.formatClipZeros()} ${stringResource(Res.string.unit_gram_short)}")
                }
                KnownNutrientList(facts = estimate.toNutritionFacts())
            }

            HorizontalDivider()

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onAlign, enabled = !saved, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Tune, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.action_align))
                }
            }
        }
    }
}

@Composable
private fun AlignBottomSheet(
    query: String,
    viewModel: AiScanViewModel,
    onPickFood: (FoodId) -> Unit,
    onCreateCustomFood: () -> Unit,
    onDismiss: () -> Unit,
) {
    val results =
        remember(query) { viewModel.searchCustomFoods(query) }.collectAsLazyPagingItems()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text(
                text = stringResource(Res.string.headline_align_saved_food),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))

            if (results.itemCount == 0) {
                Text(
                    text = stringResource(Res.string.neutral_ai_no_saved_food_matches),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                    items(results.itemCount) { index ->
                        val item = results[index]
                        if (item != null) {
                            ListItem(
                                headlineContent = { Text(item.headline) },
                                modifier =
                                    Modifier.fillMaxWidth().clickable { onPickFood(item.id) },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onCreateCustomFood, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.action_create_custom_food))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
