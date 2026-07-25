package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.maksimowiczm.foodyou.app.ui.common.form.FormField
import com.maksimowiczm.foodyou.app.ui.common.form.nonBlankStringValidator
import com.maksimowiczm.foodyou.app.ui.common.form.nonNegativeDoubleValidator
import com.maksimowiczm.foodyou.app.ui.common.form.nullableDoubleParser
import com.maksimowiczm.foodyou.app.ui.common.form.nullableStringParser
import com.maksimowiczm.foodyou.app.ui.common.form.positiveDoubleValidator
import com.maksimowiczm.foodyou.app.ui.common.form.rememberFormField
import com.maksimowiczm.foodyou.app.ui.common.form.stringParser
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalEnergyFormatter
import com.maksimowiczm.foodyou.common.compose.utility.formatClipZeros
import com.maksimowiczm.foodyou.common.domain.food.NutrientsHelper
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import org.jetbrains.compose.resources.stringResource

internal enum class QuickAddFormFieldError {
    Required,
    InvalidNumber,
    NegativeNumber,
    NotPositiveNumber;

    @Composable
    fun stringResource(): String =
        when (this) {
            Required -> stringResource(Res.string.neutral_required)
            InvalidNumber -> stringResource(Res.string.error_invalid_number)
            NegativeNumber -> stringResource(Res.string.error_invalid_number)
            NotPositiveNumber -> stringResource(Res.string.error_value_must_be_positive)
        }
}

/**
 * Seeds the shared Quick Add form used by both create and edit-historical flows.
 *
 * @param servingCount Optional serving count (Story 18). Defaults to 1 so a new entry starts at one
 *   serving; historical entries pass their stored value (or 1 when it predates Story 18).
 * @param weightGrams Optional total weight in grams (Story 18); null when not supplied.
 * @param fibre Optional dietary fibre in grams (Story 18); persisted via `NutritionFacts`.
 */
@Composable
internal fun rememberQuickAddFormState(
    name: String = "",
    proteins: Double? = null,
    carbohydrates: Double? = null,
    fats: Double? = null,
    energy: Double? = null,
    description: String? = null,
    fibre: Double? = null,
    servingCount: Double? = 1.0,
    weightGrams: Double? = null,
): QuickAddFormState {
    val energyFormatter = LocalEnergyFormatter.current
    val energyInUserUnit = energy?.let(energyFormatter::fromKcal)

    val nameForm =
        rememberFormField(
            initialValue = name,
            parser = stringParser(),
            validator = nonBlankStringValidator(onEmpty = { QuickAddFormFieldError.Required }),
            textFieldState = rememberTextFieldState(name),
        )

    val descriptionForm =
        rememberFormField(
            initialValue = description,
            parser = nullableStringParser<QuickAddFormFieldError>(),
            textFieldState = rememberTextFieldState(description ?: ""),
        )

    val proteinsForm =
        rememberFormField(
            initialValue = proteins,
            parser = nullableDoubleParser(onNotANumber = { QuickAddFormFieldError.InvalidNumber }),
            validator = {
                if (it != null && it < 0) QuickAddFormFieldError.NegativeNumber else null
            },
            textFieldState = rememberTextFieldState(proteins?.formatClipZeros() ?: ""),
        )

    val carbohydratesForm =
        rememberFormField(
            initialValue = carbohydrates,
            parser = nullableDoubleParser(onNotANumber = { QuickAddFormFieldError.InvalidNumber }),
            validator = {
                if (it != null && it < 0) QuickAddFormFieldError.NegativeNumber else null
            },
            textFieldState = rememberTextFieldState(carbohydrates?.formatClipZeros() ?: ""),
        )

    val fatsForm =
        rememberFormField(
            initialValue = fats,
            parser = nullableDoubleParser(onNotANumber = { QuickAddFormFieldError.InvalidNumber }),
            validator = {
                if (it != null && it < 0) QuickAddFormFieldError.NegativeNumber else null
            },
            textFieldState = rememberTextFieldState(fats?.formatClipZeros() ?: ""),
        )

    val fibreForm =
        rememberFormField(
            initialValue = fibre,
            parser = nullableDoubleParser(onNotANumber = { QuickAddFormFieldError.InvalidNumber }),
            validator =
                nonNegativeDoubleValidator(onNegative = { QuickAddFormFieldError.NegativeNumber }),
            textFieldState = rememberTextFieldState(fibre?.formatClipZeros() ?: ""),
        )

    val servingCountForm =
        rememberFormField(
            initialValue = servingCount,
            parser = nullableDoubleParser(onNotANumber = { QuickAddFormFieldError.InvalidNumber }),
            validator =
                positiveDoubleValidator(
                    onNotPositive = { QuickAddFormFieldError.NotPositiveNumber }
                ),
            textFieldState = rememberTextFieldState(servingCount?.formatClipZeros() ?: ""),
        )

    val weightGramsForm =
        rememberFormField(
            initialValue = weightGrams,
            parser = nullableDoubleParser(onNotANumber = { QuickAddFormFieldError.InvalidNumber }),
            validator =
                positiveDoubleValidator(
                    onNotPositive = { QuickAddFormFieldError.NotPositiveNumber }
                ),
            textFieldState = rememberTextFieldState(weightGrams?.formatClipZeros() ?: ""),
        )

    val energyForm =
        rememberFormField(
            initialValue = energyInUserUnit,
            parser = nullableDoubleParser(onNotANumber = { QuickAddFormFieldError.InvalidNumber }),
            validator = {
                if (it != null && it < 0) QuickAddFormFieldError.NegativeNumber else null
            },
            textFieldState = rememberTextFieldState(energyInUserUnit?.formatClipZeros() ?: ""),
        )

    val autoCalculateEnergyState =
        rememberSaveable(proteins, carbohydrates, fats, energy) {
            val initialState =
                if (energy == null || proteins == null || carbohydrates == null || fats == null) {
                    true
                } else {
                    NutrientsHelper.calculateEnergy(
                        proteins = proteins,
                        carbohydrates = carbohydrates,
                        fats = fats,
                    ) == energy
                }

            mutableStateOf(initialState)
        }

    LaunchedEffect(
        autoCalculateEnergyState,
        proteinsForm,
        carbohydratesForm,
        fatsForm,
        energyFormatter,
    ) {
        snapshotFlow {
                if (!autoCalculateEnergyState.value) {
                    return@snapshotFlow null
                }

                val proteinsValue = proteinsForm.value ?: 0.0
                val carbohydratesValue = carbohydratesForm.value ?: 0.0
                val fatsValue = fatsForm.value ?: 0.0

                val kcal =
                    NutrientsHelper.calculateEnergy(
                        proteins = proteinsValue,
                        carbohydrates = carbohydratesValue,
                        fats = fatsValue,
                    )

                energyFormatter.fromKcal(kcal).formatClipZeros()
            }
            .filterNotNull()
            .collectLatest { energyForm.textFieldState.setTextAndPlaceCursorAtEnd(it) }
    }

    val isModifiedState =
        remember(
            nameForm,
            name,
            descriptionForm,
            description,
            proteinsForm,
            proteins,
            carbohydratesForm,
            carbohydrates,
            fatsForm,
            fats,
            fibreForm,
            fibre,
            servingCountForm,
            servingCount,
            weightGramsForm,
            weightGrams,
            energyForm,
            energyInUserUnit,
        ) {
            derivedStateOf {
                nameForm.value != name ||
                    descriptionForm.value != description ||
                    proteinsForm.value != proteins ||
                    carbohydratesForm.value != carbohydrates ||
                    fatsForm.value != fats ||
                    fibreForm.value != fibre ||
                    servingCountForm.value != servingCount ||
                    weightGramsForm.value != weightGrams ||
                    if (energyInUserUnit == null) {
                        energyForm.value != null && energyForm.value != 0.0
                    } else {
                        energyForm.value != energyInUserUnit
                    }
            }
        }

    return remember(
        nameForm,
        descriptionForm,
        proteinsForm,
        carbohydratesForm,
        fatsForm,
        fibreForm,
        servingCountForm,
        weightGramsForm,
        energyForm,
        autoCalculateEnergyState,
        isModifiedState,
    ) {
        QuickAddFormState(
            name = nameForm,
            description = descriptionForm,
            proteins = proteinsForm,
            carbohydrates = carbohydratesForm,
            fats = fatsForm,
            fibre = fibreForm,
            servingCount = servingCountForm,
            weightGrams = weightGramsForm,
            energy = energyForm,
            autoCalculateEnergyState = autoCalculateEnergyState,
            isModified = isModifiedState,
        )
    }
}

@Stable
internal class QuickAddFormState(
    val name: FormField<String, QuickAddFormFieldError>,
    val description: FormField<String?, QuickAddFormFieldError>,
    val proteins: FormField<Double?, QuickAddFormFieldError>,
    val carbohydrates: FormField<Double?, QuickAddFormFieldError>,
    val fats: FormField<Double?, QuickAddFormFieldError>,
    val fibre: FormField<Double?, QuickAddFormFieldError>,
    val servingCount: FormField<Double?, QuickAddFormFieldError>,
    val weightGrams: FormField<Double?, QuickAddFormFieldError>,
    val energy: FormField<Double?, QuickAddFormFieldError>,
    autoCalculateEnergyState: MutableState<Boolean>,
    isModified: State<Boolean>,
) {
    var autoCalculateEnergy by autoCalculateEnergyState

    val isModified by isModified

    val isValid by derivedStateOf {
        name.error == null &&
            proteins.error == null &&
            carbohydrates.error == null &&
            fats.error == null &&
            fibre.error == null &&
            servingCount.error == null &&
            weightGrams.error == null &&
            energy.error == null
    }
}
