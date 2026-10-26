package com.maksimowiczm.foodyou.app.ui.home.goals

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GoalsCardSettings(
    onBack: () -> Unit,
    onGoalsSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GoalsViewModel = koinViewModel()
    val expand by viewModel.expandGoalsCard.collectAsStateWithLifecycle()

    GoalsCardSettings(
        onBack = onBack,
        expand = expand,
        onShowDetailsChange = viewModel::setExpandGoalsCard,
        onGoalsSettings = onGoalsSettings,
        modifier = (modifier).uiName("GoalsCardSettings.GoalsCardSettings.1", "com.maksimowiczm.foodyou.app.ui.home.goals.GoalsCardSettings", uiInspectionSource, "GoalsCardSettings"),
    )
}

@Composable
private fun GoalsCardSettings(
    onBack: () -> Unit,
    onGoalsSettings: () -> Unit,
    onShowDetailsChange: (Boolean) -> Unit,
    expand: Boolean,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = (modifier).uiName("GoalsCardSettings.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "GoalsCardSettings"),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(Res.string.headline_daily_goals), modifier = Modifier.uiName("GoalsCardSettings.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "GoalsCardSettings")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("GoalsCardSettings.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "GoalsCardSettings")) },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("GoalsCardSettings.MediumFlexibleTopAppBar.1", "androidx.compose.material3.MediumFlexibleTopAppBar", uiInspectionSource, "GoalsCardSettings")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = (Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("GoalsCardSettings.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "GoalsCardSettings"),
            contentPadding = paddingValues,
        ) {
            stickyHeader {
                GoalsCard(
                    expand = expand,
                    energy = 1600,
                    energyGoal = 2000,
                    proteins = 50,
                    proteinsGoal = 75,
                    carbohydrates = 200,
                    carbohydratesGoal = 300,
                    fats = 70,
                    fatsGoal = 90,
                    onClick = {},
                    onLongClick = {},
                    modifier = (Modifier.padding(16.dp)).uiName("GoalsCardSettings.GoalsCard.1", "com.maksimowiczm.foodyou.app.ui.home.goals.GoalsCard", uiInspectionSource, "GoalsCardSettings"),
                )
            }

            item { HorizontalDivider( modifier = Modifier.uiName("GoalsCardSettings.HorizontalDivider.1", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "GoalsCardSettings")) }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.action_show_details), modifier = Modifier.uiName("GoalsCardSettings.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "GoalsCardSettings")) },
                    modifier = (Modifier.clickable { onShowDetailsChange(!expand) }).uiName("GoalsCardSettings.ListItem.1", "androidx.compose.material3.ListItem", uiInspectionSource, "GoalsCardSettings"),
                    supportingContent = {
                        Text(stringResource(Res.string.description_show_macronutrients_goals), modifier = Modifier.uiName("GoalsCardSettings.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "GoalsCardSettings"))
                    },
                    trailingContent = {
                        Switch(checked = expand, onCheckedChange = onShowDetailsChange, modifier = Modifier.uiName("GoalsCardSettings.Switch.1", "androidx.compose.material3.Switch", uiInspectionSource, "GoalsCardSettings"))
                    },
                )
            }

            item { HorizontalDivider( modifier = Modifier.uiName("GoalsCardSettings.HorizontalDivider.2", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "GoalsCardSettings")) }

            item {
                ListItem(
                    headlineContent = {
                        Text(stringResource(Res.string.headline_daily_goals_settings), modifier = Modifier.uiName("GoalsCardSettings.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "GoalsCardSettings"))
                    },
                    modifier = (Modifier.clickable { onGoalsSettings() }).uiName("GoalsCardSettings.ListItem.2", "androidx.compose.material3.ListItem", uiInspectionSource, "GoalsCardSettings"),
                )
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.home.goals",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/goals/GoalsCardSettings.kt",
)
