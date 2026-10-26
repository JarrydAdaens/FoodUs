package com.maksimowiczm.foodyou.food.search.domain

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.RemoteMediator
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.event.EventBus
import com.maksimowiczm.foodyou.common.domain.food.ProductSource
import com.maksimowiczm.foodyou.common.domain.search.SearchQuery
import com.maksimowiczm.foodyou.common.domain.search.searchQuery
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.runBlocking

class NutritionalItemSearchUseCase(
    private val foodSearchRepository: NutritionalItemSearchRepository,
    private val foodSearchPreferencesRepository: UserPreferencesRepository<FoodSearchPreferences>,
    private val foodRemoteMediatorFactoryAggregate: FoodRemoteMediatorFactoryAggregate,
    private val eventBus: EventBus,
    private val dateProvider: DateProvider,
) {
    fun search(
        query: String?,
        source: ProductSource.Type,
        excludedRecipeId: NutritionalItemId.Recipe?,
    ): Flow<PagingData<NutritionalItemSearch>> {
        val query = searchQuery(query)

        if (query is SearchQuery.Text) {
            eventBus.publish(NutritionalItemSearchEvent(query, dateProvider.nowInstant()))
        }

        return foodSearchPreferencesRepository.observe().flatMapLatest { prefs ->
            foodSearchRepository.search(
                query = query,
                source = source,
                config = PagingConfig(pageSize = PAGE_SIZE),
                remoteMediatorFactory = prefs.remoteMediatorFactory(source)?.wrap(query),
                excludedRecipeId = excludedRecipeId,
            )
        }
    }

    fun searchRecent(
        query: String?,
        excludedRecipeId: NutritionalItemId.Recipe?,
    ): Flow<PagingData<NutritionalItemSearch>> {
        val query = searchQuery(query)

        if (query is SearchQuery.Text) {
            eventBus.publish(NutritionalItemSearchEvent(query, dateProvider.nowInstant()))
        }

        return foodSearchRepository.searchRecent(
            query = query,
            config = PagingConfig(pageSize = PAGE_SIZE),
            now = dateProvider.now(),
            excludedRecipeId = excludedRecipeId,
        )
    }

    private fun FoodSearchPreferences.remoteMediatorFactory(
        source: ProductSource.Type
    ): ProductRemoteMediatorFactory? =
        when (source) {
            ProductSource.Type.OpenFoodFacts if this.openFoodFacts.enabled ->
                foodRemoteMediatorFactoryAggregate.openFoodFactsRemoteMediatorFactory

            ProductSource.Type.USDA if this.usda.enabled ->
                foodRemoteMediatorFactoryAggregate.usdaRemoteMediatorFactory
            else -> null
        }

    @OptIn(ExperimentalPagingApi::class)
    private fun ProductRemoteMediatorFactory.wrap(query: SearchQuery): RemoteMediatorFactory =
        object : RemoteMediatorFactory {
            override fun <K : Any, T : Any> create(): RemoteMediator<K, T>? = runBlocking {
                this@wrap.create(query, PAGE_SIZE)
            }
        }

    private companion object {
        const val PAGE_SIZE = 30
    }
}
