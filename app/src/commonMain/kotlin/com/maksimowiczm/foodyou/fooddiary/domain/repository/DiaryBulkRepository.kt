package com.maksimowiczm.foodyou.fooddiary.domain.repository

import kotlinx.datetime.LocalDate

/** A diary collection is always a dated configured slot, never a slot name. */
data class DiaryEndpoint(val date: LocalDate, val slotId: Long)
enum class DiaryBulkAction { Delete, Move, Copy }
enum class DiaryBulkError { EmptySource, SameEndpoint, SlotDeleted, SourceChanged, StorageFailure }
class DiaryBulkException(val reason: DiaryBulkError) : Exception()

/** Opaque immutable preparation data. Presentation must never inspect or log its contents. */
interface DiaryBulkVersion

data class DiaryBulkPreview(val source: DiaryEndpoint, val count: Int, val version: DiaryBulkVersion)

interface DiaryBulkRepository {
    suspend fun prepare(source: DiaryEndpoint): DiaryBulkPreview
    /** Revalidates the complete captured source and commits all changes in one transaction. */
    suspend fun execute(action: DiaryBulkAction, preview: DiaryBulkPreview, destination: DiaryEndpoint? = null)
}
