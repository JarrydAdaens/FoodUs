package com.maksimowiczm.foodyou.app.ui.onboarding

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.OpenFoodFactsPrivacyCard
import com.maksimowiczm.foodyou.app.ui.common.component.UsdaPrivacyCard
import com.maksimowiczm.foodyou.common.compose.extension.add
import com.maksimowiczm.foodyou.importexport.swissfoodcompositiondatabase.domain.SwissFoodCompositionDatabaseRepository.Language
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FoodDatabaseScreen(
    onBack: () -> Unit,
    onAgree: () -> Unit,
    onSkip: () -> Unit,
    onAustralianFoodCompositionDatabase: () -> Unit,
    state: OnboardingState,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = (modifier).uiName("FoodDatabaseScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "FoodDatabaseScreen"),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(Res.string.headline_food_database), modifier = Modifier.uiName("FoodDatabaseScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "FoodDatabaseScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("FoodDatabaseScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "FoodDatabaseScreen")) },
                actions = { TextButton(onSkip, modifier = Modifier.uiName("FoodDatabaseScreen.TextButton.1", "androidx.compose.material3.TextButton", uiInspectionSource, "FoodDatabaseScreen")) { Text(stringResource(Res.string.action_skip), modifier = Modifier.uiName("FoodDatabaseScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "FoodDatabaseScreen")) } },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("FoodDatabaseScreen.LargeFlexibleTopAppBar.1", "androidx.compose.material3.LargeFlexibleTopAppBar", uiInspectionSource, "FoodDatabaseScreen")
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
                (Modifier.fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("FoodDatabaseScreen.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "FoodDatabaseScreen")
        ) {
            LazyColumn(
                modifier = (Modifier.fillMaxSize()).uiName("FoodDatabaseScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "FoodDatabaseScreen"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding =
                    paddingValues
                        .add(bottom = 72.dp) // Button height + padding
                        .add(vertical = 8.dp),
            ) {
                item {
                    Text(
                        text = stringResource(Res.string.description_food_database),
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("FoodDatabaseScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "FoodDatabaseScreen")
                    )
                }

                item {
                    OpenFoodFactsPrivacyCard(
                        selected = state.useOpenFoodFacts,
                        onSelectedChange = { state.useOpenFoodFacts = it },
                        modifier = (Modifier.fillMaxWidth()).uiName("FoodDatabaseScreen.OpenFoodFactsPrivacyCard.1", "com.maksimowiczm.foodyou.app.ui.common.component.OpenFoodFactsPrivacyCard", uiInspectionSource, "FoodDatabaseScreen"),
                    )
                }

                item {
                    UsdaPrivacyCard(
                        selected = state.useUsda,
                        onSelectedChange = { state.useUsda = it },
                        modifier = (Modifier.fillMaxWidth()).uiName("FoodDatabaseScreen.UsdaPrivacyCard.1", "com.maksimowiczm.foodyou.app.ui.common.component.UsdaPrivacyCard", uiInspectionSource, "FoodDatabaseScreen"),
                    )
                }

                item {
                    SwissFoodCompositionDatabase(
                        languages = state.swissLanguages,
                        onLanguageChange = { state.swissLanguages = it },
                        modifier = (Modifier.fillMaxWidth()).uiName("FoodDatabaseScreen.SwissFoodCompositionDatabase.1", "com.maksimowiczm.foodyou.app.ui.onboarding.SwissFoodCompositionDatabase", uiInspectionSource, "FoodDatabaseScreen"),
                    )
                }

                item {
                    DatabaseCard(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.uiName("FoodDatabaseScreen.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "FoodDatabaseScreen")) {
                                Text(
                                    text = stringResource(Res.string.headline_australian_food_composition_database),
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = (Modifier.weight(1f)).uiName("FoodDatabaseScreen.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "FoodDatabaseScreen"),
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    contentDescription = null, modifier = Modifier.uiName("FoodDatabaseScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "FoodDatabaseScreen")
                                )
                            }
                        },
                        onClick = onAustralianFoodCompositionDatabase,
                        modifier = (Modifier.fillMaxWidth()).uiName("FoodDatabaseScreen.DatabaseCard.1", "com.maksimowiczm.foodyou.app.ui.onboarding.DatabaseCard", uiInspectionSource, "FoodDatabaseScreen"),
                    ) {
                        Text(
                            text = stringResource(Res.string.description_australian_food_composition_database_short),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.uiName("FoodDatabaseScreen.Text.5", "androidx.compose.material3.Text", uiInspectionSource, "FoodDatabaseScreen")
                        )
                    }
                }
            }

            Button(
                onClick = onAgree,
                shapes = ButtonDefaults.shapes(),
                modifier =
                    (Modifier.align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .padding(bottom = paddingValues.calculateBottomPadding())
                        .height(56.dp)).uiName("FoodDatabaseScreen.Button.1", "androidx.compose.material3.Button", uiInspectionSource, "FoodDatabaseScreen"),
            ) {
                Text(stringResource(Res.string.action_agree_and_continue), modifier = Modifier.uiName("FoodDatabaseScreen.Text.6", "androidx.compose.material3.Text", uiInspectionSource, "FoodDatabaseScreen"))
            }
        }
    }
}

@Composable
private fun DatabaseCard(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val inner =
        @Composable {
            Column((Modifier.padding(contentPadding)).uiName("app.ui.onboarding.DatabaseCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DatabaseCard")) {
                title()
                Spacer(Modifier.height(8.dp))
                content()
            }
        }

    if (onClick == null) {
        Surface(
            modifier = (modifier).uiName("app.ui.onboarding.DatabaseCard.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "DatabaseCard"),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            content = inner,
        )
    } else {
        Surface(
            onClick = onClick,
            modifier = (modifier).uiName("app.ui.onboarding.DatabaseCard.Surface.2", "androidx.compose.material3.Surface", uiInspectionSource, "DatabaseCard"),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            content = inner,
        )
    }
}

@Composable
private fun SwissFoodCompositionDatabase(
    languages: Set<Language>,
    onLanguageChange: (Set<Language>) -> Unit,
    modifier: Modifier = Modifier,
) {
    DatabaseCard(
        title = {
            Text(
                text = stringResource(Res.string.headline_swiss_food_composition_database),
                style = MaterialTheme.typography.titleSmall, modifier = Modifier.uiName("app.ui.onboarding.SwissFoodCompositionDatabase.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "SwissFoodCompositionDatabase")
            )
        },
        modifier = (modifier).uiName("app.ui.onboarding.SwissFoodCompositionDatabase.DatabaseCard.1", "com.maksimowiczm.foodyou.app.ui.onboarding.DatabaseCard", uiInspectionSource, "SwissFoodCompositionDatabase"),
    ) {
        Column (modifier = Modifier.uiName("SwissFoodCompositionDatabase.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "SwissFoodCompositionDatabase")) {
            Text(
                text = stringResource(Res.string.description2_swiss_food_composition_database),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.uiName("app.ui.onboarding.SwissFoodCompositionDatabase.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "SwissFoodCompositionDatabase")
            )
            Spacer(Modifier.height(8.dp))
            LanguageButton(
                label = "English",
                onClick = {
                    onLanguageChange(
                        if (languages.contains(Language.ENGLISH)) {
                            languages - Language.ENGLISH
                        } else {
                            languages + Language.ENGLISH
                        }
                    )
                },
                selected = Language.ENGLISH in languages, modifier = Modifier.uiName("SwissFoodCompositionDatabase.LanguageButton.1", "com.maksimowiczm.foodyou.app.ui.onboarding.LanguageButton", uiInspectionSource, "SwissFoodCompositionDatabase")
            )
            LanguageButton(
                label = "Deutsch",
                onClick = {
                    onLanguageChange(
                        if (languages.contains(Language.GERMAN)) {
                            languages - Language.GERMAN
                        } else {
                            languages + Language.GERMAN
                        }
                    )
                },
                selected = Language.GERMAN in languages, modifier = Modifier.uiName("SwissFoodCompositionDatabase.LanguageButton.2", "com.maksimowiczm.foodyou.app.ui.onboarding.LanguageButton", uiInspectionSource, "SwissFoodCompositionDatabase")
            )
            LanguageButton(
                label = "Français",
                onClick = {
                    onLanguageChange(
                        if (languages.contains(Language.FRENCH)) {
                            languages - Language.FRENCH
                        } else {
                            languages + Language.FRENCH
                        }
                    )
                },
                selected = Language.FRENCH in languages, modifier = Modifier.uiName("SwissFoodCompositionDatabase.LanguageButton.3", "com.maksimowiczm.foodyou.app.ui.onboarding.LanguageButton", uiInspectionSource, "SwissFoodCompositionDatabase")
            )
            LanguageButton(
                label = "Italiano",
                onClick = {
                    onLanguageChange(
                        if (languages.contains(Language.ITALIAN)) {
                            languages - Language.ITALIAN
                        } else {
                            languages + Language.ITALIAN
                        }
                    )
                },
                selected = Language.ITALIAN in languages, modifier = Modifier.uiName("SwissFoodCompositionDatabase.LanguageButton.4", "com.maksimowiczm.foodyou.app.ui.onboarding.LanguageButton", uiInspectionSource, "SwissFoodCompositionDatabase")
            )
        }
    }
}

@Composable
private fun LanguageButton(
    label: String,
    onClick: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cornerRadius by
        animateDpAsState(
            targetValue = if (isPressed) 4.dp else 16.dp,
            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        )

    Surface(
        onClick = onClick,
        modifier = (modifier).uiName("app.ui.onboarding.LanguageButton.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "LanguageButton"),
        shape = RoundedCornerShape(cornerRadius),
        color =
            if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        contentColor =
            if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        interactionSource = interactionSource,
    ) {
        Box(modifier = (Modifier.padding(8.dp)).uiName("app.ui.onboarding.LanguageButton.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "LanguageButton"), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                modifier = (Modifier.fillMaxWidth()).uiName("app.ui.onboarding.LanguageButton.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "LanguageButton"),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.onboarding",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/onboarding/FoodDatabaseScreen.kt",
)
