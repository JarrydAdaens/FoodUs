package com.maksimowiczm.foodyou.notification.domain

/**
 * The kinds of event the Notification Center can show (Milestone 3, Story 13).
 *
 * The taxonomy is deliberately open: a type is persisted as its [key] string, never as an ordinal,
 * and every type shares one storage shape — a key plus an ordered list of display arguments. A
 * later emitter story therefore adds a notification kind with three additive edits (an entry here,
 * a string template, and the `publish` call at its own call site) and **no database migration**.
 *
 * [key] values are part of the on-disk format. Rename a constant freely; never rename a key.
 */
enum class NotificationType(val key: String, val argumentCount: Int) {
    /** A trusted device wrote an entry into this diary. Arguments: author username, food name. */
    EntryAddedToYourDiary("entry_added_to_your_diary", argumentCount = 2),

    /** An incoming entry could not be applied. Arguments: food name. */
    EntryFailedToAdd("entry_failed_to_add", argumentCount = 1),

    /**
     * A relay message carried an envelope version this build does not understand and was refused
     * rather than silently dropped (design.md, "The Multiplayer Exception"). Arguments: the
     * unrecognised version.
     */
    UnknownMessageVersionRefused("unknown_message_version_refused", argumentCount = 1);

    companion object {
        /**
         * Resolves a persisted [key], or `null` when this build does not know it — which happens
         * for rows written by a newer build. Callers render such rows generically instead of
         * discarding them.
         */
        fun fromKey(key: String): NotificationType? = entries.firstOrNull { it.key == key }
    }
}
