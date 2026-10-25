package com.maksimowiczm.foodyou.app.ui.food.diary.update

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.maksimowiczm.foodyou.app.ui.food.component.CapturedNutritionSummary
import com.maksimowiczm.foodyou.app.ui.food.component.reportedBasisLabel
import com.maksimowiczm.foodyou.common.domain.food.ProductFactBasis
import com.maksimowiczm.foodyou.app.ui.food.diary.component.ChipsDatePicker
import com.maksimowiczm.foodyou.app.ui.food.diary.component.ChipsDiarySlotPicker
import com.maksimowiczm.foodyou.app.ui.food.diary.component.FoodMeasurementFormState
import com.maksimowiczm.foodyou.app.ui.food.diary.component.Source
import com.maksimowiczm.foodyou.app.ui.food.diary.component.PortionSaveErrorDialog
import com.maksimowiczm.foodyou.app.ui.food.diary.component.rememberFoodMeasurementFormState
import com.maksimowiczm.foodyou.common.compose.extension.LaunchedCollectWithLifecycle
import com.maksimowiczm.foodyou.common.compose.extension.add
import com.maksimowiczm.foodyou.common.extension.minus
import com.maksimowiczm.foodyou.common.extension.plus
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ProductSnapshot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.RecipeSnapshot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemDiaryEntry
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemDiaryEntryId
import foodyou.app.generated.resources.*
import kotlin.time.Duration.Companion.days
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun UpdateEntryScreen(
    entryId: Long,
    onBack: () -> Unit,
    onSave: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val viewModel: UpdateNutritionalItemDiaryEntryViewModel = koinViewModel {
        parametersOf(NutritionalItemDiaryEntryId(entryId))
    }

    var saveFailed by rememberSaveable { mutableStateOf(false) }
    if (saveFailed) PortionSaveErrorDialog { saveFailed = false }
    LaunchedCollectWithLifecycle(viewModel.uiEvents) {
        when (it) {
            is UpdateEntryEvent.Saved -> onSave()
            UpdateEntryEvent.SaveFailed -> saveFailed = true
        }
    }

    val diarySlots by viewModel.diarySlots.collectAsStateWithLifecycle()
    val entry = viewModel.entry.collectAsStateWithLifecycle().value
    val possibleTypes = viewModel.possibleMeasurementTypes.collectAsStateWithLifecycle().value
    val suggestions = viewModel.suggestions.collectAsStateWithLifecycle().value
    val today by viewModel.today.collectAsStateWithLifecycle()

    if (entry == null || suggestions == null || possibleTypes == null) {
        // TODO loading state
    } else {

        val state =
            rememberFoodMeasurementFormState(
                today = today,
                possibleDates =
                    listOf(today.minus(1.days), today, today.plus(1.days), entry.date)
                        .distinct()
                        .sorted(),
                selectedDate = entry.date,
                diarySlots = remember(diarySlots) { diarySlots.map { it.name } },
                selectedDiarySlot =
                    remember(diarySlots, entry) { diarySlots.firstOrNull { it.id == entry.diarySlotId }?.name },
                suggestions = suggestions,
                possibleTypes = possibleTypes,
                selectedMeasurement = entry.measurement,
            )

        UpdateEntryScreen(
            onBack = onBack,
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
            onSave = {
                val selectedDiarySlotId =
                    state.diarySlotsState.selectedDiarySlot?.let { diarySlotName ->
                        diarySlots.firstOrNull { it.name == diarySlotName }?.id
                    }

                if (selectedDiarySlotId != null) {
                    viewModel.save(
                        measurement = state.measurementState.measurement,
                        diarySlotId = selectedDiarySlotId,
                        date = state.dateState.selectedDate,
                    )
                }
            },
            state = state,
            entry = entry,
            animatedVisibilityScope = animatedVisibilityScope,
            modifier = modifier,
        )
    }
}

@Composable
private fun UpdateEntryScreen(
    onBack: () -> Unit,
    onUnpack: () -> Unit,
    onSave: () -> Unit,
    state: FoodMeasurementFormState,
    entry: NutritionalItemDiaryEntry,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val canUnpack = entry.food is RecipeSnapshot && entry.food.reportedNutrition == null && entry.food.calculationVersion in 0..1
    val topBar =
        @Composable {
            MediumTopAppBar(
                title = { Text(entry.food.name) },
                navigationIcon = { ArrowBackIconButton(onBack) },
                scrollBehavior = scrollBehavior,
            )
        }
    val fab =
        @Composable {
            Column(
                modifier =
                    Modifier.animateFloatingActionButton(
                        visible = !animatedVisibilityScope.transition.isRunning && state.isValid && entry.food.nutritionFor(state.measurementState.measurement) != null,
                        alignment = Alignment.BottomEnd,
                    ),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (canUnpack) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (state.isValid) {
                                onUnpack()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.CallSplit,
                                contentDescription = null,
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
                            onSave()
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
                    if (canUnpack) {
                        it.add(bottom = 8.dp + 56.dp + 8.dp + 80.dp + 24.dp) // Double FAB
                    } else {
                        it.add(bottom = 80.dp + 24.dp) // FAB
                    }
                },
        ) {
            item { HorizontalDivider(Modifier.padding(horizontal = 8.dp)) }

            item {
                ChipsDatePicker(state = state.dateState, modifier = Modifier.padding(8.dp))
                HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                ChipsDiarySlotPicker(state = state.diarySlotsState, modifier = Modifier.padding(8.dp))
                HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                Text(stringResource(Res.string.portion_amount_to_log), modifier = Modifier.padding(8.dp))
                MeasurementPicker(state = state.measurementState, modifier = Modifier.padding(8.dp))
            }

            val food = entry.food
            if(food is ProductSnapshot && food.factBasis==ProductFactBasis.REFERENCE_SERVING) {
                item { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(Res.string.captured_reference_serving),style=MaterialTheme.typography.titleMedium)
                    CapturedReferenceText(food.referencePortion)
                    CapturedMetadataView(food.nutritionMetadata)
                } }
            }
            if(food is RecipeSnapshot && food.reportedNutrition!=null) {
                item { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Text(reportedBasisLabel(food.reportedNutrition.basis),style=MaterialTheme.typography.titleMedium)
                    Text(stringResource(Res.string.captured_unpack_unavailable))
                    CapturedReferenceText(food.reportedNutrition.reference)
                    var showFacts by remember(food) { mutableStateOf(false) }
                    TextButton(onClick={showFacts=!showFacts}) { Text(stringResource(if(showFacts)Res.string.captured_hide_details else Res.string.captured_show_details)) }
                    if(showFacts)CapturedNutritionSummary(food.reportedNutrition.facts)
                    CapturedMetadataView(food.reportedNutrition.metadata)
                    CapturedMetadataView(food.reportedNutrition.definitionMetadata,true)
                    food.reportedComponents.forEach { component ->
                        Text(component.name);CapturedReferenceText(component.portion);CapturedMetadataView(component.metadata)
                    }
                } }
            }
            if (food is RecipeSnapshot && canUnpack) {
                item {
                    val measurement = state.measurementState.measurement
                    val ingredients = food.unpack(measurement).orEmpty()

                    HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                    Ingredients(
                        ingredients = ingredients,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            item {
                HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                NutrientList(
                    food = food,
                    measurement = state.measurementState.measurement,
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

            if (food is ProductSnapshot) {
                item {
                    HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                    Column(Modifier.padding(8.dp)) {
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
        }
    }
}
