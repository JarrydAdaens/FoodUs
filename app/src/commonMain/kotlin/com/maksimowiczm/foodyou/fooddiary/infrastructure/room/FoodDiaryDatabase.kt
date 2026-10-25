package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

interface FoodDiaryDatabase {
    val manualDiaryEntryDao: ManualDiaryEntryDao
    val measurementDao: MeasurementDao
    val diarySlotDao: DiarySlotDao
    val mealDao: MealDao
}
