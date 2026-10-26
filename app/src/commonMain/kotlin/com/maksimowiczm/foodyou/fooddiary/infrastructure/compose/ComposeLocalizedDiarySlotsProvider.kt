package com.maksimowiczm.foodyou.fooddiary.infrastructure.compose

import com.maksimowiczm.foodyou.app.generated.resources.Res
import com.maksimowiczm.foodyou.common.log.Logger
import com.maksimowiczm.foodyou.fooddiary.domain.service.LocalizedDiarySlotsProvider
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class ComposeLocalizedDiarySlotsProvider(private val logger: Logger) : LocalizedDiarySlotsProvider {
    override suspend fun getDiarySlots(languageTag: String): List<LocalizedDiarySlotsProvider.LocalizedDiarySlot> {
        val content =
            try {
                Res.readBytes("files/meals/meals-$languageTag.json")
            } catch (e: Exception) {
                logger.w(TAG, e) {
                    "Failed to read meals file for locale $languageTag, falling back to default"
                }

                Res.readBytes("files/meals/meals.json")
            }

        return Json.decodeFromString<List<DiarySlotJson>>(content.decodeToString()).map { diarySlotJson ->
            LocalizedDiarySlotsProvider.LocalizedDiarySlot(
                name = diarySlotJson.name,
                start =
                    LocalTime(
                        hour = diarySlotJson.from.substringBefore(':').toInt(),
                        minute = diarySlotJson.from.substringAfter(':').toInt(),
                    ),
                end =
                    LocalTime(
                        hour = diarySlotJson.to.substringBefore(':').toInt(),
                        minute = diarySlotJson.to.substringAfter(':').toInt(),
                    ),
            )
        }
    }

    @Serializable private data class DiarySlotJson(val name: String, val from: String, val to: String)

    private companion object {
        const val TAG = "ComposeLocalizedMealsProvider"
    }
}
