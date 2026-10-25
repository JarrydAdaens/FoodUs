package com.maksimowiczm.foodyou.app.ui.home.meals.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.common.compose.extension.add
import com.maksimowiczm.foodyou.common.compose.extension.performToggle
import com.maksimowiczm.foodyou.fooddiary.domain.entity.DiarySlotsCardsLayout
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DiarySlotsCardsSettingsScreen(
    onBack: () -> Unit,
    onDiarySlotSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: DiarySlotsCardsSettingsViewModel = koinViewModel()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    val layout = preferences.layout
    val useTimeBasedSorting = preferences.useTimeBasedSorting
    val ignoreAllDayDiarySlots = preferences.ignoreAllDayDiarySlots

    DiarySlotCardSettings(
        layout = layout,
        onLayoutChange = { viewModel.updatePreferences(preferences.copy(layout = it)) },
        useTimeBasedSorting = useTimeBasedSorting,
        toggleTimeBased = {
            viewModel.updatePreferences(preferences.copy(useTimeBasedSorting = it))
        },
        ignoreAllDayDiarySlots = ignoreAllDayDiarySlots,
        toggleIgnoreAllDayDiarySlots = {
            viewModel.updatePreferences(preferences.copy(ignoreAllDayDiarySlots = it))
        },
        onDiarySlotsSettings = onDiarySlotSettings,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
internal fun DiarySlotCardSettings(
    layout: DiarySlotsCardsLayout,
    onLayoutChange: (DiarySlotsCardsLayout) -> Unit,
    useTimeBasedSorting: Boolean,
    toggleTimeBased: (Boolean) -> Unit,
    ignoreAllDayDiarySlots: Boolean,
    toggleIgnoreAllDayDiarySlots: (Boolean) -> Unit,
    onDiarySlotsSettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(Res.string.headline_diary_slots)) },
                navigationIcon = { ArrowBackIconButton(onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues.add(vertical = 8.dp),
        ) {
            item {
                LayoutPicker(
                    layout = layout,
                    onLayoutChange = {
                        if (layout != it) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            onLayoutChange(it)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                )
            }

            advancedLayoutSettings(
                useTimeBasedSorting = useTimeBasedSorting,
                toggleTimeBased = {
                    hapticFeedback.performToggle(it)
                    toggleTimeBased(it)
                },
                ignoreAllDayDiarySlots = ignoreAllDayDiarySlots,
                toggleIgnoreAllDayDiarySlots = {
                    hapticFeedback.performToggle(it)
                    toggleIgnoreAllDayDiarySlots(it)
                },
            )

            item { HorizontalDivider() }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.headline_diary_slots_settings)) },
                    modifier = Modifier.clickable { onDiarySlotsSettings() },
                )
            }
        }
    }
}

private fun LazyListScope.advancedLayoutSettings(
    useTimeBasedSorting: Boolean,
    toggleTimeBased: (Boolean) -> Unit,
    ignoreAllDayDiarySlots: Boolean,
    toggleIgnoreAllDayDiarySlots: (Boolean) -> Unit,
) {
    item {
        Text(
            text = stringResource(Res.string.headline_time_based_ordering),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }

    item {
        ListItem(
            headlineContent = { Text(stringResource(Res.string.action_use_time_based_ordering)) },
            modifier = Modifier.clickable { toggleTimeBased(!useTimeBasedSorting) },
            supportingContent = {
                Text(stringResource(Res.string.description_time_based_diary_slots_sorting))
            },
            trailingContent = {
                Switch(checked = useTimeBasedSorting, onCheckedChange = toggleTimeBased)
            },
        )
    }

    item {
        val contentColor =
            if (useTimeBasedSorting) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.outline
            }

        ListItem(
            headlineContent = { Text(stringResource(Res.string.action_ignore_all_day_diary_slots)) },
            modifier =
                Modifier.clickable(enabled = useTimeBasedSorting) {
                    toggleIgnoreAllDayDiarySlots(!ignoreAllDayDiarySlots)
                },
            supportingContent = {
                Text(stringResource(Res.string.description_action_ignore_all_day_diary_slots))
            },
            trailingContent = {
                Switch(
                    checked = ignoreAllDayDiarySlots,
                    onCheckedChange = toggleIgnoreAllDayDiarySlots,
                    enabled = useTimeBasedSorting,
                )
            },
            colors =
                ListItemDefaults.colors(
                    headlineColor = contentColor,
                    supportingColor = contentColor,
                ),
        )
    }
}
