package com.maksimowiczm.foodyou.app.ui.personalization

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem
import com.maksimowiczm.foodyou.settings.domain.entity.EnergyFormat
import com.maksimowiczm.foodyou.settings.domain.entity.GraphStyle
import com.maksimowiczm.foodyou.settings.domain.entity.WeekLayout
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PersonalizationScreen(
    onBack: () -> Unit,
    onHomePersonalization: () -> Unit,
    onNutritionFactsPersonalization: () -> Unit,
    onTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PersonalizationScreenViewModel = koinViewModel()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = (modifier).uiName("PersonalizationScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "PersonalizationScreen"),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(Res.string.headline_personalization), modifier = Modifier.uiName("PersonalizationScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "PersonalizationScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("PersonalizationScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "PersonalizationScreen")) },
                subtitle = { Text(stringResource(Res.string.description_personalization), modifier = Modifier.uiName("PersonalizationScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "PersonalizationScreen")) },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("PersonalizationScreen.LargeFlexibleTopAppBar.1", "androidx.compose.material3.LargeFlexibleTopAppBar", uiInspectionSource, "PersonalizationScreen")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = (Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("PersonalizationScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "PersonalizationScreen"),
            contentPadding = paddingValues,
        ) {
            item { HomeSettingsListItem(onHomePersonalization, modifier = Modifier.uiName("PersonalizationScreen.HomeSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.personalization.HomeSettingsListItem", uiInspectionSource, "PersonalizationScreen")) }
            item { PersonalizeNutritionFactsSettingsListItem(onNutritionFactsPersonalization, modifier = Modifier.uiName("PersonalizationScreen.PersonalizeNutritionFactsSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.personalization.PersonalizeNutritionFactsSettingsListItem", uiInspectionSource, "PersonalizationScreen")) }
            item {
                EnergyUnitSettingsListItem(
                    unit = viewModel.energyUnit.collectAsStateWithLifecycle().value,
                    onChange = viewModel::setEnergyFormat,
                    modifier = (Modifier.fillMaxWidth()).uiName("PersonalizationScreen.EnergyUnitSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.personalization.EnergyUnitSettingsListItem", uiInspectionSource, "PersonalizationScreen"),
                )
            }
            item {
                GraphStyleSettingsListItem(
                    style = viewModel.graphStyle.collectAsStateWithLifecycle().value,
                    onChange = viewModel::setGraphStyle,
                    modifier = (Modifier.fillMaxWidth()).uiName("PersonalizationScreen.GraphStyleSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.personalization.GraphStyleSettingsListItem", uiInspectionSource, "PersonalizationScreen"),
                )
            }
            item {
                WeekLayoutSettingsListItem(
                    layout = viewModel.weekLayout.collectAsStateWithLifecycle().value,
                    onChange = viewModel::setWeekLayout,
                    modifier = (Modifier.fillMaxWidth()).uiName("PersonalizationScreen.WeekLayoutSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.personalization.WeekLayoutSettingsListItem", uiInspectionSource, "PersonalizationScreen"),
                )
            }
            item { ThemeSettingsListItem(onClick = onTheme, modifier = (Modifier.fillMaxWidth()).uiName("PersonalizationScreen.ThemeSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.personalization.ThemeSettingsListItem", uiInspectionSource, "PersonalizationScreen")) }
            item {
                SecureScreenSettingsListItem(
                    checked = viewModel.secureScreen.collectAsStateWithLifecycle().value,
                    onToggle = viewModel::toggleSecureScreen,
                    modifier = (Modifier.fillMaxWidth()).uiName("PersonalizationScreen.SecureScreenSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.personalization.SecureScreenSettingsListItem", uiInspectionSource, "PersonalizationScreen"),
                )
            }
        }
    }
}

@Composable
private fun HomeSettingsListItem(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SettingsListItem(
        label = { Text(stringResource(Res.string.headline_home), modifier = Modifier.uiName("HomeSettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "HomeSettingsListItem")) },
        onClick = onClick,
        modifier = (modifier).uiName("HomeSettingsListItem.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "HomeSettingsListItem"),
        supportingContent = { Text(stringResource(Res.string.description_home_settings), modifier = Modifier.uiName("HomeSettingsListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "HomeSettingsListItem")) },
        icon = { Icon(imageVector = Icons.Outlined.Home, contentDescription = null, modifier = Modifier.uiName("HomeSettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "HomeSettingsListItem")) },
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun PersonalizeNutritionFactsSettingsListItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsListItem(
        label = { Text(stringResource(Res.string.headline_nutrition_facts), modifier = Modifier.uiName("PersonalizeNutritionFactsSettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "PersonalizeNutritionFactsSettingsListItem")) },
        onClick = onClick,
        modifier = (modifier).uiName("PersonalizeNutritionFactsSettingsListItem.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "PersonalizeNutritionFactsSettingsListItem"),
        supportingContent = {
            Text(stringResource(Res.string.description_personalize_nutrition_facts_short), modifier = Modifier.uiName("PersonalizeNutritionFactsSettingsListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "PersonalizeNutritionFactsSettingsListItem"))
        },
        icon = { Icon(imageVector = Icons.AutoMirrored.Outlined.List, contentDescription = null, modifier = Modifier.uiName("PersonalizeNutritionFactsSettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "PersonalizeNutritionFactsSettingsListItem")) },
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun SecureScreenSettingsListItem(
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsListItem(
        label = { Text(stringResource(Res.string.headline_secure_screen), modifier = Modifier.uiName("SecureScreenSettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "SecureScreenSettingsListItem")) },
        onClick = { onToggle(!checked) },
        modifier = (modifier).uiName("SecureScreenSettingsListItem.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "SecureScreenSettingsListItem"),
        supportingContent = { Text(stringResource(Res.string.action_prevent_screen_capture), modifier = Modifier.uiName("SecureScreenSettingsListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "SecureScreenSettingsListItem")) },
        icon = { Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.uiName("SecureScreenSettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "SecureScreenSettingsListItem")) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null, modifier = Modifier.uiName("SecureScreenSettingsListItem.Switch.1", "androidx.compose.material3.Switch", uiInspectionSource, "SecureScreenSettingsListItem")) },
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun EnergyUnitSettingsListItem(
    unit: EnergyFormat,
    onChange: (EnergyFormat) -> Unit,
    modifier: Modifier = Modifier,
) {
    val suffix =
        when (unit) {
            EnergyFormat.Kilocalories -> stringResource(Res.string.unit_kcal)
            EnergyFormat.Kilojoules -> stringResource(Res.string.unit_kilojoules)
        }
    var expanded by rememberSaveable { mutableStateOf(false) }

    val menu =
        @Composable {
            UiSourceScope(uiInspectionSource.element("EnergyUnitSettingsListItem.DropdownMenu.1", "androidx.compose.material3.DropdownMenu", "EnergyUnitSettingsListItem")) { DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.uiName("EnergyUnitSettingsListItem.DropdownMenu.1.root", "androidx.compose.material3.DropdownMenu", uiInspectionSource, "EnergyUnitSettingsListItem")) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.unit_kcal), modifier = Modifier.uiName("EnergyUnitSettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "EnergyUnitSettingsListItem")) },
                    onClick = {
                        onChange(EnergyFormat.Kilocalories)
                        expanded = false
                    }, modifier = Modifier.uiName("EnergyUnitSettingsListItem.DropdownMenuItem.1", "androidx.compose.material3.DropdownMenuItem", uiInspectionSource, "EnergyUnitSettingsListItem")
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.unit_kilojoules), modifier = Modifier.uiName("EnergyUnitSettingsListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "EnergyUnitSettingsListItem")) },
                    onClick = {
                        onChange(EnergyFormat.Kilojoules)
                        expanded = false
                    }, modifier = Modifier.uiName("EnergyUnitSettingsListItem.DropdownMenuItem.2", "androidx.compose.material3.DropdownMenuItem", uiInspectionSource, "EnergyUnitSettingsListItem")
                )
            } }
        }

    SettingsListItem(
        label = { Text(stringResource(Res.string.headline_energy_unit), modifier = Modifier.uiName("EnergyUnitSettingsListItem.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "EnergyUnitSettingsListItem")) },
        onClick = { expanded = true },
        modifier = (modifier).uiName("EnergyUnitSettingsListItem.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "EnergyUnitSettingsListItem"),
        supportingContent = { Text(stringResource(Res.string.description_energy_unit), modifier = Modifier.uiName("EnergyUnitSettingsListItem.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "EnergyUnitSettingsListItem")) },
        trailingContent = {
            Box (modifier = Modifier.uiName("EnergyUnitSettingsListItem.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "EnergyUnitSettingsListItem")) {
                Text(
                    text = suffix,
                    modifier = (Modifier.padding(horizontal = 8.dp)).uiName("EnergyUnitSettingsListItem.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "EnergyUnitSettingsListItem"),
                    color = MaterialTheme.colorScheme.primary,
                )
                menu()
            }
        },
        icon = { Icon(imageVector = Icons.Outlined.Bolt, contentDescription = null, modifier = Modifier.uiName("EnergyUnitSettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "EnergyUnitSettingsListItem")) },
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun GraphStyleSettingsListItem(
    style: GraphStyle,
    onChange: (GraphStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    val label =
        when (style) {
            GraphStyle.Bar -> stringResource(Res.string.graph_style_bar)
            GraphStyle.Pie -> stringResource(Res.string.graph_style_pie)
        }
    var expanded by rememberSaveable { mutableStateOf(false) }

    val menu =
        @Composable {
            UiSourceScope(uiInspectionSource.element("GraphStyleSettingsListItem.DropdownMenu.1", "androidx.compose.material3.DropdownMenu", "GraphStyleSettingsListItem")) { DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.uiName("GraphStyleSettingsListItem.DropdownMenu.1.root", "androidx.compose.material3.DropdownMenu", uiInspectionSource, "GraphStyleSettingsListItem")) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.graph_style_bar), modifier = Modifier.uiName("GraphStyleSettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "GraphStyleSettingsListItem")) },
                    onClick = {
                        onChange(GraphStyle.Bar)
                        expanded = false
                    }, modifier = Modifier.uiName("GraphStyleSettingsListItem.DropdownMenuItem.1", "androidx.compose.material3.DropdownMenuItem", uiInspectionSource, "GraphStyleSettingsListItem")
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.graph_style_pie), modifier = Modifier.uiName("GraphStyleSettingsListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "GraphStyleSettingsListItem")) },
                    onClick = {
                        onChange(GraphStyle.Pie)
                        expanded = false
                    }, modifier = Modifier.uiName("GraphStyleSettingsListItem.DropdownMenuItem.2", "androidx.compose.material3.DropdownMenuItem", uiInspectionSource, "GraphStyleSettingsListItem")
                )
            } }
        }

    SettingsListItem(
        label = { Text(stringResource(Res.string.headline_graphs), modifier = Modifier.uiName("GraphStyleSettingsListItem.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "GraphStyleSettingsListItem")) },
        onClick = { expanded = true },
        modifier = (modifier).uiName("GraphStyleSettingsListItem.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "GraphStyleSettingsListItem"),
        supportingContent = { Text(stringResource(Res.string.description_graphs), modifier = Modifier.uiName("GraphStyleSettingsListItem.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "GraphStyleSettingsListItem")) },
        trailingContent = {
            Box (modifier = Modifier.uiName("GraphStyleSettingsListItem.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "GraphStyleSettingsListItem")) {
                Text(
                    text = label,
                    modifier = (Modifier.padding(horizontal = 8.dp)).uiName("GraphStyleSettingsListItem.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "GraphStyleSettingsListItem"),
                    color = MaterialTheme.colorScheme.primary,
                )
                menu()
            }
        },
        icon = { Icon(imageVector = Icons.Outlined.PieChart, contentDescription = null, modifier = Modifier.uiName("GraphStyleSettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "GraphStyleSettingsListItem")) },
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun WeekLayoutSettingsListItem(
    layout: WeekLayout,
    onChange: (WeekLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    val label =
        when (layout) {
            WeekLayout.Scrolling -> stringResource(Res.string.week_layout_scrolling)
            WeekLayout.Fixed -> stringResource(Res.string.week_layout_fixed)
        }
    var expanded by rememberSaveable { mutableStateOf(false) }

    val menu =
        @Composable {
            UiSourceScope(uiInspectionSource.element("WeekLayoutSettingsListItem.DropdownMenu.1", "androidx.compose.material3.DropdownMenu", "WeekLayoutSettingsListItem")) { DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.uiName("WeekLayoutSettingsListItem.DropdownMenu.1.root", "androidx.compose.material3.DropdownMenu", uiInspectionSource, "WeekLayoutSettingsListItem")) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.week_layout_scrolling), modifier = Modifier.uiName("WeekLayoutSettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "WeekLayoutSettingsListItem")) },
                    onClick = {
                        onChange(WeekLayout.Scrolling)
                        expanded = false
                    }, modifier = Modifier.uiName("WeekLayoutSettingsListItem.DropdownMenuItem.1", "androidx.compose.material3.DropdownMenuItem", uiInspectionSource, "WeekLayoutSettingsListItem")
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.week_layout_fixed), modifier = Modifier.uiName("WeekLayoutSettingsListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "WeekLayoutSettingsListItem")) },
                    onClick = {
                        onChange(WeekLayout.Fixed)
                        expanded = false
                    }, modifier = Modifier.uiName("WeekLayoutSettingsListItem.DropdownMenuItem.2", "androidx.compose.material3.DropdownMenuItem", uiInspectionSource, "WeekLayoutSettingsListItem")
                )
            } }
        }

    SettingsListItem(
        label = { Text(stringResource(Res.string.headline_week_layout), modifier = Modifier.uiName("WeekLayoutSettingsListItem.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "WeekLayoutSettingsListItem")) },
        onClick = { expanded = true },
        modifier = (modifier).uiName("WeekLayoutSettingsListItem.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "WeekLayoutSettingsListItem"),
        supportingContent = { Text(stringResource(Res.string.description_week_layout), modifier = Modifier.uiName("WeekLayoutSettingsListItem.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "WeekLayoutSettingsListItem")) },
        trailingContent = {
            Box (modifier = Modifier.uiName("WeekLayoutSettingsListItem.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "WeekLayoutSettingsListItem")) {
                Text(
                    text = label,
                    modifier = (Modifier.padding(horizontal = 8.dp)).uiName("WeekLayoutSettingsListItem.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "WeekLayoutSettingsListItem"),
                    color = MaterialTheme.colorScheme.primary,
                )
                menu()
            }
        },
        icon = { Icon(imageVector = Icons.Outlined.CalendarViewWeek, contentDescription = null, modifier = Modifier.uiName("WeekLayoutSettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "WeekLayoutSettingsListItem")) },
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun ThemeSettingsListItem(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SettingsListItem(
        label = { Text(stringResource(Res.string.headline_colors), modifier = Modifier.uiName("ThemeSettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ThemeSettingsListItem")) },
        onClick = onClick,
        modifier = (modifier).uiName("ThemeSettingsListItem.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "ThemeSettingsListItem"),
        supportingContent = { Text(stringResource(Res.string.description_colors), modifier = Modifier.uiName("ThemeSettingsListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ThemeSettingsListItem")) },
        icon = { Icon(imageVector = Icons.Outlined.Palette, contentDescription = null, modifier = Modifier.uiName("ThemeSettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "ThemeSettingsListItem")) },
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.personalization",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/personalization/PersonalizationScreen.kt",
)
