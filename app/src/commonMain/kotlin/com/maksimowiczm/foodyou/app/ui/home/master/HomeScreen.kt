package com.maksimowiczm.foodyou.app.ui.home.master

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.home.calendar.CalendarCard
import com.maksimowiczm.foodyou.app.ui.home.goals.GoalsCard
import com.maksimowiczm.foodyou.app.ui.home.meals.card.DiarySlotsCards
import com.maksimowiczm.foodyou.app.ui.home.poll.PollsCard
import com.maksimowiczm.foodyou.app.ui.home.shared.rememberHomeState
import com.maksimowiczm.foodyou.settings.domain.entity.HomeCard
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onSettings: () -> Unit,
    onTitle: () -> Unit,
    onDiarySlotCardLongClick: (diarySlotId: Long) -> Unit,
    onDiarySlotCardAddClick: (epochDay: Long, diarySlotId: Long) -> Unit,
    onDiarySlotCardQuickAddClick: (epochDay: Long, diarySlotId: Long) -> Unit,
    onDiarySlotCardAiScanClick: (epochDay: Long, diarySlotId: Long) -> Unit,
    onDiarySlotCardFastTextClick: (epochDay: Long, diarySlotId: Long) -> Unit,
    onGoalsCardLongClick: () -> Unit,
    onGoalsCardClick: (epochDay: Long) -> Unit,
    onEditDiaryEntryClick: (foodEntryId: Long?, manualEntryId: Long?, isPlaceholder: Boolean) -> Unit,
    onMeals: (epochDay: Long) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val order by viewModel.homeOrder.collectAsStateWithLifecycle()
    val homeState = rememberHomeState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = (modifier).uiName("HomeScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "HomeScreen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.app_name),
                        modifier =
                            (Modifier.clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = onTitle,
                            )).uiName("HomeScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "HomeScreen"),
                    )
                },
                actions = {
                    TextButton(onClick = { onMeals(homeState.selectedDate.toEpochDays()) }, modifier = Modifier.uiName("HomeScreen.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "HomeScreen")) {
                        Text(stringResource(Res.string.meals_library), modifier = Modifier.uiName("HomeScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "HomeScreen"))
                    }
                    IconButton(onClick = onSettings, modifier = Modifier.uiName("HomeScreen.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "HomeScreen")) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(Res.string.action_go_to_settings), modifier = Modifier.uiName("HomeScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "HomeScreen")
                        )
                    }
                },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("HomeScreen.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "HomeScreen")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = (Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("HomeScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "HomeScreen"),
            contentPadding = paddingValues,
        ) {
            item {
                PollsCard(modifier = (Modifier.padding(horizontal = 8.dp).padding(bottom = 8.dp)).uiName("HomeScreen.PollsCard.1", "com.maksimowiczm.foodyou.app.ui.home.poll.PollsCard", uiInspectionSource, "HomeScreen"))
            }

            items(order) {
                when (it) {
                    HomeCard.Calendar ->
                        CalendarCard(
                            homeState = homeState,
                            modifier = (Modifier.padding(horizontal = 8.dp).padding(bottom = 8.dp)).uiName("HomeScreen.CalendarCard.1", "com.maksimowiczm.foodyou.app.ui.home.calendar.CalendarCard", uiInspectionSource, "HomeScreen"),
                        )

                    HomeCard.Goals ->
                        GoalsCard(
                            homeState = homeState,
                            onClick = onGoalsCardClick,
                            onLongClick = onGoalsCardLongClick,
                            modifier = (Modifier.padding(horizontal = 8.dp).padding(bottom = 8.dp)).uiName("HomeScreen.GoalsCard.1", "com.maksimowiczm.foodyou.app.ui.home.goals.GoalsCard", uiInspectionSource, "HomeScreen"),
                        )

                    HomeCard.DiarySlots ->
                        DiarySlotsCards(
                            homeState = homeState,
                            onAdd = onDiarySlotCardAddClick,
                            onQuickAdd = onDiarySlotCardQuickAddClick,
                            onAiScan = onDiarySlotCardAiScanClick,
                            onFastText = onDiarySlotCardFastTextClick,
                            onEditEntry = onEditDiaryEntryClick,
                            onLongClick = onDiarySlotCardLongClick,
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = (Modifier.padding(bottom = 8.dp)).uiName("HomeScreen.DiarySlotsCards.1", "com.maksimowiczm.foodyou.app.ui.home.meals.card.DiarySlotsCards", uiInspectionSource, "HomeScreen"),
                        )
                }
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.home.master",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/master/HomeScreen.kt",
)
