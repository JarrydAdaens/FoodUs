package com.maksimowiczm.foodyou.app.ui.food.shared.component

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.theme.LocalNutrientsPalette
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalNutrientsOrder
import com.maksimowiczm.foodyou.common.compose.utility.formatClipZeros
import com.maksimowiczm.foodyou.common.domain.food.NutrientValue
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.food.NutritionFactsField
import com.maksimowiczm.foodyou.common.domain.food.get
import com.maksimowiczm.foodyou.settings.domain.entity.NutrientsOrder
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun NutrientList(
    facts: NutritionFacts,
    modifier: Modifier = Modifier,
    incompleteValue: (NutrientValue.Incomplete) -> (@Composable () -> Unit) =
        NutrientListDefaults::incompleteValue,
) {
    val order = LocalNutrientsOrder.current

    Column(modifier = (modifier).uiName("app.ui.food.shared.component.NutrientList.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "NutrientList"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Energy(facts, incompleteValue, modifier = Modifier.uiName("NutrientList.Energy.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Energy", uiInspectionSource, "NutrientList"))

        order.forEach {
            when (it) {
                NutrientsOrder.Proteins -> Proteins(facts, incompleteValue, modifier = Modifier.uiName("app.ui.food.shared.component.NutrientList.Proteins.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Proteins", uiInspectionSource, "NutrientList"))
                NutrientsOrder.Fats -> Fats(facts, incompleteValue, modifier = Modifier.uiName("app.ui.food.shared.component.NutrientList.Fats.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Fats", uiInspectionSource, "NutrientList"))
                NutrientsOrder.Carbohydrates -> Carbohydrates(facts, incompleteValue, modifier = Modifier.uiName("app.ui.food.shared.component.NutrientList.Carbohydrates.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Carbohydrates", uiInspectionSource, "NutrientList"))
                NutrientsOrder.Other -> Other(facts, incompleteValue, modifier = Modifier.uiName("app.ui.food.shared.component.NutrientList.Other.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Other", uiInspectionSource, "NutrientList"))
                NutrientsOrder.Vitamins -> Vitamins(facts, incompleteValue, modifier = Modifier.uiName("app.ui.food.shared.component.NutrientList.Vitamins.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Vitamins", uiInspectionSource, "NutrientList"))
                NutrientsOrder.Minerals -> Minerals(facts, incompleteValue, modifier = Modifier.uiName("app.ui.food.shared.component.NutrientList.Minerals.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Minerals", uiInspectionSource, "NutrientList"))
            }
        }
    }
}

@Composable
private fun Energy(
    facts: NutritionFacts,
    incompleteValue: (NutrientValue.Incomplete) -> (@Composable () -> Unit),
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp),
) {
    val energyFormatter = LocalEnergyFormatter.current

    Nutrient(
        label = { Text(stringResource(Res.string.unit_energy), modifier = Modifier.uiName("Energy.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Energy")) },
        value = {
            when (val nut = facts.energy) {
                is NutrientValue.Complete -> Text(energyFormatter.formatEnergy(nut.value), modifier = Modifier.uiName("Energy.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "Energy"))
                is NutrientValue.Incomplete -> incompleteValue(nut)()
            }
        },
        modifier = (modifier).uiName("Energy.Nutrient.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Energy"),
        contentPadding = contentPadding,
    )
}

@Composable
private fun Proteins(
    facts: NutritionFacts,
    incompleteValue: (NutrientValue.Incomplete) -> (@Composable () -> Unit),
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp),
) {
    Nutrient(
        label = { Text(stringResource(Res.string.nutriment_proteins), modifier = Modifier.uiName("Proteins.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Proteins")) },
        value = { NutrientDisplay(facts.proteins, incompleteValue) },
        modifier = (modifier).uiName("Proteins.Nutrient.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Proteins"),
        contentPadding = contentPadding,
        shape = MaterialTheme.shapes.medium,
        containerColor =
            LocalNutrientsPalette.current.proteinsOnSurfaceContainer.copy(alpha = .33f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun Fats(
    facts: NutritionFacts,
    incompleteValue: (NutrientValue.Incomplete) -> (@Composable () -> Unit),
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp),
) {
    val fats = @Composable { NutrientDisplay(facts.fats, incompleteValue) }
    val saturatedFats = @Composable { NutrientDisplay(facts.saturatedFats, incompleteValue) }
    val transFats = @Composable { NutrientDisplay(facts.transFats, incompleteValue) }
    val monounsaturatedFats =
        @Composable { NutrientDisplay(facts.monounsaturatedFats, incompleteValue) }
    val polyunsaturatedFats =
        @Composable { NutrientDisplay(facts.polyunsaturatedFats, incompleteValue) }
    val omega3Fats = @Composable { NutrientDisplay(facts.omega3, incompleteValue) }
    val omega6Fats = @Composable { NutrientDisplay(facts.omega6, incompleteValue) }

    NutrientGroup(
        title = {
            Nutrient(
                label = { Text(stringResource(Res.string.nutriment_fats), modifier = Modifier.uiName("app.ui.food.shared.component.Fats.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Fats")) },
                value = fats,
                contentPadding = contentPadding,
                shape = MaterialTheme.shapes.medium,
                containerColor =
                    LocalNutrientsPalette.current.fatsOnSurfaceContainer.copy(alpha = .33f),
                contentColor = MaterialTheme.colorScheme.onSurface, modifier = Modifier.uiName("Fats.Nutrient.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Fats")
            )
        },
        modifier = (modifier).uiName("Fats.NutrientGroup.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.NutrientGroup", uiInspectionSource, "Fats"),
    ) {
        Nutrient(
            label = { Text(stringResource(Res.string.nutriment_saturated_fats), modifier = Modifier.uiName("Fats.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "Fats")) },
            value = saturatedFats, modifier = Modifier.uiName("Fats.Nutrient.2", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Fats")
        )
        Nutrient(
            label = { Text(stringResource(Res.string.nutriment_trans_fats), modifier = Modifier.uiName("Fats.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "Fats")) },
            value = transFats, modifier = Modifier.uiName("Fats.Nutrient.3", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Fats")
        )
        Nutrient(
            label = { Text(stringResource(Res.string.nutriment_monounsaturated_fats), modifier = Modifier.uiName("Fats.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "Fats")) },
            value = monounsaturatedFats, modifier = Modifier.uiName("Fats.Nutrient.4", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Fats")
        )

        NutrientGroup(
            title = {
                Nutrient(
                    label = { Text(stringResource(Res.string.nutriment_polyunsaturated_fats), modifier = Modifier.uiName("Fats.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "Fats")) },
                    value = polyunsaturatedFats, modifier = Modifier.uiName("Fats.Nutrient.5", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Fats")
                )
            }, modifier = Modifier.uiName("Fats.NutrientGroup.2", "com.maksimowiczm.foodyou.app.ui.food.shared.component.NutrientGroup", uiInspectionSource, "Fats")
        ) {
            Nutrient(
                label = { Text(stringResource(Res.string.nutriment_omega_3), modifier = Modifier.uiName("Fats.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "Fats")) },
                value = omega3Fats, modifier = Modifier.uiName("Fats.Nutrient.6", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Fats")
            )
            Nutrient(
                label = { Text(stringResource(Res.string.nutriment_omega_6), modifier = Modifier.uiName("Fats.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "Fats")) },
                value = omega6Fats, modifier = Modifier.uiName("Fats.Nutrient.7", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Fats")
            )
        }
    }
}

@Composable
private fun Carbohydrates(
    facts: NutritionFacts,
    incompleteValue: (NutrientValue.Incomplete) -> (@Composable () -> Unit),
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp),
) {
    NutrientGroup(
        title = {
            Nutrient(
                label = { Text(stringResource(Res.string.nutriment_carbohydrates), modifier = Modifier.uiName("app.ui.food.shared.component.Carbohydrates.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Carbohydrates")) },
                value = { NutrientDisplay(facts.carbohydrates, incompleteValue) },
                modifier = (modifier).uiName("Carbohydrates.Nutrient.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Carbohydrates"),
                contentPadding = contentPadding,
                shape = MaterialTheme.shapes.medium,
                containerColor =
                    LocalNutrientsPalette.current.carbohydratesOnSurfaceContainer.copy(
                        alpha = .33f
                    ),
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        }, modifier = Modifier.uiName("Carbohydrates.NutrientGroup.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.NutrientGroup", uiInspectionSource, "Carbohydrates")
    ) {
        NutrientGroup(
            title = {
                Nutrient(
                    label = { Text(stringResource(Res.string.nutriment_sugars), modifier = Modifier.uiName("Carbohydrates.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "Carbohydrates")) },
                    value = { NutrientDisplay(facts.sugars, incompleteValue) }, modifier = Modifier.uiName("Carbohydrates.Nutrient.2", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Carbohydrates")
                )
            }, modifier = Modifier.uiName("Carbohydrates.NutrientGroup.2", "com.maksimowiczm.foodyou.app.ui.food.shared.component.NutrientGroup", uiInspectionSource, "Carbohydrates")
        ) {
            Nutrient(
                label = { Text(stringResource(Res.string.nutriment_added_sugars), modifier = Modifier.uiName("Carbohydrates.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "Carbohydrates")) },
                value = { NutrientDisplay(facts.addedSugars, incompleteValue) }, modifier = Modifier.uiName("Carbohydrates.Nutrient.3", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Carbohydrates")
            )
        }
        NutrientGroup(
            title = {
                Nutrient(
                    label = { Text(stringResource(Res.string.nutriment_fiber), modifier = Modifier.uiName("Carbohydrates.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "Carbohydrates")) },
                    value = { NutrientDisplay(facts.dietaryFiber, incompleteValue) }, modifier = Modifier.uiName("Carbohydrates.Nutrient.4", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Carbohydrates")
                )
            }, modifier = Modifier.uiName("Carbohydrates.NutrientGroup.3", "com.maksimowiczm.foodyou.app.ui.food.shared.component.NutrientGroup", uiInspectionSource, "Carbohydrates")
        ) {
            Nutrient(
                label = { Text(stringResource(Res.string.nutriment_soluble_fiber), modifier = Modifier.uiName("Carbohydrates.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "Carbohydrates")) },
                value = { NutrientDisplay(facts.solubleFiber, incompleteValue) }, modifier = Modifier.uiName("Carbohydrates.Nutrient.5", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Carbohydrates")
            )
            Nutrient(
                label = { Text(stringResource(Res.string.nutriment_insoluble_fiber), modifier = Modifier.uiName("Carbohydrates.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "Carbohydrates")) },
                value = { NutrientDisplay(facts.insolubleFiber, incompleteValue) }, modifier = Modifier.uiName("Carbohydrates.Nutrient.6", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Carbohydrates")
            )
        }
    }
}

@Composable
private fun Other(
    facts: NutritionFacts,
    incompleteValue: (NutrientValue.Incomplete) -> (@Composable () -> Unit),
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp),
) {
    val mg = stringResource(Res.string.unit_milligram_short)

    Column(modifier = (modifier).uiName("app.ui.food.shared.component.Other.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "Other")) {
        Text(
            text = stringResource(Res.string.headline_other),
            modifier = (Modifier.padding(contentPadding)).uiName("app.ui.food.shared.component.Other.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Other"),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
        )

        Nutrient(
            label = { Text(stringResource(Res.string.nutriment_salt), modifier = Modifier.uiName("Other.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "Other")) },
            value = { NutrientDisplay(facts.salt, incompleteValue) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Other.Nutrient.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Other")
        )

        Nutrient(
            label = { Text(stringResource(Res.string.nutriment_cholesterol), modifier = Modifier.uiName("Other.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "Other")) },
            value = { NutrientDisplay(facts.cholesterol * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Other.Nutrient.2", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Other")
        )

        Nutrient(
            label = { Text(stringResource(Res.string.nutriment_caffeine), modifier = Modifier.uiName("Other.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "Other")) },
            value = { NutrientDisplay(facts.caffeine * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Other.Nutrient.3", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Other")
        )
    }
}

@Composable
private fun Vitamins(
    facts: NutritionFacts,
    incompleteValue: (NutrientValue.Incomplete) -> (@Composable () -> Unit),
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp),
) {
    val mg = stringResource(Res.string.unit_milligram_short)
    val mcg = stringResource(Res.string.unit_microgram_short)

    Column(modifier = (modifier).uiName("app.ui.food.shared.component.Vitamins.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "Vitamins")) {
        Text(
            text = stringResource(Res.string.headline_vitamins),
            modifier = (Modifier.padding(contentPadding)).uiName("app.ui.food.shared.component.Vitamins.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Vitamins"),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
        )

        Nutrient(
            label = { Text(stringResource(Res.string.vitamin_a), modifier = Modifier.uiName("Vitamins.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "Vitamins")) },
            value = { NutrientDisplay(facts.vitaminA * 1_000_000.0, incompleteValue, mcg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Vitamins.Nutrient.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Vitamins")
        )

        Nutrient(
            label = { Text(stringResource(Res.string.vitamin_b1), modifier = Modifier.uiName("Vitamins.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "Vitamins")) },
            value = { NutrientDisplay(facts.vitaminB1 * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Vitamins.Nutrient.2", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Vitamins")
        )

        Nutrient(
            label = { Text(stringResource(Res.string.vitamin_b2), modifier = Modifier.uiName("Vitamins.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "Vitamins")) },
            value = { NutrientDisplay(facts.vitaminB2 * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Vitamins.Nutrient.3", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Vitamins")
        )

        Nutrient(
            label = { Text(stringResource(Res.string.vitamin_b3), modifier = Modifier.uiName("Vitamins.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "Vitamins")) },
            value = { NutrientDisplay(facts.vitaminB3 * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Vitamins.Nutrient.4", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Vitamins")
        )

        Nutrient(
            label = { Text(stringResource(Res.string.vitamin_b5), modifier = Modifier.uiName("Vitamins.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "Vitamins")) },
            value = { NutrientDisplay(facts.vitaminB5 * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Vitamins.Nutrient.5", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Vitamins")
        )

        Nutrient(
            label = { Text(stringResource(Res.string.vitamin_b6), modifier = Modifier.uiName("Vitamins.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "Vitamins")) },
            value = { NutrientDisplay(facts.vitaminB6 * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Vitamins.Nutrient.6", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Vitamins")
        )

        Nutrient(
            label = { Text(stringResource(Res.string.vitamin_b7), modifier = Modifier.uiName("Vitamins.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "Vitamins")) },
            value = { NutrientDisplay(facts.vitaminB7 * 1_000_000.0, incompleteValue, mcg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Vitamins.Nutrient.7", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Vitamins")
        )

        Nutrient(
            label = { Text(stringResource(Res.string.vitamin_b9), modifier = Modifier.uiName("Vitamins.Text.9", "androidx.compose.material3.Text", uiInspectionSource, "Vitamins")) },
            value = { NutrientDisplay(facts.vitaminB9 * 1_000_000.0, incompleteValue, mcg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Vitamins.Nutrient.8", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Vitamins")
        )

        Nutrient(
            label = { Text(stringResource(Res.string.vitamin_b12), modifier = Modifier.uiName("Vitamins.Text.10", "androidx.compose.material3.Text", uiInspectionSource, "Vitamins")) },
            value = { NutrientDisplay(facts.vitaminB12 * 1_000_000.0, incompleteValue, mcg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Vitamins.Nutrient.9", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Vitamins")
        )

        Nutrient(
            label = { Text(stringResource(Res.string.vitamin_c), modifier = Modifier.uiName("Vitamins.Text.11", "androidx.compose.material3.Text", uiInspectionSource, "Vitamins")) },
            value = { NutrientDisplay(facts.vitaminC * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Vitamins.Nutrient.10", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Vitamins")
        )

        Nutrient(
            label = { Text(stringResource(Res.string.vitamin_d), modifier = Modifier.uiName("Vitamins.Text.12", "androidx.compose.material3.Text", uiInspectionSource, "Vitamins")) },
            value = { NutrientDisplay(facts.vitaminD * 1_000_000.0, incompleteValue, mcg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Vitamins.Nutrient.11", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Vitamins")
        )

        Nutrient(
            label = { Text(stringResource(Res.string.vitamin_e), modifier = Modifier.uiName("Vitamins.Text.13", "androidx.compose.material3.Text", uiInspectionSource, "Vitamins")) },
            value = { NutrientDisplay(facts.vitaminE * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Vitamins.Nutrient.12", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Vitamins")
        )

        Nutrient(
            label = { Text(stringResource(Res.string.vitamin_k), modifier = Modifier.uiName("Vitamins.Text.14", "androidx.compose.material3.Text", uiInspectionSource, "Vitamins")) },
            value = { NutrientDisplay(facts.vitaminK * 1_000_000.0, incompleteValue, mcg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Vitamins.Nutrient.13", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Vitamins")
        )
    }
}

@Composable
private fun Minerals(
    facts: NutritionFacts,
    incompleteValue: (NutrientValue.Incomplete) -> (@Composable () -> Unit),
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp),
) {
    val mg = stringResource(Res.string.unit_milligram_short)
    val mcg = stringResource(Res.string.unit_microgram_short)

    Column(modifier = (modifier).uiName("app.ui.food.shared.component.Minerals.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "Minerals")) {
        Text(
            text = stringResource(Res.string.headline_minerals),
            modifier = (Modifier.padding(contentPadding)).uiName("app.ui.food.shared.component.Minerals.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Minerals"),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
        )

        // Manganese
        Nutrient(
            label = { Text(stringResource(Res.string.mineral_manganese), modifier = Modifier.uiName("Minerals.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "Minerals")) },
            value = { NutrientDisplay(facts.manganese * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Minerals.Nutrient.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Minerals")
        )

        // Magnesium
        Nutrient(
            label = { Text(stringResource(Res.string.mineral_magnesium), modifier = Modifier.uiName("Minerals.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "Minerals")) },
            value = { NutrientDisplay(facts.magnesium * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Minerals.Nutrient.2", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Minerals")
        )

        // Potassium
        Nutrient(
            label = { Text(stringResource(Res.string.mineral_potassium), modifier = Modifier.uiName("Minerals.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "Minerals")) },
            value = { NutrientDisplay(facts.potassium * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Minerals.Nutrient.3", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Minerals")
        )

        // Calcium
        Nutrient(
            label = { Text(stringResource(Res.string.mineral_calcium), modifier = Modifier.uiName("Minerals.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "Minerals")) },
            value = { NutrientDisplay(facts.calcium * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Minerals.Nutrient.4", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Minerals")
        )

        // Copper
        Nutrient(
            label = { Text(stringResource(Res.string.mineral_copper), modifier = Modifier.uiName("Minerals.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "Minerals")) },
            value = { NutrientDisplay(facts.copper * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Minerals.Nutrient.5", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Minerals")
        )

        // Zinc
        Nutrient(
            label = { Text(stringResource(Res.string.mineral_zinc), modifier = Modifier.uiName("Minerals.Text.7", "androidx.compose.material3.Text", uiInspectionSource, "Minerals")) },
            value = { NutrientDisplay(facts.zinc * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Minerals.Nutrient.6", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Minerals")
        )

        // Sodium
        Nutrient(
            label = { Text(stringResource(Res.string.mineral_sodium), modifier = Modifier.uiName("Minerals.Text.8", "androidx.compose.material3.Text", uiInspectionSource, "Minerals")) },
            value = { NutrientDisplay(facts.sodium * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Minerals.Nutrient.7", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Minerals")
        )

        // Iron
        Nutrient(
            label = { Text(stringResource(Res.string.mineral_iron), modifier = Modifier.uiName("Minerals.Text.9", "androidx.compose.material3.Text", uiInspectionSource, "Minerals")) },
            value = { NutrientDisplay(facts.iron * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Minerals.Nutrient.8", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Minerals")
        )

        // Phosphorus
        Nutrient(
            label = { Text(stringResource(Res.string.mineral_phosphorus), modifier = Modifier.uiName("Minerals.Text.10", "androidx.compose.material3.Text", uiInspectionSource, "Minerals")) },
            value = { NutrientDisplay(facts.phosphorus * 1_000.0, incompleteValue, mg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Minerals.Nutrient.9", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Minerals")
        )

        // Selenium
        Nutrient(
            label = { Text(stringResource(Res.string.mineral_selenium), modifier = Modifier.uiName("Minerals.Text.11", "androidx.compose.material3.Text", uiInspectionSource, "Minerals")) },
            value = { NutrientDisplay(facts.selenium * 1_000_000.0, incompleteValue, mcg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Minerals.Nutrient.10", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Minerals")
        )

        // Iodine
        Nutrient(
            label = { Text(stringResource(Res.string.mineral_iodine), modifier = Modifier.uiName("Minerals.Text.12", "androidx.compose.material3.Text", uiInspectionSource, "Minerals")) },
            value = { NutrientDisplay(facts.iodine * 1_000_000.0, incompleteValue, mcg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Minerals.Nutrient.11", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Minerals")
        )

        // Chromium
        Nutrient(
            label = { Text(stringResource(Res.string.mineral_chromium), modifier = Modifier.uiName("Minerals.Text.13", "androidx.compose.material3.Text", uiInspectionSource, "Minerals")) },
            value = { NutrientDisplay(facts.chromium * 1_000_000.0, incompleteValue, mcg) },
            contentPadding = contentPadding, modifier = Modifier.uiName("Minerals.Nutrient.12", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "Minerals")
        )
    }
}

@Composable
private fun Nutrient(
    label: @Composable () -> Unit,
    value: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    shape: Shape = RectangleShape,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = LocalContentColor.current,
) {
    Surface(
        modifier = (modifier).uiName("Nutrient.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "Nutrient"),
        color = containerColor,
        contentColor = contentColor,
        shape = shape,
    ) {
        Row(
            modifier = (Modifier.padding(contentPadding).fillMaxWidth()).uiName("Nutrient.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "Nutrient"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                label()
                value()
            }
        }
    }
}

@Composable
private fun NutrientGroup(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = (modifier).uiName("NutrientGroup.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "NutrientGroup")) {
        title()
        Column(modifier = (Modifier.padding(start = 16.dp)).uiName("NutrientGroup.Column.2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "NutrientGroup")) { content() }
    }
}

@Composable
private fun NutrientDisplay(
    nutrientValue: NutrientValue,
    incompleteValue: (NutrientValue.Incomplete) -> (@Composable () -> Unit),
    suffix: String = stringResource(Res.string.unit_gram_short),
) {
    when (nutrientValue) {
        is NutrientValue.Complete -> {
            val value = nutrientValue.value.formatClipZeros() + " " + suffix
            Text(value, modifier = Modifier.uiName("NutrientDisplay.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "NutrientDisplay"))
        }

        is NutrientValue.Incomplete -> {
            incompleteValue(nutrientValue)()
        }
    }
}

object NutrientListDefaults {
    val incompletePrefix: String
        @Composable get() = "*"

    fun incompleteValue(value: NutrientValue.Incomplete): @Composable () -> Unit = {
        val value = value.value?.formatClipZeros()

        val str =
            value?.let { "$incompletePrefix $it ${stringResource(Res.string.unit_gram_short)}" }
                ?: stringResource(Res.string.not_available_short)

        Text(text = str, color = MaterialTheme.colorScheme.outline, modifier = Modifier.uiName("NutrientListDefaults.incompleteValue.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "NutrientListDefaults.incompleteValue"))
    }
}

/** Compact review of supplied snapshot values. Unknown nutrients are omitted rather than rendered
 * as zero. Uses the same labels and gram -> mg/mcg presentation as the full nutrient list.
 */
@Composable
fun KnownNutrientList(
    facts: NutritionFacts,
    modifier: Modifier = Modifier,
    excludedFields: Set<NutritionFactsField> = emptySet(),
) {
    val energyFormatter = LocalEnergyFormatter.current
    Column((modifier).uiName("KnownNutrientList.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "KnownNutrientList")) {
        NutritionFactsField.entries.forEach { field ->
            val value = facts[field].value
            if (value != null && field !in excludedFields) {
                Nutrient(
                    label = { Text(stringResource(field.label()), modifier = Modifier.uiName("KnownNutrientList.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "KnownNutrientList")) },
                    value = {
                        if (field == NutritionFactsField.Energy) {
                            Text(energyFormatter.formatEnergy(value), modifier = Modifier.uiName("KnownNutrientList.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "KnownNutrientList"))
                        } else {
                            val (multiplier, suffix) = when (field) {
                                NutritionFactsField.VitaminA, NutritionFactsField.VitaminB7,
                                NutritionFactsField.VitaminB9, NutritionFactsField.VitaminB12,
                                NutritionFactsField.VitaminD, NutritionFactsField.VitaminK,
                                NutritionFactsField.Selenium, NutritionFactsField.Iodine,
                                NutritionFactsField.Chromium ->
                                1_000_000.0 to stringResource(Res.string.unit_microgram_short)
                                NutritionFactsField.Cholesterol, NutritionFactsField.Caffeine,
                                NutritionFactsField.VitaminB1, NutritionFactsField.VitaminB2,
                                NutritionFactsField.VitaminB3, NutritionFactsField.VitaminB5,
                                NutritionFactsField.VitaminB6, NutritionFactsField.VitaminC,
                                NutritionFactsField.VitaminE, NutritionFactsField.Manganese,
                                NutritionFactsField.Magnesium, NutritionFactsField.Potassium,
                                NutritionFactsField.Calcium, NutritionFactsField.Copper,
                                NutritionFactsField.Zinc, NutritionFactsField.Sodium,
                                NutritionFactsField.Iron, NutritionFactsField.Phosphorus ->
                                1_000.0 to stringResource(Res.string.unit_milligram_short)
                                else -> 1.0 to stringResource(Res.string.unit_gram_short)
                            }
                            Text("${(value * multiplier).formatClipZeros()} $suffix", modifier = Modifier.uiName("KnownNutrientList.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "KnownNutrientList"))
                        }
                    }, modifier = Modifier.uiName("KnownNutrientList.Nutrient.1", "com.maksimowiczm.foodyou.app.ui.food.shared.component.Nutrient", uiInspectionSource, "KnownNutrientList")
                )
            }
        }
    }
}

private fun NutritionFactsField.label() = when (this) {
    NutritionFactsField.Energy -> Res.string.unit_energy
    NutritionFactsField.Proteins -> Res.string.nutriment_proteins
    NutritionFactsField.Fats -> Res.string.nutriment_fats
    NutritionFactsField.SaturatedFats -> Res.string.nutriment_saturated_fats
    NutritionFactsField.TransFats -> Res.string.nutriment_trans_fats
    NutritionFactsField.MonounsaturatedFats -> Res.string.nutriment_monounsaturated_fats
    NutritionFactsField.PolyunsaturatedFats -> Res.string.nutriment_polyunsaturated_fats
    NutritionFactsField.Omega3 -> Res.string.nutriment_omega_3
    NutritionFactsField.Omega6 -> Res.string.nutriment_omega_6
    NutritionFactsField.Carbohydrates -> Res.string.nutriment_carbohydrates
    NutritionFactsField.Sugars -> Res.string.nutriment_sugars
    NutritionFactsField.AddedSugars -> Res.string.nutriment_added_sugars
    NutritionFactsField.DietaryFiber -> Res.string.nutriment_fiber
    NutritionFactsField.SolubleFiber -> Res.string.nutriment_soluble_fiber
    NutritionFactsField.InsolubleFiber -> Res.string.nutriment_insoluble_fiber
    NutritionFactsField.Salt -> Res.string.nutriment_salt
    NutritionFactsField.Cholesterol -> Res.string.nutriment_cholesterol
    NutritionFactsField.Caffeine -> Res.string.nutriment_caffeine
    NutritionFactsField.VitaminA -> Res.string.vitamin_a
    NutritionFactsField.VitaminB1 -> Res.string.vitamin_b1
    NutritionFactsField.VitaminB2 -> Res.string.vitamin_b2
    NutritionFactsField.VitaminB3 -> Res.string.vitamin_b3
    NutritionFactsField.VitaminB5 -> Res.string.vitamin_b5
    NutritionFactsField.VitaminB6 -> Res.string.vitamin_b6
    NutritionFactsField.VitaminB7 -> Res.string.vitamin_b7
    NutritionFactsField.VitaminB9 -> Res.string.vitamin_b9
    NutritionFactsField.VitaminB12 -> Res.string.vitamin_b12
    NutritionFactsField.VitaminC -> Res.string.vitamin_c
    NutritionFactsField.VitaminD -> Res.string.vitamin_d
    NutritionFactsField.VitaminE -> Res.string.vitamin_e
    NutritionFactsField.VitaminK -> Res.string.vitamin_k
    NutritionFactsField.Manganese -> Res.string.mineral_manganese
    NutritionFactsField.Magnesium -> Res.string.mineral_magnesium
    NutritionFactsField.Potassium -> Res.string.mineral_potassium
    NutritionFactsField.Calcium -> Res.string.mineral_calcium
    NutritionFactsField.Copper -> Res.string.mineral_copper
    NutritionFactsField.Zinc -> Res.string.mineral_zinc
    NutritionFactsField.Sodium -> Res.string.mineral_sodium
    NutritionFactsField.Iron -> Res.string.mineral_iron
    NutritionFactsField.Phosphorus -> Res.string.mineral_phosphorus
    NutritionFactsField.Selenium -> Res.string.mineral_selenium
    NutritionFactsField.Iodine -> Res.string.mineral_iodine
    NutritionFactsField.Chromium -> Res.string.mineral_chromium
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.shared.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/component/NutrientList.kt",
)
