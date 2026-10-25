package com.maksimowiczm.foodyou.app.ui.food.diary.add

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

        AddEntryScreen(
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
        )
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
                title = { Text(food.name) },
                navigationIcon = { ArrowBackIconButton(onBack) },
                actions = { Menu(onEdit = { onEditFood(food.foodId) }, onDelete = onDelete) },
                scrollBehavior = scrollBehavior,
            )
        }

    val fab =
        @Composable {
            Column(
                modifier =
                    Modifier.animateFloatingActionButton(
                        visible =
                            !animatedVisibilityScope.transition.isRunning &&
                                state.isValid &&
                                (food.nutritionFor(state.measurementState.measurement)!=null ||
                                    food is ProductModel && food.factBasis==com.maksimowiczm.foodyou.common.domain.food.ProductFactBasis.METRIC_100) &&
                                (food !is RecipeModel || food.isValid),
                        alignment = Alignment.BottomEnd,
                    ),
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
                                contentDescription = stringResource(Res.string.action_unpack),
                            )
                        },
                        text = { Text(stringResource(Res.string.action_unpack)) },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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
                            modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize),
                        )
                    },
                    text = { Text(stringResource(Res.string.action_save)) },
                )
            }
        }

    Scaffold(modifier = modifier, topBar = topBar, floatingActionButton = fab) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .imePadding()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding =
                paddingValues.add(vertical = 8.dp).let {
                    if (food.canUnpack) {
                        it.add(bottom = 8.dp + 56.dp + 8.dp + 80.dp + 24.dp) // Double FAB
                    } else {
                        it.add(bottom = 80.dp + 24.dp) // FAB
                    }
                },
        ) {
            item { HorizontalDivider(Modifier.padding(horizontal = 8.dp)) }

            if(food is ProductModel && food.factBasis==com.maksimowiczm.foodyou.common.domain.food.ProductFactBasis.REFERENCE_SERVING) {
                item { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(Res.string.captured_reference_serving),style=MaterialTheme.typography.titleMedium)
                    CapturedReferenceText(food.referencePortion)
                    CapturedMetadataView(food.nutritionMetadata)
                } }
            }
            if(food is RecipeModel && food.reportedNutrition!=null) {
                item { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Text(reportedBasisLabel(food.reportedNutrition.basis),style=MaterialTheme.typography.titleMedium)
                    var showCapturedFacts by remember(food.foodId) { mutableStateOf(false) }
                    TextButton(onClick={showCapturedFacts=!showCapturedFacts}) {
                        Text(stringResource(if(showCapturedFacts)Res.string.captured_hide_details else Res.string.captured_show_details))
                    }
                    if(showCapturedFacts)CapturedNutritionSummary(food.reportedNutrition.facts)
                    if(food.knownYield==null)Text(stringResource(Res.string.captured_unknown_yield))
                    else Text(stringResource(Res.string.captured_known_yield,food.knownYield))
                    CapturedReferenceText(food.reportedNutrition.reference)
                    if(food.reportedNutrition.needsReview)Text(stringResource(Res.string.captured_needs_review),color=MaterialTheme.colorScheme.error)
                    Text(stringResource(Res.string.captured_unpack_unavailable))
                    CapturedMetadataView(food.reportedNutrition.definitionMetadata,true)
                    CapturedMetadataView(food.reportedNutrition.metadata)
                    Text(stringResource(Res.string.captured_components),style=MaterialTheme.typography.titleMedium)
                    food.reportedComponents.forEach { component ->
                        Text(component.name,style=MaterialTheme.typography.titleSmall)
                        CapturedReferenceText(component.portion)
                        CapturedMetadataView(component.metadata)
                    }
                } }
            }

            item {
                ChipsDatePicker(state = state.dateState, modifier = Modifier.padding(8.dp))
                HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                ChipsDiarySlotPicker(state = state.diarySlotsState, modifier = Modifier.padding(8.dp))
                HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                Text(stringResource(Res.string.portion_amount_to_log), modifier = Modifier.padding(8.dp))
                MeasurementPicker(state = state.measurementState, modifier = Modifier.padding(8.dp))
            }

            if (food is RecipeModel && food.reportedNutrition==null) {
                item {
                    // This is stupid that it is here but it's going to be deleted in 4.0.0
                    val ingredients =
                        remember(state.measurementState.measurement, food) {
                            food.unpack(state.measurementState.measurement)
                        }

                    HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                    Ingredients(
                        ingredients = ingredients,
                        onIngredient = onIngredient,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            item {
                HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                NutrientList(
                    food = food,
                    measurement = state.measurementState.measurement,
                    onEditFood = onEditFood,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }

            val note = food.note
            if (note != null) {
                item {
                    HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(Res.string.headline_note),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(text = note, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (food is ProductModel) {
                item {
                    HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(Res.string.headline_source),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Source(food.source)
                    }
                }
            }

            if (history.isNotEmpty()) {
                item {
                    HorizontalDivider(Modifier.padding(horizontal = 8.dp))
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
        DeleteDialog(onDismissRequest = { showDeleteDialog = false }, onDelete = onDelete)
    }

    Box(modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(Res.string.action_show_more),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.action_edit)) },
                onClick = {
                    expanded = false
                    onEdit()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.action_delete)) },
                onClick = {
                    expanded = false
                    showDeleteDialog = true
                },
            )
        }
    }
}

@Composable
private fun DeleteDialog(
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDelete) { Text(stringResource(Res.string.action_delete)) }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
        icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null) },
        title = { Text(stringResource(Res.string.headline_delete_food)) },
        text = { Text(stringResource(Res.string.description_delete_food)) },
    )
}
