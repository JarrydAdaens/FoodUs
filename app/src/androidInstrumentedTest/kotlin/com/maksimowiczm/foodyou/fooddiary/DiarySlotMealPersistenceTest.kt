package com.maksimowiczm.foodyou.fooddiary

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import com.maksimowiczm.foodyou.common.result.isError
import com.maksimowiczm.foodyou.common.result.isSuccess
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.ApplyMealUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.SaveMealUseCase
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.*
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import org.junit.Assert.*
import org.junit.Test

/** Reads legacy seeded app data through renamed APIs, then tests operations on a copied database.
 * Run after LegacyDiarySlotMealFixtureTest.verifyCanonicalDatabaseAfterUpgrade on the emulator.
 */
class DiarySlotMealPersistenceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun legacyDataBackupAndMealOperationsPreserveIndependentIdentitiesAndSnapshots() = runBlocking {
        val canonical = context.getDatabasePath("open_source_database.db")
        val fixture = context.getDatabasePath("story25-upgrade.db")
        check(canonical.exists()) { "Seed the legacy fixture first" }
        check(!fixture.exists()) { "Use a fresh isolated fixture database for this acceptance run" }
        // Checkpoint with Android SQLite before copying the portable database representation.
        android.database.sqlite.SQLiteDatabase.openDatabase(canonical.path, null, 0).use {
            it.rawQuery("PRAGMA wal_checkpoint(FULL)", null).use { cursor -> assertTrue(cursor.moveToFirst()) }
        }
        canonical.copyTo(fixture)
        val db = Room.databaseBuilder(context, FoodYouDatabase::class.java, fixture.name).build()
        try {
            val slots = RoomDiarySlotRepository(db.diarySlotDao)
            val meals = RoomMealRepository(db, db.mealDao)
            val manual = RoomManualDiaryEntryRepository(db.manualDiaryEntryDao)
            val foods = RoomNutritionalItemDiaryEntryRepository(db, db.measurementDao)
            val date = LocalDate.fromEpochDays(20751)
            val targetDate = LocalDate.fromEpochDays(20752)
            val slot = requireNotNull(slots.observeDiarySlot(2501).first())
            assertEquals("Synthetic Breakfast", slot.name)
            assertEquals(LocalTime(6, 15), slot.from)
            assertEquals(LocalTime(10, 45), slot.to)
            assertEquals(40, slot.rank)
            val meal = requireNotNull(meals.getMeal(MealId(2501)))
            assertEquals(listOf("Synthetic saved first", "Synthetic saved second"), meal.items.map { it.name })
            assertEquals(listOf(150.0, 420.0), meal.items.map { it.nutritionFacts.energy.value })
            val originalFoods = foods.observeAll(2501, date).first()
            assertEquals(2, originalFoods.size)
            assertTrue(originalFoods.any { it.food is ProductSnapshot })
            assertTrue(originalFoods.any { it.food is RecipeSnapshot })
            assertEquals(listOf(150.0, 200.0), originalFoods.sortedBy { it.id.value }.map { it.nutritionFacts.energy.value })
            val originalManual = manual.observeAll(2501, date).first()
            val quickAdd = originalManual.single { !it.isPlaceholder }
            assertEquals(780.0, quickAdd.nutritionFacts.energy.value)
            assertNull(quickAdd.nutritionFacts.carbohydrates.value)
            assertEquals(2.5, quickAdd.servingCount)
            assertEquals(250.0, quickAdd.weightGrams)
            assertTrue(originalManual.single { it.isPlaceholder }.description == "Synthetic note")

            val apply = ApplyMealUseCase(meals, manual, slots, db, FixtureDateProvider)
            assertTrue(apply.apply(meal.id, 2502, targetDate).isSuccess())
            assertTrue(apply.apply(meal.id, 2502, targetDate).isSuccess())
            val loaded = manual.observeAll(2502, targetDate).first()
            assertEquals(4, loaded.size)
            assertEquals(2, loaded.count { it.nutritionFacts.energy.value == 150.0 })
            meals.delete(meal.id)
            assertNull(meals.getMeal(meal.id))
            assertTrue(apply.apply(meal.id, 2502, targetDate).isError())
            assertEquals(loaded, manual.observeAll(2502, targetDate).first())
            assertEquals(slot, slots.observeDiarySlot(2501).first())
            assertEquals(originalFoods, foods.observeAll(2501, date).first())
            assertEquals(originalManual, manual.observeAll(2501, date).first())

            assertTrue(SaveMealUseCase(foods, manual, meals, db).save("Synthetic captured Meal", 2501, date).isSuccess())
            val captured = meals.observeAll().first().single { it.name == "Synthetic captured Meal" }
            assertEquals(4, captured.itemCount)
            assertEquals(listOf(150.0, 200.0, 780.0, 0.0).sorted(), captured.items.map { it.nutritionFacts.energy.value!! }.sorted())
            slots.updateDiarySlot(2501, "Synthetic renamed slot", LocalTime(7, 0), LocalTime(11, 0))
            slots.reorderDiarySlots(listOf(2502, 2501))
            assertEquals(1, slots.observeDiarySlot(2501).first()!!.rank)
            assertEquals(captured, meals.getMeal(captured.id))
            assertEquals(originalManual, manual.observeAll(2501, date).first())
        } finally {
            db.close()
        }
        // Reopen the copy as a backup compatibility check, without destructive fallback/migration.
        val reopened = Room.databaseBuilder(context, FoodYouDatabase::class.java, fixture.name).build()
        try {
            assertEquals("Synthetic renamed slot", reopened.diarySlotDao.observeDiarySlotById(2501).first()!!.name)
            assertEquals(1, reopened.mealDao.observeAll().first().count { it.meal.name == "Synthetic captured Meal" })
        } finally {
            reopened.close()
        }
    }
}

private object FixtureDateProvider : DateProvider {
    override fun nowInstant(): Instant = Instant.fromEpochSeconds(1792886400)
    override fun observeInstant(interval: Duration): Flow<Instant> = flowOf(nowInstant())
    override fun observeDate(timeZone: TimeZone): Flow<LocalDate> = flowOf(LocalDate.fromEpochDays(20751))
}
