package com.maksimowiczm.foodyou.ai.domain

/**
 * Cleans the model's textual answer into a single-line food-search query (Milestone 2, Story 9).
 *
 * Chat models frequently wrap short answers in quotes or Markdown fences, prefix them with a label
 * ("Query:"), or add an explanation on later lines. Parsing is deliberately defensive: it takes the
 * first non-empty line, strips a leading label and surrounding quotes/fences, collapses internal
 * whitespace, and returns null when nothing usable remains.
 */
object AiSearchQueryParser {
    private val labelPrefix = Regex("^(search |food )?query\\s*[:\\-]\\s*", RegexOption.IGNORE_CASE)
    private val whitespace = Regex("\\s+")

    fun parse(content: String): String? {
        val firstLine =
            content
                .lineSequence()
                .map { it.trim().trim('`').trim() }
                .firstOrNull { it.isNotBlank() } ?: return null

        val withoutLabel = firstLine.replace(labelPrefix, "")
        val unquoted = withoutLabel.trim().trim('"', '\'', '`').trim()
        val collapsed = unquoted.replace(whitespace, " ")
        return collapsed.takeIf { it.isNotBlank() }
    }
}
