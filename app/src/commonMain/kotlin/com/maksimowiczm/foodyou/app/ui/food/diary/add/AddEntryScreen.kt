package com.maksimowiczm.foodyou.app.ui.food.diary.add


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CallSplit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.food.component.MeasurementPicker
import com.maksimowiczm.foodyou.app.ui.food.component.CapturedReferenceText
import com.maksimowiczm.foodyou.app.ui.food.component.CapturedMetadataView
import com.maksimowiczm.foodyou.app.ui.food.component.reportedBasisLabel
import com.maksimowiczm.foodyou.app.ui.food.component.CapturedNutritionSummary
import com.maksimowiczm.foodyou.app.ui.food.diary.component.ChipsDatePicker
import com.maksimowiczm.foodyou.app.ui.food.diary.component.ChipsDiarySlotPicker
import com.maksimowiczm.foodyou.app.ui.food.diary.component.FoodMeasurementFormState
import com.maksimowiczm.foodyou.app.ui.food.diary.component.Source
import com.maksimowiczm.foodyou.app.ui.food.diary.component.PortionSaveErrorDialog
import com.maksimowiczm.foodyou.app.ui.food.diary.component.rememberFoodMeasurementFormState
import com.maksimowiczm.foodyou.common.compose.extension.LaunchedCollectWithLifecycle
import com.maksimowiczm.foodyou.common.compose.extension.add
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.extension.minus
import com.maksimowiczm.foodyou.common.extension.plus
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemHistory
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import foodyou.app.generated.resources.*
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AddEntryScreen(
    onBack: () -> Unit,
    onEditFood: (NutritionalItemId) -> Unit,
    onEntryAdded: () -> Unit,
    onFoodDeleted: () -> Unit,
    onIngredient: (NutritionalItemId, Measurement) -> Unit,
    foodId: NutritionalItemId,
    diarySlotId: Long,
    date: LocalDate,
    measurement: Measurement?,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val viewModel: AddEntryViewModel = koinViewModel { parametersOf(foodId) }
    var saveFailed by rememberSaveable { mutableStateOf(false) }
    if (saveFailed) PortionSaveErrorDialog { saveFailed = false }

    LaunchedCollectWithLifecycle(viewModel.uiEvents) {
        when (it) {
            is AddEntryEvent.FoodDeleted -> onFoodDeleted()
            is AddEntryEvent.EntryAdded -> onEntryAdded()
            AddEntryEvent.SaveFailed -> saveFailed = true
        }
    }

    val food = viewModel.food.collectAsStateWithLifecycle().value
    val events by viewModel.foodHistory.collectAsStateWithLifecycle()
    val diarySlots = viewModel.diarySlots.collectAsStateWithLifecycle().value
    val today by viewModel.today.collectAsStateWithLifecycle()
    val suggestions = viewModel.suggestions.collectAsStateWithLifecycle().value
    val possibleTypes = viewModel.possibleMeasurementTypes.collectAsStateWithLifecycle().value
    val measurementSuggestion by viewModel.suggestedMeasurement.collectAsStateWithLifecycle()

    // This is stupid that it is here but it's going to be deleted in 4.0.0
    val selectedMeasurement =
        remember(measurement, measurementSuggestion) {
            val realMeasurement = measurement ?: measurementSuggestion
            if (realMeasurement == null) return@remember null
            realMeasurement
        }

    if (
        food == null ||
            diarySlots == null ||
            suggestions == null ||
            possibleTypes == null ||
            selectedMeasurement == null
    ) {
        // TODO loading state
    } else {
        val state =
            rememberFoodMeasurementFormState(
                today = today,
                possibleDates =
                    listOf(today.minus(1.days), today, today.plus(1.days), date)
                        .distinct()
                        .sorted(),
                selectedDate = date,
                diarySlots = diarySlots.map { it.name },
                selectedDiarySlot =
                    remember(diarySlots, diarySlotId) {
                            diarySlots.firstOrNull { it.id == diarySlotId } ?: diarySlots.firstOrNull()
                        }
                        ?.name,
                suggestions = suggestions,
                possibleTypes = possibleTypes,
                selectedMeasurement = selectedMeasurement,
            )

        UiSourceScope(uiInspectionSource.element("AddEntryScreen.AddEntryScreen.1", "com.maksimowiczm.foodyou.app.ui.food.diary.add.AddEntryScreen", "AddEntryScreen")) { AddEntryScreen(
            onBack = onBack,
            onAdd = {
                val selectedDiarySlotId =
                    state.diarySlotsState.selectedDiarySlot?.let { diarySlotName ->
                        diarySlots.firstOrNull { it.name == diarySlotName }?.id
                    }

                if (selectedDiarySlotId != null) {
                    viewModel.addEntry(
                        measurement = state.measurementState.measurement,
                        diarySlotId = selectedDiarySlotId,
                        date = state.dateState.selectedDate,
                    )
                }
            },
            onUnpack = {
                val selectedDiarySlotId =
                    state.diarySlotsState.selectedDiarySlot?.let { diarySlotName ->
                        diarySlots.firstOrNull { it.name == diarySlotName }?.id
                    }

                if (selectedDiarySlotId != null) {
                    viewModel.unpack(
                        measurement = state.measurementState.measurement,
                        diarySlotId = selectedDiarySlotId,
                        date = state.dateState.selectedDate,
                    )
                }
            },
            onEditFood = onEditFood,
            onDelete = viewModel::deleteFood,
            onIngredient = onIngredient,
            food = food,
            history = events,
            state = state,
            animatedVisibilityScope = animatedVisibilityScope,
            modifier = modifier,
        ) }
    }
}

@Composable
private fun AddEntryScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onUnpack: () -> Unit,
    onEditFood: (NutritionalItemId) -> Unit,
    onDelete: () -> Unit,
    onIngredient: (NutritionalItemId, Measurement) -> Unit,
    food: FoodModel,
    history: List<NutritionalItemHistory>,
    state: FoodMeasurementFormState,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val topBar =
        @Composable {
            MediumTopAppBar(
                title = { Text(food.name, modifier = Modifier.uiName("AddEntryScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "AddEntryScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("AddEntryScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "AddEntryScreen")) },
                actions = { Menu(onEdit = { onEditFood(food.foodId) }, onDelete = onDelete, modifier = Modifier.uiName("AddEntryScreen.Menu.1", "com.maksimowiczm.foodyou.app.ui.food.diary.add.Menu", uiInspectionSource, "AddEntryScreen")) },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("AddEntryScreen.MediumTopAppBar.1", "androidx.compose.material3.MediumTopAppBar", uiInspectionSource, "AddEntryScreen")
            )
        }

    val fab =
        @Composable {
            Column(
                modifier =
                    (Modifier.animateFloatingActionButton(
                        visible =
                            !animatedVisibilityScope.transition.isRunning &&
                                state.isValid &&
                                (food.nutritionFor(state.measurementState.measurement)!=null ||
                                    food is ProductModel && food.factBasis==com.maksimowiczm.foodyou.common.domain.food.ProductFactBasis.METRIC_100) &&
                                (food !is RecipeModel || food.isValid),
                        alignment = Alignment.BottomEnd,
                    )).uiName("AddEntryScreen.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "AddEntryScreen"),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (food.canUnpack) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (state.isValid) {
                                onUnpack()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.CallSplit,
                                contentDescription = stringResource(Res.string.action_unpack), modifier = Modifier.uiName("AddEntryScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "AddEntryScreen")
                            )
                        },
                        text = { Text(stringResource(Res.string.action_unpack), modifier = Modifier.uiName("AddEntryScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "AddEntryScreen")) },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.uiName("AddEntryScreen.ExtendedFloatingActionButton.1", "androidx.compose.material3.ExtendedFloatingActionButton", uiInspectionSource, "AddEntryScreen")
                    )
                }
                LargeExtendedFloatingActionButton(
                    onClick = {
                        if (state.isValid) {
                            onAdd()
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(Res.string.action_save),
                            modifier = (Modifier.size(FloatingActionButtonDefaults.LargeIconSize)).uiName("AddEntryScreen.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "AddEntryScreen"),
                        )
                    },
                    text = { Text(stringResource(Res.string.action_save), modifier = Modifier.uiName("AddEntryScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "AddEntryScreen")) }, modifier = Modifier.uiName("AddEntryScreen.LargeExtendedFloatingActionButton.1", "androidx.compose.material3.LargeExtendedFloatingActionButton", uiInspectionSource, "AddEntryScreen")
                )
            }
        }

    Scaffold(modifier = (modifier).uiName("AddEntryScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "AddEntryScreen"), topBar = topBar, floatingActionButton = fab) { paddingValues ->
        LazyColumn(
            modifier =
                (Modifier.fillMaxSize()
                    .imePadding()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("AddEntryScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "AddEntryScreen"),
            contentPadding =
                paddingValues.add(vertical = 8.dp).let {
                    if (food.canUnpack) {
                        it.add(bottom = 8.dp + 56.dp + 8.dp + 80.dp + 24.dp) // Double FAB
                    } else {
                        it.add(bottom = 80.dp + 24.dp) // FAB
                    }
                },
        ) {
            item { HorizontalDivider((Modifier.padding(horizontal = 8.dp)).uiName("AddEntryScreen.HorizontalDivider.1", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "AddEntryScreen")) }

            if(food is ProductModel && food.factBasis==com.maksimowiczm.foodyou.common.domain.food.ProductFactBasis.REFERENCE_SERVING) {
                item { Column((Modifier.padding(16.dp)).uiName("AddEntryScreen.Column.2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "AddEntryScreen"),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(Res.string.captured_reference_serving),style=MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("AddEntryScreen.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "AddEntryScreen"))
                    CapturedReferenceText(food.referencePortion)
                    CapturedMetadataView(food.nutritionMetadata)
                } }
            }
            if(food is RecipeModel && food.reportedNutrition!=null) {
                item { Column((Modifier.padding(16.dp)).uiName("AddEntryScreen.Column.3", "androidx.compose.foundation.layout.Column", uiInspectionSource, "AddEntryScreen"),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Text(reportedBasisLabel(food.reportedNutrition.basis),style=MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("AddEntryScreen.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "AddEntryScreen"))
                    var showCapturedFacts by remember(food.foodId) { mutableStateOf(false) }
                    TextButton(onClick={showCapturedFacts=!showCapturedFacts}, modifier = Modifier.uiName("AddEntryScreen.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "AddEntryScreen")) {
                        Text(stringResource(if(showCapturedFacts)Res.string.captured_hide_details else Res.string.captured_show_details), modifier = Modifier.uiName("AddEntryScreen.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "AddEntryScreen"))
                    }
                    if(showCapturedFacts)CapturedNutritionSummary(food.reportedNutrition.facts)
                    if(food.knownYield==null)Text(stringResource(Res.string.captured_unknown_yield), modifier = Modifier.uiName("AddEntryScreen.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "AddEntryScreen"))
                    else Text(stringResource(Res.string.captured_known_yield,food.knownYield), modifier = Modifier.uiName("AddEntryScreen.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "AddEntryScreen"))
                    CapturedReferenceText(food.reportedNutrition.reference)
                    if(food.reportedNutrition.needsReview)Text(stringResource(Res.string.captured_needs_review),color=MaterialTheme.colorScheme.error, modifier = Modifier.uiName("AddEntryScreen.Text.9", "androidx.compose.material3.Text", uiInspectionSource, "AddEntryScreen"))
                    Text(stringResource(Res.string.captured_unpack_unavailable), modifier = Modifier.uiName("AddEntryScreen.Text.10", "androidx.compose.material3.Text", uiInspectionSource, "AddEntryScreen"))
                    CapturedMetadataView(food.reportedNutrition.definitionMetadata,true)
                    CapturedMetadataView(food.reportedNutrition.metadata)
                    Text(stringResource(Res.string.captured_components),style=MaterialTheme.typography.titleMedium, modifier = Modifier.uiName("AddEntryScreen.Text.11", "androidx.compose.material3.Text", uiInspectionSource, "AddEntryScreen"))
                    food.reportedComponents.forEach { component ->
                        Text(component.name,style=MaterialTheme.typography.titleSmall, modifier = Modifier.uiName("AddEntryScreen.Text.12", "androidx.compose.material3.Text", uiInspectionSource, "AddEntryScreen"))
                        CapturedReferenceText(component.portion)
                        CapturedMetadataView(component.metadata)
                    }
                } }
            }

            item {
                ChipsDatePicker(state = state.dateState, modifier = (Modifier.padding(8.dp)).uiName("AddEntryScreen.ChipsDatePicker.1", "com.maksimowiczm.foodyou.app.ui.food.diary.component.ChipsDatePicker", uiInspectionSource, "AddEntryScreen"))
                HorizontalDivider((Modifier.padding(horizontal = 8.dp)).uiName("AddEntryScreen.HorizontalDivider.2", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "AddEntryScreen"))
                ChipsDiarySlotPicker(state = state.diarySlotsState, modifier = (Modifier.padding(8.dp)).uiName("AddEntryScreen.ChipsDiarySlotPicker.1", "com.maksimowiczm.foodyou.app.ui.food.diary.component.ChipsDiarySlotPicker", uiInspectionSource, "AddEntryScreen"))
                HorizontalDivider((Modifier.padding(horizontal = 8.dp)).uiName("AddEntryScreen.HorizontalDivider.3", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "AddEntryScreen"))
                Text(stringResource(Res.string.portion_amount_to_log), modifier = (Modifier.padding(8.dp)).uiName("AddEntryScreen.Text.13", "androidx.compose.material3.Text", uiInspectionSource, "AddEntryScreen"))
                MeasurementPicker(state = state.measurementState, modifier = (Modifier.padding(8.dp)).uiName("AddEntryScreen.MeasurementPicker.1", "com.maksimowiczm.foodyou.app.ui.food.component.MeasurementPicker", uiInspectionSource, "AddEntryScreen"))
            }

            if (food is RecipeModel && food.reportedNutrition==null) {
                item {
                    // This is stupid that it is here but it's going to be deleted in 4.0.0
                    val ingredients =
                        remember(state.measurementState.measurement, food) {
                            food.unpack(state.measurementState.measurement)
                        }

                    HorizontalDivider((Modifier.padding(horizontal = 8.dp)).uiName("AddEntryScreen.HorizontalDivider.4", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "AddEntryScreen"))
                    Ingredients(
                        ingredients = ingredients,
                        onIngredient = onIngredient,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), modifier = Modifier.uiName("AddEntryScreen.Ingredients.1", "com.maksimowiczm.foodyou.app.ui.food.diary.add.Ingredients", uiInspectionSource, "AddEntryScreen")
                    )
                }
            }

            item {
                HorizontalDivider((Modifier.padding(horizontal = 8.dp)).uiName("AddEntryScreen.HorizontalDivider.5", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "AddEntryScreen"))
                NutrientList(
                    food = food,
                    measurement = state.measurementState.measurement,
                    onEditFood = onEditFood,
                    modifier = (Modifier.padding(horizontal = 8.dp)).uiName("AddEntryScreen.NutrientList.1", "com.maksimowiczm.foodyou.app.ui.food.diary.add.NutrientList", uiInspectionSource, "AddEntryScreen"),
                )
            }

            val note = food.note
            if (note != null) {
                item {
                    HorizontalDivider((Modifier.padding(horizontal = 8.dp)).uiName("AddEntryScreen.HorizontalDivider.6", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "AddEntryScreen"))
                    Column((Modifier.padding(16.dp)).uiName("AddEntryScreen.Column.4", "androidx.compose.foundation.layout.Column", uiInspectionSource, "AddEntryScreen")) {
                        Text(
                            text = stringResource(Res.string.headline_note),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary, modifier = Modifier.uiName("AddEntryScreen.Text.14", "androidx.compose.material3.Text", uiInspectionSource, "AddEntryScreen")
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(text = note, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("AddEntryScreen.Text.15", "androidx.compose.material3.Text", uiInspectionSource, "AddEntryScreen"))
                    }
                }
            }

            if (food is ProductModel) {
                item {
                    HorizontalDivider((Modifier.padding(horizontal = 8.dp)).uiName("AddEntryScreen.HorizontalDivider.7", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "AddEntryScreen"))
                    Column((Modifier.padding(16.dp)).uiName("AddEntryScreen.Column.5", "androidx.compose.foundation.layout.Column", uiInspectionSource, "AddEntryScreen")) {
                        Text(
                            text = stringResource(Res.string.headline_source),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary, modifier = Modifier.uiName("AddEntryScreen.Text.16", "androidx.compose.material3.Text", uiInspectionSource, "AddEntryScreen")
                        )
                        Spacer(Modifier.height(8.dp))
                        Source(food.source, modifier = Modifier.uiName("AddEntryScreen.Source.1", "com.maksimowiczm.foodyou.app.ui.food.diary.component.Source", uiInspectionSource, "AddEntryScreen"))
                    }
                }
            }

            if (history.isNotEmpty()) {
                item {
                    HorizontalDivider((Modifier.padding(horizontal = 8.dp)).uiName("AddEntryScreen.HorizontalDivider.8", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "AddEntryScreen"))
                    NutritionalItemHistory(events = history, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
private fun Menu(onEdit: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteDialog(onDismissRequest = { showDeleteDialog = false }, onDelete = onDelete, modifier = Modifier.uiName("Menu.DeleteDialog.1", "com.maksimowiczm.foodyou.app.ui.food.diary.add.DeleteDialog", uiInspectionSource, "Menu"))
    }

    Box((modifier).uiName("Menu.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "Menu")) {
        IconButton(onClick = { expanded = true }, modifier = Modifier.uiName("Menu.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "Menu")) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(Res.string.action_show_more), modifier = Modifier.uiName("Menu.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "Menu")
            )
        }
        UiSourceScope(uiInspectionSource.element("Menu.DropdownMenu.1", "androidx.compose.material3.DropdownMenu", "Menu")) { DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.uiName("Menu.DropdownMenu.1.root", "androidx.compose.material3.DropdownMenu", uiInspectionSource, "Menu")) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.action_edit), modifier = Modifier.uiName("Menu.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Menu")) },
                onClick = {
                    expanded = false
                    onEdit()
                }, modifier = Modifier.uiName("Menu.DropdownMenuItem.1", "androidx.compose.material3.DropdownMenuItem", uiInspectionSource, "Menu")
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.action_delete), modifier = Modifier.uiName("Menu.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "Menu")) },
                onClick = {
                    expanded = false
                    showDeleteDialog = true
                }, modifier = Modifier.uiName("Menu.DropdownMenuItem.2", "androidx.compose.material3.DropdownMenuItem", uiInspectionSource, "Menu")
            )
        } }
    }
}

@Composable
private fun DeleteDialog(
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    UiSourceScope(uiInspectionSource.element("app.ui.food.diary.add.DeleteDialog.AlertDialog.1", "androidx.compose.material3.AlertDialog", "DeleteDialog")) { AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDelete, modifier = Modifier.uiName("app.ui.food.diary.add.DeleteDialog.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "DeleteDialog")) { Text(stringResource(Res.string.action_delete), modifier = Modifier.uiName("app.ui.food.diary.add.DeleteDialog.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DeleteDialog")) }
        },
        modifier = (modifier).uiName("app.ui.food.diary.add.DeleteDialog.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "DeleteDialog"),
        dismissButton = {
            TextButton(onClick = onDismissRequest, modifier = Modifier.uiName("app.ui.food.diary.add.DeleteDialog.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "DeleteDialog")) {
                Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("app.ui.food.diary.add.DeleteDialog.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DeleteDialog"))
            }
        },
        icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.uiName("app.ui.food.diary.add.DeleteDialog.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DeleteDialog")) },
        title = { Text(stringResource(Res.string.headline_delete_food), modifier = Modifier.uiName("app.ui.food.diary.add.DeleteDialog.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "DeleteDialog")) },
        text = { Text(stringResource(Res.string.description_delete_food), modifier = Modifier.uiName("app.ui.food.diary.add.DeleteDialog.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "DeleteDialog")) },
    ) }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.diary.add",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/add/AddEntryScreen.kt",
)
