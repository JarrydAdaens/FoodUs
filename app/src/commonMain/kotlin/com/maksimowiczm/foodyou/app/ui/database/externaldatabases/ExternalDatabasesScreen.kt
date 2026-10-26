package com.maksimowiczm.foodyou.app.ui.database.externaldatabases

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.OpenFoodFactsPrivacyCard
import com.maksimowiczm.foodyou.app.ui.common.component.UsdaPrivacyCard
import com.maksimowiczm.foodyou.common.compose.extension.add
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExternalDatabasesScreen(
    onBack: () -> Unit,
    onSwissFoodCompositionDatabase: () -> Unit,
    onAustralianFoodCompositionDatabase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ExternalDatabasesViewModel = koinViewModel()
    val model by viewModel.foodPreferences.collectAsStateWithLifecycle()

    UiSourceScope(uiInspectionSource.element("ExternalDatabasesScreen.ExternalDatabasesScreen.1", "com.maksimowiczm.foodyou.app.ui.database.externaldatabases.ExternalDatabasesScreen", "ExternalDatabasesScreen")) { ExternalDatabasesScreen(
        onBack = onBack,
        model = model,
        onOpenFoodFactsChange = viewModel::toggleOpenFoodFacts,
        onUsdaChange = viewModel::toggleUsda,
        onSwissFoodCompositionDatabase = onSwissFoodCompositionDatabase,
        onAustralianFoodCompositionDatabase = onAustralianFoodCompositionDatabase,
        modifier = modifier,
    ) }
}

@Composable
private fun ExternalDatabasesScreen(
    onBack: () -> Unit,
    model: FoodPreferencesModel,
    onOpenFoodFactsChange: (Boolean) -> Unit,
    onUsdaChange: (Boolean) -> Unit,
    onSwissFoodCompositionDatabase: () -> Unit,
    onAustralianFoodCompositionDatabase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = (modifier).uiName("ExternalDatabasesScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "ExternalDatabasesScreen"),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(Res.string.headline_food_database), modifier = Modifier.uiName("ExternalDatabasesScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ExternalDatabasesScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("ExternalDatabasesScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "ExternalDatabasesScreen")) },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("ExternalDatabasesScreen.LargeFlexibleTopAppBar.1", "androidx.compose.material3.LargeFlexibleTopAppBar", uiInspectionSource, "ExternalDatabasesScreen")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                (Modifier.fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("ExternalDatabasesScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "ExternalDatabasesScreen"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = paddingValues.add(vertical = 8.dp),
        ) {
            item {
                Text(
                    text = stringResource(Res.string.description_food_database),
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("ExternalDatabasesScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ExternalDatabasesScreen")
                )
            }

            item {
                OpenFoodFactsPrivacyCard(
                    selected = model.useOpenFoodFacts ?: false,
                    onSelectedChange = onOpenFoodFactsChange, modifier = Modifier.uiName("ExternalDatabasesScreen.OpenFoodFactsPrivacyCard.1", "com.maksimowiczm.foodyou.app.ui.common.component.OpenFoodFactsPrivacyCard", uiInspectionSource, "ExternalDatabasesScreen")
                )
            }
            item {
                UsdaPrivacyCard(selected = model.useUsda ?: false, onSelectedChange = onUsdaChange, modifier = Modifier.uiName("ExternalDatabasesScreen.UsdaPrivacyCard.1", "com.maksimowiczm.foodyou.app.ui.common.component.UsdaPrivacyCard", uiInspectionSource, "ExternalDatabasesScreen"))
            }

            item {
                SwissFoodCompositionDatabase(
                    onClick = onSwissFoodCompositionDatabase,
                    modifier = (Modifier.fillMaxWidth()).uiName("ExternalDatabasesScreen.SwissFoodCompositionDatabase.1", "com.maksimowiczm.foodyou.app.ui.database.externaldatabases.SwissFoodCompositionDatabase", uiInspectionSource, "ExternalDatabasesScreen"),
                )
            }

            item {
                AustralianFoodCompositionDatabase(
                    onClick = onAustralianFoodCompositionDatabase,
                    modifier = (Modifier.fillMaxWidth()).uiName("ExternalDatabasesScreen.AustralianFoodCompositionDatabase.1", "com.maksimowiczm.foodyou.app.ui.database.externaldatabases.AustralianFoodCompositionDatabase", uiInspectionSource, "ExternalDatabasesScreen"),
                )
            }
        }
    }
}

@Composable
private fun AustralianFoodCompositionDatabase(onClick: () -> Unit, modifier: Modifier = Modifier) {
    DatabaseCard(
        title = {
            Row(
                modifier = (Modifier.fillMaxSize()).uiName("AustralianFoodCompositionDatabase.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "AustralianFoodCompositionDatabase"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text =
                        stringResource(Res.string.headline_australian_food_composition_database),
                    style = MaterialTheme.typography.titleSmall, modifier = Modifier.uiName("AustralianFoodCompositionDatabase.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "AustralianFoodCompositionDatabase")
                )
                Box(modifier = (Modifier.size(48.dp)).uiName("AustralianFoodCompositionDatabase.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "AustralianFoodCompositionDatabase"), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null, modifier = Modifier.uiName("AustralianFoodCompositionDatabase.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "AustralianFoodCompositionDatabase")
                    )
                }
            }
        },
        modifier = (modifier).uiName("AustralianFoodCompositionDatabase.DatabaseCard.1", "com.maksimowiczm.foodyou.app.ui.database.externaldatabases.DatabaseCard", uiInspectionSource, "AustralianFoodCompositionDatabase"),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 16.dp),
        onClick = onClick,
    ) {
        Text(
            text = stringResource(Res.string.description_australian_food_composition_database_short),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.uiName("AustralianFoodCompositionDatabase.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "AustralianFoodCompositionDatabase")
        )
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
            Column((Modifier.padding(contentPadding)).uiName("app.ui.database.externaldatabases.DatabaseCard.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DatabaseCard")) {
                title()
                Spacer(Modifier.height(8.dp))
                content()
            }
        }

    if (onClick == null) {
        Surface(
            modifier = (modifier).uiName("app.ui.database.externaldatabases.DatabaseCard.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "DatabaseCard"),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            content = inner,
        )
    } else {
        Surface(
            onClick = onClick,
            modifier = (modifier).uiName("app.ui.database.externaldatabases.DatabaseCard.Surface.2", "androidx.compose.material3.Surface", uiInspectionSource, "DatabaseCard"),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            content = inner,
        )
    }
}

@Composable
private fun SwissFoodCompositionDatabase(onClick: () -> Unit, modifier: Modifier = Modifier) {
    DatabaseCard(
        title = {
            Row(
                modifier = (Modifier.fillMaxSize()).uiName("SwissFoodCompositionDatabase.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "SwissFoodCompositionDatabase"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.headline_swiss_food_composition_database),
                    style = MaterialTheme.typography.titleSmall, modifier = Modifier.uiName("app.ui.database.externaldatabases.SwissFoodCompositionDatabase.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "SwissFoodCompositionDatabase")
                )
                Box(modifier = (Modifier.size(48.dp)).uiName("SwissFoodCompositionDatabase.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "SwissFoodCompositionDatabase"), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null, modifier = Modifier.uiName("SwissFoodCompositionDatabase.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "SwissFoodCompositionDatabase")
                    )
                }
            }
        },
        modifier = (modifier).uiName("app.ui.database.externaldatabases.SwissFoodCompositionDatabase.DatabaseCard.1", "com.maksimowiczm.foodyou.app.ui.database.externaldatabases.DatabaseCard", uiInspectionSource, "SwissFoodCompositionDatabase"),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 16.dp),
        onClick = onClick,
    ) {
        Text(
            text = stringResource(Res.string.description_swiss_food_composition_database_short),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.uiName("app.ui.database.externaldatabases.SwissFoodCompositionDatabase.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "SwissFoodCompositionDatabase")
        )
        Spacer(Modifier.height(8.dp))
        FeaturesContainer (modifier = Modifier.uiName("SwissFoodCompositionDatabase.FeaturesContainer.1", "com.maksimowiczm.foodyou.app.ui.database.externaldatabases.FeaturesContainer", uiInspectionSource, "SwissFoodCompositionDatabase")) { ManualImport( modifier = Modifier.uiName("SwissFoodCompositionDatabase.ManualImport.1", "com.maksimowiczm.foodyou.app.ui.database.externaldatabases.ManualImport", uiInspectionSource, "SwissFoodCompositionDatabase")) }
    }
}

@Composable
private fun FeaturesContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = (modifier).uiName("FeaturesContainer.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "FeaturesContainer"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.headline_features),
            style = MaterialTheme.typography.labelLarge, modifier = Modifier.uiName("FeaturesContainer.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "FeaturesContainer")
        )

        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodySmall) {
            content()
        }
    }
}

@Composable
private fun ManualImport(modifier: Modifier = Modifier) {
    Row(modifier = (modifier).uiName("ManualImport.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "ManualImport"), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Outlined.FileOpen, contentDescription = null, modifier = Modifier.uiName("ManualImport.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "ManualImport"))
        Spacer(Modifier.width(16.dp))
        Text(stringResource(Res.string.feature_manual_database_import), modifier = Modifier.uiName("ManualImport.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ManualImport"))
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.database.externaldatabases",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/database/externaldatabases/ExternalDatabasesScreen.kt",
)
