package com.maksimowiczm.foodyou.app.ui.food.meals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.utility.stringResource
import com.maksimowiczm.foodyou.app.ui.food.diary.component.ChipsDatePicker
import com.maksimowiczm.foodyou.app.ui.food.diary.component.rememberChipsDatePickerState
import com.maksimowiczm.foodyou.app.ui.food.recipe.MeasureIngredientScreen
import com.maksimowiczm.foodyou.app.ui.food.search.FoodSearchApp
import com.maksimowiczm.foodyou.app.ui.food.component.CapturedMealPanel
import com.maksimowiczm.foodyou.app.ui.food.component.CapturedReferenceText
import com.maksimowiczm.foodyou.app.ui.food.component.CapturedMetadataView
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.extension.now
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import foodyou.app.generated.resources.*
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MealsScreen(
    onBack: () -> Unit, epochDay: Long, onUpdateUsdaApiKey: () -> Unit,
    onUpdateOpenFoodFactsCredentials: () -> Unit, modifier: Modifier = Modifier,
) {
    MealsApp(
        viewModel = koinViewModel(), onBack = onBack, initialDate = LocalDate.fromEpochDays(epochDay),
        onUpdateUsdaApiKey = onUpdateUsdaApiKey,
        onUpdateOpenFoodFactsCredentials = onUpdateOpenFoodFactsCredentials, modifier = modifier,
    )
}

@Composable
internal fun MealsApp(
    viewModel: MealsViewModel, onBack: () -> Unit, initialDate: LocalDate,
    onUpdateUsdaApiKey: () -> Unit = {}, onUpdateOpenFoodFactsCredentials: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val meals by viewModel.meals.collectAsStateWithLifecycle()
    val editorSnapshot = viewModel.editor.collectAsStateWithLifecycle().value
    val draft = editorSnapshot?.meal
    val readFailed by viewModel.readFailed.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val slots by viewModel.diarySlots.collectAsStateWithLifecycle()
    var editingItem by remember { mutableStateOf<Int?>(null) }
    var search by remember { mutableStateOf(false) }
    var selection by remember { mutableStateOf<Pair<NutritionalItemId, Measurement>?>(null) }
    var deleteTarget by remember { mutableStateOf<Meal?>(null) }
    var loadTarget by remember { mutableStateOf<Meal?>(null) }
    var discard by remember { mutableStateOf(false) }

    androidx.navigationevent.compose.NavigationEventHandler(
        state = androidx.navigationevent.compose.rememberNavigationEventState(androidx.navigationevent.NavigationEventInfo.None),
        isBackEnabled = draft != null,
        onBackCompleted = {
            if (!busy) when {
                selection != null -> selection = null
                search -> search = false
                editingItem != null -> editingItem = null
                else -> discard = true
            }
        },
    )

    if (message != null) AlertDialog(
        onDismissRequest = { viewModel.message.value = null },
        title = { Text(stringResource(Res.string.meals_library)) },
        text = { Text(stringResource(when (message) {
            MealMessage.Invalid -> Res.string.meals_invalid
            MealMessage.SaveFailed -> Res.string.meals_save_failed
            MealMessage.LoadFailed -> Res.string.meals_load_failed
            MealMessage.DeleteFailed -> Res.string.meals_delete_failed
            else -> Res.string.meals_unavailable
        })) },
        confirmButton = { TextButton(onClick = { viewModel.message.value = null }) { Text(stringResource(Res.string.positive_ok)) } },
    )
    if (discard) MealConfirmation(
        title = stringResource(Res.string.meals_discard), text = stringResource(Res.string.meals_discard_help),
        onCancel = { discard = false },
        onConfirm = { discard = false; editingItem = null; search = false; selection = null; viewModel.cancel() },
    )
    deleteTarget?.let { target ->
        MealConfirmation(
            title = stringResource(Res.string.meals_delete), text = stringResource(Res.string.meals_delete_help),
            onCancel = { if (!busy) deleteTarget = null },
            onConfirm = { viewModel.delete(target.id) { deleteTarget = null } }, busy = busy,
        )
    }
    loadTarget?.let { target ->
        val dateState = rememberChipsDatePickerState(LocalDate.now(), listOf(initialDate), initialDate)
        var slotId by remember(target.id) { mutableStateOf<Long?>(null) }
        AlertDialog(
            onDismissRequest = { if (!busy) loadTarget = null },
            title = { Text(stringResource(Res.string.action_load_meal)) },
            text = {
                Column {
                    Text(target.name)
                    ChipsDatePicker(dateState)
                    Text(stringResource(Res.string.meals_choose_slot))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        slots.forEach { slot ->
                            FilterChip(selected = slotId == slot.id, onClick = { slotId = slot.id }, label = { Text(slot.name) })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = !busy && slots.any { it.id == slotId }, onClick = {
                    slotId?.let { destination -> viewModel.load(target.id, destination, dateState.selectedDate) { loadTarget = null } }
                }) { Text(stringResource(Res.string.action_load_meal)) }
            },
            dismissButton = { TextButton(enabled = !busy, onClick = { loadTarget = null }) { Text(stringResource(Res.string.action_cancel)) } },
        )
    }
    val selected = selection
    val current = draft
    when {
        current != null && selected != null -> MeasureIngredientScreen(
            onBack = { if (!busy) selection = null }, measurement = selected.second,
            viewModel = koinViewModel(key = "meal-portion-" + selected.first.toString()) { parametersOf(selected.first) },
            onSave = { measurement -> viewModel.capture(selected.first, measurement) { selection = null; search = false } },
            modifier = modifier,
        )
        current != null && search -> Scaffold(
            modifier = modifier,
            topBar = { TopAppBar(title = { Text(stringResource(Res.string.meals_add_item)) },
                navigationIcon = { ArrowBackIconButton(onClick = { search = false }) }) },
        ) { padding ->
            FoodSearchApp(
                onFoodClick = { item, measurement -> selection = item.id to measurement },
                onUpdateUsdaApiKey = onUpdateUsdaApiKey,
                onUpdateOpenFoodFactsCredentials = onUpdateOpenFoodFactsCredentials,
                modifier = Modifier.fillMaxSize().padding(padding).testTag("meal-item-search"),
            )
        }
        current != null && editingItem != null -> {
            val index = editingItem!!
            current.items.getOrNull(index)?.let { item ->
                MealItemEditor(item = item, onCancel = { editingItem = null }, onSave = {
                    viewModel.setItem(index, it); editingItem = null
                }, modifier = modifier)
            }
        }
        else -> Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(if (current == null) Res.string.meals_library else Res.string.meals_edit)) },
                    navigationIcon = { ArrowBackIconButton(onClick = { if (current == null) onBack() else if (!busy) discard = true }) },
                    actions = {
                        if (current != null) {
                            TextButton(onClick = { discard = true }, enabled = !busy) { Text(stringResource(Res.string.action_cancel)) }
                            TextButton(onClick = viewModel::save, enabled = !busy) { Text(stringResource(Res.string.action_save)) }
                        }
                    },
                )
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag(if (current == null) "meals-library" else "meal-editor"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (current != null && editorSnapshot != null) {
                    item {
                        OutlinedTextField(value = current.name, onValueChange = viewModel::rename,
                            label = { Text(stringResource(Res.string.label_meal_name)) },
                            modifier = Modifier.fillMaxWidth().testTag("meal-name"), singleLine = true)
                    }
                    item { Button(onClick = { search = true }, enabled = !busy) { Text(stringResource(Res.string.meals_add_item)) } }
                    if(current.reportedNutrition!=null)item{CapturedMealPanel(current)}
                    itemsIndexed(current.items, key = { index, _ -> editorSnapshot.itemKeys[index] }) { index, item ->
                        OutlinedCard(Modifier.fillMaxWidth().testTag("meal-item-$index")) {
                            Column(Modifier.padding(12.dp)) {
                                MealItemSummary(item)
                                Row {
                                    TextButton(onClick = { editingItem = index }, enabled = !busy) { Text(stringResource(Res.string.action_edit_entry)) }
                                    TextButton(onClick = { viewModel.remove(index) }, enabled = !busy) { Text(stringResource(Res.string.meals_remove_item)) }
                                }
                            }
                        }
                    }
                } else if (readFailed) {
                    item { Text(stringResource(Res.string.meals_unavailable)) }
                    item { TextButton(onClick = viewModel::retryRead) { Text(stringResource(Res.string.meals_retry)) } }
                } else if (meals == null) {
                    item { Text(stringResource(Res.string.meals_loading)) }
                } else if (meals!!.isEmpty()) {
                    item { Text(stringResource(Res.string.meals_empty), style = MaterialTheme.typography.titleLarge) }
                    item { Text(stringResource(Res.string.meals_empty_help)) }
                    item { TextButton(onClick = onBack) { Text(stringResource(Res.string.meals_go_diary)) } }
                } else {
                    items(meals!!, key = { it.id.value }) { meal ->
                        OutlinedCard(Modifier.fillMaxWidth().testTag("saved-meal-" + meal.id.value)) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(meal.name, style = MaterialTheme.typography.titleLarge)
                                Text(stringResource(Res.string.neutral_meal_item_count, meal.itemCount))
                                meal.items.forEach { MealItemSummary(it) }
                                if(meal.reportedNutrition!=null)CapturedMealPanel(meal)
                                FlowRow {
                                    TextButton(onClick = { viewModel.edit(meal) }, enabled = !busy) { Text(stringResource(Res.string.meals_edit)) }
                                    TextButton(onClick = { loadTarget = meal }, enabled = !busy) { Text(stringResource(Res.string.action_load_meal)) }
                                    TextButton(onClick = { deleteTarget = meal }, enabled = !busy) { Text(stringResource(Res.string.meals_delete)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MealItemSummary(item: MealItem) {
    Text(item.name, style = MaterialTheme.typography.titleMedium)
    Text(item.capturedPortion?.stringResource() ?: stringResource(Res.string.meals_original_unknown))
    if(item.portionDescription!=null) {
        Text(stringResource(Res.string.captured_original_quantity))
        CapturedReferenceText(item.portionDescription)
    }
    CapturedMetadataView(item.metadata)
}

@Composable
private fun MealConfirmation(
    title: String, text: String, onCancel: () -> Unit, onConfirm: () -> Unit, busy: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = { if (!busy) onCancel() }, title = { Text(title) }, text = { Text(text) },
        confirmButton = { TextButton(enabled = !busy, onClick = onConfirm) { Text(stringResource(Res.string.positive_ok)) } },
        dismissButton = { TextButton(enabled = !busy, onClick = onCancel) { Text(stringResource(Res.string.action_cancel)) } },
    )
}
