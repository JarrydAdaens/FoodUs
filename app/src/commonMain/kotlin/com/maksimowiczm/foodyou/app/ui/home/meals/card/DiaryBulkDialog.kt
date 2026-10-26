package com.maksimowiczm.foodyou.app.ui.home.meals.card

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.developer.inspection.*
import com.maksimowiczm.foodyou.app.ui.food.diary.component.ChipsDatePicker
import com.maksimowiczm.foodyou.app.ui.food.diary.component.rememberChipsDatePickerState
import com.maksimowiczm.foodyou.common.compose.utility.LocalDateFormatter
import com.maksimowiczm.foodyou.common.extension.now
import com.maksimowiczm.foodyou.fooddiary.domain.repository.*
import foodyou.app.generated.resources.*
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal fun DiaryBulkIntent.labelResource(): StringResource = when (this) {
    DiaryBulkIntent.DeleteAll -> Res.string.diary_bulk_delete
    DiaryBulkIntent.MoveTo -> Res.string.diary_bulk_move
    DiaryBulkIntent.CopyTo -> Res.string.diary_bulk_copy_to
    DiaryBulkIntent.CopyFrom -> Res.string.diary_bulk_copy_from
}

@Composable
internal fun DiaryBulkDialog(
    session: DiaryBulkSession,
    slots: List<DiarySlotModel>,
    onSelect: (DiaryEndpoint) -> Unit,
    onRefresh: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = LocalDateFormatter.current
    val today = LocalDate.now()
    val dateState = rememberChipsDatePickerState(today, listOf(session.current.date, session.other.date, today).distinct(), session.other.date)
    LaunchedEffect(dateState.selectedDate) {
        if (!session.busy && dateState.selectedDate != session.other.date)
            onSelect(session.other.copy(date = dateState.selectedDate))
    }
    val sourceName = slots.firstOrNull { it.id == session.source.slotId }?.name.orEmpty()
    val destinationName = slots.firstOrNull { it.id == session.destination.slotId }?.name.orEmpty()
    UiSourceScope(uiInspectionSource.element("DiaryBulkDialog.AlertDialog.1", "androidx.compose.material3.AlertDialog", "DiaryBulkDialog")) {
        AlertDialog(
            onDismissRequest = { if (!session.busy) onDismiss() },
            modifier = modifier.uiName("DiaryBulkDialog.AlertDialog.1.root", "androidx.compose.material3.AlertDialog", uiInspectionSource, "DiaryBulkDialog"),
            title = { Text(stringResource(session.intent.labelResource()), modifier = Modifier.uiName("DiaryBulkDialog.Title.1", "androidx.compose.material3.Text", uiInspectionSource, "DiaryBulkDialog")) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()).uiName("DiaryBulkDialog.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DiaryBulkDialog"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(Res.string.diary_bulk_source, formatter.formatDateShort(session.source.date), sourceName), modifier = Modifier.uiName("DiaryBulkDialog.Source.1", "androidx.compose.material3.Text", uiInspectionSource, "DiaryBulkDialog"))
                    if (session.intent != DiaryBulkIntent.DeleteAll) {
                        Text(stringResource(Res.string.diary_bulk_destination, formatter.formatDateShort(session.destination.date), destinationName), modifier = Modifier.uiName("DiaryBulkDialog.Destination.1", "androidx.compose.material3.Text", uiInspectionSource, "DiaryBulkDialog"))
                        if (!session.busy) {
                            ChipsDatePicker(dateState, modifier = Modifier.uiName("DiaryBulkDialog.ChipsDatePicker.1", "com.maksimowiczm.foodyou.app.ui.food.diary.component.ChipsDatePicker", uiInspectionSource, "DiaryBulkDialog"))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.uiName("DiaryBulkDialog.FlowRow.1", "androidx.compose.foundation.layout.FlowRow", uiInspectionSource, "DiaryBulkDialog")) {
                                slots.forEach { slot ->
                                    FilterChip(selected = session.other.slotId == slot.id,
                                        onClick = { onSelect(session.other.copy(slotId = slot.id)) },
                                        label = { Text(slot.name, modifier = Modifier.uiName("DiaryBulkDialog.SlotName.1", "androidx.compose.material3.Text", uiInspectionSource, "DiaryBulkDialog")) },
                                        modifier = Modifier.uiName("DiaryBulkDialog.FilterChip.1", "androidx.compose.material3.FilterChip", uiInspectionSource, "DiaryBulkDialog"))
                                }
                            }
                        }
                    }
                    Text(if (session.loading) stringResource(Res.string.diary_bulk_loading) else stringResource(Res.string.diary_bulk_count, session.preview?.count ?: 0), modifier = Modifier.uiName("DiaryBulkDialog.Count.1", "androidx.compose.material3.Text", uiInspectionSource, "DiaryBulkDialog"))
                    Text(stringResource(if (session.intent == DiaryBulkIntent.DeleteAll) Res.string.diary_bulk_delete_help else Res.string.diary_bulk_append_help), modifier = Modifier.uiName("DiaryBulkDialog.Help.1", "androidx.compose.material3.Text", uiInspectionSource, "DiaryBulkDialog"))
                    if (session.intent != DiaryBulkIntent.DeleteAll && session.source == session.destination)
                        Text(stringResource(Res.string.diary_bulk_same), modifier = Modifier.uiName("DiaryBulkDialog.Same.1", "androidx.compose.material3.Text", uiInspectionSource, "DiaryBulkDialog"))
                    session.error?.let { error ->
                        val message = when (error) {
                            DiaryBulkError.EmptySource -> Res.string.diary_bulk_empty
                            DiaryBulkError.SourceChanged -> Res.string.diary_bulk_stale
                            DiaryBulkError.SameEndpoint -> Res.string.diary_bulk_same
                            DiaryBulkError.SlotDeleted -> Res.string.diary_bulk_slot_deleted
                            DiaryBulkError.StorageFailure -> Res.string.diary_bulk_failed
                        }
                        Text(stringResource(message), color = MaterialTheme.colorScheme.error, modifier = Modifier.uiName("DiaryBulkDialog.Error.1", "androidx.compose.material3.Text", uiInspectionSource, "DiaryBulkDialog"))
                        TextButton(onClick = onRefresh, enabled = !session.busy && !session.loading, modifier = Modifier.uiName("DiaryBulkDialog.Refresh.1", "androidx.compose.material3.TextButton", uiInspectionSource, "DiaryBulkDialog")) {
                            Text(stringResource(Res.string.diary_bulk_refresh), modifier = Modifier.uiName("DiaryBulkDialog.RefreshText.1", "androidx.compose.material3.Text", uiInspectionSource, "DiaryBulkDialog"))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onSubmit, enabled = session.canSubmit && slots.any { it.id == session.source.slotId } && slots.any { it.id == session.destination.slotId }, modifier = Modifier.uiName("DiaryBulkDialog.Confirm.1", "androidx.compose.material3.TextButton", uiInspectionSource, "DiaryBulkDialog")) {
                    Text(stringResource(Res.string.diary_bulk_confirm), modifier = Modifier.uiName("DiaryBulkDialog.ConfirmText.1", "androidx.compose.material3.Text", uiInspectionSource, "DiaryBulkDialog"))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss, enabled = !session.busy, modifier = Modifier.uiName("DiaryBulkDialog.Cancel.1", "androidx.compose.material3.TextButton", uiInspectionSource, "DiaryBulkDialog")) {
                    Text(stringResource(Res.string.action_cancel), modifier = Modifier.uiName("DiaryBulkDialog.CancelText.1", "androidx.compose.material3.Text", uiInspectionSource, "DiaryBulkDialog"))
                }
            },
        )
    }
}

private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.home.meals.card",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/meals/card/DiaryBulkDialog.kt",
)
