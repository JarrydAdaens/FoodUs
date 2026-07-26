package com.maksimowiczm.foodyou.ai.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames

/**
 * Parses the model's textual answer into an [AiFoodEstimate].
 *
 * The model is prompted to return raw JSON, but vision models frequently wrap it in Markdown fences
 * or surrounding prose, so parsing is deliberately defensive: it extracts the first balanced JSON
 * object, tolerates unknown keys and common key spellings, and clamps out-of-range values. A result
 * without a usable name is treated as a parse failure (returns null).
 */
object AiFoodEstimateParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun parse(content: String): AiFoodEstimate? {
        val objectText = extractJsonObject(content) ?: return null
        val dto = runCatching { json.decodeFromString<AiFoodEstimateDto>(objectText) }.getOrNull()
            ?: return null
        val name = dto.name?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return AiFoodEstimate(
            name = name,
            certainty = dto.certainty?.coerceIn(0.0, 1.0),
            calories = dto.calories?.takeIf { it >= 0.0 },
            protein = dto.protein?.takeIf { it >= 0.0 },
            fat = dto.fat?.takeIf { it >= 0.0 },
            fibre = dto.fibre?.takeIf { it >= 0.0 },
            sugar = dto.sugar?.takeIf { it >= 0.0 },
        )
    }

    /** Returns the substring spanning the first `{` to the last `}`, or null when absent. */
    private fun extractJsonObject(content: String): String? {
        val start = content.indexOf('{')
        val end = content.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        return content.substring(start, end + 1)
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
private data class AiFoodEstimateDto(
    val name: String? = null,
    val certainty: Double? = null,
    @JsonNames("kcal", "energy") val calories: Double? = null,
    @JsonNames("proteins") val protein: Double? = null,
    @JsonNames("fats") val fat: Double? = null,
    @JsonNames("fiber") val fibre: Double? = null,
    @JsonNames("sugars") val sugar: Double? = null,
)
