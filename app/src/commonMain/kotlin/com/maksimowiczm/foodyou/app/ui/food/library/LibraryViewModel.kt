package com.maksimowiczm.foodyou.app.ui.food.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.food.domain.entity.*
import com.maksimowiczm.foodyou.food.domain.repository.*
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveNutritionalItemUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CancellationException
import com.maksimowiczm.foodyou.settings.domain.entity.LibrarySortOrder

internal sealed interface LibraryRead<out T> {
    data object Loading : LibraryRead<Nothing>
    data object Unavailable : LibraryRead<Nothing>
    data class Ready<T>(val value: T, val revision: Long = 0) : LibraryRead<T>
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
    private var revision = 0L
    private fun names(read: () -> Flow<List<NutritionalItemName>>) = refresh.flatMapLatest {
        flow { emitAll(read()) }.map<List<NutritionalItemName>, LibraryRead<List<NutritionalItemName>>> { LibraryRead.Ready(it, ++revision) }
            .catch { emit(LibraryRead.Unavailable) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryRead.Loading)

    val products = names(products::observeLibraryNames)
    val recipes = names(recipes::observeLibraryNames)
    fun retry() { refresh.value++ }

    private data class OrderKey(val tab: LibraryTab, val query: String, val order: LibrarySortOrder)
    private val orderCache = LinkedHashMap<OrderKey, LibraryRead.Ready<List<NutritionalItemName>>>()
    fun cachedOrder(tab: LibraryTab, query: String, order: LibrarySortOrder): LibraryRead<List<NutritionalItemName>> =
        orderCache[OrderKey(tab, query.trim().lowercase(), order)] ?: LibraryRead.Loading

    /** Derive compact keys with at most four live graphs; cancellation releases observers. */
    fun orderedNames(names: List<NutritionalItemName>, order: LibrarySortOrder,
        tab: LibraryTab = if (names.firstOrNull()?.id is NutritionalItemId.Product) LibraryTab.Products else LibraryTab.Recipes,
        query: String = "",
    ): Flow<LibraryRead<List<NutritionalItemName>>> = flow<LibraryRead<List<NutritionalItemName>>> {
        val energy = if (order.byCalories) names.asFlow().flatMapMerge(concurrency = 4) { name ->
            flow {
                val value = try {
                    observeItem.observe(name.id).first()?.let(::libraryNutrition)?.facts?.energy
                        ?.takeIf { it.isComplete }?.value?.takeIf { it.isFinite() && it >= 0.0 }
                } catch (cancelled: CancellationException) { throw cancelled }
                emit(name.id to value)
            }
        }.toList().toMap() else emptyMap()
        val result = LibraryRead.Ready(orderLibrary(names, order, { it.name }, { it.id.toString() },
            { it.createdEpochSeconds }, { energy[it.id] }))
        val key = OrderKey(tab, query.trim().lowercase(), order)
        orderCache.remove(key)
        orderCache[key] = result
        if (orderCache.size > 3) orderCache.remove(orderCache.keys.first())
        emit(result)
    }.catch { emit(LibraryRead.Unavailable) }


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
