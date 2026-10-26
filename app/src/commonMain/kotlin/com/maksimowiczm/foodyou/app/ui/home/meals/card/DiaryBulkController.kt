package com.maksimowiczm.foodyou.app.ui.home.meals.card

import com.maksimowiczm.foodyou.fooddiary.domain.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DiaryBulkIntent { DeleteAll, MoveTo, CopyTo, CopyFrom }

internal data class DiaryBulkSession(
    val id: Long,
    val intent: DiaryBulkIntent,
    val current: DiaryEndpoint,
    val other: DiaryEndpoint = current,
    val preview: DiaryBulkPreview? = null,
    val loading: Boolean = true,
    val busy: Boolean = false,
    val error: DiaryBulkError? = null,
) {
    val source get() = if (intent == DiaryBulkIntent.CopyFrom) other else current
    val destination get() = if (intent == DiaryBulkIntent.CopyFrom) current else other
    val canSubmit get() = !loading && !busy && error == null && (preview?.count ?: 0) > 0 &&
        (intent == DiaryBulkIntent.DeleteAll || source != destination)
}

/** A modal owns its captured date/slot and one consumable submission, independently of Home date. */
internal class DiaryBulkController(private val repository: DiaryBulkRepository, private val scope: CoroutineScope) {
    private val mutableSession = MutableStateFlow<DiaryBulkSession?>(null)
    val session = mutableSession.asStateFlow()
    private var generation = 0L
    private var preparation: Job? = null

    fun open(intent: DiaryBulkIntent, endpoint: DiaryEndpoint) {
        if (mutableSession.value?.busy == true) return
        mutableSession.value = DiaryBulkSession(++generation, intent, endpoint)
        refresh()
    }

    fun select(endpoint: DiaryEndpoint) {
        val state = mutableSession.value ?: return
        if (state.busy || state.other == endpoint) return
        mutableSession.value = state.copy(other = endpoint)
        if (state.intent == DiaryBulkIntent.CopyFrom) refresh()
    }

    fun dismiss() {
        if (mutableSession.value?.busy == true) return
        preparation?.cancel()
        generation++
        mutableSession.value = null
    }

    fun refresh() {
        val state = mutableSession.value ?: return
        if (state.busy) return
        preparation?.cancel()
        val revision = ++generation
        mutableSession.value = state.copy(id = revision, preview = null, loading = true, error = null)
        val source = state.source
        preparation = scope.launch {
            try {
                val preview = repository.prepare(source)
                val current = mutableSession.value
                if (current?.id == revision && current.source == source)
                    mutableSession.value = current.copy(preview = preview, loading = false,
                        error = if (preview.count == 0) DiaryBulkError.EmptySource else null)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: DiaryBulkException) { failPreparation(revision, failure.reason) }
            catch (_: Exception) { failPreparation(revision, DiaryBulkError.StorageFailure) }
        }
    }

    private fun failPreparation(revision: Long, reason: DiaryBulkError) {
        mutableSession.value?.takeIf { it.id == revision }?.let {
            mutableSession.value = it.copy(loading = false, error = reason)
        }
    }

    fun submit() {
        val state = mutableSession.value ?: return
        if (!state.canSubmit) return
        // Synchronous guard before scheduling protects repeated button/keyboard activation.
        mutableSession.value = state.copy(busy = true)
        scope.launch {
            try {
                val action = when (state.intent) {
                    DiaryBulkIntent.DeleteAll -> DiaryBulkAction.Delete
                    DiaryBulkIntent.MoveTo -> DiaryBulkAction.Move
                    DiaryBulkIntent.CopyTo, DiaryBulkIntent.CopyFrom -> DiaryBulkAction.Copy
                }
                repository.execute(action, checkNotNull(state.preview), state.destination)
                if (mutableSession.value?.id == state.id) mutableSession.value = null
            } catch (cancelled: CancellationException) {
                // Never replay an ambiguous cancelled submission automatically.
                if (mutableSession.value?.id == state.id) mutableSession.value = null
                throw cancelled
            } catch (failure: DiaryBulkException) { failedSubmit(state, failure.reason) }
            catch (_: Exception) { failedSubmit(state, DiaryBulkError.StorageFailure) }
        }
    }

    private fun failedSubmit(state: DiaryBulkSession, reason: DiaryBulkError) {
        if (mutableSession.value?.id == state.id)
            mutableSession.value = state.copy(busy = false, preview = null, error = reason)
    }
}
