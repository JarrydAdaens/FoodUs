package com.maksimowiczm.foodyou.food.infrastructure.repository

import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.domain.measurement.from
import com.maksimowiczm.foodyou.common.domain.measurement.rawValue
import com.maksimowiczm.foodyou.common.domain.measurement.type
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.entity.Recipe
import com.maksimowiczm.foodyou.food.domain.entity.RecipeIngredient
import com.maksimowiczm.foodyou.food.domain.repository.RecipeRepository
import com.maksimowiczm.foodyou.food.infrastructure.room.RecipeDao
import com.maksimowiczm.foodyou.food.infrastructure.room.RecipeEntity
import com.maksimowiczm.foodyou.food.infrastructure.room.RecipeIngredientEntity
import com.maksimowiczm.foodyou.food.infrastructure.room.ReportedRecipeComponentEntity
import com.maksimowiczm.foodyou.common.domain.food.DefinitionKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class RoomRecipeRepository(
    private val recipeDao: RecipeDao,
    private val productDataSource: RoomProductRepository,
) : RecipeRepository {
    override suspend fun recipeDependencies(id: NutritionalItemId.Recipe): List<NutritionalItemId.Recipe> =
        recipeDao.getRecipeIngredients(id.id).mapNotNull { it.ingredientRecipeId?.let(NutritionalItemId::Recipe) } +
            recipeDao.getReportedComponents(id.id).mapNotNull { it.component.reference?.takeIf { it.kind == DefinitionKind.RECIPE }?.localId?.let(NutritionalItemId::Recipe) }

    override fun observeRecipe(recipeId: NutritionalItemId.Recipe): Flow<Recipe?> = observeRecipe(recipeId, emptySet())

    private fun observeRecipe(recipeId: NutritionalItemId.Recipe, ancestors: Set<NutritionalItemId.Recipe>): Flow<Recipe?> {
        if (recipeId in ancestors) return flowOf(null)
        return combine(recipeDao.observeRecipe(recipeId.id), recipeDao.observeRecipeIngredients(recipeId.id)) { recipe, ingredients ->
            recipe to ingredients
        }.flatMapLatest { (recipe, ingredients) ->
            if (recipe == null) return@flatMapLatest flowOf(null)
            if (recipe.reportedNutrition != null) return@flatMapLatest recipeDao.observeReportedComponents(recipeId.id).map { components ->
                Recipe(id = recipeId, name = recipe.name, servings = recipe.servings, ingredients = emptyList(), note = recipe.note,
                    isLiquid = recipe.isLiquid, reportedNutrition = recipe.reportedNutrition, reportedComponents = components.map { it.component })
            }
            val flows: List<Flow<RecipeIngredient?>> = ingredients.map { entity ->
                val item: Flow<com.maksimowiczm.foodyou.food.domain.entity.NutritionalItem?> = when {
                    entity.ingredientRecipeId != null && entity.ingredientProductId == null ->
                        observeRecipe(NutritionalItemId.Recipe(entity.ingredientRecipeId), ancestors + recipeId)
                    entity.ingredientProductId != null && entity.ingredientRecipeId == null ->
                        productDataSource.observeProduct(NutritionalItemId.Product(entity.ingredientProductId))
                    else -> flowOf(null)
                }
                item.map { it?.let { RecipeIngredient(it, Measurement.from(entity.measurement, entity.quantity)) } }
            }
            val resolved = if (flows.isEmpty()) flowOf(emptyList()) else combine(flows) { it.toList() }
            resolved.map { list ->
                if (list.any { it == null }) null else Recipe(
                    id = recipeId, name = recipe.name, servings = recipe.servings,
                    note = recipe.note, isLiquid = recipe.isLiquid, ingredients = list.filterNotNull(),
                )
            }
        }
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        val entity = recipe.toEntity()
        recipeDao.delete(entity)
    }

    override suspend fun insertRecipe(
        name: String,
        servings: Int,
        note: String?,
        isLiquid: Boolean,
        ingredients: List<RecipeIngredient>,
    ): NutritionalItemId.Recipe {
        val recipe =
            Recipe(
                id = NutritionalItemId.Recipe(0L),
                name = name,
                servings = servings,
                note = note,
                isLiquid = isLiquid,
                ingredients = ingredients,
            )

        val recipeEntity = recipe.toEntity()

        val ingredients =
            recipe.ingredients.map { (food, measurement) ->
                val foodId = food.id

                RecipeIngredientEntity(
                    ingredientRecipeId = (foodId as? NutritionalItemId.Recipe)?.id,
                    ingredientProductId = (foodId as? NutritionalItemId.Product)?.id,
                    measurement = measurement.type,
                    quantity = measurement.rawValue,
                )
            }

        val id =
            recipeDao
                .insertRecipeWithIngredients(recipe = recipeEntity, ingredients = ingredients)
                .let(NutritionalItemId::Recipe)

        return id
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        val recipeEntity = recipe.toEntity()

        val ingredients =
            recipe.ingredients.map { (food, measurement) ->
                val foodId = food.id

                RecipeIngredientEntity(
                    ingredientRecipeId = (foodId as? NutritionalItemId.Recipe)?.id,
                    ingredientProductId = (foodId as? NutritionalItemId.Product)?.id,
                    measurement = measurement.type,
                    quantity = measurement.rawValue,
                )
            }

        recipeDao.updateRecipeWithIngredients(recipeEntity, ingredients, recipe.reportedComponents.mapIndexed { index, component -> ReportedRecipeComponentEntity(recipe.id.id, index, component) })
    }

    override suspend fun insertRecipe(recipe: Recipe): NutritionalItemId.Recipe {
        require(recipe.isReported && recipe.ingredients.isEmpty()) { "Expected reported Recipe" }
        val id = recipeDao.insertRecipeWithIngredients(recipe.toEntity().copy(id = 0), emptyList(),
            recipe.reportedComponents.mapIndexed { index, component -> ReportedRecipeComponentEntity(0, index, component) })
        return NutritionalItemId.Recipe(id)
    }
}

private fun Recipe.toEntity(): RecipeEntity =
    RecipeEntity(
        id = this.id.id,
        name = this.name,
        servings = this.servings,
        note = this.note,
        isLiquid = this.isLiquid,
        reportedNutrition = this.reportedNutrition,
    )

private val RecipeIngredientEntity.foodId: NutritionalItemId
    get() =
        ingredientRecipeId?.let { NutritionalItemId.Recipe(it) }
            ?: ingredientProductId?.let { NutritionalItemId.Product(it) }
            ?: error(
                "RecipeIngredientEntity must have either ingredientRecipeId or ingredientProductId set"
            )
