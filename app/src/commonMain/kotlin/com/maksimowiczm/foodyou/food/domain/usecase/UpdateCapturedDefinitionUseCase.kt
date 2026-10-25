package com.maksimowiczm.foodyou.food.domain.usecase

import com.maksimowiczm.foodyou.common.domain.database.TransactionProvider
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.domain.repository.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

/** Explicit edits to captured definitions never reinterpret source facts or silently change modes. */
class UpdateCapturedDefinitionUseCase(
    private val products:ProductRepository,private val recipes:RecipeRepository,private val transactions:TransactionProvider,
) {
    suspend fun product(candidate:Product,referenceConfirmed:Boolean):Boolean=safely {
        transactions.withTransaction {
            val old=products.observeProduct(candidate.id).first() ?: return@withTransaction false
            if(candidate.name.isBlank() || candidate.name.length>512 || candidate.factBasis!=old.factBasis ||
                candidate.nutritionMetadata!=old.nutritionMetadata || !candidate.nutritionFacts.hasValidValues() || !validPortion(candidate.referencePortion) ||
                listOf(candidate.packageWeight,candidate.servingWeight).any { it!=null && (!it.isFinite() || it<=0.0) })return@withTransaction false
            val reference=candidate.referencePortion
            if(reference?.metricUnit!=null && (reference.metricUnit==MetricUnit.MILLILITER)!=candidate.isLiquid)return@withTransaction false
            if(candidate.factBasis==ProductFactBasis.METRIC_100 && (reference?.metricAmount!=100.0 || reference.metricUnit==null))return@withTransaction false
            if(candidate.factBasis==ProductFactBasis.REFERENCE_SERVING && candidate.servingWeight!=reference?.metricAmount)return@withTransaction false
            val changed=old.referencePortion!=reference || old.isLiquid!=candidate.isLiquid || old.packageWeight!=candidate.packageWeight || old.servingWeight!=candidate.servingWeight
            if(changed && !referenceConfirmed)return@withTransaction false
            products.updateProduct(candidate)
            true
        }
    }

    suspend fun recipe(candidate:Recipe,panelConfirmed:Boolean):Boolean=safely {
        transactions.withTransaction {
            val old=recipes.observeRecipe(candidate.id).first() ?: return@withTransaction false
            val panel=candidate.reportedNutrition ?: return@withTransaction false
            val previous=old.reportedNutrition ?: return@withTransaction false
            if(candidate.name.isBlank() || candidate.name.length>512 || candidate.ingredients.isNotEmpty() ||
                candidate.servings?.let { it<=0 }==true || !panel.facts.hasValidValues() || !validPortion(panel.reference) ||
                panel.metadata!=previous.metadata || panel.definitionMetadata!=previous.definitionMetadata ||
                candidate.reportedComponents.any { it.name.isBlank() || it.name.length>512 || !validPortion(it.portion) || it.facts?.hasValidValues()==false })return@withTransaction false
            val retainedLinks=old.reportedComponents.mapNotNull { it.reference }.toSet()
            if(candidate.reportedComponents.mapNotNull { it.reference }.any { it !in retainedLinks })return@withTransaction false
            fun composition(components:List<ReportedRecipeComponent>)=components.groupingBy { it.copy(metadata=null) }.eachCount()
            val changed=composition(old.reportedComponents)!=composition(candidate.reportedComponents) || old.servings!=candidate.servings ||
                old.isLiquid!=candidate.isLiquid || previous.basis!=panel.basis || previous.reference!=panel.reference
            recipes.updateRecipe(candidate.copy(reportedNutrition=panel.copy(needsReview=(previous.needsReview || changed) && !panelConfirmed)))
            true
        }
    }

    private suspend fun safely(block:suspend()->Boolean):Boolean=try { block() }catch(cancelled:CancellationException){throw cancelled}catch(_:Exception){false}
}

private fun validPortion(portion:PortionDescription?):Boolean=portion==null ||
    listOf(portion.amount,portion.metricAmount).all { it==null || it.isFinite() && it>0.0 } &&
    (portion.metricAmount==null)==(portion.metricUnit==null) && (portion.unitText?.length ?: 0)<=256 && (portion.text?.length ?: 0)<=16_384
