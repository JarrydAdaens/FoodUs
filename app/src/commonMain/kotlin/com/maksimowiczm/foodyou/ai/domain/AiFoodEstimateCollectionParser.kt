package com.maksimowiczm.foodyou.ai.domain

import kotlinx.serialization.json.*

/** Validates each pictured food separately; malformed siblings cannot erase usable estimates. */
object AiFoodEstimateCollectionParser {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun parse(content: String): AiScanResult.Success? {
        val start = content.indexOf('{')
        val end = content.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        val root = runCatching {
            json.parseToJsonElement(content.substring(start, end + 1)) as? JsonObject
        }.getOrNull() ?: return null
        if ("foods" !in root) {
            return AiFoodEstimateParser.parse(root.toString())?.let(AiScanResult::Success)
        }
        val members = root["foods"] as? JsonArray ?: return null
        val estimates = members.mapNotNull { member ->
            (member as? JsonObject)?.let { AiFoodEstimateParser.parse(it.toString()) }
        }
        return estimates.takeIf { it.isNotEmpty() }?.let {
            AiScanResult.Success(it, members.size - it.size)
        }
    }
}
