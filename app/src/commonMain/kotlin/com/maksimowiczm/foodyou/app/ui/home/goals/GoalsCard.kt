package com.maksimowiczm.foodyou.app.ui.home.goals

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.theme.LocalNutrientsPalette
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalGraphStyle
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalNutrientsOrder
import com.maksimowiczm.foodyou.app.ui.home.shared.FoodYouHomeCard
import com.maksimowiczm.foodyou.app.ui.home.shared.HomeState
import com.maksimowiczm.foodyou.common.compose.extension.toDp
import com.maksimowiczm.foodyou.settings.domain.entity.GraphStyle
import com.maksimowiczm.foodyou.settings.domain.entity.NutrientsOrder
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.shimmer
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun GoalsCard(
    homeState: HomeState,
    onClick: (epochDay: Long) -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GoalsViewModel = koinViewModel(),
) {
    LaunchedEffect(homeState.selectedDate) { viewModel.setDate(homeState.selectedDate) }

    val model = viewModel.model.collectAsStateWithLifecycle().value
    val expand by viewModel.expandGoalsCard.collectAsStateWithLifecycle()
    val readFailed by viewModel.readFailed.collectAsStateWithLifecycle()

    if (readFailed) {
        FoodYouHomeCard(modifier = (modifier).uiName("GoalsCard.FoodYouHomeCard.1", "com.maksimowiczm.foodyou.app.ui.home.shared.FoodYouHomeCard", uiInspectionSource, "GoalsCard"), onClick = viewModel::retryRead, onLongClick = onLongClick) {
            Column((Modifier.padding(16.dp)).uiName("GoalsCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "GoalsCard")) {
                Text(stringResource(Res.string.meals_diary_unavailable), modifier = Modifier.uiName("GoalsCard.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "GoalsCard"))
                TextButton(onClick = viewModel::retryRead, modifier = Modifier.uiName("GoalsCard.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "GoalsCard")) { Text(stringResource(Res.string.meals_retry), modifier = Modifier.uiName("GoalsCard.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "GoalsCard")) }
            }
        }
    } else if (model == null) {
        GoalsCardSkeleton(
            shimmer = homeState.shimmer,
            expand = expand,
            onClick = { onClick(homeState.selectedDate.toEpochDays()) },
            onLongClick = onLongClick,
            modifier = (modifier).uiName("GoalsCard.GoalsCardSkeleton.1", "com.maksimowiczm.foodyou.app.ui.home.goals.GoalsCardSkeleton", uiInspectionSource, "GoalsCard"),
        )
    } else {
        GoalsCard(
            expand = expand,
            energy = model.energy,
            energyGoal = model.energyGoal,
            proteins = model.proteins,
            proteinsGoal = model.proteinsGoal,
            carbohydrates = model.carbohydrates,
            carbohydratesGoal = model.carbohydratesGoal,
            fats = model.fats,
            fatsGoal = model.fatsGoal,
            onClick = { onClick(homeState.selectedDate.toEpochDays()) },
            onLongClick = onLongClick,
            modifier = (modifier).uiName("GoalsCard.GoalsCard.1", "com.maksimowiczm.foodyou.app.ui.home.goals.GoalsCard", uiInspectionSource, "GoalsCard"),
        )
    }
}

@Composable
internal fun GoalsCard(
    expand: Boolean,
    energy: Int,
    energyGoal: Int,
    proteins: Int,
    proteinsGoal: Int,
    carbohydrates: Int,
    carbohydratesGoal: Int,
    fats: Int,
    fatsGoal: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val proteinsPercentage =
        animateFloatAsState(
                targetValue = proteins.toFloat() / proteinsGoal,
                animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
            )
            .value

    val carbsPercentage =
        animateFloatAsState(
                targetValue = carbohydrates.toFloat() / carbohydratesGoal,
                animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
            )
            .value

    val fatsPercentage =
        animateFloatAsState(
                targetValue = fats.toFloat() / fatsGoal,
                animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
            )
            .value

    FoodYouHomeCard(modifier = (modifier).uiName("GoalsCard.FoodYouHomeCard.2", "com.maksimowiczm.foodyou.app.ui.home.shared.FoodYouHomeCard", uiInspectionSource, "GoalsCard"), onClick = onClick, onLongClick = onLongClick) {
        Column(modifier = (Modifier.padding(16.dp)).uiName("GoalsCard.Column.2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "GoalsCard")) {
            GoalsCardContent(
                energy = energy,
                energyGoal = energyGoal,
                proteins = proteins,
                carbohydrates = carbohydrates,
                fats = fats,
                proteinsPercentage = proteinsPercentage,
                carbsPercentage = carbsPercentage,
                fatsPercentage = fatsPercentage,
                modifier = (Modifier.fillMaxWidth()).uiName("GoalsCard.GoalsCardContent.1", "com.maksimowiczm.foodyou.app.ui.home.goals.GoalsCardContent", uiInspectionSource, "GoalsCard"),
            )

            AnimatedVisibility(
                visible = expand,
                enter = expandVertically(),
                exit = shrinkVertically(), modifier = Modifier.uiName("GoalsCard.AnimatedVisibility.1", "androidx.compose.animation.AnimatedVisibility", uiInspectionSource, "GoalsCard")
            ) {
                Column (modifier = Modifier.uiName("GoalsCard.Column.3", "androidx.compose.foundation.layout.Column", uiInspectionSource, "GoalsCard")) {
                    Spacer(Modifier.height(16.dp))

                    ExpandedCardContent(
                        proteinsGrams = proteins,
                        proteinsGoalGrams = proteinsGoal,
                        carbohydratesGrams = carbohydrates,
                        carbohydratesGoalGrams = carbohydratesGoal,
                        fatsGrams = fats,
                        fatsGoalGrams = fatsGoal,
                        modifier = (Modifier.fillMaxWidth()).uiName("GoalsCard.ExpandedCardContent.1", "com.maksimowiczm.foodyou.app.ui.home.goals.ExpandedCardContent", uiInspectionSource, "GoalsCard"),
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalsCardContent(
    energy: Int,
    energyGoal: Int,
    proteins: Int,
    carbohydrates: Int,
    fats: Int,
    proteinsPercentage: Float,
    carbsPercentage: Float,
    fatsPercentage: Float,
    modifier: Modifier = Modifier,
) {
    val nutrientsPalette = LocalNutrientsPalette.current
    val nutrientsOrder = LocalNutrientsOrder.current
    val energyFormatter = LocalEnergyFormatter.current
    val graphStyle = LocalGraphStyle.current

    val typography = MaterialTheme.typography
    val colorScheme = MaterialTheme.colorScheme
    val outlineColor = MaterialTheme.colorScheme.outline

    val caloriesString = buildAnnotatedString {
        withStyle(
            typography.headlineLargeEmphasized
                .merge(
                    color =
                        when {
                            energy < energyGoal -> colorScheme.onSurface
                            energy == energyGoal -> colorScheme.onSurface
                            else -> colorScheme.error
                        }
                )
                .toSpanStyle()
        ) {
            append(energyFormatter.formatEnergy(energy, withSuffix = false))
            append(" ")
        }
        withStyle(typography.bodyMedium.merge(outlineColor).toSpanStyle()) {
            val energyGoal = energyFormatter.formatEnergy(energyGoal)
            append("/ $energyGoal")
        }
    }

    val left = remember(energy, energyGoal) { energyGoal - energy }

    Row(
        modifier = (modifier).uiName("GoalsCardContent.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "GoalsCardContent"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.uiName("GoalsCardContent.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "GoalsCardContent")) {
            Text(text = caloriesString, style = typography.headlineLargeEmphasized, modifier = Modifier.uiName("app.ui.home.goals.GoalsCardContent.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "GoalsCardContent"))

            when {
                left > 0 ->
                    Text(
                        text = energyFormatter.energyLeft(left),
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyMediumEmphasized, modifier = Modifier.uiName("GoalsCardContent.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "GoalsCardContent")
                    )

                left == 0 ->
                    Text(
                        text = stringResource(Res.string.positive_goal_reached),
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyMediumEmphasized, modifier = Modifier.uiName("GoalsCardContent.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "GoalsCardContent")
                    )

                else ->
                    Text(
                        text = energyFormatter.energyExceeded(-left),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMediumEmphasized, modifier = Modifier.uiName("GoalsCardContent.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "GoalsCardContent")
                    )
            }
        }

        when (graphStyle) {
            GraphStyle.Bar ->
                Row(
                    modifier = (Modifier.height(64.dp)).uiName("GoalsCardContent.Row.2", "androidx.compose.foundation.layout.Row", uiInspectionSource, "GoalsCardContent"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    nutrientsOrder.forEach { field ->
                        when (field) {
                            NutrientsOrder.Proteins ->
                                MacroBar(
                                    progress = proteinsPercentage,
                                    containerColor =
                                        nutrientsPalette.proteinsOnSurfaceContainer.copy(
                                            alpha = .25f
                                        ),
                                    barColor = nutrientsPalette.proteinsOnSurfaceContainer, modifier = Modifier.uiName("GoalsCardContent.MacroBar.1", "com.maksimowiczm.foodyou.app.ui.home.goals.MacroBar", uiInspectionSource, "GoalsCardContent")
                                )

                            NutrientsOrder.Fats ->
                                MacroBar(
                                    progress = fatsPercentage,
                                    containerColor =
                                        nutrientsPalette.fatsOnSurfaceContainer.copy(alpha = .25f),
                                    barColor = nutrientsPalette.fatsOnSurfaceContainer, modifier = Modifier.uiName("GoalsCardContent.MacroBar.2", "com.maksimowiczm.foodyou.app.ui.home.goals.MacroBar", uiInspectionSource, "GoalsCardContent")
                                )

                            NutrientsOrder.Carbohydrates ->
                                MacroBar(
                                    progress = carbsPercentage,
                                    containerColor =
                                        nutrientsPalette.carbohydratesOnSurfaceContainer.copy(
                                            alpha = .25f
                                        ),
                                    barColor = nutrientsPalette.carbohydratesOnSurfaceContainer, modifier = Modifier.uiName("GoalsCardContent.MacroBar.3", "com.maksimowiczm.foodyou.app.ui.home.goals.MacroBar", uiInspectionSource, "GoalsCardContent")
                                )

                            NutrientsOrder.Other,
                            NutrientsOrder.Vitamins,
                            NutrientsOrder.Minerals -> Unit
                        }
                    }
                }

            GraphStyle.Pie -> {
                val slices =
                    nutrientsOrder.mapNotNull { field ->
                        when (field) {
                            NutrientsOrder.Proteins ->
                                MacroPieSlice(
                                    value = proteins,
                                    color = nutrientsPalette.proteinsOnSurfaceContainer,
                                )

                            NutrientsOrder.Fats ->
                                MacroPieSlice(
                                    value = fats,
                                    color = nutrientsPalette.fatsOnSurfaceContainer,
                                )

                            NutrientsOrder.Carbohydrates ->
                                MacroPieSlice(
                                    value = carbohydrates,
                                    color = nutrientsPalette.carbohydratesOnSurfaceContainer,
                                )

                            NutrientsOrder.Other,
                            NutrientsOrder.Vitamins,
                            NutrientsOrder.Minerals -> null
                        }
                    }

                MacroPieChart(slices = slices, modifier = Modifier.uiName("GoalsCardContent.MacroPieChart.1", "com.maksimowiczm.foodyou.app.ui.home.goals.MacroPieChart", uiInspectionSource, "GoalsCardContent"))
            }
        }
    }
}

@Composable
private fun MacroBar(
    progress: Float,
    containerColor: Color,
    barColor: Color,
    modifier: Modifier = Modifier,
    overflowColor: Color = MaterialTheme.colorScheme.error,
) {
    val containerFraction = (1 - progress).coerceIn(0f, 1f)
    val overflowFraction = (progress - 1).coerceIn(0f, 1f)

    Canvas(
        modifier =
            (modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 8.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 4.dp,
                    )
                )
                .fillMaxHeight()
                .width(24.dp)).uiName("MacroBar.Canvas.1", "androidx.compose.foundation.Canvas", uiInspectionSource, "MacroBar")
    ) {
        if (overflowFraction > 0f) {
            val barHeight = 1 - overflowFraction

            drawRect(
                color = barColor,
                size = Size(width = size.width, height = size.height * barHeight - 1.dp.toPx()),
            )
            drawRect(
                color = overflowColor,
                topLeft = Offset(x = 0f, y = size.height * barHeight + 1.dp.toPx()),
                size =
                    Size(width = size.width, height = size.height * overflowFraction - 1.dp.toPx()),
            )
        } else {
            drawRect(
                color = containerColor,
                size =
                    Size(width = size.width, height = size.height * containerFraction - 1.dp.toPx()),
            )
            drawRect(
                color = barColor,
                topLeft = Offset(x = 0f, y = size.height * containerFraction + 1.dp.toPx()),
                size = Size(width = size.width, height = size.height * progress - 1.dp.toPx()),
            )
        }
    }
}

@Composable
private fun ExpandedCardContent(
    proteinsGrams: Int,
    proteinsGoalGrams: Int,
    carbohydratesGrams: Int,
    carbohydratesGoalGrams: Int,
    fatsGrams: Int,
    fatsGoalGrams: Int,
    modifier: Modifier = Modifier,
) {
    val typography = MaterialTheme.typography
    val colorScheme = MaterialTheme.colorScheme
    val nutrientsPalette = LocalNutrientsPalette.current
    val nutrientsOrder = LocalNutrientsOrder.current
    val gramShort = stringResource(Res.string.unit_gram_short)

    val proteinsString = buildAnnotatedString {
        val color =
            if (proteinsGrams > proteinsGoalGrams) {
                colorScheme.error
            } else {
                nutrientsPalette.proteinsOnSurfaceContainer
            }

        withStyle(typography.headlineSmall.merge(color).toSpanStyle()) {
            append(" $proteinsGrams ")
        }
        withStyle(typography.bodyMedium.merge(colorScheme.outline).toSpanStyle()) {
            append("/ $proteinsGoalGrams $gramShort")
        }
    }

    val carbohydratesString = buildAnnotatedString {
        val color =
            if (carbohydratesGrams > carbohydratesGoalGrams) {
                colorScheme.error
            } else {
                nutrientsPalette.carbohydratesOnSurfaceContainer
            }

        withStyle(typography.headlineSmall.merge(color).toSpanStyle()) {
            append(" $carbohydratesGrams ")
        }
        withStyle(typography.bodyMedium.merge(colorScheme.outline).toSpanStyle()) {
            append("/ $carbohydratesGoalGrams $gramShort")
        }
    }

    val fatsString = buildAnnotatedString {
        val color =
            if (fatsGrams > fatsGoalGrams) {
                colorScheme.error
            } else {
                nutrientsPalette.fatsOnSurfaceContainer
            }

        withStyle(typography.headlineSmall.merge(color).toSpanStyle()) { append(" $fatsGrams ") }
        withStyle(typography.bodyMedium.merge(colorScheme.outline).toSpanStyle()) {
            append("/ $fatsGoalGrams $gramShort")
        }
    }

    Column(modifier = (modifier).uiName("ExpandedCardContent.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ExpandedCardContent")) {
        nutrientsOrder.forEach {
            Row(
                modifier = (Modifier.fillMaxWidth()).uiName("ExpandedCardContent.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "ExpandedCardContent"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (it) {
                    NutrientsOrder.Proteins -> {
                        RoundedSquare(LocalNutrientsPalette.current.proteinsOnSurfaceContainer, modifier = Modifier.uiName("ExpandedCardContent.RoundedSquare.1", "com.maksimowiczm.foodyou.app.ui.home.goals.RoundedSquare", uiInspectionSource, "ExpandedCardContent"))

                        Text(
                            text = stringResource(Res.string.nutriment_proteins),
                            modifier = (Modifier.weight(1f)).uiName("ExpandedCardContent.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ExpandedCardContent"),
                            style = MaterialTheme.typography.labelLarge,
                        )

                        Text(text = proteinsString, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.uiName("ExpandedCardContent.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ExpandedCardContent"))
                    }

                    NutrientsOrder.Carbohydrates -> {
                        RoundedSquare(LocalNutrientsPalette.current.carbohydratesOnSurfaceContainer, modifier = Modifier.uiName("ExpandedCardContent.RoundedSquare.2", "com.maksimowiczm.foodyou.app.ui.home.goals.RoundedSquare", uiInspectionSource, "ExpandedCardContent"))

                        Text(
                            text = stringResource(Res.string.nutriment_carbohydrates),
                            modifier = (Modifier.weight(1f)).uiName("ExpandedCardContent.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "ExpandedCardContent"),
                            style = MaterialTheme.typography.labelLarge,
                        )

                        Text(
                            text = carbohydratesString,
                            style = MaterialTheme.typography.headlineSmall, modifier = Modifier.uiName("ExpandedCardContent.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "ExpandedCardContent")
                        )
                    }

                    NutrientsOrder.Fats -> {
                        RoundedSquare(LocalNutrientsPalette.current.fatsOnSurfaceContainer, modifier = Modifier.uiName("ExpandedCardContent.RoundedSquare.3", "com.maksimowiczm.foodyou.app.ui.home.goals.RoundedSquare", uiInspectionSource, "ExpandedCardContent"))

                        Text(
                            text = stringResource(Res.string.nutriment_fats),
                            modifier = (Modifier.weight(1f)).uiName("ExpandedCardContent.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "ExpandedCardContent"),
                            style = MaterialTheme.typography.labelLarge,
                        )

                        Text(text = fatsString, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.uiName("ExpandedCardContent.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "ExpandedCardContent"))
                    }

                    NutrientsOrder.Other,
                    NutrientsOrder.Vitamins,
                    NutrientsOrder.Minerals -> Unit
                }
            }
        }
    }
}

@Composable
private fun RoundedSquare(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = (modifier.size(16.dp).clip(MaterialTheme.shapes.extraSmall)).uiName("RoundedSquare.Canvas.1", "androidx.compose.foundation.Canvas", uiInspectionSource, "RoundedSquare")) {
        drawRect(color = color, size = size)
    }
}

@Composable
private fun GoalsCardSkeleton(
    shimmer: Shimmer,
    expand: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FoodYouHomeCard(modifier = (modifier).uiName("GoalsCardSkeleton.FoodYouHomeCard.1", "com.maksimowiczm.foodyou.app.ui.home.shared.FoodYouHomeCard", uiInspectionSource, "GoalsCardSkeleton"), onClick = onClick, onLongClick = onLongClick) {
        Column((Modifier.padding(16.dp)).uiName("GoalsCardSkeleton.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "GoalsCardSkeleton")) {
            Row(
                modifier = (Modifier.fillMaxWidth()).uiName("GoalsCardSkeleton.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "GoalsCardSkeleton"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.uiName("GoalsCardSkeleton.Column.2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "GoalsCardSkeleton")) {
                    Spacer(
                        Modifier.shimmer(shimmer)
                            .width(60.dp)
                            .height(MaterialTheme.typography.headlineLargeEmphasized.toDp())
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    )

                    Spacer(
                        Modifier.shimmer(shimmer)
                            .size(120.dp, MaterialTheme.typography.bodyMediumEmphasized.toDp())
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    )
                }

                Row(
                    modifier = (Modifier.height(64.dp)).uiName("GoalsCardSkeleton.Row.2", "androidx.compose.foundation.layout.Row", uiInspectionSource, "GoalsCardSkeleton"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MacroBar(
                        progress = 1f,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        barColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = (Modifier.shimmer(shimmer)).uiName("GoalsCardSkeleton.MacroBar.1", "com.maksimowiczm.foodyou.app.ui.home.goals.MacroBar", uiInspectionSource, "GoalsCardSkeleton"),
                    )

                    MacroBar(
                        progress = 1f,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        barColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = (Modifier.shimmer(shimmer)).uiName("GoalsCardSkeleton.MacroBar.2", "com.maksimowiczm.foodyou.app.ui.home.goals.MacroBar", uiInspectionSource, "GoalsCardSkeleton"),
                    )

                    MacroBar(
                        progress = 1f,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        barColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = (Modifier.shimmer(shimmer)).uiName("GoalsCardSkeleton.MacroBar.3", "com.maksimowiczm.foodyou.app.ui.home.goals.MacroBar", uiInspectionSource, "GoalsCardSkeleton"),
                    )
                }
            }

            AnimatedVisibility(
                visible = expand,
                enter = expandVertically(),
                exit = shrinkVertically(), modifier = Modifier.uiName("GoalsCardSkeleton.AnimatedVisibility.1", "androidx.compose.animation.AnimatedVisibility", uiInspectionSource, "GoalsCardSkeleton")
            ) {
                Column (modifier = Modifier.uiName("GoalsCardSkeleton.Column.3", "androidx.compose.foundation.layout.Column", uiInspectionSource, "GoalsCardSkeleton")) {
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = (Modifier.fillMaxWidth()).uiName("GoalsCardSkeleton.Row.3", "androidx.compose.foundation.layout.Row", uiInspectionSource, "GoalsCardSkeleton"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RoundedSquare(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = (Modifier.shimmer(shimmer)).uiName("GoalsCardSkeleton.RoundedSquare.1", "com.maksimowiczm.foodyou.app.ui.home.goals.RoundedSquare", uiInspectionSource, "GoalsCardSkeleton"),
                        )

                        Spacer(
                            Modifier.shimmer(shimmer)
                                .width(100.dp)
                                .height(MaterialTheme.typography.labelLarge.toDp())
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        )

                        Spacer(Modifier.weight(1f))

                        Spacer(
                            Modifier.shimmer(shimmer)
                                .size(80.dp, MaterialTheme.typography.headlineSmall.toDp() - 4.dp)
                                .padding(vertical = 2.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        )
                    }

                    Row(
                        modifier = (Modifier.fillMaxWidth()).uiName("GoalsCardSkeleton.Row.4", "androidx.compose.foundation.layout.Row", uiInspectionSource, "GoalsCardSkeleton"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RoundedSquare(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = (Modifier.shimmer(shimmer)).uiName("GoalsCardSkeleton.RoundedSquare.2", "com.maksimowiczm.foodyou.app.ui.home.goals.RoundedSquare", uiInspectionSource, "GoalsCardSkeleton"),
                        )

                        Spacer(
                            Modifier.shimmer(shimmer)
                                .width(100.dp)
                                .height(MaterialTheme.typography.labelLarge.toDp())
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        )

                        Spacer(Modifier.weight(1f))

                        Spacer(
                            Modifier.shimmer(shimmer)
                                .size(80.dp, MaterialTheme.typography.headlineSmall.toDp() - 4.dp)
                                .padding(vertical = 2.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        )
                    }

                    Row(
                        modifier = (Modifier.fillMaxWidth()).uiName("GoalsCardSkeleton.Row.5", "androidx.compose.foundation.layout.Row", uiInspectionSource, "GoalsCardSkeleton"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RoundedSquare(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = (Modifier.shimmer(shimmer)).uiName("GoalsCardSkeleton.RoundedSquare.3", "com.maksimowiczm.foodyou.app.ui.home.goals.RoundedSquare", uiInspectionSource, "GoalsCardSkeleton"),
                        )

                        Spacer(
                            Modifier.shimmer(shimmer)
                                .width(100.dp)
                                .height(MaterialTheme.typography.labelLarge.toDp())
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        )

                        Spacer(Modifier.weight(1f))

                        Spacer(
                            Modifier.shimmer(shimmer)
                                .size(80.dp, MaterialTheme.typography.headlineSmall.toDp() - 4.dp)
                                .padding(vertical = 2.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        )
                    }
                }
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.home.goals",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/goals/GoalsCard.kt",
)
