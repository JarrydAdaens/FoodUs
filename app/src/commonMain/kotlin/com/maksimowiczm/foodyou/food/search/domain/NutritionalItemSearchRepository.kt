package com.maksimowiczm.foodyou.food.search.domain

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.maksimowiczm.foodyou.common.domain.food.ProductSource
import com.maksimowiczm.foodyou.common.domain.search.SearchQuery
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

interface NutritionalItemSearchRepository {
    fun search(
        query: SearchQuery,
        source: ProductSource.Type,
        config: PagingConfig,
        remoteMediatorFactory: RemoteMediatorFactory?,
        excludedRecipeId: NutritionalItemId.Recipe?,
    ): Flow<PagingData<NutritionalItemSearch>>

    fun searchRecent(
        query: SearchQuery,
        config: PagingConfig,
        now: LocalDateTime,
        excludedRecipeId: NutritionalItemId.Recipe?,
    ): Flow<PagingData<NutritionalItemSearch>>

    fun searchFoodCount(
        query: SearchQuery,
        source: ProductSource.Type,
        excludedRecipeId: NutritionalItemId.Recipe?,
    ): Flow<Int>

    fun searchRecentFoodCount(
        query: SearchQuery,
        now: LocalDateTime,
        excludedRecipeId: NutritionalItemId.Recipe?,
    ): Flow<Int>
}
