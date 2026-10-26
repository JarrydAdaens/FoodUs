package com.maksimowiczm.foodyou.app.ui.meal


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

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
        UiSourceScope(uiInspectionSource.element("DiarySlotSettingsScreen.DiarySlotSettingsScreen.1", "com.maksimowiczm.foodyou.app.ui.meal.DiarySlotSettingsScreen", "DiarySlotSettingsScreen")) { DiarySlotSettingsScreen(
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
        ) }
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
        modifier = (modifier).uiName("DiarySlotSettingsScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "DiarySlotSettingsScreen"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_diary_slots), modifier = Modifier.uiName("DiarySlotSettingsScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotSettingsScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("DiarySlotSettingsScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "DiarySlotSettingsScreen")) },
                actions = {
                    if (isReordering) {
                        IconButton(
                            onClick = {
                                onSaveOrder(diarySlotsOrder)
                                isReordering = false
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            }, modifier = Modifier.uiName("DiarySlotSettingsScreen.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "DiarySlotSettingsScreen")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = stringResource(Res.string.action_save), modifier = Modifier.uiName("DiarySlotSettingsScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DiarySlotSettingsScreen")
                            )
                        }
                    } else {
                        UiSourceScope(uiInspectionSource.element("DiarySlotSettingsScreen.TooltipBox.1", "androidx.compose.material3.TooltipBox", "DiarySlotSettingsScreen")) { TooltipBox(
                            positionProvider =
                                TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Above
                                ),
                            tooltip = {
                                PlainTooltip(modifier = Modifier.uiName("DiarySlotSettingsScreen.PlainTooltip.1", "androidx.compose.material3.PlainTooltip", uiInspectionSource, "DiarySlotSettingsScreen")) { Text(stringResource(Res.string.action_reorder), modifier = Modifier.uiName("DiarySlotSettingsScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotSettingsScreen")) }
                            },
                            state = rememberTooltipState(), modifier = Modifier.uiName("DiarySlotSettingsScreen.TooltipBox.1.root", "androidx.compose.material3.TooltipBox", uiInspectionSource, "DiarySlotSettingsScreen")
                        ) {
                            IconButton(
                                onClick = {
                                    isReordering = true
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                }, modifier = Modifier.uiName("DiarySlotSettingsScreen.IconButton.2", "androidx.compose.material3.IconButton", uiInspectionSource, "DiarySlotSettingsScreen")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Reorder,
                                    contentDescription = stringResource(Res.string.action_reorder), modifier = Modifier.uiName("DiarySlotSettingsScreen.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "DiarySlotSettingsScreen")
                                )
                            }
                        } }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("DiarySlotSettingsScreen.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "DiarySlotSettingsScreen")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                (Modifier.fillMaxSize()
                    .imePadding()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("DiarySlotSettingsScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "DiarySlotSettingsScreen"),
            state = lazyListState,
            contentPadding = paddingValues.add(vertical = 8.dp),
        ) {
            items(items = diarySlotsOrder, key = { it.id }) { diarySlot ->
                val state = rememberDiarySlotCardState(diarySlot)

                ReorderableItem(state = reorderableLazyListState, key = diarySlot.id, modifier = Modifier.uiName("DiarySlotSettingsScreen.ReorderableItem.1", "sh.calvin.reorderable.ReorderableItem", uiInspectionSource, "DiarySlotSettingsScreen")) { isDragging ->
                    DiarySlotCard(
                        state = state,
                        isDragging = isDragging,
                        onSave = { onUpdate(diarySlot.id, state) },
                        onDelete = { onDelete(diarySlot) },
                        modifier =
                            (Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 8.dp)).uiName("DiarySlotSettingsScreen.DiarySlotCard.1", "com.maksimowiczm.foodyou.app.ui.meal.DiarySlotCard", uiInspectionSource, "DiarySlotSettingsScreen"),
                        action =
                            if (isReordering) {
                                {
                                    IconButton(
                                        onClick = {},
                                        modifier =
                                            (Modifier.clearAndSetSemantics {}.hapticDraggableHandle()).uiName("DiarySlotSettingsScreen.IconButton.3", "androidx.compose.material3.IconButton", uiInspectionSource, "DiarySlotSettingsScreen"),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DragHandle,
                                            contentDescription =
                                                stringResource(Res.string.action_reorder), modifier = Modifier.uiName("DiarySlotSettingsScreen.Icon.3", "androidx.compose.material3.Icon", uiInspectionSource, "DiarySlotSettingsScreen")
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
                        (Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .focusRequester(formFocusRequester)).uiName("DiarySlotSettingsScreen.CreateDiarySlotCard.1", "com.maksimowiczm.foodyou.app.ui.meal.CreateDiarySlotCard", uiInspectionSource, "DiarySlotSettingsScreen"),
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
            modifier = (modifier).uiName("CreateDiarySlotCard.DiarySlotCard.1", "com.maksimowiczm.foodyou.app.ui.meal.DiarySlotCard", uiInspectionSource, "CreateDiarySlotCard"),
            action = {
                when {
                    !state.isModified ->
                        IconButton(onClick = { onShowFormChange(false) }, modifier = Modifier.uiName("CreateDiarySlotCard.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "CreateDiarySlotCard")) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.uiName("CreateDiarySlotCard.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "CreateDiarySlotCard"))
                        }

                    else ->
                        FilledIconButton(
                            onClick = {
                                onSave(state)
                                onShowFormChange(false)
                            },
                            enabled = state.isValid, modifier = Modifier.uiName("CreateDiarySlotCard.FilledIconButton.1", "androidx.compose.material3.FilledIconButton", uiInspectionSource, "CreateDiarySlotCard")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = stringResource(Res.string.action_save), modifier = Modifier.uiName("CreateDiarySlotCard.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "CreateDiarySlotCard")
                            )
                        }
                }
            },
        )
    } else {
        Surface(
            onClick = { onShowFormChange(true) },
            modifier = (modifier).uiName("CreateDiarySlotCard.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "CreateDiarySlotCard"),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Box(modifier = (Modifier.padding(16.dp)).uiName("CreateDiarySlotCard.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "CreateDiarySlotCard"), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.action_add_diary_slot), modifier = Modifier.uiName("CreateDiarySlotCard.Icon.3", "androidx.compose.material3.Icon", uiInspectionSource, "CreateDiarySlotCard")
                )
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.meal",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/meal/DiarySlotSettingsScreen.kt",
)
