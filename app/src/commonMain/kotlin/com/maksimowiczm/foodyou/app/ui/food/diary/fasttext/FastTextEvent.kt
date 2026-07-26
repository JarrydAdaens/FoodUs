package com.maksimowiczm.foodyou.app.ui.food.diary.fasttext

/** One-off UI events emitted by [FastTextViewModel]. */
internal sealed interface FastTextEvent {
    /** The placeholder was persisted; the caller should navigate back. */
    data object Saved : FastTextEvent
}
