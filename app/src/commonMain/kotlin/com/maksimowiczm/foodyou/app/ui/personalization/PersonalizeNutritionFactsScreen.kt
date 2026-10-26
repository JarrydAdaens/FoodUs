package com.maksimowiczm.foodyou.app.ui.personalization


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.ResetToDefaultDialog
import com.maksimowiczm.foodyou.app.ui.common.extension.hapticDraggableHandle
import com.maksimowiczm.foodyou.common.compose.extension.add
import com.maksimowiczm.foodyou.settings.domain.entity.NutrientsOrder
import foodyou.app.generated.resources.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun PersonalizeNutritionFactsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: PersonalizeNutritionFactsViewModel = koinViewModel()
    val order by viewModel.order.collectAsStateWithLifecycle()

    UiSourceScope(uiInspectionSource.element("PersonalizeNutritionFactsScreen.PersonalizeNutritionFactsScreen.1", "com.maksimowiczm.foodyou.app.ui.personalization.PersonalizeNutritionFactsScreen", "PersonalizeNutritionFactsScreen")) { PersonalizeNutritionFactsScreen(
        order = order,
        onUpdateOrder = viewModel::updateOrder,
        onReset = viewModel::resetOrder,
        onBack = onBack,
        modifier = modifier,
    ) }
}

@OptIn(FlowPreview::class)
@Composable
private fun PersonalizeNutritionFactsScreen(
    order: List<NutrientsOrder>,
    onUpdateOrder: (List<NutrientsOrder>) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showResetDialog by rememberSaveable { mutableStateOf(false) }

    if (showResetDialog) {
        ResetToDefaultDialog(
            onDismissRequest = { showResetDialog = false },
            onConfirm = {
                onReset()
                showResetDialog = false
            }, modifier = Modifier.uiName("PersonalizeNutritionFactsScreen.ResetToDefaultDialog.1", "com.maksimowiczm.foodyou.app.ui.common.component.ResetToDefaultDialog", uiInspectionSource, "PersonalizeNutritionFactsScreen")
        ) {
            Text(stringResource(Res.string.description_reset_nutrition_facts), modifier = Modifier.uiName("PersonalizeNutritionFactsScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "PersonalizeNutritionFactsScreen"))
        }
    }

    val lazyListState = rememberLazyListState()
    var localOrder by rememberSaveable(order) { mutableStateOf(order) }

    val reorderableLazyListState =
        rememberReorderableLazyListState(lazyListState) { from, to ->
            localOrder =
                localOrder.toMutableList().apply {
                    val fromIndex = from.index
                    val toIndex = to.index

                    if (fromIndex != toIndex) {
                        add(toIndex, removeAt(fromIndex))
                    }
                }
        }

    val moveUp: (NutrientsOrder) -> Boolean = {
        val index = localOrder.indexOf(it)
        if (index > 0) {
            localOrder = localOrder.toMutableList().apply { add(index, removeAt(index)) }
            true
        } else {
            false
        }
    }

    val moveDown: (NutrientsOrder) -> Boolean = {
        val index = localOrder.indexOf(it)
        if (index < localOrder.size - 1) {
            localOrder = localOrder.toMutableList().apply { add(index, removeAt(index)) }
            true
        } else {
            false
        }
    }

    val latestOnReorder by rememberUpdatedState(onUpdateOrder)
    LaunchedEffect(Unit) {
        snapshotFlow { localOrder }
            .debounce(50)
            .distinctUntilChanged()
            .collectLatest { latestOnReorder(it) }
    }

    val moveUpString = stringResource(Res.string.action_move_up)
    val moveDownString = stringResource(Res.string.action_move_down)

    Scaffold(
        modifier = (modifier).uiName("PersonalizeNutritionFactsScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "PersonalizeNutritionFactsScreen"),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(Res.string.headline_nutrition_facts), modifier = Modifier.uiName("PersonalizeNutritionFactsScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "PersonalizeNutritionFactsScreen")) },
                subtitle = {
                    Text(stringResource(Res.string.description_personalize_nutrition_facts_short), modifier = Modifier.uiName("PersonalizeNutritionFactsScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "PersonalizeNutritionFactsScreen"))
                },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("PersonalizeNutritionFactsScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "PersonalizeNutritionFactsScreen")) },
                actions = {
                    IconButton(onClick = { showResetDialog = true }, modifier = Modifier.uiName("PersonalizeNutritionFactsScreen.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "PersonalizeNutritionFactsScreen")) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_reset_settings),
                            contentDescription = stringResource(Res.string.action_reset_ordering), modifier = Modifier.uiName("PersonalizeNutritionFactsScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "PersonalizeNutritionFactsScreen")
                        )
                    }
                },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("PersonalizeNutritionFactsScreen.LargeFlexibleTopAppBar.1", "androidx.compose.material3.LargeFlexibleTopAppBar", uiInspectionSource, "PersonalizeNutritionFactsScreen")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = (Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("PersonalizeNutritionFactsScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "PersonalizeNutritionFactsScreen"),
            state = lazyListState,
            contentPadding = paddingValues.add(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items = localOrder, key = { it.name }) {
                ReorderableItem(state = reorderableLazyListState, key = it.name, modifier = Modifier.uiName("PersonalizeNutritionFactsScreen.ReorderableItem.1", "sh.calvin.reorderable.ReorderableItem", uiInspectionSource, "PersonalizeNutritionFactsScreen")) { isDragging ->
                    ListItem(
                        item = it,
                        isDragging = isDragging,
                        modifier =
                            (Modifier.fillMaxWidth().padding(horizontal = 16.dp).semantics {
                                customActions =
                                    listOf(
                                        CustomAccessibilityAction(
                                            label = moveUpString,
                                            action = { moveUp(it) },
                                        ),
                                        CustomAccessibilityAction(
                                            label = moveDownString,
                                            action = { moveDown(it) },
                                        ),
                                    )
                            }).uiName("PersonalizeNutritionFactsScreen.ListItem.1", "com.maksimowiczm.foodyou.app.ui.personalization.ListItem", uiInspectionSource, "PersonalizeNutritionFactsScreen"),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReorderableCollectionItemScope.ListItem(
    item: NutrientsOrder,
    isDragging: Boolean,
    modifier: Modifier = Modifier,
) {
    val elevation by animateDpAsState(if (isDragging) 16.dp else 0.dp)
    val containerColor by
        animateColorAsState(
            if (isDragging) MaterialTheme.colorScheme.surfaceContainerHighest
            else MaterialTheme.colorScheme.surfaceContainer
        )

    Surface(
        modifier = (modifier).uiName("ListItem.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "ListItem"),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = elevation,
        tonalElevation = elevation,
    ) {
        Row(modifier = (Modifier.padding(16.dp)).uiName("ListItem.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "ListItem")) {
            Text(text = item.stringResource(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.uiName("ListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ListItem"))
            Spacer(Modifier.weight(1f))
            DragHandle((Modifier.hapticDraggableHandle()).uiName("ListItem.DragHandle.1", "com.maksimowiczm.foodyou.app.ui.personalization.DragHandle", uiInspectionSource, "ListItem"))
        }
    }
}

@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
    Box((modifier.clearAndSetSemantics {}).uiName("DragHandle.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "DragHandle")) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = null,
            modifier = (Modifier.clickable(onClick = {}, indication = null, interactionSource = null)).uiName("app.ui.personalization.DragHandle.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DragHandle"),
        )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.personalization",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/personalization/PersonalizeNutritionFactsScreen.kt",
)
