package com.maksimowiczm.foodyou.app.ui.food.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.domain.repository.*
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveNutritionalItemUseCase
import kotlinx.coroutines.flow.*

internal sealed interface LibraryRead<out T> {
    data object Loading : LibraryRead<Nothing>
    data object Unavailable : LibraryRead<Nothing>
    data class Ready<T>(val value: T) : LibraryRead<T>
}

internal enum class LibraryTab { Recipes, Meals, Products }

internal fun filterLibraryNames(names: List<NutritionalItemName>, query: String): List<NutritionalItemName> =
    names.filter { it.name.contains(query.trim(), ignoreCase = true) }
        .sortedWith(compareBy<NutritionalItemName> { it.name.lowercase() }.thenBy { it.id.toString() })

internal class LibraryViewModel(
    products: ProductRepository,
    recipes: RecipeRepository,
    private val observeItem: ObserveNutritionalItemUseCase,
) : ViewModel() {
    private val refresh = MutableStateFlow(0)
    private fun names(read: () -> Flow<List<NutritionalItemName>>) = refresh.flatMapLatest {
        flow { emitAll(read()) }.map<List<NutritionalItemName>, LibraryRead<List<NutritionalItemName>>> { LibraryRead.Ready(it) }
            .catch { emit(LibraryRead.Unavailable) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryRead.Loading)

    val products = names(products::observeLibraryNames)
    val recipes = names(recipes::observeLibraryNames)
    fun retry() { refresh.value++ }

    // Keep only lightweight display facts, never hydrated Recipe graphs. Stable row heights also
    // preserve the LazyList anchor when returning after the upstream subscription has stopped.
    private val rowCache = LinkedHashMap<NutritionalItemId, LibraryRead<LibraryNutrition>>()
    fun cachedRow(id: NutritionalItemId): LibraryRead<LibraryNutrition> = rowCache[id] ?: LibraryRead.Loading

    /** Collected only by composed LazyColumn rows; leaving a row cancels graph observation. */
    fun row(id: NutritionalItemId): Flow<LibraryRead<LibraryNutrition>> = flow { emitAll(observeItem.observe(id)) }
        .map<NutritionalItem?, LibraryRead<LibraryNutrition>> { item ->
            if (item == null) LibraryRead.Unavailable else LibraryRead.Ready(libraryNutrition(item))
        }.catch { emit(LibraryRead.Unavailable) }
        .onEach { value ->
            rowCache.remove(id)
            rowCache[id] = value
            if (rowCache.size > 128) rowCache.remove(rowCache.keys.first())
        }.onStart { emit(cachedRow(id)) }
}
