package com.maksimowiczm.foodyou.app.ui.food.library

import com.maksimowiczm.foodyou.settings.domain.entity.LibrarySortOrder

/** Unknown keys remain last in both directions; equal keys have stable name/identity ties. */
internal fun <T> orderLibrary(items: List<T>, order: LibrarySortOrder,
    name: (T) -> String, identity: (T) -> String, created: (T) -> Long?, calories: (T) -> Double?,
): List<T> {
    val ties = compareBy<T> { name(it).lowercase() }.thenBy(identity)
    return when (order) {
        LibrarySortOrder.Alphabetical -> items.sortedWith(ties)
        LibrarySortOrder.ReverseAlphabetical -> items.sortedWith(compareByDescending<T> { name(it).lowercase() }.thenBy(identity))
        else -> items.sortedWith(Comparator { a, b ->
            val left: Double? = if (order.byCalories) calories(a)?.takeIf { it.isFinite() && it >= 0.0 } else created(a)?.toDouble()
            val right: Double? = if (order.byCalories) calories(b)?.takeIf { it.isFinite() && it >= 0.0 } else created(b)?.toDouble()
            val reverse = order == LibrarySortOrder.NewestCreated || order == LibrarySortOrder.HighestCalories
            val result = when { left == null && right == null -> 0; left == null -> 1; right == null -> -1
                reverse -> right.compareTo(left); else -> left.compareTo(right) }
            if (result == 0) ties.compare(a, b) else result
        })
    }
}
