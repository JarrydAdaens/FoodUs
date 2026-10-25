package com.maksimowiczm.foodyou.app.ui.food.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import com.maksimowiczm.foodyou.app.ui.common.utility.EnergyFormatter
import com.maksimowiczm.foodyou.app.ui.food.meals.mealDisplayFactor
import com.maksimowiczm.foodyou.app.ui.goals.master.stringResource as nutrientLabel
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.withMealNutrient
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/** Untouched facts never make a lossy display-unit round trip, and unknowns remain editable blanks. */
internal class CapturedNutritionForm(private val original:NutritionFacts,val energy:EnergyFormatter) {
    val canonicalFallback=NutritionFactsField.entries.filter { field ->
        original[field].value?.let { !display(field,it).isFinite() } ?: false
    }.toSet()
    private val initial=NutritionFactsField.entries.associateWith { field -> original[field].value?.let {
        if(field in canonicalFallback)it.toString()else display(field,it).toString()
    }.orEmpty() }
    val values=mutableStateMapOf<NutritionFactsField,String>().apply { putAll(initial) }
    private fun display(field:NutritionFactsField,value:Double)=if(field==NutritionFactsField.Energy)energy.fromKcal(value)else value*field.mealDisplayFactor()
    fun candidate():NutritionFacts? {
        var result=original
        for(field in NutritionFactsField.entries) {
            val text=values[field].orEmpty()
            if(text==initial[field])continue
            val value=if(text.isBlank())null else {
                val shown=text.toDoubleOrNull()?.takeIf { it.isFinite() && it>=0.0 } ?: return null
                val canonical=when {
                    field in canonicalFallback -> shown
                    field==NutritionFactsField.Energy -> energy.toKcal(shown)
                    else -> shown/field.mealDisplayFactor()
                }
                if(!canonical.isFinite() || (shown>0.0 && canonical==0.0))return null
                canonical
            }
            result=result.withMealNutrient(field,value)
        }
        return result.takeIf { it.hasValidValues() }
    }
}

internal fun LazyListScope.capturedNutritionFields(form:CapturedNutritionForm,prefix:String) {
    items(NutritionFactsField.entries,key={prefix+it.name}) { field ->
        val suffix=when {
            field in form.canonicalFallback -> if(field==NutritionFactsField.Energy)"kcal" else stringResource(Res.string.unit_gram_short)
            field==NutritionFactsField.Energy -> form.energy.suffix()
            else -> stringResource(when(field.mealDisplayFactor()) {
                1_000_000.0 -> Res.string.unit_microgram_short
                1_000.0 -> Res.string.unit_milligram_short
                else -> Res.string.unit_gram_short
            })
        }
        CapturedTextField(form.values[field].orEmpty(),{form.values[field]=it},field.nutrientLabel()+" ("+suffix+")",prefix+field.name,true)
    }
}

@Composable
internal fun CapturedTextField(value:String,onChange:(String)->Unit,label:String,tag:String,number:Boolean=false) {
    OutlinedTextField(value,onChange,label={Text(label)},modifier=Modifier.fillMaxWidth().testTag(tag),
        keyboardOptions=KeyboardOptions(keyboardType=if(number)KeyboardType.Decimal else KeyboardType.Text),singleLine=number)
}
