package com.maksimowiczm.foodyou.app.ui.theme

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.common.compose.extension.add
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ThemeScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ThemeSettingsViewModel = koinViewModel()

    val themeSettings = viewModel.themeSettings.collectAsStateWithLifecycle().value
    val nutrientsColors = viewModel.nutrientsColors.collectAsStateWithLifecycle().value

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    if (themeSettings == null || nutrientsColors == null) {
        // TODO loading state
    } else {
        Scaffold(
            modifier = (modifier).uiName("ThemeScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "ThemeScreen"),
            topBar = {
                LargeFlexibleTopAppBar(
                    title = { Text(stringResource(Res.string.headline_appearance), modifier = Modifier.uiName("ThemeScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ThemeScreen")) },
                    navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("ThemeScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "ThemeScreen")) },
                    scrollBehavior = scrollBehavior, modifier = Modifier.uiName("ThemeScreen.LargeFlexibleTopAppBar.1", "androidx.compose.material3.LargeFlexibleTopAppBar", uiInspectionSource, "ThemeScreen")
                )
            },
            content = { paddingValues ->
                ThemeScreenContent(
                    themeSettings = themeSettings,
                    onUpdateTheme = viewModel::updateTheme,
                    onUpdateThemeOption = viewModel::updateThemeOption,
                    onRandomizeTheme = viewModel::updateRandomizeTheme,
                    nutrientsColors = nutrientsColors,
                    onNutrientsColorsChange = viewModel::updateNutrientsColors,
                    contentPadding = paddingValues.add(vertical = 8.dp),
                    modifier = (Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("ThemeScreen.ThemeScreenContent.1", "com.maksimowiczm.foodyou.app.ui.theme.ThemeScreenContent", uiInspectionSource, "ThemeScreen"),
                )
            },
        )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.theme",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/theme/ThemeScreen.kt",
)
