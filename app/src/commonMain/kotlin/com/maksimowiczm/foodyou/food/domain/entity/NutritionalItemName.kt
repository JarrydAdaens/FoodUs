package com.maksimowiczm.foodyou.food.domain.entity

/** Lightweight library identity; observing names never hydrates Recipe ingredient graphs. */
data class NutritionalItemName(val id: NutritionalItemId, val name: String, val note: String? = null, val createdEpochSeconds: Long? = null)
