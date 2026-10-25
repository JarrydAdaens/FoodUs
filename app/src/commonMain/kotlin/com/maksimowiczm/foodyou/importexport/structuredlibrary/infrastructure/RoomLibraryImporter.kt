package com.maksimowiczm.foodyou.importexport.structuredlibrary.infrastructure

import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.infrastructure.room.toNutritionFacts
import com.maksimowiczm.foodyou.common.infrastructure.room.toValidatedEntityNutrients
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.domain.repository.*
import com.maksimowiczm.foodyou.food.infrastructure.room.RecipeEntity
import com.maksimowiczm.foodyou.food.infrastructure.room.RecipeIngredientEntity
import com.maksimowiczm.foodyou.common.domain.measurement.type
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository
import com.maksimowiczm.foodyou.importexport.structuredlibrary.domain.*
import com.maksimowiczm.foodyou.importexport.structuredlibrary.infrastructure.room.LibraryIdentityEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first

/** Source validation precedes DML; all definitions, captured rows, history and ledger share one transaction. */
internal class RoomLibraryImporter(
    private val database:FoodYouDatabase,
    private val products:ProductRepository,
    private val recipes:RecipeRepository,
    private val meals:MealRepository,
    private val history:NutritionalItemHistoryRepository,
    private val clock:DateProvider,
) : LibraryImporter {
    override suspend fun preview(library:ValidatedLibrary):LibraryImportPreview = safely {
        database.withTransaction { analyze(library).preview }
    }

    override suspend fun apply(library:ValidatedLibrary):LibraryImportResult = safely {
        database.withTransaction {
            val state=analyze(library)
            if(state.conflicts.isNotEmpty())throw LibraryImportException(LibraryErrorCode.CONFLICT)
            val bundle=library.bundle
            val ids=state.ids.toMutableMap()
            suspend fun remember(key:LibraryKey,id:Long) {
                currentCoroutineContext().ensureActive()
                database.libraryIdentityDao.insert(LibraryIdentityEntity(bundle.namespace,key.kind.name,key.externalId,
                    library.digests.getValue(key),id))
                ids[key]=id
            }
            for(product in bundle.products) {
                val key=LibraryKey(DefinitionKind.PRODUCT,product.externalId)
                if(key in ids)continue
                currentCoroutineContext().ensureActive()
                val id=products.insertProduct(product.toProduct(bundle.namespace,0))
                history.insert(id,NutritionalItemHistory.Imported(clock.nowInstant()))
                remember(key,id.id)
            }
            val definitions=Definitions(bundle,ids)
            val newRecipes=mutableListOf<NutritionalItemId.Recipe>()
            val recipeSources=bundle.recipes.associateBy { it.externalId }
            suspend fun insertRecipe(key:LibraryKey) {
                if(key in ids)return
                currentCoroutineContext().ensureActive()
                val source=recipeSources.getValue(key.externalId)
                val dependencies=source.ingredients.map { it.reference }+source.components.mapNotNull { it.reference }
                for(reference in dependencies.filter { it.kind==DefinitionKind.RECIPE })insertRecipe(LibraryKey(reference.kind,reference.externalId))
                val recipe=definitions.recipe(key.externalId)
                val id=if(recipe.isReported)recipes.insertRecipe(recipe) else recipes.insertRecipe(
                    recipe.name,requireNotNull(recipe.servings),recipe.note,recipe.isLiquid,recipe.ingredients)
                history.insert(id,NutritionalItemHistory.Imported(clock.nowInstant()))
                remember(key,id.id)
                newRecipes.add(id)
                definitions.clear()
            }
            for(recipe in bundle.recipes)insertRecipe(LibraryKey(DefinitionKind.RECIPE,recipe.externalId))
            validateStoredGraph(newRecipes)
            for(meal in bundle.meals) {
                val key=LibraryKey(DefinitionKind.MEAL,meal.externalId)
                if(key in ids)continue
                currentCoroutineContext().ensureActive()
                val id=meals.save(meal.toMeal(bundle.namespace,clock.now()))
                remember(key,id.value)
            }
            currentCoroutineContext().ensureActive()
            LibraryImportResult(state.preview.products,state.preview.recipes,state.preview.meals,state.preview.skipped)
        }
    }

    private suspend fun analyze(library:ValidatedLibrary):State {
        val bundle=library.bundle
        val rows=database.libraryIdentityDao.getNamespace(bundle.namespace)
        val ledger=rows.associateBy { LibraryKey(DefinitionKind.valueOf(it.kind),it.externalId) }
        if(rows.groupBy { it.kind to it.localId }.any { it.value.size>1 })throw LibraryImportException(LibraryErrorCode.CONFLICT)
        val ids=ledger.mapValues { it.value.localId }
        val conflicts=mutableSetOf<LibraryKey>()
        val current=mutableMapOf<LibraryKey,NutritionalItem>()
        val currentRecipes=mutableMapOf<Long,RecipeEntity?>()
        var skipped=0
        for((key,digest)in library.digests) {
            currentCoroutineContext().ensureActive()
            val old=ledger[key] ?: continue
            val exists=if(old.localId<=0)false else when(key.kind) {
                DefinitionKind.PRODUCT -> products.observeProduct(NutritionalItemId.Product(old.localId)).first()?.also { current[key]=it }!=null
                DefinitionKind.RECIPE -> database.recipeDao.observeRecipe(old.localId).first().also { currentRecipes[old.localId]=it }!=null
                DefinitionKind.MEAL -> meals.getMeal(MealId(old.localId))!=null
            }
            if(!exists || old.sourceDigest!=digest)conflicts.add(key) else skipped++
        }
        val definitions=Definitions(bundle,ids)
        val expected=mutableMapOf<LibraryKey,NutritionalItem>()
        bundle.products.forEach { source -> expected[LibraryKey(DefinitionKind.PRODUCT,source.externalId)]=definitions.product(source.externalId) }
        bundle.recipes.forEachIndexed { index,source ->
            val model=definitions.recipe(source.externalId)
            if(!model.isReported && (!model.isResolvable || !model.totalNutrition.hasValidValues()))
                throw LibraryImportException(LibraryErrorCode.BASIS,DefinitionKind.RECIPE,index)
            expected[LibraryKey(DefinitionKind.RECIPE,source.externalId)]=model
        }
        val recipeSources=bundle.recipes.associateBy { it.externalId }
        val dependencyChanges=mutableMapOf<LibraryKey,Boolean>()
        suspend fun existingMatches(key:LibraryKey):Boolean {
            val wanted=expected[key]
            if(wanted is Product)return current[key]==wanted
            if(wanted !is Recipe)return false
            val id=ids[key] ?: return false
            val header=currentRecipes[id] ?: return false
            return sameRecipe(wanted,header,database.recipeDao.getRecipeIngredients(id),
                database.recipeDao.getReportedComponents(id).map { it.component })
        }
        suspend fun dependencyChanged(reference:LibraryReference):Boolean {
            val key=LibraryKey(reference.kind,reference.externalId)
            return dependencyChanges.getOrPut(key) {
                if(key in ledger && !existingMatches(key))true
                else if(key.kind!=DefinitionKind.RECIPE)false
                else {
                    val source=recipeSources.getValue(key.externalId)
                    source.mode==LibraryRecipeMode.DERIVED && source.ingredients.any { dependencyChanged(it.reference) }
                }
            }
        }
        for(source in bundle.recipes) {
            val key=LibraryKey(DefinitionKind.RECIPE,source.externalId)
            if(key !in ledger && source.mode==LibraryRecipeMode.DERIVED && source.ingredients.any { dependencyChanged(it.reference) })conflicts.add(key)
        }
        val counts=library.digests.keys.filter { it !in ledger }.groupingBy { it.kind }.eachCount()
        val preview=LibraryImportPreview(counts[DefinitionKind.PRODUCT] ?: 0,counts[DefinitionKind.RECIPE] ?: 0,
            counts[DefinitionKind.MEAL] ?: 0,skipped,conflicts.size,
            expected.values.count { it.weight(com.maksimowiczm.foodyou.common.domain.measurement.Measurement.Serving(1.0))==null },
            expected.values.count { it.supportedMeasurementTypes().isEmpty() })
        return State(ids,conflicts,preview)
    }

    /** Flat DAO edges, one visit per reachable Recipe, including shared branches and malformed cycles. */
    private suspend fun validateStoredGraph(roots:List<NutritionalItemId.Recipe>) {
        val completed=mutableSetOf<NutritionalItemId.Recipe>()
        val active=mutableSetOf<NutritionalItemId.Recipe>()
        val stack=ArrayDeque<Pair<NutritionalItemId.Recipe,Boolean>>()
        for(root in roots) {
            stack.addLast(root to false)
            while(stack.isNotEmpty()) {
                currentCoroutineContext().ensureActive()
                val (id,exit)=stack.removeLast()
                if(exit) { active.remove(id); completed.add(id); continue }
                if(id in completed)continue
                if(!active.add(id))throw LibraryImportException(LibraryErrorCode.CYCLE)
                stack.addLast(id to true)
                recipes.recipeDependencies(id).forEach { stack.addLast(it to false) }
            }
        }
    }

    private suspend fun <T>safely(block:suspend()->T):T=try { block() }
        catch(cancelled:CancellationException){throw cancelled}
        catch(invalid:LibraryImportException){throw invalid}
        catch(_:Exception){throw LibraryImportException(LibraryErrorCode.PERSISTENCE)}

    private data class State(val ids:Map<LibraryKey,Long>,val conflicts:Set<LibraryKey>,val preview:LibraryImportPreview)
}

/** Compare stored definition data without hydrating a recursively expanding observable graph. */
private fun sameRecipe(expected:Recipe,actual:RecipeEntity,ingredients:List<RecipeIngredientEntity>,components:List<ReportedRecipeComponent>):Boolean =
    actual.id==expected.id.id && actual.name==expected.name && actual.servings==expected.servings &&
        actual.note==expected.note && actual.isLiquid==expected.isLiquid && actual.reportedNutrition==expected.reportedNutrition &&
        components==expected.reportedComponents && ingredients.sortedBy { it.id }.map {
            Triple(it.ingredientProductId,it.ingredientRecipeId,it.measurement to it.quantity)
        }==expected.ingredients.map {
            Triple((it.food.id as? NutritionalItemId.Product)?.id,(it.food.id as? NutritionalItemId.Recipe)?.id,it.measurement.type to it.measurement.rawValue)
        }

/** A closed, validated input graph used for read-only preview and again with allocated typed IDs. */
private class Definitions(private val bundle:LibraryBundle,private val ids:Map<LibraryKey,Long>) {
    private val products=bundle.products.associateBy { it.externalId }
    private val recipes=bundle.recipes.associateBy { it.externalId }
    private val cache=mutableMapOf<String,Recipe>()
    fun clear(){cache.clear()}
    fun product(id:String):Product {
        val source=products.getValue(id)
        // Preview and dependency comparison use precisely the existing encoded-storage round trip.
        return source.toProduct(bundle.namespace,ids[LibraryKey(DefinitionKind.PRODUCT,id)] ?: 0)
            .copy(nutritionFacts=source.nutrition.stored())
    }
    fun recipe(id:String):Recipe=cache.getOrPut(id) {
        val source=recipes.getValue(id)
        val ingredients=source.ingredients.map { ingredient ->
            val item=when(ingredient.reference.kind) {
                DefinitionKind.PRODUCT -> product(ingredient.reference.externalId)
                DefinitionKind.RECIPE -> recipe(ingredient.reference.externalId)
                DefinitionKind.MEAL -> throw LibraryImportException(LibraryErrorCode.REFERENCE)
            }
            RecipeIngredient(item,ingredient.portion.measurement())
        }
        Recipe(NutritionalItemId.Recipe(ids[LibraryKey(DefinitionKind.RECIPE,id)] ?: 0),source.name,source.yield,ingredients,source.note,
            if(source.mode==LibraryRecipeMode.REPORTED)source.reportedPanel?.reference?.metricUnit==MetricUnit.MILLILITER else ingredients.all { it.food.isLiquid },
            source.capturedPanel(),source.components.map { component -> ReportedRecipeComponent(component.name,component.portion,
                component.nutrition?.toNutritionFacts(),component.reference?.captured(bundle.namespace,ids),component.metadata?.captured()) })
    }
}

private fun LibraryReference.captured(namespace:String,ids:Map<LibraryKey,Long> = emptyMap())=
    DefinitionProvenance(kind,namespace,externalId,ids[LibraryKey(kind,externalId)])

private fun LibraryFacts.stored():NutritionFacts {
    val encoded=toValidatedEntityNutrients(toNutritionFacts())
    return toNutritionFacts(encoded.first,encoded.second,encoded.third)
}

private fun LibraryProduct.toProduct(namespace:String,id:Long):Product = Product(
    NutritionalItemId.Product(id),name,brand,barcode,note,metricUnit()==MetricUnit.MILLILITER,packageMetric?.amount,
    servingMetric?.amount ?: if(basis==ProductFactBasis.REFERENCE_SERVING)reference.metricAmount else null,
    ProductSource(ProductSource.Type.User),nutrition.toNutritionFacts(),basis,reference,
    (metadata ?: LibraryMetadata()).captured(DefinitionProvenance(DefinitionKind.PRODUCT,namespace,externalId)),
)

private fun LibraryMeal.toMeal(namespace:String,createdAt:kotlinx.datetime.LocalDateTime)=Meal(MealId(0),name,createdAt,items.map { item ->
    MealItem(item.name,item.nutrition.toNutritionFacts(),capturedPortion=item.capturedPortion?.measurement(),servingCount=item.servingCount,
        weightGrams=item.weightGrams,portionDescription=item.portion,
        metadata=if(item.metadata!=null || item.reference!=null)(item.metadata ?: LibraryMetadata()).captured(item.reference?.captured(namespace))else null)
},reportedPanel?.captured(),metadata?.captured())
