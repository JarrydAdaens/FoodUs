package com.maksimowiczm.foodyou.app.ui.home.meals.settings

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

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
        modifier = (modifier).uiName("DiarySlotsCardsSettingsScreen.DiarySlotCardSettings.1", "com.maksimowiczm.foodyou.app.ui.home.meals.settings.DiarySlotCardSettings", uiInspectionSource, "DiarySlotsCardsSettingsScreen"),
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
                title = { Text(stringResource(Res.string.headline_diary_slots), modifier = Modifier.uiName("DiarySlotCardSettings.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotCardSettings")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("DiarySlotCardSettings.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "DiarySlotCardSettings")) },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("DiarySlotCardSettings.MediumFlexibleTopAppBar.1", "androidx.compose.material3.MediumFlexibleTopAppBar", uiInspectionSource, "DiarySlotCardSettings")
            )
        },
        modifier = (modifier).uiName("DiarySlotCardSettings.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "DiarySlotCardSettings"),
    ) { paddingValues ->
        LazyColumn(
            modifier = (Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("DiarySlotCardSettings.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "DiarySlotCardSettings"),
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
                    modifier = (Modifier.fillMaxWidth().padding(vertical = 8.dp)).uiName("DiarySlotCardSettings.LayoutPicker.1", "com.maksimowiczm.foodyou.app.ui.home.meals.settings.LayoutPicker", uiInspectionSource, "DiarySlotCardSettings"),
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

            item { HorizontalDivider( modifier = Modifier.uiName("DiarySlotCardSettings.HorizontalDivider.1", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "DiarySlotCardSettings")) }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.headline_diary_slots_settings), modifier = Modifier.uiName("DiarySlotCardSettings.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DiarySlotCardSettings")) },
                    modifier = (Modifier.clickable { onDiarySlotsSettings() }).uiName("DiarySlotCardSettings.ListItem.1", "androidx.compose.material3.ListItem", uiInspectionSource, "DiarySlotCardSettings"),
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
            modifier = (Modifier.padding(horizontal = 16.dp, vertical = 8.dp)).uiName("advancedLayoutSettings.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "advancedLayoutSettings"),
        )
    }

    item {
        ListItem(
            headlineContent = { Text(stringResource(Res.string.action_use_time_based_ordering), modifier = Modifier.uiName("advancedLayoutSettings.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "advancedLayoutSettings")) },
            modifier = (Modifier.clickable { toggleTimeBased(!useTimeBasedSorting) }).uiName("advancedLayoutSettings.ListItem.1", "androidx.compose.material3.ListItem", uiInspectionSource, "advancedLayoutSettings"),
            supportingContent = {
                Text(stringResource(Res.string.description_time_based_diary_slots_sorting), modifier = Modifier.uiName("advancedLayoutSettings.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "advancedLayoutSettings"))
            },
            trailingContent = {
                Switch(checked = useTimeBasedSorting, onCheckedChange = toggleTimeBased, modifier = Modifier.uiName("advancedLayoutSettings.Switch.1", "androidx.compose.material3.Switch", uiInspectionSource, "advancedLayoutSettings"))
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
            headlineContent = { Text(stringResource(Res.string.action_ignore_all_day_diary_slots), modifier = Modifier.uiName("advancedLayoutSettings.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "advancedLayoutSettings")) },
            modifier =
                (Modifier.clickable(enabled = useTimeBasedSorting) {
                    toggleIgnoreAllDayDiarySlots(!ignoreAllDayDiarySlots)
                }).uiName("advancedLayoutSettings.ListItem.2", "androidx.compose.material3.ListItem", uiInspectionSource, "advancedLayoutSettings"),
            supportingContent = {
                Text(stringResource(Res.string.description_action_ignore_all_day_diary_slots), modifier = Modifier.uiName("advancedLayoutSettings.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "advancedLayoutSettings"))
            },
            trailingContent = {
                Switch(
                    checked = ignoreAllDayDiarySlots,
                    onCheckedChange = toggleIgnoreAllDayDiarySlots,
                    enabled = useTimeBasedSorting, modifier = Modifier.uiName("advancedLayoutSettings.Switch.2", "androidx.compose.material3.Switch", uiInspectionSource, "advancedLayoutSettings")
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


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.home.meals.settings",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/meals/settings/DiarySlotsCardsSettingsScreen.kt",
)
