package com.maksimowiczm.foodyou.app.ui.food.meals

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

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
        onUpdateOpenFoodFactsCredentials = onUpdateOpenFoodFactsCredentials, modifier = (modifier).uiName("MealsScreen.MealsApp.1", "com.maksimowiczm.foodyou.app.ui.food.meals.MealsApp", uiInspectionSource, "MealsScreen"),
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

    if (message != null) UiSourceScope(uiInspectionSource.element("MealsApp.AlertDialog.1", "androidx.compose.material3.AlertDialog", "MealsApp")) { AlertDialog(
        onDismissRequest = { viewModel.message.value = null },
        title = { Text(stringResource(Res.string.meals_library), modifier = Modifier.uiName("MealsApp.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) },
        text = { Text(stringResource(when (message) {
            MealMessage.Invalid -> Res.string.meals_invalid
            MealMessage.SaveFailed -> Res.string.meals_save_failed
            MealMessage.LoadFailed -> Res.string.meals_load_failed
            MealMessage.DeleteFailed -> Res.string.meals_delete_failed
            else -> Res.string.meals_unavailable
        }), modifier = Modifier.uiName("MealsApp.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) },
        confirmButton = { TextButton(onClick = { viewModel.message.value = null }, modifier = Modifier.uiName("MealsApp.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "MealsApp")) { Text(stringResource(Res.string.positive_ok), modifier = Modifier.uiName("MealsApp.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) } }, modifier = Modifier.uiName("MealsApp.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "MealsApp")
    ) }
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
        UiSourceScope(uiInspectionSource.element("MealsApp.AlertDialog.2", "androidx.compose.material3.AlertDialog", "MealsApp")) { AlertDialog(
            onDismissRequest = { if (!busy) loadTarget = null },
            title = { Text(stringResource(Res.string.action_load_meal), modifier = Modifier.uiName("MealsApp.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) },
            text = {
                Column (modifier = Modifier.uiName("MealsApp.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "MealsApp")) {
                    Text(target.name, modifier = Modifier.uiName("MealsApp.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp"))
                    ChipsDatePicker(dateState, modifier = Modifier.uiName("MealsApp.ChipsDatePicker.1", "com.maksimowiczm.foodyou.app.ui.food.diary.component.ChipsDatePicker", uiInspectionSource, "MealsApp"))
                    Text(stringResource(Res.string.meals_choose_slot), modifier = Modifier.uiName("MealsApp.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp"))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.uiName("MealsApp.FlowRow.1", "androidx.compose.foundation.layout.FlowRow", uiInspectionSource, "MealsApp")) {
                        slots.forEach { slot ->
                            FilterChip(selected = slotId == slot.id, onClick = { slotId = slot.id }, label = { Text(slot.name, modifier = Modifier.uiName("MealsApp.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) }, modifier = Modifier.uiName("MealsApp.FilterChip.1", "androidx.compose.material3.FilterChip", uiInspectionSource, "MealsApp"))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = !busy && slots.any { it.id == slotId }, onClick = {
                    slotId?.let { destination -> viewModel.load(target.id, destination, dateState.selectedDate) { loadTarget = null } }
                }, modifier = Modifier.uiName("MealsApp.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "MealsApp")) { Text(stringResource(Res.string.action_load_meal), modifier = Modifier.uiName("MealsApp.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) }
            },
            dismissButton = { TextButton(enabled = !busy, onClick = { loadTarget = null }, modifier = Modifier.uiName("MealsApp.TextButton.3", "androidx.compose.material3.TextButton", uiInspectionSource, "MealsApp")) { Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("MealsApp.Text.9", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) } }, modifier = Modifier.uiName("MealsApp.AlertDialog.2.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "MealsApp")
        ) }
    }
    val selected = selection
    val current = draft
    when {
        current != null && selected != null -> UiSourceScope(uiInspectionSource.element("MealsApp.MeasureIngredientScreen.1", "com.maksimowiczm.foodyou.app.ui.food.recipe.MeasureIngredientScreen", "MealsApp")) { MeasureIngredientScreen(
            onBack = { if (!busy) selection = null }, measurement = selected.second,
            viewModel = koinViewModel(key = "meal-portion-" + selected.first.toString()) { parametersOf(selected.first) },
            onSave = { measurement -> viewModel.capture(selected.first, measurement) { selection = null; search = false } },
            modifier = modifier,
        ) }
        current != null && search -> Scaffold(
            modifier = (modifier).uiName("MealsApp.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "MealsApp"),
            topBar = { TopAppBar(title = { Text(stringResource(Res.string.meals_add_item), modifier = Modifier.uiName("MealsApp.Text.10", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) },
                navigationIcon = { ArrowBackIconButton(onClick = { search = false }, modifier = Modifier.uiName("MealsApp.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "MealsApp")) }, modifier = Modifier.uiName("MealsApp.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "MealsApp")) },
        ) { padding ->
            FoodSearchApp(
                onFoodClick = { item, measurement -> selection = item.id to measurement },
                onUpdateUsdaApiKey = onUpdateUsdaApiKey,
                onUpdateOpenFoodFactsCredentials = onUpdateOpenFoodFactsCredentials,
                modifier = (Modifier.fillMaxSize().padding(padding).testTag("meal-item-search")).uiName("MealsApp.FoodSearchApp.1", "com.maksimowiczm.foodyou.app.ui.food.search.FoodSearchApp", uiInspectionSource, "MealsApp"),
            )
        }
        current != null && editingItem != null -> {
            val index = editingItem!!
            current.items.getOrNull(index)?.let { item ->
                MealItemEditor(item = item, onCancel = { editingItem = null }, onSave = {
                    viewModel.setItem(index, it); editingItem = null
                }, modifier = (modifier).uiName("MealsApp.MealItemEditor.1", "com.maksimowiczm.foodyou.app.ui.food.meals.MealItemEditor", uiInspectionSource, "MealsApp"))
            }
        }
        else -> Scaffold(
            modifier = (modifier).uiName("MealsApp.Scaffold.2", "androidx.compose.material3.Scaffold", uiInspectionSource, "MealsApp"),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(if (current == null) Res.string.meals_library else Res.string.meals_edit), modifier = Modifier.uiName("MealsApp.Text.11", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) },
                    navigationIcon = { ArrowBackIconButton(onClick = { if (current == null) onBack() else if (!busy) discard = true }, modifier = Modifier.uiName("MealsApp.ArrowBackIconButton.2", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "MealsApp")) },
                    actions = {
                        if (current != null) {
                            TextButton(onClick = { discard = true }, enabled = !busy, modifier = Modifier.uiName("MealsApp.TextButton.4", "androidx.compose.material3.TextButton", uiInspectionSource, "MealsApp")) { Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("MealsApp.Text.12", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) }
                            TextButton(onClick = viewModel::save, enabled = !busy, modifier = Modifier.uiName("MealsApp.TextButton.5", "androidx.compose.material3.TextButton", uiInspectionSource, "MealsApp")) { Text(stringResource(Res.string.action_save), modifier = Modifier.uiName("MealsApp.Text.13", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) }
                        }
                    }, modifier = Modifier.uiName("MealsApp.TopAppBar.2", "androidx.compose.material3.TopAppBar", uiInspectionSource, "MealsApp")
                )
            },
        ) { padding ->
            LazyColumn(
                modifier = (Modifier.fillMaxSize().testTag(if (current == null) "meals-library" else "meal-editor")).uiName("MealsApp.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "MealsApp"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (current != null && editorSnapshot != null) {
                    item {
                        OutlinedTextField(value = current.name, onValueChange = viewModel::rename,
                            label = { Text(stringResource(Res.string.label_meal_name), modifier = Modifier.uiName("MealsApp.Text.14", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) },
                            modifier = (Modifier.fillMaxWidth().testTag("meal-name")).uiName("MealsApp.OutlinedTextField.1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "MealsApp"), singleLine = true)
                    }
                    item { Button(onClick = { search = true }, enabled = !busy, modifier = Modifier.uiName("MealsApp.Button.1", "androidx.compose.material3.Button", uiInspectionSource, "MealsApp")) { Text(stringResource(Res.string.meals_add_item), modifier = Modifier.uiName("MealsApp.Text.15", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) } }
                    if(current.reportedNutrition!=null)item{CapturedMealPanel(current)}
                    itemsIndexed(current.items, key = { index, _ -> editorSnapshot.itemKeys[index] }) { index, item ->
                        OutlinedCard((Modifier.fillMaxWidth().testTag("meal-item-$index")).uiName("MealsApp.OutlinedCard.1", "androidx.compose.material3.OutlinedCard", uiInspectionSource, "MealsApp")) {
                            Column((Modifier.padding(12.dp)).uiName("MealsApp.Column.2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "MealsApp")) {
                                MealItemSummary(item)
                                Row (modifier = Modifier.uiName("MealsApp.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "MealsApp")) {
                                    TextButton(onClick = { editingItem = index }, enabled = !busy, modifier = Modifier.uiName("MealsApp.TextButton.6", "androidx.compose.material3.TextButton", uiInspectionSource, "MealsApp")) { Text(stringResource(Res.string.action_edit_entry), modifier = Modifier.uiName("MealsApp.Text.16", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) }
                                    TextButton(onClick = { viewModel.remove(index) }, enabled = !busy, modifier = Modifier.uiName("MealsApp.TextButton.7", "androidx.compose.material3.TextButton", uiInspectionSource, "MealsApp")) { Text(stringResource(Res.string.meals_remove_item), modifier = Modifier.uiName("MealsApp.Text.17", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) }
                                }
                            }
                        }
                    }
                } else if (readFailed) {
                    item { Text(stringResource(Res.string.meals_unavailable), modifier = Modifier.uiName("MealsApp.Text.18", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) }
                    item { TextButton(onClick = viewModel::retryRead, modifier = Modifier.uiName("MealsApp.TextButton.8", "androidx.compose.material3.TextButton", uiInspectionSource, "MealsApp")) { Text(stringResource(Res.string.meals_retry), modifier = Modifier.uiName("MealsApp.Text.19", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) } }
                } else if (meals == null) {
                    item { Text(stringResource(Res.string.meals_loading), modifier = Modifier.uiName("MealsApp.Text.20", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) }
                } else if (meals!!.isEmpty()) {
                    item { Text(stringResource(Res.string.meals_empty), style = MaterialTheme.typography.titleLarge, modifier = Modifier.uiName("MealsApp.Text.21", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) }
                    item { Text(stringResource(Res.string.meals_empty_help), modifier = Modifier.uiName("MealsApp.Text.22", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) }
                    item { TextButton(onClick = onBack, modifier = Modifier.uiName("MealsApp.TextButton.9", "androidx.compose.material3.TextButton", uiInspectionSource, "MealsApp")) { Text(stringResource(Res.string.meals_go_diary), modifier = Modifier.uiName("MealsApp.Text.23", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) } }
                } else {
                    items(meals!!, key = { it.id.value }) { meal ->
                        OutlinedCard((Modifier.fillMaxWidth().testTag("saved-meal-" + meal.id.value)).uiName("MealsApp.OutlinedCard.2", "androidx.compose.material3.OutlinedCard", uiInspectionSource, "MealsApp")) {
                            Column((Modifier.padding(12.dp)).uiName("MealsApp.Column.3", "androidx.compose.foundation.layout.Column", uiInspectionSource, "MealsApp"), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(meal.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.uiName("MealsApp.Text.24", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp"))
                                Text(stringResource(Res.string.neutral_meal_item_count, meal.itemCount), modifier = Modifier.uiName("MealsApp.Text.25", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp"))
                                meal.items.forEach { MealItemSummary(it) }
                                if(meal.reportedNutrition!=null)CapturedMealPanel(meal)
                                FlowRow (modifier = Modifier.uiName("MealsApp.FlowRow.2", "androidx.compose.foundation.layout.FlowRow", uiInspectionSource, "MealsApp")) {
                                    TextButton(onClick = { viewModel.edit(meal) }, enabled = !busy, modifier = Modifier.uiName("MealsApp.TextButton.10", "androidx.compose.material3.TextButton", uiInspectionSource, "MealsApp")) { Text(stringResource(Res.string.meals_edit), modifier = Modifier.uiName("MealsApp.Text.26", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) }
                                    TextButton(onClick = { loadTarget = meal }, enabled = !busy, modifier = Modifier.uiName("MealsApp.TextButton.11", "androidx.compose.material3.TextButton", uiInspectionSource, "MealsApp")) { Text(stringResource(Res.string.action_load_meal), modifier = Modifier.uiName("MealsApp.Text.27", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) }
                                    TextButton(onClick = { deleteTarget = meal }, enabled = !busy, modifier = Modifier.uiName("MealsApp.TextButton.12", "androidx.compose.material3.TextButton", uiInspectionSource, "MealsApp")) { Text(stringResource(Res.string.meals_delete), modifier = Modifier.uiName("MealsApp.Text.28", "androidx.compose.material3.Text", uiInspectionSource, "MealsApp")) }
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
    Text(item.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("MealItemSummary.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "MealItemSummary"))
    Text(item.capturedPortion?.stringResource() ?: stringResource(Res.string.meals_original_unknown), modifier = Modifier.uiName("MealItemSummary.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "MealItemSummary"))
    if(item.portionDescription!=null) {
        Text(stringResource(Res.string.captured_original_quantity), modifier = Modifier.uiName("MealItemSummary.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "MealItemSummary"))
        CapturedReferenceText(item.portionDescription)
    }
    CapturedMetadataView(item.metadata)
}

@Composable
private fun MealConfirmation(
    title: String, text: String, onCancel: () -> Unit, onConfirm: () -> Unit, busy: Boolean = false,
) {
    UiSourceScope(uiInspectionSource.element("MealConfirmation.AlertDialog.1", "androidx.compose.material3.AlertDialog", "MealConfirmation")) { AlertDialog(
        onDismissRequest = { if (!busy) onCancel() }, title = { Text(title, modifier = Modifier.uiName("MealConfirmation.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "MealConfirmation")) }, text = { Text(text, modifier = Modifier.uiName("MealConfirmation.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "MealConfirmation")) },
        confirmButton = { TextButton(enabled = !busy, onClick = onConfirm, modifier = Modifier.uiName("MealConfirmation.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "MealConfirmation")) { Text(stringResource(Res.string.positive_ok), modifier = Modifier.uiName("MealConfirmation.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "MealConfirmation")) } },
        dismissButton = { TextButton(enabled = !busy, onClick = onCancel, modifier = Modifier.uiName("MealConfirmation.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "MealConfirmation")) { Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("MealConfirmation.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "MealConfirmation")) } }, modifier = Modifier.uiName("MealConfirmation.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "MealConfirmation")
    ) }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.meals",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/meals/MealsScreen.kt",
)
