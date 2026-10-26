package com.maksimowiczm.foodyou.app.ui.database.master

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DatabaseSettingsScreen(
    onBack: () -> Unit,
    onImportCsvProducts: () -> Unit,
    onExportCsvProducts: () -> Unit,
    onDatabaseBackup: () -> Unit,
    onImportLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = (modifier).uiName("DatabaseSettingsScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "DatabaseSettingsScreen"),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(Res.string.headline_database), modifier = Modifier.uiName("DatabaseSettingsScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DatabaseSettingsScreen")) },
                subtitle = { Text(stringResource(Res.string.description_manage_database), modifier = Modifier.uiName("DatabaseSettingsScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "DatabaseSettingsScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("DatabaseSettingsScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "DatabaseSettingsScreen")) },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("DatabaseSettingsScreen.LargeFlexibleTopAppBar.1", "androidx.compose.material3.LargeFlexibleTopAppBar", uiInspectionSource, "DatabaseSettingsScreen")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = (Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)).uiName("DatabaseSettingsScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "DatabaseSettingsScreen"),
            contentPadding = paddingValues,
        ) {
            item { SettingsListItem(icon={Icon(Icons.Outlined.FileOpen,null, modifier = Modifier.uiName("DatabaseSettingsScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DatabaseSettingsScreen"))},label={Text(stringResource(Res.string.library_import_title), modifier = Modifier.uiName("DatabaseSettingsScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "DatabaseSettingsScreen"))},
                supportingContent={Text(stringResource(Res.string.library_import_description), modifier = Modifier.uiName("DatabaseSettingsScreen.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "DatabaseSettingsScreen"))},onClick=onImportLibrary, modifier = Modifier.uiName("DatabaseSettingsScreen.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "DatabaseSettingsScreen")) }
            item { ImportCsvProductsSettingsListItem(onImportCsvProducts, modifier = Modifier.uiName("DatabaseSettingsScreen.ImportCsvProductsSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.database.master.ImportCsvProductsSettingsListItem", uiInspectionSource, "DatabaseSettingsScreen")) }
            item { ExportCsvProductsSettingsListItem(onExportCsvProducts, modifier = Modifier.uiName("DatabaseSettingsScreen.ExportCsvProductsSettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.database.master.ExportCsvProductsSettingsListItem", uiInspectionSource, "DatabaseSettingsScreen")) }
            item { DatabaseBackup(onDatabaseBackup, modifier = Modifier.uiName("DatabaseSettingsScreen.DatabaseBackup.1", "com.maksimowiczm.foodyou.app.ui.database.master.DatabaseBackup", uiInspectionSource, "DatabaseSettingsScreen")) }
        }
    }
}

@Composable
private fun ImportCsvProductsSettingsListItem(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SettingsListItem(
        icon = { Icon(Icons.Outlined.FileOpen, null, modifier = Modifier.uiName("ImportCsvProductsSettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "ImportCsvProductsSettingsListItem")) },
        label = { Text(stringResource(Res.string.action_import_csv_food_products), modifier = Modifier.uiName("ImportCsvProductsSettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ImportCsvProductsSettingsListItem")) },
        supportingContent = {
            Text(stringResource(Res.string.description_import_csv_food_products), modifier = Modifier.uiName("ImportCsvProductsSettingsListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ImportCsvProductsSettingsListItem"))
        },
        onClick = onClick,
        modifier = (modifier).uiName("ImportCsvProductsSettingsListItem.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "ImportCsvProductsSettingsListItem"),
    )
}

@Composable
private fun ExportCsvProductsSettingsListItem(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SettingsListItem(
        icon = { Icon(painterResource(Res.drawable.ic_file_export), null, modifier = Modifier.uiName("ExportCsvProductsSettingsListItem.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "ExportCsvProductsSettingsListItem")) },
        label = { Text(stringResource(Res.string.action_export_csv_food_products), modifier = Modifier.uiName("ExportCsvProductsSettingsListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ExportCsvProductsSettingsListItem")) },
        supportingContent = {
            Text(stringResource(Res.string.description_export_csv_food_products), modifier = Modifier.uiName("ExportCsvProductsSettingsListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ExportCsvProductsSettingsListItem"))
        },
        onClick = onClick,
        modifier = (modifier).uiName("ExportCsvProductsSettingsListItem.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "ExportCsvProductsSettingsListItem"),
    )
}

@Composable
private fun DatabaseBackup(onClick: () -> Unit, modifier: Modifier = Modifier) {
    SettingsListItem(
        icon = { Icon(Icons.Outlined.Archive, null, modifier = Modifier.uiName("DatabaseBackup.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "DatabaseBackup")) },
        label = { Text(stringResource(Res.string.headline_database_backup), modifier = Modifier.uiName("DatabaseBackup.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "DatabaseBackup")) },
        onClick = onClick,
        modifier = (modifier).uiName("DatabaseBackup.SettingsListItem.1", "com.maksimowiczm.foodyou.app.ui.common.component.SettingsListItem", uiInspectionSource, "DatabaseBackup"),
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.database.master",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/database/master/DatabaseSettingsScreen.kt",
)
