package com.maksimowiczm.foodyou.app.ui.home.meals.card

import androidx.compose.runtime.saveable.SaverScope
import com.maksimowiczm.foodyou.app.ui.food.diary.component.ChipsDatePickerState
import com.maksimowiczm.foodyou.fooddiary.domain.repository.*
import com.maksimowiczm.foodyou.fooddiary.domain.entity.orderedDiaryEntries
import kotlinx.coroutines.*
import kotlinx.datetime.LocalDate
import kotlin.test.*

class DiaryBulkControllerTest {
    private val source = DiaryEndpoint(LocalDate(2020, 2, 3), 1)
    private val target = DiaryEndpoint(LocalDate(2030, 4, 5), 2)
    private object Version : DiaryBulkVersion

    @Test fun legacyEqualNamesUseTypedIdsAndExplicitOrderSurvivesRenaming() {
        val time = kotlinx.datetime.LocalDateTime(2020, 2, 3, 4, 5)
        val manual = com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntry(
            com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntryId(1), 1, source.date, "Equal",
            com.maksimowiczm.foodyou.common.domain.food.NutritionFacts(), time, time)
        val product = com.maksimowiczm.foodyou.fooddiary.domain.entity.ProductSnapshot("Equal",
            com.maksimowiczm.foodyou.common.domain.food.NutritionFacts(), null, null, false,
            com.maksimowiczm.foodyou.common.domain.food.ProductSource(com.maksimowiczm.foodyou.common.domain.food.ProductSource.Type.User), null)
        val food = com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemDiaryEntry(
            com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemDiaryEntryId(1), 1, source.date,
            com.maksimowiczm.foodyou.common.domain.measurement.Measurement.Gram(100.0), product, time, time)
        assertEquals(listOf(manual, food), listOf(food, manual).orderedDiaryEntries())
        val renamedFirst = manual.copy(name = "Z", sortOrder = 0)
        val renamedLast = food.copy(food = product.copy(name = "A"), sortOrder = 1)
        assertEquals(listOf(renamedFirst, renamedLast), listOf(renamedLast, renamedFirst).orderedDiaryEntries())
    }

    @Test fun pickerRestorePreservesDifferentTodayAndHistoricalSelectedDate() {
        val today = LocalDate(2026, 10, 26)
        val state = ChipsDatePickerState(today, listOf(today, source.date, target.date), source.date)
        val saved = with(ChipsDatePickerState.saver) { with(object : SaverScope { override fun canBeSaved(value: Any) = true }) { save(state) } }!!
        val restored = ChipsDatePickerState.saver.restore(saved)!!
        assertEquals(today, restored.today); assertEquals(source.date, restored.selectedDate)
        assertEquals(state.dates, restored.dates)
    }

    @Test fun repeatedSubmitUsesCapturedEndpointsAndConsumesSuccessfulSession() = runBlocking {
        val gate = CompletableDeferred<Unit>()
        val calls = mutableListOf<Triple<DiaryBulkAction, DiaryEndpoint, DiaryEndpoint?>>()
        val repository = object : DiaryBulkRepository {
            override suspend fun prepare(source: DiaryEndpoint) = DiaryBulkPreview(source, 3, Version)
            override suspend fun execute(action: DiaryBulkAction, preview: DiaryBulkPreview, destination: DiaryEndpoint?) {
                calls += Triple(action, preview.source, destination); gate.await()
            }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val controller = DiaryBulkController(repository, scope)
            controller.open(DiaryBulkIntent.MoveTo, source)
            assertFalse(controller.session.value!!.canSubmit)
            controller.select(target); assertTrue(controller.session.value!!.canSubmit)
            controller.submit(); controller.submit(); controller.dismiss(); controller.select(source)
            assertEquals(listOf<Triple<DiaryBulkAction, DiaryEndpoint, DiaryEndpoint?>>(Triple(DiaryBulkAction.Move, source, target)), calls)
            gate.complete(Unit); yield()
            assertNull(controller.session.value)
            controller.submit(); assertEquals(1, calls.size)
        } finally { scope.cancel() }
    }

    @Test fun copyFromPreviewsSelectedSourceAndLateNonCancellablePreviewCannotReplaceIt() = runBlocking {
        val pending = mutableMapOf<DiaryEndpoint, CompletableDeferred<DiaryBulkPreview>>()
        val repository = object : DiaryBulkRepository {
            override suspend fun prepare(source: DiaryEndpoint): DiaryBulkPreview = withContext(NonCancellable) {
                pending.getOrPut(source) { CompletableDeferred() }.await()
            }
            override suspend fun execute(action: DiaryBulkAction, preview: DiaryBulkPreview, destination: DiaryEndpoint?) = Unit
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val controller = DiaryBulkController(repository, scope)
            controller.open(DiaryBulkIntent.CopyFrom, source)
            controller.select(target)
            pending[target]!!.complete(DiaryBulkPreview(target, 5, Version)); yield()
            pending[source]!!.complete(DiaryBulkPreview(source, 9, Version)); yield()
            val session = controller.session.value!!
            assertEquals(target, session.source); assertEquals(source, session.destination)
            assertEquals(5, session.preview!!.count); assertTrue(session.canSubmit)
            controller.dismiss(); assertNull(controller.session.value)
        } finally { pending.values.forEach { it.cancel() }; scope.cancel() }
    }

    @Test fun failureRequiresExplicitRefreshAndCancelNeverWrites() = runBlocking {
        var writes = 0; var reads = 0
        val repository = object : DiaryBulkRepository {
            override suspend fun prepare(source: DiaryEndpoint) = DiaryBulkPreview(source, ++reads, Version)
            override suspend fun execute(action: DiaryBulkAction, preview: DiaryBulkPreview, destination: DiaryEndpoint?) {
                writes++; throw DiaryBulkException(DiaryBulkError.SourceChanged)
            }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val controller = DiaryBulkController(repository, scope)
            controller.open(DiaryBulkIntent.DeleteAll, source); controller.dismiss()
            assertEquals(0, writes)
            controller.open(DiaryBulkIntent.CopyTo, source); controller.select(target); controller.submit()
            assertEquals(DiaryBulkError.SourceChanged, controller.session.value!!.error)
            controller.submit(); assertEquals(1, writes)
            controller.refresh(); assertEquals(3, reads); assertTrue(controller.session.value!!.canSubmit)
        } finally { scope.cancel() }
    }
}
