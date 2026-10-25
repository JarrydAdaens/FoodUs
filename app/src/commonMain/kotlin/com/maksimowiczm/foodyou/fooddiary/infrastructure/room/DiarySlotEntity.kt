package com.maksimowiczm.foodyou.fooddiary.infrastructure.room

import androidx.room.Entity
import androidx.room.PrimaryKey

// Legacy table identity is retained for existing databases and portable backups.
@Entity(tableName = "Meal")
data class DiarySlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val fromHour: Int,
    val fromMinute: Int,
    val toHour: Int,
    val toMinute: Int,
    val rank: Int = 0,
)
