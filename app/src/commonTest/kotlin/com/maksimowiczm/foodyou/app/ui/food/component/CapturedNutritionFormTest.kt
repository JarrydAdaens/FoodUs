package com.maksimowiczm.foodyou.app.ui.food.component

import com.maksimowiczm.foodyou.app.ui.common.utility.EnergyFormatter
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.withMealNutrient
import kotlin.test.*

class CapturedNutritionFormTest {
    @Test fun untouchedAll43FieldsPreserveExactValuesCompletenessZerosAndUnknowns() {
        var facts=NutritionFacts()
        NutritionFactsField.entries.forEachIndexed { index,field -> facts=facts.withMealNutrient(field,when(index%3){0->0.0;1->null;else->(index+1)*0.000000123456789}) }
        facts=facts.copy(fats=NutrientValue.Incomplete(0.12345678912345678),carbohydrates=NutrientValue.Incomplete(null))
        for(energy in listOf(EnergyFormatter.kilocalories,EnergyFormatter.kilojoules)) {
            val form=CapturedNutritionForm(facts,energy)
            assertEquals(facts,form.candidate())
            assertEquals("",form.values[NutritionFactsField.Carbohydrates])
        }
    }

    @Test fun editedMicrogramsZerosAndUnknownsKeepIndependentDirectEnergy() {
        val original=NutritionFacts(energy=NutrientValue.Complete(999.0),vitaminA=NutrientValue.Complete(0.1))
        val form=CapturedNutritionForm(original,EnergyFormatter.kilojoules)
        form.values[NutritionFactsField.VitaminB12]="4"
        form.values[NutritionFactsField.Proteins]="0"
        form.values[NutritionFactsField.VitaminA]=""
        val candidate=assertNotNull(form.candidate())
        assertEquals(0.000004,candidate.vitaminB12.value)
        assertEquals(0.0,candidate.proteins.value)
        assertNull(candidate.vitaminA.value)
        assertNull(candidate.carbohydrates.value)
        assertEquals(original.energy,candidate.energy)
        for(invalid in listOf("Infinity","NaN","-1","1e999","1e-323")) {
            form.values[NutritionFactsField.VitaminB12]=invalid
            assertNull(form.candidate(),invalid)
        }
    }

    @Test fun finiteCanonicalEnergyUsesCanonicalFallbackWhenDisplayConversionOverflows() {
        val facts=NutritionFacts(energy=NutrientValue.Complete(1e308))
        val form=CapturedNutritionForm(facts,EnergyFormatter.kilojoules)
        assertTrue(NutritionFactsField.Energy in form.canonicalFallback)
        assertEquals("1.0E308",form.values[NutritionFactsField.Energy])
        assertEquals(facts,form.candidate())
        form.values[NutritionFactsField.Energy]="9e307"
        assertEquals(9e307,form.candidate()!!.energy.value)
    }

    @Test fun referenceDraftPreservesAbsentVersusEmptyEvidenceAndRequiresPairedMetric() {
        val absent=CapturedReferenceForm(null)
        assertNull(absent.candidate())
        val empty=PortionDescription(unitText="",text="")
        assertEquals(empty,CapturedReferenceForm(empty).candidate())
        val serving=CapturedReferenceForm(PortionDescription(2.0,"pieces","about two pieces"))
        assertTrue(serving.isValid)
        assertNull(serving.candidate()!!.metricAmount)
        serving.metric="20"
        assertFalse(serving.isValid)
        serving.metricUnit=MetricUnit.GRAM
        assertTrue(serving.isValid)
        assertEquals(20.0,serving.candidate()!!.metricAmount)
        serving.amount="0"
        assertFalse(serving.isValid)
    }
}
