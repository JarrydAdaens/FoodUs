package com.maksimowiczm.foodyou.fooddiary.domain.usecase

import com.maksimowiczm.foodyou.common.domain.database.TransactionProvider
import com.maksimowiczm.foodyou.common.domain.database.TransactionScope
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.domain.food.NutrientValue
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.result.isError
import com.maksimowiczm.foodyou.common.result.isSuccess
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiaryFood
import com.maksimowiczm.foodyou.fooddiary.domain.entity.FoodDiaryEntry
import com.maksimowiczm.foodyou.fooddiary.domain.entity.FoodDiaryEntryId
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntry
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntryId
import com.maksimowiczm.foodyou.fooddiary.domain.entity.Meal
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealTemplate
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealTemplateId
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealTemplateItem
import com.maksimowiczm.foodyou.fooddiary.domain.repository.FoodDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.ManualDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealTemplateRepository
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
 * Covers the core template loop (Milestone 2, Story 13): snapshotting a meal's entries into a
 * template and fanning that template back out into separate diary entries on another day. The
 * snapshot mapping works over the [com.maksimowiczm.foodyou.fooddiary.domain.entity.DiaryEntry]
 * interface (name + total nutrition), so manual entries exercise the same path products and recipes
 * take.
 */
class MealTemplateUseCasesTest {

    @Test
    fun savingAndApplyingRecreatesEachItemAsItsOwnEntryWithMatchingNutrition() = runBlocking {
        val foodRepository = FakeFoodDiaryEntryRepository()
        val manualRepository = FakeManualDiaryEntryRepository()
        val templateRepository = FakeMealTemplateRepository()

        val sourceMealId = 1L
        val sourceDate = LocalDate(2026, 1, 1)

        manualRepository.entries +=
            manualEntry(sourceMealId, sourceDate, "Oats", energy = 300.0, proteins = 11.0)
        manualRepository.entries +=
            manualEntry(sourceMealId, sourceDate, "Black coffee", energy = 5.0, proteins = 0.0)

        val saveUseCase =
            SaveMealTemplateUseCase(foodRepository, manualRepository, templateRepository)
        val applyUseCase =
            ApplyMealTemplateUseCase(
                templateRepository = templateRepository,
                manualEntryRepository = manualRepository,
                mealRepository = FakeMealRepository(existingMealId = 2L),
                transactionProvider = ImmediateTransactionProvider,
                dateProvider = FixedDateProvider,
            )

        val saveResult =
            saveUseCase.save(name = "  My breakfast  ", mealId = sourceMealId, date = sourceDate)
        assertTrue(saveResult.isSuccess(), "saving a non-empty meal should succeed")

        val saved = templateRepository.stored.values.single()
        assertEquals("My breakfast", saved.name, "name should be trimmed")
        assertEquals(2, saved.items.size, "both entries should be snapshotted")

        val targetMealId = 2L
        val targetDate = LocalDate(2026, 6, 15)
        val applyResult = applyUseCase.apply(saved.id, targetMealId, targetDate)
        assertTrue(applyResult.isSuccess(), "applying to an existing meal should succeed")

        val applied = manualRepository.entries.filter { it.date == targetDate }
        assertEquals(2, applied.size, "each template item becomes its own diary entry")
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
            SaveMealTemplateUseCase(
                FakeFoodDiaryEntryRepository(),
                manualRepository,
                FakeMealTemplateRepository(),
            )

        assertTrue(useCase.save(name = "   ", mealId = 1L, date = LocalDate(2026, 1, 1)).isError())
    }

    @Test
    fun savingEmptyMealFails() = runBlocking {
        val useCase =
            SaveMealTemplateUseCase(
                FakeFoodDiaryEntryRepository(),
                FakeManualDiaryEntryRepository(),
                FakeMealTemplateRepository(),
            )

        assertTrue(
            useCase.save(name = "Anything", mealId = 1L, date = LocalDate(2026, 1, 1)).isError()
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
    mealId: Long,
    date: LocalDate,
    name: String,
    energy: Double,
    proteins: Double,
) =
    ManualDiaryEntry(
        id = ManualDiaryEntryId(0),
        mealId = mealId,
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

    override fun observeAll(mealId: Long, date: LocalDate): Flow<List<ManualDiaryEntry>> =
        flowOf(entries.filter { it.mealId == mealId && it.date == date })

    override suspend fun insert(
        name: String,
        mealId: Long,
        date: LocalDate,
        nutritionFacts: NutritionFacts,
        createdAt: LocalDateTime,
        description: String?,
        isPlaceholder: Boolean,
    ): ManualDiaryEntryId {
        val id = ManualDiaryEntryId((entries.size + 1).toLong())
        entries +=
            ManualDiaryEntry(
                id = id,
                mealId = mealId,
                date = date,
                name = name,
                nutritionFacts = nutritionFacts,
                createdAt = createdAt,
                updatedAt = createdAt,
                description = description,
                isPlaceholder = isPlaceholder,
            )
        return id
    }

    override suspend fun update(entry: ManualDiaryEntry) = Unit

    override suspend fun delete(id: ManualDiaryEntryId) {
        entries.removeAll { it.id == id }
    }
}

private class FakeFoodDiaryEntryRepository : FoodDiaryEntryRepository {
    override fun observe(id: FoodDiaryEntryId): Flow<FoodDiaryEntry?> = flowOf(null)

    override fun observeAll(mealId: Long, date: LocalDate): Flow<List<FoodDiaryEntry>> =
        flowOf(emptyList())

    override suspend fun insert(
        measurement: Measurement,
        mealId: Long,
        date: LocalDate,
        food: DiaryFood,
        createdAt: LocalDateTime,
    ): FoodDiaryEntryId = FoodDiaryEntryId(0)

    override suspend fun update(entry: FoodDiaryEntry) = Unit

    override suspend fun delete(id: FoodDiaryEntryId) = Unit
}

private class FakeMealTemplateRepository : MealTemplateRepository {
    val stored = linkedMapOf<Long, MealTemplate>()
    private val flow = MutableStateFlow<List<MealTemplate>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<MealTemplate>> = flow

    override suspend fun getTemplate(id: MealTemplateId): MealTemplate? = stored[id.value]

    override suspend fun save(name: String, items: List<MealTemplateItem>): MealTemplateId {
        val id = nextId++
        stored[id] = MealTemplate(MealTemplateId(id), name, EPOCH, items)
        flow.value = stored.values.toList()
        return MealTemplateId(id)
    }

    override suspend fun delete(id: MealTemplateId) {
        stored.remove(id.value)
        flow.value = stored.values.toList()
    }
}

private class FakeMealRepository(private val existingMealId: Long) : MealRepository {
    override fun observeMeal(mealId: Long): Flow<Meal?> =
        flowOf(
            if (mealId == existingMealId) {
                Meal(mealId, "Lunch", LocalTime(12, 0), LocalTime(14, 0), rank = 1)
            } else {
                null
            }
        )

    override fun observeMeals(): Flow<List<Meal>> = flowOf(emptyList())

    override suspend fun insertMealWithLastRank(name: String, from: LocalTime, to: LocalTime) = Unit

    override suspend fun deleteMeal(mealId: Long) = Unit

    override suspend fun updateMeal(id: Long, name: String, from: LocalTime, to: LocalTime) = Unit

    override suspend fun reorderMeals(order: List<Long>) = Unit
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
