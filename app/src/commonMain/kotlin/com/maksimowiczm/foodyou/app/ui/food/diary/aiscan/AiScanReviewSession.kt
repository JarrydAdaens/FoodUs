package com.maksimowiczm.foodyou.app.ui.food.diary.aiscan

import com.maksimowiczm.foodyou.ai.domain.AiFoodEstimate

internal data class ScanReviewItem(val id: String, val estimate: AiFoodEstimate)

/** In-memory, scanner-back-stack-owned queue. Counts describe successful Quick Add saves only. */
internal data class AiScanReviewSession(
    val items: List<ScanReviewItem>,
    val selectedIds: Set<String> = items.map { it.id }.toSet(),
    val savedIds: Set<String> = emptySet(),
    val queue: List<String> = emptyList(),
) {
    val selectedCount: Int get() = selectedIds.count { it !in savedIds }
    val next: ScanReviewItem? get() = queue.firstOrNull()?.let { id -> items.firstOrNull { it.id == id } }

    fun select(id: String, selected: Boolean): AiScanReviewSession {
        if (items.none { it.id == id } || id in savedIds) return this
        return copy(selectedIds = if (selected) selectedIds + id else selectedIds - id)
    }

    /** Restart after cancellation from still-selected, unsaved snapshots in response order. */
    fun start(): AiScanReviewSession = copy(queue = items.map { it.id }.filter {
        it in selectedIds && it !in savedIds
    })

    fun acknowledgeSaved(id: String): AiScanReviewSession {
        if (queue.firstOrNull() != id || id in savedIds) return this
        return copy(savedIds = savedIds + id, selectedIds = selectedIds - id, queue = queue.drop(1))
    }

    companion object {
        fun create(generation: String, estimates: List<AiFoodEstimate>) = AiScanReviewSession(
            estimates.mapIndexed { index, estimate -> ScanReviewItem("$generation:$index", estimate) }
        )
    }
}
