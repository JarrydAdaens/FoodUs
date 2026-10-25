package com.maksimowiczm.foodyou.app.ui.meal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.extension.hapticDraggableHandle
import com.maksimowiczm.foodyou.common.compose.extension.add
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun DiarySlotSettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: DiarySlotSettingsViewModel = koinViewModel()

    val diarySlots = viewModel.diarySlots.collectAsStateWithLifecycle().value

    if (diarySlots == null) {
        // TODO loading state
    } else {
        DiarySlotSettingsScreen(
            onBack = onBack,
            onDelete = viewModel::deleteDiarySlot,
            onUpdate = { diarySlotId, state ->
                val model = state.intoDiarySlotModel(diarySlotId)
                viewModel.updateDiarySlot(model)
            },
            onCreate = {
                val model = it.intoDiarySlotModel(0)
                viewModel.createDiarySlot(model)
            },
            onSaveOrder = viewModel::updateDiarySlotOrder,
            diarySlots = diarySlots,
            modifier = modifier,
        )
    }
}

@Composable
private fun DiarySlotSettingsScreen(
    onBack: () -> Unit,
    onDelete: (DiarySlotModel) -> Unit,
    onUpdate: (diarySlotId: Long, DiarySlotCardState) -> Unit,
    onCreate: (DiarySlotCardState) -> Unit,
    onSaveOrder: (List<DiarySlotModel>) -> Unit,
    diarySlots: List<DiarySlotModel>,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current

    var isReordering by rememberSaveable { mutableStateOf(false) }
    var showForm by rememberSaveable { mutableStateOf(false) }
    val formFocusRequester = remember { FocusRequester() }
    LaunchedEffect(showForm) {
        val _ = runCatching { formFocusRequester.requestFocus() }
    }

    val lazyListState = rememberLazyListState()
    var diarySlotsOrder by remember(diarySlots) { mutableStateOf(diarySlots) }
    val reorderableLazyListState =
        rememberReorderableLazyListState(lazyListState) { from, to ->
            diarySlotsOrder = diarySlotsOrder.toMutableList().apply { add(to.index, removeAt(from.index)) }
        }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_diary_slots)) },
                navigationIcon = { ArrowBackIconButton(onBack) },
                actions = {
                    if (isReordering) {
                        IconButton(
                            onClick = {
                                onSaveOrder(diarySlotsOrder)
                                isReordering = false
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = stringResource(Res.string.action_save),
                            )
                        }
                    } else {
                        TooltipBox(
                            positionProvider =
                                TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Above
                                ),
                            tooltip = {
                                PlainTooltip { Text(stringResource(Res.string.action_reorder)) }
                            },
                            state = rememberTooltipState(),
                        ) {
                            IconButton(
                                onClick = {
                                    isReordering = true
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Reorder,
                                    contentDescription = stringResource(Res.string.action_reorder),
                                )
                            }
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .imePadding()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
            state = lazyListState,
            contentPadding = paddingValues.add(vertical = 8.dp),
        ) {
            items(items = diarySlotsOrder, key = { it.id }) { diarySlot ->
                val state = rememberDiarySlotCardState(diarySlot)

                ReorderableItem(state = reorderableLazyListState, key = diarySlot.id) { isDragging ->
                    DiarySlotCard(
                        state = state,
                        isDragging = isDragging,
                        onSave = { onUpdate(diarySlot.id, state) },
                        onDelete = { onDelete(diarySlot) },
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 8.dp),
                        action =
                            if (isReordering) {
                                {
                                    IconButton(
                                        onClick = {},
                                        modifier =
                                            Modifier.clearAndSetSemantics {}.hapticDraggableHandle(),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DragHandle,
                                            contentDescription =
                                                stringResource(Res.string.action_reorder),
                                        )
                                    }
                                }
                            } else {
                                null
                            },
                    )
                }
            }

            item {
                CreateDiarySlotCard(
                    showForm = showForm,
                    onShowFormChange = { showForm = it },
                    onSave = onCreate,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .focusRequester(formFocusRequester),
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CreateDiarySlotCard(
    showForm: Boolean,
    onShowFormChange: (Boolean) -> Unit,
    onSave: (DiarySlotCardState) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (showForm) {
        val state = rememberDiarySlotCardState(null)

        DiarySlotCard(
            state = state,
            isDragging = false,
            onSave = {
                onSave(state)
                onShowFormChange(false)
            },
            onDelete = { onShowFormChange(false) },
            modifier = modifier,
            action = {
                when {
                    !state.isModified ->
                        IconButton(onClick = { onShowFormChange(false) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        }

                    else ->
                        FilledIconButton(
                            onClick = {
                                onSave(state)
                                onShowFormChange(false)
                            },
                            enabled = state.isValid,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = stringResource(Res.string.action_save),
                            )
                        }
                }
            },
        )
    } else {
        Surface(
            onClick = { onShowFormChange(true) },
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.action_add_diary_slot),
                )
            }
        }
    }
}
