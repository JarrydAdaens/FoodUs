package com.maksimowiczm.foodyou.fooddiary.domain.usecase

import com.maksimowiczm.foodyou.common.domain.database.TransactionProvider
import com.maksimowiczm.foodyou.common.domain.database.TransactionScope
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.food.NutrientValue
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.result.isError
import com.maksimowiczm.foodyou.common.result.isSuccess
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemSnapshot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiarySlot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemDiaryEntry
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemDiaryEntryId
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntry
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntryId
import com.maksimowiczm.foodyou.fooddiary.domain.entity.Meal
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealId
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealItem
import com.maksimowiczm.foodyou.fooddiary.domain.repository.DiarySlotRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.NutritionalItemDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.ManualDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

/**
 * Covers the core Meal loop (Milestone 2, Story 13): snapshotting a diarySlot's entries into a
 * Meal and fanning that Meal back out into separate diary entries on another day. The
 * snapshot mapping works over the [com.maksimowiczm.foodyou.fooddiary.domain.entity.DiaryEntry]
 * interface (name + total nutrition), so manual entries exercise the same path products and recipes
 * take.
 */
class MealUseCasesTest {

    @Test
    fun mealAndSlotWithSameNumericIdRemainIndependentAndRepeatedLoadsAppend() = runBlocking {
        val entries = FakeManualDiaryEntryRepository()
        val meals = FakeMealRepository()
        val date = LocalDate(2026, 1, 1)
        val mealId = meals.save("Saved breakfast", listOf(MealItem("Toast", nutrition(125.0, 4.0))))
        val slotId = mealId.value
        entries.entries += manualEntry(slotId, date, "Existing", 12.0, 1.0)
        val apply = ApplyMealUseCase(meals, entries, FakeDiarySlotRepository(slotId),
            ImmediateTransactionProvider, FixedDateProvider)

        assertTrue(apply.apply(mealId, slotId, date).isSuccess())
        assertTrue(apply.apply(mealId, slotId, date).isSuccess())
        val historical = entries.entries.toList()
        assertEquals(listOf("Existing", "Toast", "Toast"), historical.map { it.name })
        assertTrue(historical.all { it.diarySlotId == slotId && it.date == date })
        assertEquals(listOf(12.0, 125.0, 125.0), historical.map { it.nutritionFacts.energy.value })

        meals.delete(mealId)
        assertTrue(apply.apply(mealId, slotId, date).isError())
        assertEquals(historical, entries.entries, "Deleting a saved Meal preserves all diary snapshots")
    }

    @Test
    fun unknownDestinationDoesNotAppendOrConfuseMealIdWithSlotId() = runBlocking {
        val entries = FakeManualDiaryEntryRepository()
        val meals = FakeMealRepository()
        val id = meals.save("Meal", listOf(MealItem("Item", nutrition(80.0, 3.0))))
        val apply = ApplyMealUseCase(meals, entries, FakeDiarySlotRepository(42),
            ImmediateTransactionProvider, FixedDateProvider)

        assertTrue(apply.apply(id, id.value, LocalDate(2026, 1, 1)).isError())
        assertTrue(entries.entries.isEmpty())
        assertEquals(1, meals.stored.size)
    }

    @Test
    fun savingCapturesOnlySelectedSlotAndDateAndKeepsUnknownQuickAddNutrition() = runBlocking {
        val date = LocalDate(2026, 1, 1)
        val entries = FakeManualDiaryEntryRepository()
        val meals = FakeMealRepository()
        val quickAdd = manualEntry(7, date, "Quick Add", 250.0, 8.0).copy(
            servingCount = 3.0, weightGrams = 600.0,
            nutritionFacts = NutritionFacts(energy = NutrientValue.Complete(250.0)),
        )
        val placeholder = manualEntry(7, date, "Unresolved", 0.0, 0.0).copy(isPlaceholder = true)
        entries.entries += listOf(quickAdd, placeholder,
            manualEntry(8, date, "Other slot", 900.0, 1.0),
            manualEntry(7, LocalDate(2026, 1, 2), "Other day", 800.0, 1.0))
        val before = entries.entries.toList()

        assertTrue(SaveMealUseCase(FakeFoodDiaryEntryRepository(), entries, meals, ImmediateTransactionProvider)
            .save("Captured", 7, date).isSuccess())
        val saved = meals.stored.values.single()
        assertEquals(listOf("Quick Add", "Unresolved"), saved.items.map { it.name })
        assertEquals(quickAdd.nutritionFacts, saved.items.first().nutritionFacts)
        assertEquals(null, saved.items.first().nutritionFacts.proteins.value)
        assertEquals(before, entries.entries, "Saving does not mutate diary kinds or portion metadata")
    }

    @Test
    fun savingAndApplyingRecreatesEachItemAsItsOwnEntryWithMatchingNutrition() = runBlocking {
        val foodRepository = FakeFoodDiaryEntryRepository()
        val manualRepository = FakeManualDiaryEntryRepository()
        val mealRepository = FakeMealRepository()

        val sourceDiarySlotId = 1L
        val sourceDate = LocalDate(2026, 1, 1)

        manualRepository.entries +=
            manualEntry(sourceDiarySlotId, sourceDate, "Oats", energy = 300.0, proteins = 11.0)
        manualRepository.entries +=
            manualEntry(sourceDiarySlotId, sourceDate, "Black coffee", energy = 5.0, proteins = 0.0)

        val saveUseCase =
            SaveMealUseCase(foodRepository, manualRepository, mealRepository, ImmediateTransactionProvider)
        val applyUseCase =
            ApplyMealUseCase(
                mealRepository = mealRepository,
                manualEntryRepository = manualRepository,
                diarySlotRepository = FakeDiarySlotRepository(existingDiarySlotId = 2L),
                transactionProvider = ImmediateTransactionProvider,
                dateProvider = FixedDateProvider,
            )

        val saveResult =
            saveUseCase.save(name = "  My breakfast  ", diarySlotId = sourceDiarySlotId, date = sourceDate)
        assertTrue(saveResult.isSuccess(), "saving a non-empty meal should succeed")

        val saved = mealRepository.stored.values.single()
        assertEquals("My breakfast", saved.name, "name should be trimmed")
        assertEquals(2, saved.items.size, "both entries should be snapshotted")

        val targetDiarySlotId = 2L
        val targetDate = LocalDate(2026, 6, 15)
        val applyResult = applyUseCase.apply(saved.id, targetDiarySlotId, targetDate)
        assertTrue(applyResult.isSuccess(), "applying to an existing meal should succeed")

        val applied = manualRepository.entries.filter { it.date == targetDate }
        assertEquals(2, applied.size, "each Meal item becomes its own diary entry")
        assertEquals(setOf("Black coffee", "Oats"), applied.map { it.name }.toSet())

        val oats = applied.single { it.name == "Oats" }
        assertEquals(300.0, oats.nutritionFacts.energy.value)
        assertEquals(11.0, oats.nutritionFacts.proteins.value)

        val sourceEntries = manualRepository.entries.filter { it.date == sourceDate }
        assertEquals(2, sourceEntries.size, "the original day is untouched")
    }

    @Test
    fun savingBlankNameFails() = runBlocking {
        val manualRepository = FakeManualDiaryEntryRepository()
        manualRepository.entries += manualEntry(1L, LocalDate(2026, 1, 1), "Apple", 50.0, 0.0)
        val useCase =
            SaveMealUseCase(
                FakeFoodDiaryEntryRepository(),
                manualRepository,
                FakeMealRepository(),
                ImmediateTransactionProvider,
            )

        assertTrue(useCase.save(name = "   ", diarySlotId = 1L, date = LocalDate(2026, 1, 1)).isError())
    }

    @Test
    fun savingEmptyDiarySlotFails() = runBlocking {
        val useCase =
            SaveMealUseCase(
                FakeFoodDiaryEntryRepository(),
                FakeManualDiaryEntryRepository(),
                FakeMealRepository(),
                ImmediateTransactionProvider,
            )

        assertTrue(
            useCase.save(name = "Anything", diarySlotId = 1L, date = LocalDate(2026, 1, 1)).isError()
        )
    }
}

private val EPOCH = LocalDateTime(2026, 1, 1, 8, 0)

private fun nutrition(energy: Double, proteins: Double) =
    NutritionFacts(
        energy = NutrientValue.Complete(energy),
        proteins = NutrientValue.Complete(proteins),
    )

private fun manualEntry(
    diarySlotId: Long,
    date: LocalDate,
    name: String,
    energy: Double,
    proteins: Double,
) =
    ManualDiaryEntry(
        id = ManualDiaryEntryId(0),
        diarySlotId = diarySlotId,
        date = date,
        name = name,
        nutritionFacts = nutrition(energy, proteins),
        createdAt = EPOCH,
        updatedAt = EPOCH,
    )

private class FakeManualDiaryEntryRepository : ManualDiaryEntryRepository {
    val entries = mutableListOf<ManualDiaryEntry>()

    override fun observe(id: ManualDiaryEntryId): Flow<ManualDiaryEntry?> =
        flowOf(entries.firstOrNull { it.id == id })

    override fun observeAll(diarySlotId: Long, date: LocalDate): Flow<List<ManualDiaryEntry>> =
        flowOf(entries.filter { it.diarySlotId == diarySlotId && it.date == date })

    override suspend fun insert(
        name: String,
        diarySlotId: Long,
        date: LocalDate,
        nutritionFacts: NutritionFacts,
        createdAt: LocalDateTime,
        description: String?,
        isPlaceholder: Boolean,
        servingCount: Double?,
        weightGrams: Double?,
        capturedPortion: com.maksimowiczm.foodyou.common.domain.measurement.Measurement?,
    ): ManualDiaryEntryId {
        val id = ManualDiaryEntryId((entries.size + 1).toLong())
        entries +=
            ManualDiaryEntry(
                id = id,
                diarySlotId = diarySlotId,
                date = date,
                name = name,
                nutritionFacts = nutritionFacts,
                createdAt = createdAt,
                updatedAt = createdAt,
                description = description,
                isPlaceholder = isPlaceholder,
                servingCount = servingCount,
                weightGrams = weightGrams,
                capturedPortion = capturedPortion,
            )
        return id
    }

    override suspend fun update(entry: ManualDiaryEntry) = Unit

    override suspend fun delete(id: ManualDiaryEntryId) {
        entries.removeAll { it.id == id }
    }
}

private class FakeFoodDiaryEntryRepository : NutritionalItemDiaryEntryRepository {
    override fun observe(id: NutritionalItemDiaryEntryId): Flow<NutritionalItemDiaryEntry?> = flowOf(null)

    override fun observeAll(diarySlotId: Long, date: LocalDate): Flow<List<NutritionalItemDiaryEntry>> =
        flowOf(emptyList())

    override suspend fun insert(
        measurement: Measurement,
        diarySlotId: Long,
        date: LocalDate,
        food: NutritionalItemSnapshot,
        createdAt: LocalDateTime,
    ): NutritionalItemDiaryEntryId = NutritionalItemDiaryEntryId(0)

    override suspend fun update(entry: NutritionalItemDiaryEntry) = Unit

    override suspend fun delete(id: NutritionalItemDiaryEntryId) = Unit
}

private class FakeMealRepository : MealRepository {
    val stored = linkedMapOf<Long, Meal>()
    private val flow = MutableStateFlow<List<Meal>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<Meal>> = flow

    override suspend fun getMeal(id: MealId): Meal? = stored[id.value]

    override suspend fun save(name: String, items: List<MealItem>): MealId {
        val id = nextId++
        stored[id] = Meal(MealId(id), name, EPOCH, items)
        flow.value = stored.values.toList()
        return MealId(id)
    }

    override suspend fun update(id: MealId, name: String, items: List<MealItem>): Boolean {
        val current = stored[id.value] ?: return false
        stored[id.value] = current.copy(name = name, items = items)
        flow.value = stored.values.toList()
        return true
    }

    override suspend fun delete(id: MealId) {
        stored.remove(id.value)
        flow.value = stored.values.toList()
    }
}

private class FakeDiarySlotRepository(private val existingDiarySlotId: Long) : DiarySlotRepository {
    override fun observeDiarySlot(diarySlotId: Long): Flow<DiarySlot?> =
        flowOf(
            if (diarySlotId == existingDiarySlotId) {
                DiarySlot(diarySlotId, "Lunch", LocalTime(12, 0), LocalTime(14, 0), rank = 1)
            } else {
                null
            }
        )

    override fun observeDiarySlots(): Flow<List<DiarySlot>> = flowOf(emptyList())

    override suspend fun insertDiarySlotWithLastRank(name: String, from: LocalTime, to: LocalTime) = Unit

    override suspend fun deleteDiarySlot(diarySlotId: Long) = Unit

    override suspend fun updateDiarySlot(id: Long, name: String, from: LocalTime, to: LocalTime) = Unit

    override suspend fun reorderDiarySlots(order: List<Long>) = Unit
}

private object ImmediateTransactionProvider : TransactionProvider {
    override suspend fun <T> withTransaction(block: suspend TransactionScope<T>.() -> T): T =
        block(NoOpTransactionScope())
}

private class NoOpTransactionScope<T> : TransactionScope<T> {
    override suspend fun rollback(result: T) = Unit
}

private object FixedDateProvider : DateProvider {
    override fun nowInstant(): Instant = Instant.fromEpochSeconds(1_767_254_400)

    override fun observeInstant(interval: Duration): Flow<Instant> = flowOf(nowInstant())

    override fun observeDate(timeZone: TimeZone): Flow<LocalDate> = flowOf(LocalDate(2026, 1, 1))
}
