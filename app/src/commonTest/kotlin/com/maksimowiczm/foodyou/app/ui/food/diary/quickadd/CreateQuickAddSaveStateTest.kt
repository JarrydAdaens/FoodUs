package com.maksimowiczm.foodyou.app.ui.food.diary.quickadd

import kotlin.test.*

class CreateQuickAddSaveStateTest {
    @Test fun inFlightAndCompletedFormsCannotStartAnotherInsert() {
        val inserting = assertNotNull(CreateQuickAddSaveState().start())
        assertTrue(inserting.busy)
        assertNull(inserting.start())
        val committed = inserting.success()
        assertTrue(committed.busy)
        assertTrue(committed.saved)
        assertNull(committed.start())
    }

    @Test fun failedInsertCanBeRetriedAndClearsFailureForTheRetry() {
        val failed = assertNotNull(CreateQuickAddSaveState().start()).failure()
        assertFalse(failed.busy)
        assertTrue(failed.failed)
        val retry = assertNotNull(failed.start())
        assertTrue(retry.saving)
        assertFalse(retry.failed)
        assertTrue(retry.success().saved)
    }
}
