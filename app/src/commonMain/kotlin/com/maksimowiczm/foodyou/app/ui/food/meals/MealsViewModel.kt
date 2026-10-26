package com.maksimowiczm.foodyou.app.ui.food.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.measurement.Measurement
import com.maksimowiczm.foodyou.common.result.isSuccess
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemId
import com.maksimowiczm.foodyou.food.domain.usecase.ObserveNutritionalItemUseCase
import com.maksimowiczm.foodyou.fooddiary.domain.entity.*
import com.maksimowiczm.foodyou.fooddiary.domain.repository.*
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

internal data class MealEditorDraft(val meal: Meal, val itemKeys: List<Long>,val originalItems:List<MealItem> = meal.items,
    val originalPanelNeedsReview:Boolean=meal.reportedNutrition?.needsReview==true)

private fun MealEditorDraft.withItems(items:List<MealItem>):Meal {
    fun composition(values:List<MealItem>)=values.groupingBy { it.copy(name="",id=null,metadata=null) }.eachCount()
    return meal.copy(items=items,reportedNutrition=meal.reportedNutrition?.copy(needsReview=originalPanelNeedsReview || composition(originalItems)!=composition(items)))
}

internal enum class MealMessage { Invalid, Unavailable, SaveFailed, LoadFailed, DeleteFailed }

/** Saved aggregates and an isolated edit draft. No draft mutation writes to Room. */
internal class MealsViewModel(
    private val repository: MealRepository,
    private val updateMeal: UpdateMealUseCase,
    private val applyMeal: ApplyMealUseCase,
    slots: DiarySlotRepository,
    private val observeItem: ObserveNutritionalItemUseCase,
) : ViewModel() {
    val message = MutableStateFlow<MealMessage?>(null)
    val busy = MutableStateFlow(false)
    private var nextDraftKey = -1L
    val editor = MutableStateFlow<MealEditorDraft?>(null)
    val readFailed = MutableStateFlow(false)
    private val refresh = MutableStateFlow(0)
    val meals: StateFlow<List<Meal>?> = refresh.flatMapLatest {
        repository.observeAll().map<List<Meal>, List<Meal>?> { it }
            .catch { readFailed.value = true; message.value = MealMessage.Unavailable; emit(null) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val diarySlots = refresh.flatMapLatest {
        slots.observeDiarySlots().catch { readFailed.value = true; message.value = MealMessage.Unavailable; emit(emptyList()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun retryRead() { readFailed.value = false; message.value = null; refresh.value++ }
    fun edit(meal: Meal) {
        if (!busy.value) {
            editor.value = MealEditorDraft(meal.copy(items = meal.items.toList()), meal.items.map { it.id?.value ?: nextDraftKey-- })
            message.value = null
        }
    }
    fun cancel() { if (!busy.value) { editor.value = null; message.value = null } }
    fun rename(name: String) { editor.value = editor.value?.let { it.copy(meal = it.meal.copy(name = name)) } }
    fun remove(index: Int) {
        editor.value = editor.value?.let { draft -> draft.copy(
            meal = draft.withItems(draft.meal.items.filterIndexed { i, _ -> i != index }),
            itemKeys = draft.itemKeys.filterIndexed { i, _ -> i != index },
        ) }
    }
    fun setItem(index: Int, item: MealItem) {
        editor.value = editor.value?.let { draft -> draft.copy(
            meal = draft.withItems(draft.meal.items.mapIndexed { i, old -> if (i == index) item else old }),
        ) }
    }

    fun capture(id: NutritionalItemId, measurement: Measurement, onAdded: () -> Unit) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            try {
                val item = observeItem.observe(id).first()
                val facts = item?.nutritionFor(measurement)
                if (item == null || facts == null || !facts.validMealNutrition()) {
                    message.value = MealMessage.Invalid
                } else {
                    val captured = MealItem(item.headline, facts.captureMealNutrition(), capturedPortion = measurement)
                    if (validateMeal("Meal", listOf(captured)) != null) message.value = MealMessage.Invalid
                    else {
                        editor.value = editor.value?.let { it.copy(meal = it.withItems(it.meal.items + captured), itemKeys = it.itemKeys + nextDraftKey--) }
                        message.value = null
                        onAdded()
                    }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { message.value = MealMessage.Unavailable }
            finally { busy.value = false }
        }
    }

    fun save() {
        val current = editor.value?.meal ?: return
        if (busy.value) return
        if (validateMeal(current.name, current.items) != null) { message.value = MealMessage.Invalid; return }
        busy.value = true
        viewModelScope.launch {
            try {
                if (updateMeal.update(current.id, current.name, current.items).isSuccess()) {
                    editor.value = null; message.value = null
                } else message.value = MealMessage.SaveFailed
            } finally { busy.value = false }
        }
    }

    fun load(id: MealId, slotId: Long, date: LocalDate, onLoaded: () -> Unit) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            try {
                if (applyMeal.apply(id, slotId, date).isSuccess()) { message.value = null; onLoaded() }
                else message.value = MealMessage.LoadFailed
            } finally { busy.value = false }
        }
    }

    fun delete(id: MealId, onDeleted: () -> Unit) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            try { repository.delete(id); message.value = null; onDeleted() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { message.value = MealMessage.DeleteFailed }
            finally { busy.value = false }
        }
    }
}
