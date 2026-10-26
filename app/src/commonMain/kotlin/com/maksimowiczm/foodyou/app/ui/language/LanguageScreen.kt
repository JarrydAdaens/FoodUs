package com.maksimowiczm.foodyou.app.ui.language

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalAppConfig
import com.maksimowiczm.foodyou.settings.domain.entity.Translation
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LanguageScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: LanguageViewModel = koinViewModel()
    val appConfig = LocalAppConfig.current
    val uriHandler = LocalUriHandler.current

    val selectedTranslation by viewModel.translation.collectAsStateWithLifecycle()
    val translations by viewModel.translations.collectAsStateWithLifecycle()

    UiSourceScope(uiInspectionSource.element("LanguageScreen.LanguageScreen.1", "com.maksimowiczm.foodyou.app.ui.language.LanguageScreen", "LanguageScreen")) { LanguageScreen(
        onBack = onBack,
        onLanguageSelect = viewModel::selectTranslation,
        onHelpTranslate = { uriHandler.openUri(appConfig.translationUri) },
        selectedTranslation = selectedTranslation,
        translations = translations,
        modifier = modifier,
    ) }
}

@Composable
private fun LanguageScreen(
    onBack: () -> Unit,
    onLanguageSelect: (translation: Translation?) -> Unit,
    onHelpTranslate: () -> Unit,
    selectedTranslation: Translation,
    translations: List<Translation>,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = (modifier).uiName("LanguageScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "LanguageScreen"),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.headline_language), modifier = Modifier.uiName("LanguageScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "LanguageScreen")) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.uiName("LanguageScreen.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "LanguageScreen")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.action_go_back), modifier = Modifier.uiName("LanguageScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "LanguageScreen")
                        )
                    }
                },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("LanguageScreen.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "LanguageScreen")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = (Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("LanguageScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "LanguageScreen"),
            contentPadding = paddingValues,
        ) {
            item { TranslateButton(onClick = onHelpTranslate, modifier = (Modifier.padding(8.dp)).uiName("LanguageScreen.TranslateButton.1", "com.maksimowiczm.foodyou.app.ui.language.TranslateButton", uiInspectionSource, "LanguageScreen")) }

            item {
                val interactionSource = remember { MutableInteractionSource() }
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.headline_system), modifier = Modifier.uiName("LanguageScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "LanguageScreen")) },
                    leadingContent = {
                        RadioButton(
                            selected = false,
                            onClick = null,
                            interactionSource = interactionSource, modifier = Modifier.uiName("LanguageScreen.RadioButton.1", "androidx.compose.material3.RadioButton", uiInspectionSource, "LanguageScreen")
                        )
                    },
                    modifier =
                        (Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current,
                            onClick = { onLanguageSelect(null) },
                        )).uiName("LanguageScreen.ListItem.1", "androidx.compose.material3.ListItem", uiInspectionSource, "LanguageScreen"),
                )
            }

            translations.forEach { translation ->
                item {
                    val interactionSource = remember { MutableInteractionSource() }
                    ListItem(
                        headlineContent = { Text(translation.languageName, modifier = Modifier.uiName("LanguageScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "LanguageScreen")) },
                        supportingContent = {
                            translation.authorsStrings
                                .takeIf { it.isNotEmpty() }
                                ?.let {
                                    Column (modifier = Modifier.uiName("LanguageScreen.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "LanguageScreen")) {
                                        it.forEach { author ->
                                            Text(
                                                text = author.toAnnotatedString(),
                                                color = MaterialTheme.colorScheme.primary, modifier = Modifier.uiName("LanguageScreen.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "LanguageScreen")
                                            )
                                        }
                                    }
                                }
                        },
                        leadingContent = {
                            RadioButton(
                                selected = selectedTranslation == translation,
                                onClick = null,
                                interactionSource = interactionSource, modifier = Modifier.uiName("LanguageScreen.RadioButton.2", "androidx.compose.material3.RadioButton", uiInspectionSource, "LanguageScreen")
                            )
                        },
                        modifier =
                            (Modifier.clickable(
                                interactionSource = interactionSource,
                                indication = LocalIndication.current,
                                onClick = { onLanguageSelect(translation) },
                            )).uiName("LanguageScreen.ListItem.2", "androidx.compose.material3.ListItem", uiInspectionSource, "LanguageScreen"),
                    )
                }
            }

            item { SystemSettingsListItem( modifier = Modifier.uiName("LanguageScreen.SystemSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.language.SystemSettingsListItem", uiInspectionSource, "LanguageScreen")) }
        }
    }
}

@Composable internal expect fun SystemSettingsListItem(modifier: Modifier = Modifier)


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.language",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/language/LanguageScreen.kt",
)
