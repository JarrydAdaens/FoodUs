package com.maksimowiczm.foodyou.app.ui.food.diary.aiscan

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

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
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
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
    onAlignPickFood: (NutritionalItemId) -> Unit,
    onCreateCustomFood: () -> Unit,
    modifier: Modifier = Modifier,
    savedScanItemId: String? = null,
    onSavedScanItemConsumed: () -> Unit = {},
    onScanCompleted: () -> Unit = {},
) {
    val viewModel: AiScanViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val configured by viewModel.aiConfigured.collectAsStateWithLifecycle()

    var alignQuery by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(savedScanItemId) {
        savedScanItemId?.let { id ->
            val accepted = viewModel.acknowledgeSaved(id)
            onSavedScanItemConsumed()
            if (accepted != null) {
                val next = accepted.next
                if (next != null) onSaveToQuickAdd(next.estimate, next.id)
                else onScanCompleted()
            }
        }
    }

    Scaffold(
        modifier = (modifier).uiName("AiScanScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "AiScanScreen"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_ai_scanning), modifier = Modifier.uiName("AiScanScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "AiScanScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("AiScanScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "AiScanScreen")) }, modifier = Modifier.uiName("AiScanScreen.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "AiScanScreen")
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                (Modifier.fillMaxWidth()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)).uiName("AiScanScreen.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "AiScanScreen"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AiScanCameraSection(
                jpeg = state.jpeg,
                onPhotoCaptured = viewModel::onPhotoCaptured, modifier = Modifier.uiName("AiScanScreen.AiScanCameraSection.1", "com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.AiScanCameraSection", uiInspectionSource, "AiScanScreen")
            )

            when (val current = state) {
                is AiScanUiState.NoPhoto,
                is AiScanUiState.Captured,
                is AiScanUiState.Failed -> {
                    if (current is AiScanUiState.Failed) {
                        Text(
                            text = current.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("AiScanScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "AiScanScreen")
                        )
                    }
                    SubmitSection(
                        hint = viewModel.hint,
                        configured = configured,
                        photoCaptured = current.jpeg != null,
                        onSubmit = viewModel::submit, modifier = Modifier.uiName("AiScanScreen.SubmitSection.1", "com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.SubmitSection", uiInspectionSource, "AiScanScreen")
                    )
                }

                is AiScanUiState.Scanning -> ScanningIndicator( modifier = Modifier.uiName("AiScanScreen.ScanningIndicator.1", "com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.ScanningIndicator", uiInspectionSource, "AiScanScreen"))

                is AiScanUiState.Result -> {
                    Text(stringResource(Res.string.neutral_ai_saved_count, current.review.savedIds.size), modifier = Modifier.uiName("AiScanScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "AiScanScreen"))
                    if (current.rejectedCount > 0) {
                        Text(stringResource(Res.string.neutral_ai_omitted_count, current.rejectedCount), modifier = Modifier.uiName("AiScanScreen.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "AiScanScreen"))
                    }
                    current.review.items.forEach { item ->
                        key(item.id) {
                            AiResultCard(
                                estimate = item.estimate,
                                selected = item.id in current.review.selectedIds,
                                saved = item.id in current.review.savedIds,
                                single = current.review.items.size == 1,
                                onSelected = { viewModel.select(item.id, it) },
                                onAlign = { alignQuery = item.estimate.name }, modifier = Modifier.uiName("AiScanScreen.AiResultCard.1", "com.maksimowiczm.foodyou.app.ui.food.diary.aiscan.AiResultCard", uiInspectionSource, "AiScanScreen")
                            )
                        }
                    }
                    Button(
                        enabled = current.review.selectedCount > 0,
                        onClick = {
                            viewModel.startSelected()?.let { onSaveToQuickAdd(it.estimate, it.id) }
                        },
                        modifier = (Modifier.fillMaxWidth()).uiName("AiScanScreen.Button.1", "androidx.compose.material3.Button", uiInspectionSource, "AiScanScreen"),
                    ) {
                        Text(if (current.review.items.size == 1) stringResource(Res.string.action_save)
                            else stringResource(Res.string.action_ai_edit_selected, current.review.selectedCount), modifier = Modifier.uiName("AiScanScreen.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "AiScanScreen"))
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

    Column(modifier = (modifier).uiName("SubmitSection.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "SubmitSection"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            state = hint,
            modifier =
                (Modifier.fillMaxWidth()
                    .bringIntoViewRequester(bringHintIntoView)
                    .onFocusChanged { hintFocused = it.isFocused }).uiName("SubmitSection.OutlinedTextField.1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "SubmitSection"),
            label = { Text(stringResource(Res.string.label_ai_scan_hint), modifier = Modifier.uiName("SubmitSection.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "SubmitSection")) },
            supportingText = { Text(stringResource(Res.string.description_ai_scan_hint), modifier = Modifier.uiName("SubmitSection.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "SubmitSection")) },
            lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 3, maxHeightInLines = 3),
        )
        Button(
            onClick = onSubmit,
            enabled = photoCaptured && configured,
            modifier = (Modifier.fillMaxWidth()).uiName("SubmitSection.Button.1", "androidx.compose.material3.Button", uiInspectionSource, "SubmitSection"),
        ) {
            Text(stringResource(Res.string.action_submit), modifier = Modifier.uiName("SubmitSection.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "SubmitSection"))
        }
        if (!configured) {
            Text(
                text = stringResource(Res.string.neutral_ai_not_configured),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error, modifier = Modifier.uiName("SubmitSection.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "SubmitSection")
            )
        }
    }
}

@Composable
private fun ScanningIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = (modifier.fillMaxWidth()).uiName("ScanningIndicator.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "ScanningIndicator"),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(modifier = (Modifier.height(24.dp)).uiName("ScanningIndicator.CircularProgressIndicator.1", "androidx.compose.material3.CircularProgressIndicator", uiInspectionSource, "ScanningIndicator"))
        Text(stringResource(Res.string.neutral_ai_analysing), modifier = Modifier.uiName("ScanningIndicator.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ScanningIndicator"))
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
    ElevatedCard(modifier = (modifier.fillMaxWidth()).uiName("AiResultCard.ElevatedCard.1", "androidx.compose.material3.ElevatedCard", uiInspectionSource, "AiResultCard")) {
        Column(
            modifier = (Modifier.fillMaxWidth().padding(16.dp)).uiName("AiResultCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "AiResultCard"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.uiName("AiResultCard.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "AiResultCard")) {
                Checkbox(checked = selected, onCheckedChange = onSelected, enabled = !saved, modifier = Modifier.uiName("AiResultCard.Checkbox.1", "androidx.compose.material3.Checkbox", uiInspectionSource, "AiResultCard"))
                Text(text = estimate.name, style = MaterialTheme.typography.titleLarge, modifier = (Modifier.weight(1f)).uiName("AiResultCard.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "AiResultCard"))
            }
            if (saved) Text(stringResource(Res.string.neutral_ai_item_saved), modifier = Modifier.uiName("AiResultCard.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "AiResultCard"))
            OutlinedButton(onClick = { expanded = !expanded }, modifier = Modifier.uiName("AiResultCard.OutlinedButton.1", "androidx.compose.material3.OutlinedButton", uiInspectionSource, "AiResultCard")) {
                Text(stringResource(if (expanded) Res.string.action_ai_hide_details else Res.string.action_ai_show_details), modifier = Modifier.uiName("AiResultCard.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "AiResultCard"))
            }
            if (expanded) {
                estimate.certainty?.let {
                    Text(
                        text =
                            stringResource(
                                Res.string.neutral_ai_certainty,
                                "${(it * 100).roundToInt()}%",
                            ),
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("AiResultCard.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "AiResultCard")
                    )
                }
                estimate.description?.let { Text(it, modifier = Modifier.uiName("AiResultCard.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "AiResultCard")) }
                estimate.servingCount?.let {
                    Text("${stringResource(Res.string.label_number_of_servings)}: ${it.formatClipZeros()}", modifier = Modifier.uiName("AiResultCard.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "AiResultCard"))
                }
                estimate.weightGrams?.let {
                    Text("${stringResource(Res.string.weight)}: ${it.formatClipZeros()} ${stringResource(Res.string.unit_gram_short)}", modifier = Modifier.uiName("AiResultCard.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "AiResultCard"))
                }
                KnownNutrientList(facts = estimate.toNutritionFacts(), modifier = Modifier.uiName("AiResultCard.KnownNutrientList.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.KnownNutrientList", uiInspectionSource, "AiResultCard"))
            }

            HorizontalDivider( modifier = Modifier.uiName("AiResultCard.HorizontalDivider.1", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "AiResultCard"))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.uiName("AiResultCard.Row.2", "androidx.compose.foundation.layout.Row", uiInspectionSource, "AiResultCard")) {
                OutlinedButton(onClick = onAlign, enabled = !saved, modifier = (Modifier.weight(1f)).uiName("AiResultCard.OutlinedButton.2", "androidx.compose.material3.OutlinedButton", uiInspectionSource, "AiResultCard")) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.uiName("AiResultCard.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "AiResultCard"))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.action_align), modifier = Modifier.uiName("AiResultCard.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "AiResultCard"))
                }
            }
        }
    }
}

@Composable
private fun AlignBottomSheet(
    query: String,
    viewModel: AiScanViewModel,
    onPickFood: (NutritionalItemId) -> Unit,
    onCreateCustomFood: () -> Unit,
    onDismiss: () -> Unit,
) {
    val results =
        remember(query) { viewModel.searchCustomFoods(query) }.collectAsLazyPagingItems()

    UiSourceScope(uiInspectionSource.element("AlignBottomSheet.ModalBottomSheet.1", "androidx.compose.material3.ModalBottomSheet", "AlignBottomSheet")) { ModalBottomSheet(onDismissRequest = onDismiss, modifier = Modifier.uiName("AlignBottomSheet.ModalBottomSheet.1.root", "androidx.compose.material3.ModalBottomSheet", uiInspectionSource, "AlignBottomSheet")) {
        Column(modifier = (Modifier.fillMaxWidth().padding(horizontal = 16.dp)).uiName("AlignBottomSheet.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "AlignBottomSheet")) {
            Text(
                text = stringResource(Res.string.headline_align_saved_food),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, modifier = Modifier.uiName("AlignBottomSheet.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "AlignBottomSheet")
            )
            Spacer(Modifier.height(8.dp))

            if (results.itemCount == 0) {
                Text(
                    text = stringResource(Res.string.neutral_ai_no_saved_food_matches),
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("AlignBottomSheet.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "AlignBottomSheet")
                )
            } else {
                LazyColumn(modifier = (Modifier.fillMaxWidth().height(280.dp)).uiName("AlignBottomSheet.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "AlignBottomSheet")) {
                    items(results.itemCount) { index ->
                        val item = results[index]
                        if (item != null) {
                            ListItem(
                                headlineContent = { Text(item.headline, modifier = Modifier.uiName("AlignBottomSheet.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "AlignBottomSheet")) },
                                modifier =
                                    (Modifier.fillMaxWidth().clickable { onPickFood(item.id) }).uiName("AlignBottomSheet.ListItem.1", "androidx.compose.material3.ListItem", uiInspectionSource, "AlignBottomSheet"),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onCreateCustomFood, modifier = (Modifier.fillMaxWidth()).uiName("AlignBottomSheet.OutlinedButton.1", "androidx.compose.material3.OutlinedButton", uiInspectionSource, "AlignBottomSheet")) {
                Text(stringResource(Res.string.action_create_custom_food), modifier = Modifier.uiName("AlignBottomSheet.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "AlignBottomSheet"))
            }
            Spacer(Modifier.height(16.dp))
        }
    } }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.diary.aiscan",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/aiscan/AiScanScreen.kt",
)
