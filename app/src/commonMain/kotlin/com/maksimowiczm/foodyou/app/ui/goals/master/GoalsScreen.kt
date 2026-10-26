package com.maksimowiczm.foodyou.app.ui.goals.master


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.IncompleteFoodsList
import com.maksimowiczm.foodyou.app.ui.common.theme.LocalNutrientsPalette
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalNutrientsOrder
import com.maksimowiczm.foodyou.common.compose.utility.LocalDateFormatter
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.food.NutritionFactsField
import com.maksimowiczm.foodyou.common.domain.food.get
import com.maksimowiczm.foodyou.common.domain.food.sum
import com.maksimowiczm.foodyou.goals.domain.entity.DailyGoal
import com.maksimowiczm.foodyou.settings.domain.entity.NutrientsOrder
import foodyou.app.generated.resources.*
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GoalsScreen(onBack: () -> Unit, epochDay: Long, modifier: Modifier = Modifier) {
    val date = LocalDate.fromEpochDays(epochDay)
    val viewModel: GoalsViewModel = koinViewModel()
    val dateFormatter = LocalDateFormatter.current
    val failedDates by viewModel.failedDates.collectAsStateWithLifecycle()

    val screenState = rememberGoalsScreenState(selectedDate = date)

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    if (showDatePicker) {
        CalendarCardDatePickerDialog(
            goalsState = screenState,
            onDismissRequest = { showDatePicker = false },
        )
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = (modifier).uiName("GoalsScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "GoalsScreen"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_summary), modifier = Modifier.uiName("GoalsScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "GoalsScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("GoalsScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "GoalsScreen")) },
                actions = {
                    IconButton(onClick = { showDatePicker = true }, modifier = Modifier.uiName("GoalsScreen.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "GoalsScreen")) {
                        Icon(imageVector = Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.uiName("GoalsScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "GoalsScreen"))
                    }
                },
                subtitle = { Text(dateFormatter.formatDate(screenState.selectedDate), modifier = Modifier.uiName("GoalsScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "GoalsScreen")) },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("GoalsScreen.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "GoalsScreen")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = (Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("GoalsScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "GoalsScreen"),
            contentPadding = paddingValues,
        ) {
            item {
                HorizontalPager(
                    state = screenState.pagerState,
                    beyondViewportPageCount = 3,
                    verticalAlignment = Alignment.Top, modifier = Modifier.uiName("GoalsScreen.HorizontalPager.1", "androidx.compose.foundation.pager.HorizontalPager", uiInspectionSource, "GoalsScreen")
                ) { page ->
                    val date = screenState.dateForPage(page)
                    val uiState =
                        viewModel.observeUiStateByDate(date).collectAsStateWithLifecycle().value

                    if (date in failedDates) {
                        Column((Modifier.padding(16.dp)).uiName("GoalsScreen.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "GoalsScreen")) {
                            Text(stringResource(Res.string.meals_diary_unavailable), modifier = Modifier.uiName("GoalsScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "GoalsScreen"))
                            TextButton(onClick = { viewModel.retryRead(date) }, modifier = Modifier.uiName("GoalsScreen.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "GoalsScreen")) { Text(stringResource(Res.string.meals_retry), modifier = Modifier.uiName("GoalsScreen.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "GoalsScreen")) }
                        }
                    } else if (uiState == null) {
                        Box(
                            modifier = (Modifier.fillParentMaxSize()).uiName("GoalsScreen.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "GoalsScreen"),
                            contentAlignment = Alignment.Center,
                        ) {
                            ContainedLoadingIndicator( modifier = Modifier.uiName("GoalsScreen.ContainedLoadingIndicator.1", "androidx.compose.material3.ContainedLoadingIndicator", uiInspectionSource, "GoalsScreen"))
                        }
                    } else {
                        GoalsPage(uiState = uiState, modifier = Modifier.uiName("GoalsScreen.GoalsPage.1", "com.maksimowiczm.foodyou.app.ui.goals.master.GoalsPage", uiInspectionSource, "GoalsScreen"))
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalsPage(uiState: GoalsScreenUiState, modifier: Modifier = Modifier) {
    val diarySlots = uiState.diarySlots
    val goals = uiState.goal

    var selectedDiarySlotsIds by rememberSaveable(diarySlots) { mutableStateOf(diarySlots.map { it.id }) }

    val filteredDiarySlots =
        remember(diarySlots, selectedDiarySlotsIds) { diarySlots.filter { it.id in selectedDiarySlotsIds } }
    val nutritionFacts = remember(filteredDiarySlots) { filteredDiarySlots.map { it.nutritionFacts }.sum() }

    Column((modifier).uiName("GoalsPage.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "GoalsPage")) {
        DiarySlotsFilter(
            diarySlots = diarySlots,
            selectedDiarySlotsIds = selectedDiarySlotsIds,
            onSelectedDiarySlotsIdsChange = { selectedDiarySlotsIds = it },
            modifier = (Modifier.fillMaxWidth().padding(horizontal = 16.dp)).uiName("GoalsPage.DiarySlotsFilter.1", "com.maksimowiczm.foodyou.app.ui.goals.master.DiarySlotsFilter", uiInspectionSource, "GoalsPage"),
        )
        NutrientList(
            nutritionFacts = nutritionFacts,
            goals = goals,
            modifier = (Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)).uiName("GoalsPage.NutrientList.1", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientList", uiInspectionSource, "GoalsPage"),
        )
        if (diarySlots.incompleteFoods.isNotEmpty()) {
            IncompleteFoodsList(
                foods = diarySlots.incompleteFoods,
                modifier = (Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)).uiName("GoalsPage.IncompleteFoodsList.1", "com.maksimowiczm.foodyou.app.ui.common.component.IncompleteFoodsList", uiInspectionSource, "GoalsPage"),
            )
        }
    }
}

@Composable
private fun DiarySlotsFilter(
    diarySlots: List<DiarySlotModel>,
    selectedDiarySlotsIds: List<Long>,
    onSelectedDiarySlotsIdsChange: (List<Long>) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(modifier = (modifier).uiName("DiarySlotsFilter.FlowRow.1", "androidx.compose.foundation.layout.FlowRow", uiInspectionSource, "DiarySlotsFilter"), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        diarySlots.forEachIndexed { i, diarySlot ->
            val selected = diarySlot.id in selectedDiarySlotsIds

            key(diarySlot.id) {
                FilterChip(
                    selected = selected,
                    onClick = {
                        val selectedDiarySlotsIds =
                            if (selected) {
                                selectedDiarySlotsIds - diarySlot.id
                            } else {
                                selectedDiarySlotsIds + diarySlot.id
                            }
                        onSelectedDiarySlotsIdsChange(selectedDiarySlotsIds)
                    },
                    label = { Text(diarySlot.name, modifier = Modifier.uiName("DiarySlotsFilter.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotsFilter")) },
                    modifier = (Modifier.animatePlacement()).uiName("DiarySlotsFilter.FilterChip.1", "androidx.compose.material3.FilterChip", uiInspectionSource, "DiarySlotsFilter"),
                    leadingIcon = {
                        if (selected) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = (Modifier.size(FilterChipDefaults.IconSize)).uiName("DiarySlotsFilter.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DiarySlotsFilter"),
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun NutrientList(
    nutritionFacts: NutritionFacts,
    goals: DailyGoal,
    modifier: Modifier = Modifier,
) {
    val order = LocalNutrientsOrder.current

    Column(modifier = (modifier).uiName("app.ui.goals.master.NutrientList.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "NutrientList"), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        NutrientGoal(
            label = stringResource(Res.string.unit_energy),
            target =
                NutrientGoalDefaults.energyTargetString(
                    value = nutritionFacts[NutritionFactsField.Energy],
                    target = goals[NutritionFactsField.Energy],
                ),
            color = MaterialTheme.colorScheme.primary,
            state =
                rememberNutrientGoalState(
                    nutritionFacts[NutritionFactsField.Energy],
                    goals[NutritionFactsField.Energy],
                ),
            modifier = (Modifier.fillMaxWidth()).uiName("NutrientList.NutrientGoal.1", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "NutrientList"),
        )

        order.forEach {
            when (it) {
                NutrientsOrder.Proteins -> Proteins(nutritionFacts, goals, modifier = Modifier.uiName("app.ui.goals.master.NutrientList.Proteins.1", "com.maksimowiczm.foodyou.app.ui.goals.master.Proteins", uiInspectionSource, "NutrientList"))
                NutrientsOrder.Fats -> Fats(nutritionFacts, goals, modifier = Modifier.uiName("app.ui.goals.master.NutrientList.Fats.1", "com.maksimowiczm.foodyou.app.ui.goals.master.Fats", uiInspectionSource, "NutrientList"))
                NutrientsOrder.Carbohydrates -> Carbohydrates(nutritionFacts, goals, modifier = Modifier.uiName("app.ui.goals.master.NutrientList.Carbohydrates.1", "com.maksimowiczm.foodyou.app.ui.goals.master.Carbohydrates", uiInspectionSource, "NutrientList"))
                NutrientsOrder.Other -> Other(nutritionFacts, goals, modifier = Modifier.uiName("app.ui.goals.master.NutrientList.Other.1", "com.maksimowiczm.foodyou.app.ui.goals.master.Other", uiInspectionSource, "NutrientList"))
                NutrientsOrder.Vitamins -> Vitamins(nutritionFacts, goals, modifier = Modifier.uiName("app.ui.goals.master.NutrientList.Vitamins.1", "com.maksimowiczm.foodyou.app.ui.goals.master.Vitamins", uiInspectionSource, "NutrientList"))
                NutrientsOrder.Minerals -> Minerals(nutritionFacts, goals, modifier = Modifier.uiName("app.ui.goals.master.NutrientList.Minerals.1", "com.maksimowiczm.foodyou.app.ui.goals.master.Minerals", uiInspectionSource, "NutrientList"))
            }
        }
    }
}

@Composable
private fun Proteins(
    nutritionFacts: NutritionFacts,
    goals: DailyGoal,
    modifier: Modifier = Modifier,
) {
    val nutrientsPalette = LocalNutrientsPalette.current

    Column(modifier = (modifier).uiName("Proteins.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "Proteins"), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NutrientGoal(
            field = NutritionFactsField.Proteins,
            value = nutritionFacts.proteins,
            target = goals[NutritionFactsField.Proteins],
            color = nutrientsPalette.proteinsOnSurfaceContainer,
            modifier = (Modifier.fillMaxWidth()).uiName("Proteins.NutrientGoal.1", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Proteins"),
        )
    }
}

@Composable
private fun Fats(nutritionFacts: NutritionFacts, goals: DailyGoal, modifier: Modifier = Modifier) {
    val nutrientsPalette = LocalNutrientsPalette.current

    Column(modifier = (modifier).uiName("Fats.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "Fats"), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NutrientGoal(
            field = NutritionFactsField.Fats,
            value = nutritionFacts.fats,
            target = goals[NutritionFactsField.Fats],
            color = nutrientsPalette.fatsOnSurfaceContainer,
            modifier = (Modifier.fillMaxWidth()).uiName("Fats.NutrientGoal.1", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Fats"),
        )
        NutrientGoal(
            field = NutritionFactsField.SaturatedFats,
            value = nutritionFacts.saturatedFats,
            target = goals[NutritionFactsField.SaturatedFats],
            modifier = (Modifier.fillMaxWidth()).uiName("Fats.NutrientGoal.2", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Fats"),
        )
        NutrientGoal(
            field = NutritionFactsField.TransFats,
            value = nutritionFacts.transFats,
            target = goals[NutritionFactsField.TransFats],
            modifier = (Modifier.fillMaxWidth()).uiName("Fats.NutrientGoal.3", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Fats"),
        )
        NutrientGoal(
            field = NutritionFactsField.MonounsaturatedFats,
            value = nutritionFacts.monounsaturatedFats,
            target = goals[NutritionFactsField.MonounsaturatedFats],
            modifier = (Modifier.fillMaxWidth()).uiName("Fats.NutrientGoal.4", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Fats"),
        )
        NutrientGoal(
            field = NutritionFactsField.PolyunsaturatedFats,
            value = nutritionFacts.polyunsaturatedFats,
            target = goals[NutritionFactsField.PolyunsaturatedFats],
            modifier = (Modifier.fillMaxWidth()).uiName("Fats.NutrientGoal.5", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Fats"),
        )
        NutrientGoal(
            field = NutritionFactsField.Omega3,
            value = nutritionFacts.omega3,
            target = goals[NutritionFactsField.Omega3],
            modifier = (Modifier.fillMaxWidth()).uiName("Fats.NutrientGoal.6", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Fats"),
        )
        NutrientGoal(
            field = NutritionFactsField.Omega6,
            value = nutritionFacts.omega6,
            target = goals[NutritionFactsField.Omega6],
            modifier = (Modifier.fillMaxWidth()).uiName("Fats.NutrientGoal.7", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Fats"),
        )
    }
}

@Composable
private fun Carbohydrates(
    nutritionFacts: NutritionFacts,
    goals: DailyGoal,
    modifier: Modifier = Modifier,
) {
    val nutrientsPalette = LocalNutrientsPalette.current

    Column(modifier = (modifier).uiName("Carbohydrates.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "Carbohydrates"), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NutrientGoal(
            field = NutritionFactsField.Carbohydrates,
            value = nutritionFacts.carbohydrates,
            target = goals[NutritionFactsField.Carbohydrates],
            modifier = (Modifier.fillMaxWidth()).uiName("Carbohydrates.NutrientGoal.1", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Carbohydrates"),
            color = nutrientsPalette.carbohydratesOnSurfaceContainer,
        )
        NutrientGoal(
            field = NutritionFactsField.Sugars,
            value = nutritionFacts.sugars,
            target = goals[NutritionFactsField.Sugars],
            modifier = (Modifier.fillMaxWidth()).uiName("Carbohydrates.NutrientGoal.2", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Carbohydrates"),
        )
        NutrientGoal(
            field = NutritionFactsField.AddedSugars,
            value = nutritionFacts.addedSugars,
            target = goals[NutritionFactsField.AddedSugars],
            modifier = (Modifier.fillMaxWidth()).uiName("Carbohydrates.NutrientGoal.3", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Carbohydrates"),
        )
        NutrientGoal(
            field = NutritionFactsField.DietaryFiber,
            value = nutritionFacts.dietaryFiber,
            target = goals[NutritionFactsField.DietaryFiber],
            modifier = (Modifier.fillMaxWidth()).uiName("Carbohydrates.NutrientGoal.4", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Carbohydrates"),
        )
        NutrientGoal(
            field = NutritionFactsField.SolubleFiber,
            value = nutritionFacts.solubleFiber,
            target = goals[NutritionFactsField.SolubleFiber],
            modifier = (Modifier.fillMaxWidth()).uiName("Carbohydrates.NutrientGoal.5", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Carbohydrates"),
        )
        NutrientGoal(
            field = NutritionFactsField.InsolubleFiber,
            value = nutritionFacts.insolubleFiber,
            target = goals[NutritionFactsField.InsolubleFiber],
            modifier = (Modifier.fillMaxWidth()).uiName("Carbohydrates.NutrientGoal.6", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Carbohydrates"),
        )
    }
}

@Composable
private fun Other(nutritionFacts: NutritionFacts, goals: DailyGoal, modifier: Modifier = Modifier) {
    Column(modifier = (modifier).uiName("app.ui.goals.master.Other.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "Other"), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NutrientGoal(
            field = NutritionFactsField.Salt,
            value = nutritionFacts.salt,
            target = goals[NutritionFactsField.Salt],
            modifier = (Modifier.fillMaxWidth()).uiName("Other.NutrientGoal.1", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Other"),
        )
        NutrientGoal(
            field = NutritionFactsField.Cholesterol,
            value = nutritionFacts.cholesterol * 1000.0,
            target = (goals[NutritionFactsField.Cholesterol] * 1000.0),
            modifier = (Modifier.fillMaxWidth()).uiName("Other.NutrientGoal.2", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Other"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.Caffeine,
            value = nutritionFacts.caffeine * 1000.0,
            target = (goals[NutritionFactsField.Caffeine] * 1000.0),
            modifier = (Modifier.fillMaxWidth()).uiName("Other.NutrientGoal.3", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Other"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
    }
}

@Composable
private fun Vitamins(
    nutritionFacts: NutritionFacts,
    goals: DailyGoal,
    modifier: Modifier = Modifier,
) {
    Column(modifier = (modifier).uiName("app.ui.goals.master.Vitamins.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "Vitamins"), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NutrientGoal(
            field = NutritionFactsField.VitaminA,
            value = nutritionFacts.vitaminA * 1_000_000.0,
            target = (goals[NutritionFactsField.VitaminA] * 1_000_000.0),
            modifier = (Modifier.fillMaxWidth()).uiName("Vitamins.NutrientGoal.1", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Vitamins"),
            suffix = stringResource(Res.string.unit_microgram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.VitaminB1,
            value = nutritionFacts.vitaminB1 * 1000.0,
            target = (goals[NutritionFactsField.VitaminB1] * 1000.0),
            modifier = (Modifier.fillMaxWidth()).uiName("Vitamins.NutrientGoal.2", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Vitamins"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.VitaminB2,
            value = nutritionFacts.vitaminB2 * 1000.0,
            target = (goals[NutritionFactsField.VitaminB2] * 1000.0),
            modifier = (Modifier.fillMaxWidth()).uiName("Vitamins.NutrientGoal.3", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Vitamins"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.VitaminB3,
            value = nutritionFacts.vitaminB3 * 1000.0,
            target = (goals[NutritionFactsField.VitaminB3] * 1000.0),
            modifier = (Modifier.fillMaxWidth()).uiName("Vitamins.NutrientGoal.4", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Vitamins"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.VitaminB5,
            value = nutritionFacts.vitaminB5 * 1000.0,
            target = (goals[NutritionFactsField.VitaminB5] * 1000.0),
            modifier = (Modifier.fillMaxWidth()).uiName("Vitamins.NutrientGoal.5", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Vitamins"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.VitaminB6,
            value = nutritionFacts.vitaminB6 * 1000.0,
            target = (goals[NutritionFactsField.VitaminB6] * 1000.0),
            modifier = (Modifier.fillMaxWidth()).uiName("Vitamins.NutrientGoal.6", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Vitamins"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.VitaminB7,
            value = nutritionFacts.vitaminB7 * 1_000_000.0,
            target = (goals[NutritionFactsField.VitaminB7] * 1000_000f),
            modifier = (Modifier.fillMaxWidth()).uiName("Vitamins.NutrientGoal.7", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Vitamins"),
            suffix = stringResource(Res.string.unit_microgram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.VitaminB9,
            value = nutritionFacts.vitaminB9 * 1_000_000.0,
            target = (goals[NutritionFactsField.VitaminB9] * 1000_000f),
            modifier = (Modifier.fillMaxWidth()).uiName("Vitamins.NutrientGoal.8", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Vitamins"),
            suffix = stringResource(Res.string.unit_microgram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.VitaminB12,
            value = nutritionFacts.vitaminB12 * 1_000_000.0,
            target = (goals[NutritionFactsField.VitaminB12] * 1000_000f),
            modifier = (Modifier.fillMaxWidth()).uiName("Vitamins.NutrientGoal.9", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Vitamins"),
            suffix = stringResource(Res.string.unit_microgram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.VitaminC,
            value = nutritionFacts.vitaminC * 1000.0,
            target = (goals[NutritionFactsField.VitaminC] * 1000.0),
            modifier = (Modifier.fillMaxWidth()).uiName("Vitamins.NutrientGoal.10", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Vitamins"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.VitaminD,
            value = nutritionFacts.vitaminD * 1_000_000.0,
            target = (goals[NutritionFactsField.VitaminD] * 1000_000f),
            modifier = (Modifier.fillMaxWidth()).uiName("Vitamins.NutrientGoal.11", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Vitamins"),
            suffix = stringResource(Res.string.unit_microgram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.VitaminE,
            value = nutritionFacts.vitaminE * 1000.0,
            target = (goals[NutritionFactsField.VitaminE] * 1000.0),
            modifier = (Modifier.fillMaxWidth()).uiName("Vitamins.NutrientGoal.12", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Vitamins"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.VitaminK,
            value = nutritionFacts.vitaminK * 1_000_000.0,
            target = (goals[NutritionFactsField.VitaminK] * 1000_000f),
            modifier = (Modifier.fillMaxWidth()).uiName("Vitamins.NutrientGoal.13", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Vitamins"),
            suffix = stringResource(Res.string.unit_microgram_short),
        )
    }
}

@Composable
private fun Minerals(
    nutritionFacts: NutritionFacts,
    goals: DailyGoal,
    modifier: Modifier = Modifier,
) {
    Column(modifier = (modifier).uiName("app.ui.goals.master.Minerals.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "Minerals"), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NutrientGoal(
            field = NutritionFactsField.Manganese,
            value = nutritionFacts.manganese * 1000.0,
            target = (goals[NutritionFactsField.Manganese] * 1000),
            modifier = (Modifier.fillMaxWidth()).uiName("Minerals.NutrientGoal.1", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Minerals"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.Magnesium,
            value = nutritionFacts.magnesium * 1000.0,
            target = (goals[NutritionFactsField.Magnesium] * 1000),
            modifier = (Modifier.fillMaxWidth()).uiName("Minerals.NutrientGoal.2", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Minerals"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.Potassium,
            value = nutritionFacts.potassium * 1000.0,
            target = (goals[NutritionFactsField.Potassium] * 1000),
            modifier = (Modifier.fillMaxWidth()).uiName("Minerals.NutrientGoal.3", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Minerals"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.Calcium,
            value = nutritionFacts.calcium * 1000.0,
            target = (goals[NutritionFactsField.Calcium] * 1000),
            modifier = (Modifier.fillMaxWidth()).uiName("Minerals.NutrientGoal.4", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Minerals"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.Copper,
            value = nutritionFacts.copper * 1000.0,
            target = (goals[NutritionFactsField.Copper] * 1000),
            modifier = (Modifier.fillMaxWidth()).uiName("Minerals.NutrientGoal.5", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Minerals"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.Zinc,
            value = nutritionFacts.zinc * 1000.0,
            target = (goals[NutritionFactsField.Zinc] * 1000),
            modifier = (Modifier.fillMaxWidth()).uiName("Minerals.NutrientGoal.6", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Minerals"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.Sodium,
            value = nutritionFacts.sodium * 1000.0,
            target = (goals[NutritionFactsField.Sodium] * 1000),
            modifier = (Modifier.fillMaxWidth()).uiName("Minerals.NutrientGoal.7", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Minerals"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.Iron,
            value = nutritionFacts.iron * 1000.0,
            target = (goals[NutritionFactsField.Iron] * 1000),
            modifier = (Modifier.fillMaxWidth()).uiName("Minerals.NutrientGoal.8", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Minerals"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.Phosphorus,
            value = nutritionFacts.phosphorus * 1000.0,
            target = (goals[NutritionFactsField.Phosphorus] * 1000),
            modifier = (Modifier.fillMaxWidth()).uiName("Minerals.NutrientGoal.9", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Minerals"),
            suffix = stringResource(Res.string.unit_milligram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.Selenium,
            value = nutritionFacts.selenium * 1_000_000.0,
            target = (goals[NutritionFactsField.Selenium] * 1_000_000.0),
            modifier = (Modifier.fillMaxWidth()).uiName("Minerals.NutrientGoal.10", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Minerals"),
            suffix = stringResource(Res.string.unit_microgram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.Iodine,
            value = nutritionFacts.iodine * 1_000_000.0,
            target = (goals[NutritionFactsField.Iodine] * 1_000_000.0),
            modifier = (Modifier.fillMaxWidth()).uiName("Minerals.NutrientGoal.11", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Minerals"),
            suffix = stringResource(Res.string.unit_microgram_short),
        )
        NutrientGoal(
            field = NutritionFactsField.Chromium,
            value = nutritionFacts.chromium * 1_000_000.0,
            target = (goals[NutritionFactsField.Chromium] * 1_000_000.0),
            modifier = (Modifier.fillMaxWidth()).uiName("Minerals.NutrientGoal.12", "com.maksimowiczm.foodyou.app.ui.goals.master.NutrientGoal", uiInspectionSource, "Minerals"),
            suffix = stringResource(Res.string.unit_microgram_short),
        )
    }
}

@Composable
private fun CalendarCardDatePickerDialog(
    goalsState: GoalsScreenState,
    onDismissRequest: () -> Unit,
) {
    val state = goalsState.rememberDatePickerState()

    UiSourceScope(uiInspectionSource.element("app.ui.goals.master.CalendarCardDatePickerDialog.DatePickerDialog.1", "androidx.compose.material3.DatePickerDialog", "CalendarCardDatePickerDialog")) { DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let {
                        goalsState.goToDate(
                            date =
                                Instant.fromEpochMilliseconds(it)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .date
                        )
                    }
                    onDismissRequest()
                }, modifier = Modifier.uiName("app.ui.goals.master.CalendarCardDatePickerDialog.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "CalendarCardDatePickerDialog")
            ) {
                Text(text = stringResource(Res.string.positive_ok), modifier = Modifier.uiName("app.ui.goals.master.CalendarCardDatePickerDialog.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "CalendarCardDatePickerDialog"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest, modifier = Modifier.uiName("app.ui.goals.master.CalendarCardDatePickerDialog.TextButton.2", "androidx.compose.material3.TextButton", uiInspectionSource, "CalendarCardDatePickerDialog")) {
                Text(text = stringResource(Res.string.action_cancel), modifier = Modifier.uiName("app.ui.goals.master.CalendarCardDatePickerDialog.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "CalendarCardDatePickerDialog"))
            }
        }, modifier = Modifier.uiName("app.ui.goals.master.CalendarCardDatePickerDialog.DatePickerDialog.1.root", "androidx.compose.material3.DatePickerDialog", uiInspectionSource, "CalendarCardDatePickerDialog")
    ) {
        DatePicker(
            state = state,
            title = {
                Row(
                    modifier =
                        (Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 16.dp)).uiName("app.ui.goals.master.CalendarCardDatePickerDialog.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "CalendarCardDatePickerDialog"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    DatePickerDefaults.DatePickerTitle(displayMode = state.displayMode,
                        modifier = Modifier.uiName("app.ui.goals.master.CalendarCardDatePickerDialog.DatePickerTitle.1", "androidx.compose.material3.DatePickerDefaults.DatePickerTitle", uiInspectionSource, "CalendarCardDatePickerDialog"))

                    TextButton(
                        onClick = {
                            goalsState.goToToday()
                            onDismissRequest()
                        }, modifier = Modifier.uiName("app.ui.goals.master.CalendarCardDatePickerDialog.TextButton.3", "androidx.compose.material3.TextButton", uiInspectionSource, "CalendarCardDatePickerDialog")
                    ) {
                        Text(stringResource(Res.string.action_go_to_today), modifier = Modifier.uiName("app.ui.goals.master.CalendarCardDatePickerDialog.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "CalendarCardDatePickerDialog"))
                    }
                }
            },
            // It won't fit on small screens, so we need to scroll
            modifier = (Modifier.verticalScroll(rememberScrollState())).uiName("app.ui.goals.master.CalendarCardDatePickerDialog.DatePicker.1", "androidx.compose.material3.DatePicker", uiInspectionSource, "CalendarCardDatePickerDialog"),
        )
    } }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.goals.master",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/goals/master/GoalsScreen.kt",
)
