package com.maksimowiczm.foodyou.fooddiary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.ai.domain.*
import com.maksimowiczm.foodyou.app.infrastructure.room.FoodYouDatabase
import com.maksimowiczm.foodyou.app.ui.food.diary.placeholder.PlaceholderMetaViewModel
import com.maksimowiczm.foodyou.app.ui.home.goals.GoalsViewModel as HomeSummaryViewModel
import com.maksimowiczm.foodyou.app.ui.goals.master.GoalsViewModel as DailySummaryViewModel
import com.maksimowiczm.foodyou.common.config.AppConfig
import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.domain.measurement.*
import com.maksimowiczm.foodyou.common.domain.userpreferences.*
import com.maksimowiczm.foodyou.food.ProductTestClock
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.ObserveDiarySlotEntriesUseCase
import com.maksimowiczm.foodyou.fooddiary.infrastructure.repository.*
import com.maksimowiczm.foodyou.fooddiary.infrastructure.room.DiarySlotEntity
import com.maksimowiczm.foodyou.goals.domain.entity.*
import com.maksimowiczm.foodyou.goals.domain.repository.GoalsRepository
import com.maksimowiczm.foodyou.settings.domain.entity.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.LocalDate
import org.junit.Assert.*
import org.junit.Test

class MealSummaryReadRecoveryTest {
    private class Prefs<T : UserPreferences>(initial: T) : UserPreferencesRepository<T> {
        private val state = MutableStateFlow(initial)
        override fun observe() = state
        override suspend fun update(transform: T.() -> T) { state.value = transform(state.value) }
    }

    @Test fun malformedManualIsUnavailableInBothSummariesAndPlaceholderUntilExplicitRetry() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, FoodYouDatabase::class.java).build()
        val viewModels = mutableListOf<ViewModel>()
        val collectors = mutableListOf<Job>()
        try {
            val date = LocalDate(2026, 10, 25)
            db.diarySlotDao.insertWithLastRank(DiarySlotEntity(2401, "Synthetic summary slot", 0, 0, 23, 59))
            val manual = RoomManualDiaryEntryRepository(db.manualDiaryEntryDao)
            val foods = RoomNutritionalItemDiaryEntryRepository(db, db.measurementDao)
            val slots = RoomDiarySlotRepository(db.diarySlotDao)
            manual.insert("Synthetic known diary", 2401, date,
                NutritionFacts(energy = NutrientValue.Complete(150.0)), ProductTestClock.now())
            val id = manual.insert("Synthetic placeholder", 2401, date, NutritionFacts.Empty, ProductTestClock.now(),
                isPlaceholder = true, capturedPortion = Measurement.Serving(1.0))
            val good = db.manualDiaryEntryDao.observe(id.value).first()!!
            val malformed = good.copy(capturedPortionType = null)
            db.manualDiaryEntryDao.update(malformed)
            val slotPrefs = Prefs(DiarySlotsPreferences(DiarySlotsCardsLayout.Vertical, false, false))
            val observe = ObserveDiarySlotEntriesUseCase(slots, slotPrefs, foods, manual, ProductTestClock)
            val goals = object : GoalsRepository {
                override suspend fun updateWeeklyGoals(weeklyGoals: WeeklyGoals) = error("Read-only fixture")
                override fun observeWeeklyGoals() = flowOf(WeeklyGoals.defaultGoals)
                override fun observeDailyGoals(date: LocalDate) = flowOf(DailyGoal.defaultGoals)
            }
            val settings = Prefs(Settings(null, false, false, NutrientsOrder.entries, false, HomeCard.defaultOrder,
                false, true, EnergyFormat.Kilocalories, GraphStyle.Bar, WeekLayout.Scrolling, AppLaunchInfo(null, null, 0)))
            val config = EMPTY_CONFIG
            val home = HomeSummaryViewModel(settings, observe, goals)
            val daily = DailySummaryViewModel(goals, observe)
            val placeholder = PlaceholderMetaViewModel(id.value, manual, slots,
                object : AiSearchQueryGenerator {
                    override suspend fun generateQuery(diarySlotName: String?, note: String): AiQueryResult = error("No AI in read fixture")
                }, ObserveAiConfigured(Prefs(AiSettings(null, null, null, null)), config))
            viewModels += listOf(home, daily, placeholder)
            home.setDate(date)
            val dailyState = daily.observeUiStateByDate(date)
            collectors += launch { home.model.collect() }
            collectors += launch { dailyState.collect() }
            collectors += launch { placeholder.uiState.collect() }
            withTimeout(10_000) {
                home.readFailed.first { it }
                daily.failedDates.first { date in it }
                placeholder.readFailed.first { it }
            }
            assertNull(home.model.value)
            assertNull(dailyState.value)
            assertFalse(placeholder.uiState.value.loaded)
            assertEquals(malformed, db.manualDiaryEntryDao.observe(id.value).first())
            db.manualDiaryEntryDao.update(good)
            home.retryRead(); daily.retryRead(date); placeholder.retryRead()
            val summary = withTimeout(10_000) { home.model.filterNotNull().first() }
            val details = withTimeout(10_000) { dailyState.filterNotNull().first() }
            val restored = withTimeout(10_000) { placeholder.uiState.first { it.loaded && it.diarySlotName != null } }
            assertEquals(150, summary.energy)
            assertEquals(150.0, details.diarySlots.single().nutritionFacts.energy.value)
            assertEquals("Synthetic placeholder", restored.name)
            assertEquals("Synthetic summary slot", restored.diarySlotName)
            assertFalse(home.readFailed.value)
            assertFalse(date in daily.failedDates.value)
            assertFalse(placeholder.readFailed.value)
            assertEquals(good, db.manualDiaryEntryDao.observe(id.value).first())
        } finally {
            collectors.forEach { it.cancel() }
            collectors.joinAll()
            viewModels.forEach { it.viewModelScope.cancel() }
            db.close()
        }
    }

    private companion object {
        val EMPTY_CONFIG = object : AppConfig {
            override val versionName = ""
            override val forkVersionName = ""
            override val contactEmailUri = ""
            override val translationUri = ""
            override val sourceCodeUri = ""
            override val issueTrackerUri = ""
            override val upstreamAuthorUri = ""
            override val privacyPolicyUri = ""
            override val openFoodFactsTermsOfUseUri = ""
            override val openFoodFactsPrivacyPolicyUri = ""
            override val foodDataCentralPrivacyPolicyUri = ""
            override val openFoodFactsWebsiteUri = ""
            override val foodDataCentralWebsiteUri = ""
            override val swissFoodCompositionDatabaseWebsiteUri = ""
            override val australianFoodCompositionDatabaseWebsiteUri = ""
            override val aiApiKey = ""
            override val aiEndpoint = ""
            override val aiModel = ""
        }
    }
}
