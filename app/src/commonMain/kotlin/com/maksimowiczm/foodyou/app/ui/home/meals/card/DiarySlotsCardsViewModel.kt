package com.maksimowiczm.foodyou.app.ui.home.meals.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiaryEntry
import com.maksimowiczm.foodyou.fooddiary.domain.entity.RecipeSnapshot
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ProductSnapshot
import com.maksimowiczm.foodyou.common.domain.food.ProductFactBasis
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiarySlotEntries
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiarySlotsPreferences
import com.maksimowiczm.foodyou.fooddiary.domain.entity.NutritionalItemDiaryEntry
import com.maksimowiczm.foodyou.fooddiary.domain.entity.ManualDiaryEntry
import com.maksimowiczm.foodyou.fooddiary.domain.entity.MealId
import com.maksimowiczm.foodyou.fooddiary.domain.repository.NutritionalItemDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.ManualDiaryEntryRepository
import com.maksimowiczm.foodyou.fooddiary.domain.repository.MealRepository
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.ApplyMealUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.ObserveDiarySlotEntriesUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.SaveMealUseCase
import com.maksimowiczm.foodyou.common.result.isSuccess
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate

internal class DiarySlotsCardsViewModel(
    private val observeDiarySlotEntriesUseCase: ObserveDiarySlotEntriesUseCase,
    private val foodEntryRepository: NutritionalItemDiaryEntryRepository,
    private val manualEntryRepository: ManualDiaryEntryRepository,
    private val saveMealUseCase: SaveMealUseCase,
    private val applyMealUseCase: ApplyMealUseCase,
    private val mealRepository: MealRepository,
    diarySlotsPreferencesRepository: UserPreferencesRepository<DiarySlotsPreferences>,
    bulkRepository: com.maksimowiczm.foodyou.fooddiary.domain.repository.DiaryBulkRepository,
) : ViewModel() {
    val bulk = DiaryBulkController(bulkRepository, viewModelScope)
    private val dateState = MutableStateFlow<LocalDate?>(null)
    private val refresh = MutableStateFlow(0)
    val readFailed = MutableStateFlow(false)
    fun retryRead() { readFailed.value = false; refresh.value++ }

    val diarySlotEntries: StateFlow<List<DiarySlotModel>?> =
        dateState
            .filterNotNull()
            .flatMapLatest { date ->
                refresh.flatMapLatest {
                    observeDiarySlotEntriesUseCase.observe(date)
                        .map<List<DiarySlotEntries>, List<DiarySlotModel>?> { list -> list.map { it.toDiarySlotModel() } }
                        .catch { readFailed.value = true; emit(null) }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(60_000),
                initialValue = null,
            )

    private val _layout = diarySlotsPreferencesRepository.observe().map { it.layout }
    val layout =
        _layout.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000),
            initialValue = runBlocking { _layout.first() },
        )

    val meals: StateFlow<List<MealModel>> =
        refresh.flatMapLatest {
            mealRepository.observeAll()
                .map { list -> list.map { MealModel(id = it.id, name = it.name, itemCount = it.itemCount) } }
                .catch { readFailed.value = true; emit(emptyList()) }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(60_000),
                initialValue = emptyList(),
            )

    fun setDate(date: LocalDate) {
        viewModelScope.launch { dateState.value = date }
    }

    private var mealOperationInFlight = false

    fun saveAsMeal(diarySlotId: Long, name: String, onResult: (Boolean) -> Unit) {
        val date = dateState.value
        if (date == null || mealOperationInFlight) { onResult(false); return }
        mealOperationInFlight = true
        viewModelScope.launch {
            try { onResult(saveMealUseCase.save(name, diarySlotId, date).isSuccess()) }
            finally { mealOperationInFlight = false }
        }
    }

    fun applyMeal(mealId: MealId, diarySlotId: Long, onResult: (Boolean) -> Unit) {
        val date = dateState.value
        if (date == null || mealOperationInFlight) { onResult(false); return }
        mealOperationInFlight = true
        viewModelScope.launch {
            try { onResult(applyMealUseCase.apply(mealId, diarySlotId, date).isSuccess()) }
            finally { mealOperationInFlight = false }
        }
    }

    fun deleteMeal(mealId: MealId, onResult: (Boolean) -> Unit) {
        if (mealOperationInFlight) { onResult(false); return }
        mealOperationInFlight = true
        viewModelScope.launch {
            try { mealRepository.delete(mealId); onResult(true) }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { onResult(false) }
            finally { mealOperationInFlight = false }
        }
    }

    fun onDeleteEntry(model: DiarySlotEntryModel) {
        viewModelScope.launch {
            when (model) {
                is FoodDiarySlotEntryModel -> foodEntryRepository.delete(model.id)
                is ManualDiarySlotEntryModel -> manualEntryRepository.delete(model.id)
            }
        }
    }
}

private fun DiarySlotEntries.toDiarySlotModel(): DiarySlotModel =
    DiarySlotModel(
        id = diarySlot.id,
        name = diarySlot.name,
        from = diarySlot.from,
        to = diarySlot.to,
        isAllDay = diarySlot.from == diarySlot.to,
        foods = entries.map { it.toDiarySlotEntryModel() },
        energy = nutritionFacts.energy.value?.roundToInt() ?: 0,
        proteins = nutritionFacts.proteins.value ?: 0.0,
        carbohydrates = nutritionFacts.carbohydrates.value ?: 0.0,
        fats = nutritionFacts.fats.value ?: 0.0,
    )

internal fun DiaryEntry.toDiarySlotEntryModel(): DiarySlotEntryModel =
    when (this) {
        is NutritionalItemDiaryEntry -> {
            val captured = food is ProductSnapshot && food.factBasis == ProductFactBasis.REFERENCE_SERVING ||
                food is RecipeSnapshot && food.reportedNutrition != null
            val facts = if(captured) food.nutritionFor(measurement) else nutritionFacts
            FoodDiarySlotEntryModel(
                id = id,
                name = food.name,
                energy = if(captured) facts?.energy?.value?.takeIf { it.isFinite() }?.roundToInt() else nutritionFacts.energy.value?.roundToInt(),
                proteins = facts?.proteins?.value,
                carbohydrates = facts?.carbohydrates?.value,
                fats = facts?.fats?.value,
                measurement = measurement,
                weight = weight,
                isLiquid = food.isLiquid,
                isRecipe = food is RecipeSnapshot,
                totalWeight = food.totalWeight,
                servingWeight = food.servingWeight,
                isCapturedContext = captured,
                capturedFacts = facts.takeIf { captured },
                referencePortion = when(food) {
                    is ProductSnapshot -> food.referencePortion
                    is RecipeSnapshot -> food.reportedNutrition?.reference
                },
                reportedBasis = (food as? RecipeSnapshot)?.reportedNutrition?.basis,
            )
        }

        is ManualDiaryEntry ->
            ManualDiarySlotEntryModel(
                id = id,
                name = name,
                energy = nutritionFacts.energy.value?.takeIf { it.isFinite() }?.roundToInt(),
                proteins = nutritionFacts.proteins.value,
                carbohydrates = nutritionFacts.carbohydrates.value,
                fats = nutritionFacts.fats.value,
                isPlaceholder = isPlaceholder,
                capturedPortion = capturedPortion,
                capturedFacts = nutritionFacts.takeIf { !isPlaceholder && (portionDescription != null || nutritionMetadata != null) },
                portionDescription = portionDescription,
            )
    }
