package com.maksimowiczm.foodyou.app.ui.home.personalization


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.extension.hapticDraggableHandle
import com.maksimowiczm.foodyou.settings.domain.entity.HomeCard
import foodyou.app.generated.resources.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun HomePersonalizationScreen(
    onBack: () -> Unit,
    onDiarySlots: () -> Unit,
    onGoals: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: HomePersonalizationViewModel = koinViewModel()

    val order by viewModel.homeOrder.collectAsStateWithLifecycle()

    UiSourceScope(uiInspectionSource.element("HomePersonalizationScreen.HomePersonalizationScreen.1", "com.maksimowiczm.foodyou.app.ui.home.personalization.HomePersonalizationScreen", "HomePersonalizationScreen")) { HomePersonalizationScreen(
        order = order,
        onBack = onBack,
        onDiarySlots = onDiarySlots,
        onGoals = onGoals,
        onReorder = viewModel::updateOrder,
        modifier = modifier,
    ) }
}

@OptIn(FlowPreview::class)
@Composable
private fun HomePersonalizationScreen(
    order: List<HomeCard>,
    onBack: () -> Unit,
    onDiarySlots: () -> Unit,
    onGoals: () -> Unit,
    onReorder: (List<HomeCard>) -> Unit,
    modifier: Modifier = Modifier,
) {
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

    val moveUpString = stringResource(Res.string.action_move_up)
    val moveUp: (HomeCard) -> Boolean = {
        val index = localOrder.indexOf(it)
        if (index > 0) {
            localOrder = localOrder.toMutableList().apply { add(index - 1, removeAt(index)) }
            true
        } else {
            false
        }
    }

    val moveDownString = stringResource(Res.string.action_move_down)
    val moveDown: (HomeCard) -> Boolean = {
        val index = localOrder.indexOf(it)
        if (index < localOrder.size - 1) {
            localOrder = localOrder.toMutableList().apply { add(index + 1, removeAt(index)) }
            true
        } else {
            false
        }
    }

    val latestOnReorder by rememberUpdatedState(onReorder)
    LaunchedEffect(Unit) {
        snapshotFlow { localOrder }
            .debounce(50)
            .distinctUntilChanged()
            .collectLatest { latestOnReorder(it) }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = (modifier).uiName("HomePersonalizationScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "HomePersonalizationScreen"),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(Res.string.headline_home_settings), modifier = Modifier.uiName("HomePersonalizationScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "HomePersonalizationScreen")) },
                subtitle = { Text(stringResource(Res.string.description_home_settings), modifier = Modifier.uiName("HomePersonalizationScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "HomePersonalizationScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("HomePersonalizationScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "HomePersonalizationScreen")) },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("HomePersonalizationScreen.LargeFlexibleTopAppBar.1", "androidx.compose.material3.LargeFlexibleTopAppBar", uiInspectionSource, "HomePersonalizationScreen")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                (Modifier.padding(8.dp)
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("HomePersonalizationScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "HomePersonalizationScreen"),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = paddingValues,
        ) {
            items(items = localOrder, key = { it.name }) { card ->
                ReorderableItem(
                    state = reorderableLazyListState,
                    key = card.name,
                    modifier = (modifier).uiName("HomePersonalizationScreen.ReorderableItem.1", "sh.calvin.reorderable.ReorderableItem", uiInspectionSource, "HomePersonalizationScreen"),
                ) { isDragging ->
                    MyCard(
                        isDragging = isDragging,
                        modifier =
                            (Modifier.semantics {
                                customActions =
                                    listOf(
                                        CustomAccessibilityAction(
                                            label = moveUpString,
                                            action = { moveUp(card) },
                                        ),
                                        CustomAccessibilityAction(
                                            label = moveDownString,
                                            action = { moveDown(card) },
                                        ),
                                    )
                            }).uiName("HomePersonalizationScreen.MyCard.1", "com.maksimowiczm.foodyou.app.ui.home.personalization.MyCard", uiInspectionSource, "HomePersonalizationScreen"),
                    ) {
                        with(it) {
                            when (card) {
                                HomeCard.Calendar -> CalendarCardContent()
                                HomeCard.Goals -> GoalsCardContent(onMore = onGoals)
                                HomeCard.DiarySlots -> DiarySlotsCardContent(onMore = onDiarySlots)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReorderableCollectionItemScope.MyCard(
    isDragging: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ReorderableCollectionItemScope.(RowScope) -> Unit,
) {
    val elevation by animateDpAsState(if (isDragging) 16.dp else 0.dp)
    val containerColor by
        animateColorAsState(
            if (isDragging) MaterialTheme.colorScheme.surfaceContainerHighest
            else MaterialTheme.colorScheme.surfaceContainer
        )

    Surface(
        modifier = (modifier).uiName("MyCard.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "MyCard"),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = elevation,
        tonalElevation = elevation,
    ) {
        Row(modifier = (Modifier.padding(8.dp)).uiName("MyCard.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "MyCard"), verticalAlignment = Alignment.CenterVertically) {
            content(this)
        }
    }
}

@Composable
context(_: ReorderableCollectionItemScope)
private fun RowScope.CalendarCardContent() {
    Box(modifier = (Modifier.size(48.dp)).uiName("CalendarCardContent.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "CalendarCardContent"), contentAlignment = Alignment.Center) {
        Icon(imageVector = Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.uiName("CalendarCardContent.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "CalendarCardContent"))
    }
    Spacer(Modifier.width(16.dp))
    Text(stringResource(Res.string.headline_calendar), modifier = Modifier.uiName("CalendarCardContent.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "CalendarCardContent"))
    Spacer(Modifier.weight(1f))
    DragHandle(modifier = (Modifier.hapticDraggableHandle()).uiName("CalendarCardContent.DragHandle.1", "com.maksimowiczm.foodyou.app.ui.home.personalization.DragHandle", uiInspectionSource, "CalendarCardContent"))
}

@Composable
context(_: ReorderableCollectionItemScope)
private fun RowScope.DiarySlotsCardContent(onMore: () -> Unit) {
    Box(modifier = (Modifier.size(48.dp)).uiName("DiarySlotsCardContent.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "DiarySlotsCardContent"), contentAlignment = Alignment.Center) {
        Icon(imageVector = Icons.Outlined.Restaurant, contentDescription = null, modifier = Modifier.uiName("DiarySlotsCardContent.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DiarySlotsCardContent"))
    }
    Spacer(Modifier.width(16.dp))
    Text(stringResource(Res.string.headline_diary_slots), modifier = Modifier.uiName("DiarySlotsCardContent.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotsCardContent"))
    Spacer(Modifier.weight(1f))
    IconButton(onClick = onMore, modifier = Modifier.uiName("DiarySlotsCardContent.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "DiarySlotsCardContent")) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = stringResource(Res.string.action_show_more), modifier = Modifier.uiName("DiarySlotsCardContent.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "DiarySlotsCardContent")
        )
    }
    DragHandle(modifier = (Modifier.hapticDraggableHandle()).uiName("DiarySlotsCardContent.DragHandle.1", "com.maksimowiczm.foodyou.app.ui.home.personalization.DragHandle", uiInspectionSource, "DiarySlotsCardContent"))
}

@Composable
context(_: ReorderableCollectionItemScope)
private fun RowScope.GoalsCardContent(onMore: () -> Unit) {
    Box(modifier = (Modifier.size(48.dp)).uiName("GoalsCardContent.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "GoalsCardContent"), contentAlignment = Alignment.Center) {
        Icon(imageVector = Icons.Outlined.Flag, contentDescription = null, modifier = Modifier.uiName("GoalsCardContent.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "GoalsCardContent"))
    }
    Spacer(Modifier.width(16.dp))
    Text(stringResource(Res.string.headline_daily_goals), modifier = Modifier.uiName("app.ui.home.personalization.GoalsCardContent.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "GoalsCardContent"))
    Spacer(Modifier.weight(1f))
    IconButton(onClick = onMore, modifier = Modifier.uiName("GoalsCardContent.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "GoalsCardContent")) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = stringResource(Res.string.action_show_more), modifier = Modifier.uiName("GoalsCardContent.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "GoalsCardContent")
        )
    }
    DragHandle(modifier = (Modifier.hapticDraggableHandle()).uiName("GoalsCardContent.DragHandle.1", "com.maksimowiczm.foodyou.app.ui.home.personalization.DragHandle", uiInspectionSource, "GoalsCardContent"))
}

@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
    IconButton(onClick = {}, modifier = (Modifier.clearAndSetSemantics {}.then(modifier)).uiName("DragHandle.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "DragHandle")) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = stringResource(Res.string.action_reorder), modifier = Modifier.uiName("app.ui.home.personalization.DragHandle.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DragHandle")
        )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.home.personalization",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/personalization/HomePersonalizationScreen.kt",
)
