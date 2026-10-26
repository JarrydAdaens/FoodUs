package com.maksimowiczm.foodyou.common.infrastructure.room

import androidx.room.TypeConverter
import com.maksimowiczm.foodyou.common.domain.food.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Internal database payloads, not an external import schema. Missing legacy columns stay null. */
class LibraryValueConverters {
    private val json = Json { encodeDefaults = true }
    @TypeConverter fun basis(value: ProductFactBasis): String = value.name
    @TypeConverter fun basis(value: String): ProductFactBasis = ProductFactBasis.valueOf(value)
    @TypeConverter fun portion(value: PortionDescription?): String? = value?.let { json.encodeToString(it) }
    @TypeConverter fun portion(value: String?): PortionDescription? = value?.let { json.decodeFromString(it) }
    @TypeConverter fun metadata(value: NutritionMetadata?): String? = value?.let { json.encodeToString(it) }
    @TypeConverter fun metadata(value: String?): NutritionMetadata? = value?.let { json.decodeFromString(it) }
    @TypeConverter fun panel(value: ReportedNutrition?): String? = value?.let { json.encodeToString(it) }
    @TypeConverter fun panel(value: String?): ReportedNutrition? = value?.let { json.decodeFromString(it) }
    @TypeConverter fun component(value: ReportedRecipeComponent): String = json.encodeToString(value)
    @TypeConverter fun component(value: String): ReportedRecipeComponent = json.decodeFromString(value)
}
