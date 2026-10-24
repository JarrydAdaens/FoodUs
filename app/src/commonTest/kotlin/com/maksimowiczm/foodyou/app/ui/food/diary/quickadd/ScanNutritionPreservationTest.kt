package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import com.maksimowiczm.foodyou.ai.domain.*
import com.maksimowiczm.foodyou.app.navigation.FoodDiaryCreateQuickAdd
import com.maksimowiczm.foodyou.app.navigation.PromoteToProduct
import com.maksimowiczm.foodyou.app.navigation.PromoteToRecipe
import com.maksimowiczm.foodyou.app.ui.common.utility.EnergyFormatter
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.infrastructure.room.toEntityNutrients
import com.maksimowiczm.foodyou.common.infrastructure.room.toNutritionFacts
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class ScanNutritionPreservationTest {
    @Test fun reportedSampleRetains780AndUnknownCarbohydratesThroughRouteMappingAndStorageColumns() {
        val estimate = requireNotNull(AiFoodEstimateParser.parse(
            """{"name":"Synthetic reported food","calories":780,"protein":35,"fat":38,"fibre":6,"sugar":12}"""
        ))
        val route = FoodDiaryCreateQuickAdd(0, 1, estimate)
        val restoredRoute = Json.decodeFromString<FoodDiaryCreateQuickAdd>(Json.encodeToString(route))
        val accepted = requireNotNull(AiFoodEstimateParser.parse(requireNotNull(restoredRoute.scanEstimateJson)))
        assertEquals(estimate, accepted)
        assertFalse(shouldAutoCalculateQuickAddEnergy(35.0, null, 38.0, 780.0, true))
        val mapped = map(accepted.toNutritionFacts())
        val (nutrients, vitamins, minerals) = toEntityNutrients(mapped)
        val readback = toNutritionFacts(nutrients, vitamins, minerals)
        assertEquals(780.0, readback.energy.value)
        assertEquals(35.0, readback.proteins.value)
        assertEquals(38.0, readback.fats.value)
        assertEquals(6.0, readback.dietaryFiber.value)
        assertEquals(12.0, readback.sugars.value)
        assertNull(readback.carbohydrates.value)
        assertNull(accepted.description)
        assertNull(accepted.servingCount)
        assertNull(accepted.weightGrams)
    }

    @Test fun quantitiesUnitsOptionalValuesAndRealZeroSurviveWithoutScaling() {
        val estimate = requireNotNull(AiFoodEstimateParser.parse(
            """{"name":"Synthetic portion","description":" test portion ","energyKj":3263.52,"weightKg":0.25,"servingCount":2.5,"carbs":0,"sugar":0,"sodium":0.0012,"vitaminC":0.012,"saturatedFats":2.25}"""
        ))
        assertEquals(780.0, estimate.calories!!, 0.00001)
        assertEquals(250.0, estimate.weightGrams)
        assertEquals(2.5, estimate.servingCount)
        assertEquals("test portion", estimate.description)
        val facts = map(estimate.toNutritionFacts())
        val (n, v, m) = toEntityNutrients(facts)
        val readback = toNutritionFacts(n, v, m)
        assertEquals(780.0, readback.energy.value!!, 0.00001)
        assertEquals(NutrientValue.Complete(0.0), readback.carbohydrates)
        assertEquals(NutrientValue.Complete(0.0), readback.sugars)
        assertEquals(0.0012, readback.sodium.value)
        assertEquals(0.012, readback.vitaminC.value)
        assertEquals(2.25, readback.saturatedFats.value)
        assertNull(readback.proteins.value)
        // User unit changes are presentation only, not portion multiplication.
        val kj = EnergyFormatter.kilojoules
        assertEquals(780.0, kj.toKcal(kj.fromKcal(780.0)), 0.00001)
    }

    @Test fun absentEnergyStaysUnknownUntilCalculationIsChosenAndManualDefaultsRemain() {
        assertFalse(shouldAutoCalculateQuickAddEnergy(35.0, null, 38.0, null, true))
        val absent = mapQuickAddNutrition(energyKcal=null, proteins=null, carbohydrates=null,
            fats=null, fibre=null, sugars=null, preserveUnknown=true)
        assertNull(absent.energy.value)
        assertNull(absent.proteins.value)
        val manual = mapQuickAddNutrition(energyKcal=null, proteins=null, carbohydrates=null,
            fats=null, fibre=null, sugars=null, preserveUnknown=false)
        assertEquals(0.0, manual.energy.value)
        assertEquals(0.0, manual.carbohydrates.value)
        assertTrue(shouldAutoCalculateQuickAddEnergy(null, null, null, null, false))
        assertTrue(shouldAutoCalculateQuickAddEnergy(35.0, 0.0, 38.0, 482.0, false))
        assertFalse(shouldAutoCalculateQuickAddEnergy(35.0, 0.0, 38.0, 780.0, false))
        // Same control, deliberately toggled by the user, still calculates 482.
        assertEquals(482.0, NutrientsHelper.calculateEnergy(35.0, 0.0, 38.0))
    }

    @Test fun invalidOptionalsBecomeUnknownAndInvalidStructureIsRejected() {
        val invalid = requireNotNull(AiFoodEstimateParser.parse(
            """{"name":"Synthetic invalid values","calories":-1,"protein":-2,"carbohydrates":-1,"sugar":-1,"sodium":-1,"vitaminC":-1,"weightGrams":0,"servingCount":-1}"""
        ))
        assertNull(invalid.calories)
        assertNull(invalid.protein)
        assertNull(invalid.carbohydrates)
        assertNull(invalid.sugar)
        assertNull(invalid.sodium)
        assertNull(invalid.vitaminC)
        assertNull(invalid.weightGrams)
        assertNull(invalid.servingCount)
        assertNull(AiFoodEstimateParser.parse("""{"name":" "}"""))
        assertNull(AiFoodEstimateParser.parse("""{"name":"Synthetic","protein":{}}"""))
        assertNotNull(AiFoodEstimateParser.parse("""{"name":"Synthetic","energyKj":{},"weightKg":[]}"""))
        assertNotNull(AiFoodEstimateParser.parse("""{"name":"Synthetic","energyKj":[],"weightKg":{}}"""))
    }

    @Test fun everySupportedNutrientSurvivesAcceptedRouteAndRoomColumnMapping() {
        val legacyKeys = mapOf(
            NutritionFactsField.Energy to "calories", NutritionFactsField.Proteins to "protein",
            NutritionFactsField.Fats to "fat", NutritionFactsField.DietaryFiber to "fibre",
            NutritionFactsField.Sugars to "sugar",
        )
        val expected = NutritionFactsField.entries.associateWith { (it.ordinal + 1) / 10.0 }
        val keys = expected.entries.joinToString(",") { (field, value) ->
            val key = legacyKeys[field] ?: field.name.replaceFirstChar { it.lowercaseChar() }
            "\"$key\":$value"
        }
        val estimate = requireNotNull(AiFoodEstimateParser.parse("{\"name\":\"Synthetic full nutrition\",$keys}"))
        val route = FoodDiaryCreateQuickAdd(0, 1, estimate)
        val restored = Json.decodeFromString<FoodDiaryCreateQuickAdd>(Json.encodeToString(route))
        val accepted = requireNotNull(AiFoodEstimateParser.parse(requireNotNull(restored.scanEstimateJson)))
        val (n, v, m) = toEntityNutrients(map(accepted.toNutritionFacts()))
        val stored = toNutritionFacts(n, v, m)
        val promotion = QuickAddNutritionSnapshot.decode(QuickAddNutritionSnapshot.encode(stored))
        expected.forEach { (field, value) ->
            assertEquals(value, requireNotNull(stored[field].value), 0.00000001, "Retained field $field")
            assertEquals(value, requireNotNull(promotion[field].value), 0.00000001, "Promotion field $field")
        }
    }

    @Test fun promotionSnapshotKeepsSugarAndOtherOptionalNutrientsAcrossLocalSerialization() {
        val nutrition = requireNotNull(AiFoodEstimateParser.parse(
            """{"name":"Synthetic promotion","calories":780,"sugar":12,"fibre":6,"sodium":0.0012,"vitaminC":0,"weightGrams":250}"""
        )).toNutritionFacts()
        val seed = QuickAddPromotionSeed("Synthetic promotion", null, 780.0, null, null, null,
            6.0, null, 250.0, nutritionFacts = nutrition)
        val productRoute = Json.decodeFromString<PromoteToProduct>(Json.encodeToString(PromoteToProduct(seed)))
        val recipeRoute = Json.decodeFromString<PromoteToRecipe>(Json.encodeToString(PromoteToRecipe(seed)))
        assertEquals(nutrition, productRoute.toSeed().nutritionFacts)
        assertEquals(nutrition, recipeRoute.toSeed().nutritionFacts)
        val product = productRoute.toSeed().toProductPrefill()
        assertEquals(312.0, product.nutritionFacts.energy.value!!, 0.000001)
        assertEquals(4.8, product.nutritionFacts.sugars.value!!, 0.000001)
        assertEquals(0.00048, product.nutritionFacts.sodium.value!!, 0.000000001)
        assertEquals(0.0, product.nutritionFacts.vitaminC.value)
        assertNull(product.nutritionFacts.carbohydrates.value)
    }

    private fun map(facts: NutritionFacts) = mapQuickAddNutrition(
        original=facts, energyKcal=facts.energy.value, proteins=facts.proteins.value,
        carbohydrates=facts.carbohydrates.value, fats=facts.fats.value,
        fibre=facts.dietaryFiber.value, sugars=facts.sugars.value, preserveUnknown=true,
    )
}
