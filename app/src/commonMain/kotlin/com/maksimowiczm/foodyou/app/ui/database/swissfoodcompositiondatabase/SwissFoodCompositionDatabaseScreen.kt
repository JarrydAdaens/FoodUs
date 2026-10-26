package com.maksimowiczm.foodyou.app.ui.database.swissfoodcompositiondatabase

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.WebsiteChip
import com.maksimowiczm.foodyou.app.ui.common.utility.LocalAppConfig
import com.maksimowiczm.foodyou.common.compose.extension.add
import com.maksimowiczm.foodyou.importexport.swissfoodcompositiondatabase.domain.SwissFoodCompositionDatabaseRepository.Language
import foodyou.app.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun SwissFoodCompositionDatabaseScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel = koinInject<SwissFoodCompositionDatabaseViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    UiSourceScope(uiInspectionSource.element("SwissFoodCompositionDatabaseScreen.SwissFoodCompositionDatabaseScreen.1", "com.maksimowiczm.foodyou.app.ui.database.swissfoodcompositiondatabase.SwissFoodCompositionDatabaseScreen", "SwissFoodCompositionDatabaseScreen")) { SwissFoodCompositionDatabaseScreen(
        uiState = uiState,
        onBack = onBack,
        onImport = viewModel::import,
        modifier = modifier,
    ) }
}

@Composable
private fun SwissFoodCompositionDatabaseScreen(
    uiState: SwissFoodCompositionDatabaseUiState,
    onBack: () -> Unit,
    onImport: (Set<Language>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutinesScope = rememberCoroutineScope()

    val uriHandler = LocalUriHandler.current
    val websiteUri = LocalAppConfig.current.swissFoodCompositionDatabaseWebsiteUri

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var languages by rememberSaveable { mutableStateOf(setOf<Language>()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val pleaseWaitMessage =
        stringResource(Res.string.description_please_wait_while_importing_products)

    LaunchedEffect(uiState) {
        when (uiState) {
            SwissFoodCompositionDatabaseUiState.Finished ->
                snackbarHostState.currentSnackbarData?.dismiss()

            is SwissFoodCompositionDatabaseUiState.Importing,
            SwissFoodCompositionDatabaseUiState.LanguagePick -> Unit
        }
    }

    NavigationEventHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = uiState is SwissFoodCompositionDatabaseUiState.Importing,
        onBackCompleted = {
            coroutinesScope.launch { snackbarHostState.showSnackbar(pleaseWaitMessage) }
        },
    )

    Scaffold(
        modifier = (modifier).uiName("SwissFoodCompositionDatabaseScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "SwissFoodCompositionDatabaseScreen"),
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(stringResource(Res.string.headline_swiss_food_composition_database), modifier = Modifier.uiName("SwissFoodCompositionDatabaseScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "SwissFoodCompositionDatabaseScreen"))
                },
                navigationIcon = {
                    ArrowBackIconButton(
                        onClick = onBack,
                        enabled = uiState !is SwissFoodCompositionDatabaseUiState.Importing, modifier = Modifier.uiName("SwissFoodCompositionDatabaseScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "SwissFoodCompositionDatabaseScreen")
                    )
                },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("SwissFoodCompositionDatabaseScreen.LargeFlexibleTopAppBar.1", "androidx.compose.material3.LargeFlexibleTopAppBar", uiInspectionSource, "SwissFoodCompositionDatabaseScreen")
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.uiName("SwissFoodCompositionDatabaseScreen.SnackbarHost.1", "androidx.compose.material3.SnackbarHost", uiInspectionSource, "SwissFoodCompositionDatabaseScreen")) },
    ) { paddingValues ->
        Box(
            modifier =
                (Modifier.fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("SwissFoodCompositionDatabaseScreen.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "SwissFoodCompositionDatabaseScreen")
        ) {
            when (uiState) {
                SwissFoodCompositionDatabaseUiState.Finished ->
                    ImportingFinished(
                        modifier = (Modifier.align(Alignment.Center).padding(paddingValues)).uiName("SwissFoodCompositionDatabaseScreen.ImportingFinished.1", "com.maksimowiczm.foodyou.app.ui.database.swissfoodcompositiondatabase.ImportingFinished", uiInspectionSource, "SwissFoodCompositionDatabaseScreen")
                    )

                is SwissFoodCompositionDatabaseUiState.Importing ->
                    ImportingProgress(
                        progress = uiState.progress,
                        modifier = (Modifier.align(Alignment.Center).padding(paddingValues)).uiName("SwissFoodCompositionDatabaseScreen.ImportingProgress.1", "com.maksimowiczm.foodyou.app.ui.database.swissfoodcompositiondatabase.ImportingProgress", uiInspectionSource, "SwissFoodCompositionDatabaseScreen"),
                    )

                SwissFoodCompositionDatabaseUiState.LanguagePick ->
                    LazyColumn(
                        modifier =
                            (Modifier.fillMaxSize()
                                .nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("SwissFoodCompositionDatabaseScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "SwissFoodCompositionDatabaseScreen"),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding =
                            paddingValues
                                .add(bottom = 72.dp) // Button height + padding
                                .add(vertical = 8.dp),
                    ) {
                        item {
                            Text(
                                text =
                                    stringResource(
                                        Res.string.description2_swiss_food_composition_database
                                    ),
                                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("SwissFoodCompositionDatabaseScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "SwissFoodCompositionDatabaseScreen")
                            )
                        }

                        item { WebsiteChip(onClick = { uriHandler.openUri(websiteUri) }, modifier = Modifier.uiName("SwissFoodCompositionDatabaseScreen.WebsiteChip.1", "com.maksimowiczm.foodyou.app.ui.common.component.WebsiteChip", uiInspectionSource, "SwissFoodCompositionDatabaseScreen")) }

                        item {
                            LanguagePicker(selected = languages, onLanguages = { languages = it }, modifier = Modifier.uiName("SwissFoodCompositionDatabaseScreen.LanguagePicker.1", "com.maksimowiczm.foodyou.app.ui.database.swissfoodcompositiondatabase.LanguagePicker", uiInspectionSource, "SwissFoodCompositionDatabaseScreen"))
                        }
                    }
            }

            AnimatedVisibility(
                visible =
                    uiState == SwissFoodCompositionDatabaseUiState.LanguagePick &&
                        languages.isNotEmpty(),
                modifier =
                    (Modifier.zIndex(10f)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .padding(bottom = paddingValues.calculateBottomPadding())).uiName("SwissFoodCompositionDatabaseScreen.AnimatedVisibility.1", "androidx.compose.animation.AnimatedVisibility", uiInspectionSource, "SwissFoodCompositionDatabaseScreen"),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                Button(
                    onClick = { onImport(languages) },
                    shapes = ButtonDefaults.shapes(),
                    modifier = (Modifier.height(56.dp)).uiName("SwissFoodCompositionDatabaseScreen.Button.1", "androidx.compose.material3.Button", uiInspectionSource, "SwissFoodCompositionDatabaseScreen"),
                ) {
                    Text(stringResource(Res.string.action_import), modifier = Modifier.uiName("SwissFoodCompositionDatabaseScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "SwissFoodCompositionDatabaseScreen"))
                }
            }

            AnimatedVisibility(
                visible = uiState == SwissFoodCompositionDatabaseUiState.Finished,
                modifier =
                    (Modifier.zIndex(10f)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .padding(bottom = paddingValues.calculateBottomPadding())).uiName("SwissFoodCompositionDatabaseScreen.AnimatedVisibility.2", "androidx.compose.animation.AnimatedVisibility", uiInspectionSource, "SwissFoodCompositionDatabaseScreen"),
                enter = slideInVertically { it } + fadeIn(),
            ) {
                Button(
                    onClick = onBack,
                    shapes = ButtonDefaults.shapes(),
                    modifier = (Modifier.height(56.dp)).uiName("SwissFoodCompositionDatabaseScreen.Button.2", "androidx.compose.material3.Button", uiInspectionSource, "SwissFoodCompositionDatabaseScreen"),
                ) {
                    Text(stringResource(Res.string.action_done), modifier = Modifier.uiName("SwissFoodCompositionDatabaseScreen.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "SwissFoodCompositionDatabaseScreen"))
                }
            }
        }
    }
}

@Composable
private fun LanguagePicker(
    selected: Set<Language>,
    onLanguages: (Set<Language>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = (modifier).uiName("LanguagePicker.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "LanguagePicker"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LanguageButton(
            label = "English",
            selected = Language.ENGLISH in selected,
            onClick = {
                if (Language.ENGLISH in selected) {
                    onLanguages(selected - Language.ENGLISH)
                } else {
                    onLanguages(selected + Language.ENGLISH)
                }
            }, modifier = Modifier.uiName("LanguagePicker.LanguageButton.1", "com.maksimowiczm.foodyou.app.ui.database.swissfoodcompositiondatabase.LanguageButton", uiInspectionSource, "LanguagePicker")
        )
        LanguageButton(
            label = "Deutsch",
            selected = Language.GERMAN in selected,
            onClick = {
                if (Language.GERMAN in selected) {
                    onLanguages(selected - Language.GERMAN)
                } else {
                    onLanguages(selected + Language.GERMAN)
                }
            }, modifier = Modifier.uiName("LanguagePicker.LanguageButton.2", "com.maksimowiczm.foodyou.app.ui.database.swissfoodcompositiondatabase.LanguageButton", uiInspectionSource, "LanguagePicker")
        )
        LanguageButton(
            label = "Français",
            selected = Language.FRENCH in selected,
            onClick = {
                if (Language.FRENCH in selected) {
                    onLanguages(selected - Language.FRENCH)
                } else {
                    onLanguages(selected + Language.FRENCH)
                }
            }, modifier = Modifier.uiName("LanguagePicker.LanguageButton.3", "com.maksimowiczm.foodyou.app.ui.database.swissfoodcompositiondatabase.LanguageButton", uiInspectionSource, "LanguagePicker")
        )
        LanguageButton(
            label = "Italiano",
            selected = Language.ITALIAN in selected,
            onClick = {
                if (Language.ITALIAN in selected) {
                    onLanguages(selected - Language.ITALIAN)
                } else {
                    onLanguages(selected + Language.ITALIAN)
                }
            }, modifier = Modifier.uiName("LanguagePicker.LanguageButton.4", "com.maksimowiczm.foodyou.app.ui.database.swissfoodcompositiondatabase.LanguageButton", uiInspectionSource, "LanguagePicker")
        )
    }
}

@Composable
private fun LanguageButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by
        animateDpAsState(
            targetValue = if (isPressed) 24.dp else 16.dp,
            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        )

    val color by
        animateColorAsState(
            targetValue =
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                }
        )
    val contentColor by
        animateColorAsState(
            targetValue =
                if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
        )

    Surface(
        onClick = onClick,
        modifier = (modifier).uiName("app.ui.database.swissfoodcompositiondatabase.LanguageButton.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "LanguageButton"),
        shape = RoundedCornerShape(cornerRadius),
        color = color,
        contentColor = contentColor,
        interactionSource = interactionSource,
    ) {
        Box(
            modifier = (Modifier.fillMaxWidth().padding(16.dp)).uiName("app.ui.database.swissfoodcompositiondatabase.LanguageButton.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "LanguageButton"),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                modifier = (Modifier.fillMaxWidth()).uiName("app.ui.database.swissfoodcompositiondatabase.LanguageButton.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "LanguageButton"),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
private fun ImportingProgress(progress: Float, modifier: Modifier = Modifier) {
    val animatedProgress by
        animateFloatAsState(
            targetValue = progress,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        )

    Column(modifier = (modifier).uiName("ImportingProgress.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ImportingProgress"), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularWavyProgressIndicator(
            progress = { animatedProgress },
            modifier = (Modifier.size(68.dp)).uiName("ImportingProgress.CircularWavyProgressIndicator.1", "androidx.compose.material3.CircularWavyProgressIndicator", uiInspectionSource, "ImportingProgress"),
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.notification_importing_products),
            modifier = (Modifier.fillMaxWidth()).uiName("ImportingProgress.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ImportingProgress"),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.description_please_wait_while_importing_products),
            modifier = (Modifier.fillMaxWidth()).uiName("ImportingProgress.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ImportingProgress"),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ImportingFinished(modifier: Modifier = Modifier) {
    Column(modifier = (modifier).uiName("ImportingFinished.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ImportingFinished"), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = (Modifier.size(68.dp)).uiName("ImportingFinished.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "ImportingFinished"),
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.notification_importing_products_success),
            modifier = (Modifier.fillMaxWidth()).uiName("ImportingFinished.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ImportingFinished"),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.database.swissfoodcompositiondatabase",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/database/swissfoodcompositiondatabase/SwissFoodCompositionDatabaseScreen.kt",
)
