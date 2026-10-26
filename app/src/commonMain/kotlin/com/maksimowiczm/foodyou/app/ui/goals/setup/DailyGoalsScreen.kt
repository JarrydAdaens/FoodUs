package com.maksimowiczm.foodyou.app.ui.goals.setup



import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.DiscardDialog
import com.maksimowiczm.foodyou.app.ui.common.form.FormField
import com.maksimowiczm.foodyou.app.ui.common.theme.LocalNutrientsPalette
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalNutrientsOrder
import com.maksimowiczm.foodyou.common.compose.extension.LaunchedCollectWithLifecycle
import com.maksimowiczm.foodyou.common.compose.extension.add
import com.maksimowiczm.foodyou.common.compose.utility.LocalDateFormatter
import com.maksimowiczm.foodyou.settings.domain.entity.NutrientsOrder
import foodyou.app.generated.resources.*
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DailyGoalsScreen(onBack: () -> Unit, onSave: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: DailyGoalsViewModel = koinViewModel()
    val weeklyGoals = viewModel.weeklyGoals.collectAsStateWithLifecycle().value

    LaunchedCollectWithLifecycle(viewModel.events) {
        when (it) {
            DailyGoalsViewModelEvent.Updated -> onSave()
        }
    }

    if (weeklyGoals == null) {
        // TODO loading state
        return
    }

    val state = rememberWeeklyGoalsState(weeklyGoals)

    DailyGoalsContent(
        weeklyState = state,
        onBack = onBack,
        onSave = {
            val weeklyGoals = state.intoWeeklyGoals()
            viewModel.updateWeeklyGoals(weeklyGoals)
        },
        modifier = (modifier).uiName("DailyGoalsScreen.DailyGoalsContent.1", "com.maksimowiczm.foodyou.app.ui.goals.setup.DailyGoalsContent", uiInspectionSource, "DailyGoalsScreen"),
    )
}

@Composable
internal fun DailyGoalsContent(
    weeklyState: WeeklyGoalsState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier,
) {
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    val handleOnBack = { if (weeklyState.isModified) showDiscardDialog = true else onBack() }
    NavigationEventHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = weeklyState.isModified,
        onBackCompleted = { showDiscardDialog = true },
    )
    if (showDiscardDialog) {
        DiscardDialog(onDismissRequest = { showDiscardDialog = false }, onDiscard = onBack, modifier = Modifier.uiName("DailyGoalsContent.DiscardDialog.1", "com.maksimowiczm.foodyou.app.ui.common.component.DiscardDialog", uiInspectionSource, "DailyGoalsContent")) {
            Text(stringResource(Res.string.question_discard_changes), modifier = Modifier.uiName("DailyGoalsContent.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DailyGoalsContent"))
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = (modifier).uiName("DailyGoalsContent.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "DailyGoalsContent"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_daily_goals), modifier = Modifier.uiName("DailyGoalsContent.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DailyGoalsContent")) },
                navigationIcon = { ArrowBackIconButton(handleOnBack, modifier = Modifier.uiName("DailyGoalsContent.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "DailyGoalsContent")) },
                actions = {
                    FilledIconButton(
                        onClick = onSave,
                        shapes = IconButtonDefaults.shapes(),
                        enabled = weeklyState.isValid, modifier = Modifier.uiName("DailyGoalsContent.FilledIconButton.1", "androidx.compose.material3.FilledIconButton", uiInspectionSource, "DailyGoalsContent")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = stringResource(Res.string.action_save), modifier = Modifier.uiName("DailyGoalsContent.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DailyGoalsContent")
                        )
                    }
                },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("DailyGoalsContent.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "DailyGoalsContent")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                (Modifier.fillMaxSize()
                    .imePadding()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("DailyGoalsContent.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "DailyGoalsContent"),
            contentPadding = paddingValues.add(vertical = 8.dp),
        ) {
            item {
                DayPicker(
                    useSeparateGoals = weeklyState.useSeparateGoals,
                    onUseSeparateGoalsChange = { weeklyState.useSeparateGoals = it },
                    selectedDay = weeklyState.selectedDay,
                    onSelectedDayChange = { weeklyState.selectedDay = it },
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = (Modifier.fillMaxWidth()).uiName("DailyGoalsContent.DayPicker.1", "com.maksimowiczm.foodyou.app.ui.goals.setup.DayPicker", uiInspectionSource, "DailyGoalsContent"),
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                val state = weeklyState.selectedDayGoals

                Column((modifier).uiName("DailyGoalsContent.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DailyGoalsContent")) {
                    Text(
                        text = stringResource(Res.string.action_set_goals),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = (Modifier.padding(horizontal = 16.dp)).uiName("DailyGoalsContent.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "DailyGoalsContent"),
                    )
                    WeightOrPercentageToggle(
                        useDistribution = state.inputType == InputType.Percentage,
                        onUseDistributionChange = {
                            state.inputType = if (it) InputType.Percentage else InputType.Weight
                        },
                        modifier =
                            (Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp)).uiName("DailyGoalsContent.WeightOrPercentageToggle.1", "com.maksimowiczm.foodyou.app.ui.goals.setup.WeightOrPercentageToggle", uiInspectionSource, "DailyGoalsContent"),
                    )
                    if (state.inputType == InputType.Percentage) {
                        MacroInputSliderForm(
                            state = state,
                            modifier = (Modifier.fillMaxWidth().padding(horizontal = 16.dp)).uiName("DailyGoalsContent.MacroInputSliderForm.1", "com.maksimowiczm.foodyou.app.ui.goals.setup.MacroInputSliderForm", uiInspectionSource, "DailyGoalsContent"),
                        )
                    } else {
                        MacroInput(
                            state = state,
                            modifier = (Modifier.fillMaxWidth().padding(horizontal = 16.dp)).uiName("DailyGoalsContent.MacroInput.1", "com.maksimowiczm.foodyou.app.ui.goals.setup.MacroInput", uiInspectionSource, "DailyGoalsContent"),
                        )
                    }
                    HorizontalDivider((Modifier.padding(vertical = 8.dp)).uiName("DailyGoalsContent.HorizontalDivider.1", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "DailyGoalsContent"))
                    AdditionalGoalsForm(
                        state = state.additionalState,
                        modifier = (Modifier.fillMaxWidth().padding(horizontal = 16.dp)).uiName("DailyGoalsContent.AdditionalGoalsForm.1", "com.maksimowiczm.foodyou.app.ui.goals.setup.AdditionalGoalsForm", uiInspectionSource, "DailyGoalsContent"),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayPicker(
    useSeparateGoals: Boolean,
    onUseSeparateGoalsChange: (Boolean) -> Unit,
    selectedDay: Int,
    onSelectedDayChange: (Int) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current

    Column((modifier).uiName("DayPicker.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DayPicker")) {
        Text(
            text = stringResource(Res.string.headline_pick_the_days),
            modifier = (Modifier.padding(contentPadding)).uiName("DayPicker.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DayPicker"),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier =
                (Modifier.fillMaxWidth()
                    .clickable { onUseSeparateGoalsChange(!useSeparateGoals) }
                    .padding(contentPadding)).uiName("DayPicker.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "DayPicker"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Checkbox(
                modifier = (Modifier.padding(vertical = 16.dp)).uiName("DayPicker.Checkbox.1", "androidx.compose.material3.Checkbox", uiInspectionSource, "DayPicker"),
                checked = useSeparateGoals,
                onCheckedChange = null,
            )
            Text(
                text = stringResource(Res.string.action_set_separate_goals),
                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("DayPicker.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DayPicker")
            )
        }
        AnimatedVisibility(useSeparateGoals, modifier = Modifier.uiName("DayPicker.AnimatedVisibility.1", "androidx.compose.animation.AnimatedVisibility", uiInspectionSource, "DayPicker")) {
            LazyRow(
                modifier = (Modifier.fillMaxWidth()).uiName("DayPicker.LazyRow.1", "androidx.compose.foundation.lazy.LazyRow", uiInspectionSource, "DayPicker"),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                contentPadding = contentPadding,
            ) {
                item {
                    val dateFormatter = LocalDateFormatter.current
                    val weekDayNamesShort = dateFormatter.weekDayNamesShort

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                ButtonGroupDefaults.ConnectedSpaceBetween,
                                Alignment.CenterHorizontally,
                            ), modifier = Modifier.uiName("DayPicker.Row.2", "androidx.compose.foundation.layout.Row", uiInspectionSource, "DayPicker")
                    ) {
                        weekDayNamesShort.forEachIndexed { i, name ->
                            ToggleButton(
                                checked = selectedDay == i,
                                onCheckedChange = {
                                    onSelectedDayChange(i)
                                    hapticFeedback.performHapticFeedback(
                                        HapticFeedbackType.SegmentTick
                                    )
                                },
                                modifier = (Modifier.semantics { role = Role.RadioButton }).uiName("DayPicker.ToggleButton.1", "androidx.compose.material3.ToggleButton", uiInspectionSource, "DayPicker"),
                                shapes =
                                    when (i) {
                                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                        weekDayNamesShort.lastIndex ->
                                            ButtonGroupDefaults.connectedTrailingButtonShapes()

                                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                    },
                            ) {
                                Text(name, modifier = Modifier.uiName("DayPicker.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "DayPicker"))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightOrPercentageToggle(
    useDistribution: Boolean,
    onUseDistributionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = (modifier).uiName("WeightOrPercentageToggle.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "WeightOrPercentageToggle"),
        horizontalArrangement =
            Arrangement.spacedBy(
                ButtonGroupDefaults.ConnectedSpaceBetween,
                Alignment.CenterHorizontally,
            ),
    ) {
        ToggleButton(
            checked = !useDistribution,
            onCheckedChange = { onUseDistributionChange(false) },
            modifier = (Modifier.height(56.dp).semantics { role = Role.RadioButton }).uiName("WeightOrPercentageToggle.ToggleButton.1", "androidx.compose.material3.ToggleButton", uiInspectionSource, "WeightOrPercentageToggle"),
            shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
        ) {
            Icon(painter = painterResource(Res.drawable.ic_weight), contentDescription = null, modifier = Modifier.uiName("WeightOrPercentageToggle.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "WeightOrPercentageToggle"))
            Spacer(Modifier.width(ToggleButtonDefaults.IconSpacing))
            Text(stringResource(Res.string.weight), modifier = Modifier.uiName("WeightOrPercentageToggle.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "WeightOrPercentageToggle"))
        }
        ToggleButton(
            checked = useDistribution,
            onCheckedChange = { onUseDistributionChange(true) },
            modifier = (Modifier.height(56.dp).semantics { role = Role.RadioButton }).uiName("WeightOrPercentageToggle.ToggleButton.2", "androidx.compose.material3.ToggleButton", uiInspectionSource, "WeightOrPercentageToggle"),
            shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
        ) {
            Icon(imageVector = Icons.Outlined.Percent, contentDescription = null, modifier = Modifier.uiName("WeightOrPercentageToggle.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "WeightOrPercentageToggle"))
            Spacer(Modifier.width(ToggleButtonDefaults.IconSpacing))
            Text(stringResource(Res.string.headline_percentages), modifier = Modifier.uiName("WeightOrPercentageToggle.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "WeightOrPercentageToggle"))
        }
    }
}

@Composable
private fun MacroInputSliderForm(state: DailyGoalsFormState, modifier: Modifier = Modifier) {
    val nutrientsPalette = LocalNutrientsPalette.current
    val nutrientsOrder = LocalNutrientsOrder.current

    Column(modifier = (modifier).uiName("MacroInputSliderForm.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "MacroInputSliderForm"), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            state = state.energy.textFieldState,
            modifier = (Modifier.fillMaxWidth()).uiName("MacroInputSliderForm.OutlinedTextField.1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "MacroInputSliderForm"),
            label = { Text(stringResource(Res.string.unit_energy), modifier = Modifier.uiName("MacroInputSliderForm.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "MacroInputSliderForm")) },
            suffix = { Text(stringResource(Res.string.unit_kcal), modifier = Modifier.uiName("MacroInputSliderForm.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "MacroInputSliderForm")) },
            isError = state.energy.error != null,
            keyboardOptions =
                KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        )

        nutrientsOrder.forEach {
            when (it) {
                NutrientsOrder.Proteins ->
                    MacroSlider(
                        value = state.proteinsSlider,
                        onValueChange = { state.proteinsSlider = it },
                        color = nutrientsPalette.proteinsOnSurfaceContainer,
                        label = stringResource(Res.string.nutriment_proteins), modifier = Modifier.uiName("MacroInputSliderForm.MacroSlider.1", "com.maksimowiczm.foodyou.app.ui.goals.setup.MacroSlider", uiInspectionSource, "MacroInputSliderForm")
                    )

                NutrientsOrder.Fats ->
                    MacroSlider(
                        value = state.fatsSlider,
                        onValueChange = { state.fatsSlider = it },
                        color = nutrientsPalette.fatsOnSurfaceContainer,
                        label = stringResource(Res.string.nutriment_fats), modifier = Modifier.uiName("MacroInputSliderForm.MacroSlider.2", "com.maksimowiczm.foodyou.app.ui.goals.setup.MacroSlider", uiInspectionSource, "MacroInputSliderForm")
                    )

                NutrientsOrder.Carbohydrates ->
                    MacroSlider(
                        value = state.carbsSlider,
                        onValueChange = { state.carbsSlider = it },
                        color = nutrientsPalette.carbohydratesOnSurfaceContainer,
                        label = stringResource(Res.string.nutriment_carbohydrates), modifier = Modifier.uiName("MacroInputSliderForm.MacroSlider.3", "com.maksimowiczm.foodyou.app.ui.goals.setup.MacroSlider", uiInspectionSource, "MacroInputSliderForm")
                    )

                NutrientsOrder.Other,
                NutrientsOrder.Vitamins,
                NutrientsOrder.Minerals -> Unit
            }
        }
    }
}

@Composable
private fun MacroSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    color: Color,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column((modifier).uiName("MacroSlider.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "MacroSlider")) {
        Row(modifier = (Modifier.fillMaxWidth()).uiName("MacroSlider.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "MacroSlider"), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, color = color, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("MacroSlider.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "MacroSlider"))
            Text(
                text =
                    buildString {
                        append(value.roundToInt())
                        append("%")
                    },
                color = color,
                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("MacroSlider.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "MacroSlider")
            )
        }
        Slider(
            value = value,
            onValueChange = { onValueChange(it.roundToInt().toFloat()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(activeTrackColor = color, thumbColor = color), modifier = Modifier.uiName("MacroSlider.Slider.1", "androidx.compose.material3.Slider", uiInspectionSource, "MacroSlider")
        )
    }
}

@Composable
private fun MacroInput(state: DailyGoalsFormState, modifier: Modifier = Modifier) {
    val nutrientsPalette = LocalNutrientsPalette.current
    val nutrientsOrder = LocalNutrientsOrder.current

    Column(modifier = (modifier).uiName("MacroInput.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "MacroInput"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        nutrientsOrder.forEach {
            when (it) {
                NutrientsOrder.Proteins ->
                    state.proteins.TextField(
                        label = stringResource(Res.string.nutriment_proteins),
                        color = nutrientsPalette.proteinsOnSurfaceContainer,
                        modifier = (Modifier.fillMaxWidth()).uiName("MacroInput.TextField.1", "com.maksimowiczm.foodyou.app.ui.goals.setup.TextField", uiInspectionSource, "MacroInput"),
                    )

                NutrientsOrder.Fats ->
                    state.fats.TextField(
                        label = stringResource(Res.string.nutriment_fats),
                        color = nutrientsPalette.fatsOnSurfaceContainer,
                        modifier = (Modifier.fillMaxWidth()).uiName("MacroInput.TextField.2", "com.maksimowiczm.foodyou.app.ui.goals.setup.TextField", uiInspectionSource, "MacroInput"),
                    )

                NutrientsOrder.Carbohydrates ->
                    state.carbs.TextField(
                        label = stringResource(Res.string.nutriment_carbohydrates),
                        color = nutrientsPalette.carbohydratesOnSurfaceContainer,
                        modifier = (Modifier.fillMaxWidth()).uiName("MacroInput.TextField.3", "com.maksimowiczm.foodyou.app.ui.goals.setup.TextField", uiInspectionSource, "MacroInput"),
                    )

                NutrientsOrder.Other,
                NutrientsOrder.Vitamins,
                NutrientsOrder.Minerals -> Unit
            }
        }

        val str = buildString {
            append("=")
            append(" ${state.energy.value.roundToInt()} ")
            append(stringResource(Res.string.unit_kcal))
        }

        Text(
            text = str,
            modifier = (Modifier.fillMaxWidth()).uiName("MacroInput.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "MacroInput"),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Composable
private fun FormField<Double, DailyGoalsFormError>.TextField(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    suffix: String = stringResource(Res.string.unit_gram_short),
    imeAction: ImeAction = ImeAction.Next,
) {
    OutlinedTextField(
        state = textFieldState,
        modifier = (modifier).uiName("app.ui.goals.setup.TextField.OutlinedTextField.1.declaration2", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "TextField"),
        label = { Text(label, modifier = Modifier.uiName("app.ui.goals.setup.TextField.Text.2.declaration2", "androidx.compose.material3.Text", uiInspectionSource, "TextField")) },
        suffix = { Text(suffix, modifier = Modifier.uiName("app.ui.goals.setup.TextField.Text.3.declaration2", "androidx.compose.material3.Text", uiInspectionSource, "TextField")) },
        keyboardOptions =
            KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = imeAction),
        isError = error != null,
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = color,
                unfocusedBorderColor = color,
                focusedLabelColor = color,
                unfocusedLabelColor = color,
            ),
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.goals.setup",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/goals/setup/DailyGoalsScreen.kt",
)
