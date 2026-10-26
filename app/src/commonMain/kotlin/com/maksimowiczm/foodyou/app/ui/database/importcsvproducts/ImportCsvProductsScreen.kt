package com.maksimowiczm.foodyou.app.ui.database.importcsvproducts

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName
import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceScope

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import com.maksimowiczm.foodyou.app.ui.common.component.DiscardDialog
import com.maksimowiczm.foodyou.importexport.domain.entity.ProductField
import com.maksimowiczm.foodyou.importexport.domain.entity.csvHeader
import foodyou.app.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
expect fun ImportCsvProductsScreen(
    onBack: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
internal fun ImportCsvProductsScreen(
    onBack: () -> Unit,
    onImport: (Map<ProductField, String>) -> Unit,
    header: List<String>,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }

    var fieldsMap by rememberSaveable { mutableStateOf(mapOf<ProductField, String>()) }

    // Auto-select fields when CSV headers match known exported headers
    LaunchedEffect(header) {
        if (fieldsMap.isEmpty()) {
            val autoMapped =
                header
                    .mapNotNull { columnName ->
                        val normalized = normalizeCsvHeader(columnName)
                        val field = expectedFieldByNormalizedHeader[normalized]
                        field?.let { it to columnName }
                    }
                    .toMap()

            if (autoMapped.isNotEmpty()) {
                fieldsMap = autoMapped
            }
        }
    }

    NavigationEventHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = fieldsMap.isNotEmpty(),
        onBackCompleted = { showDiscardDialog = true },
    )

    if (showDiscardDialog) {
        DiscardDialog(onDiscard = onBack, onDismissRequest = { showDiscardDialog = false }, modifier = Modifier.uiName("ImportCsvProductsScreen.DiscardDialog.1", "com.maksimowiczm.foodyou.app.ui.common.component.DiscardDialog", uiInspectionSource, "ImportCsvProductsScreen")) {
            Text(stringResource(Res.string.question_discard_changes), modifier = Modifier.uiName("ImportCsvProductsScreen.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ImportCsvProductsScreen"))
        }
    }

    Scaffold(
        modifier = (modifier).uiName("app.ui.database.importcsvproducts.ImportCsvProductsScreen.Scaffold.1", "androidx.compose.material3.Scaffold", uiInspectionSource, "ImportCsvProductsScreen"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.action_import_csv_food_products), modifier = Modifier.uiName("ImportCsvProductsScreen.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ImportCsvProductsScreen")) },
                navigationIcon = { ArrowBackIconButton(onBack, modifier = Modifier.uiName("ImportCsvProductsScreen.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "ImportCsvProductsScreen")) },
                actions = {
                    FilledIconButton(
                        onClick = { onImport(fieldsMap) },
                        enabled = fieldsMap.keys.containsAll(required), modifier = Modifier.uiName("ImportCsvProductsScreen.FilledIconButton.1", "androidx.compose.material3.FilledIconButton", uiInspectionSource, "ImportCsvProductsScreen")
                    ) {
                        Icon(painterResource(Res.drawable.ic_database_upload), null, modifier = Modifier.uiName("ImportCsvProductsScreen.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "ImportCsvProductsScreen"))
                    }
                },
                scrollBehavior = scrollBehavior, modifier = Modifier.uiName("ImportCsvProductsScreen.TopAppBar.1", "androidx.compose.material3.TopAppBar", uiInspectionSource, "ImportCsvProductsScreen")
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                (Modifier.fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = 16.dp)).uiName("ImportCsvProductsScreen.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "ImportCsvProductsScreen"),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(modifier = (Modifier).uiName("ImportCsvProductsScreen.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ImportCsvProductsScreen"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.description_import_csv_food_products_hint),
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("ImportCsvProductsScreen.Text.3", "androidx.compose.material3.Text", uiInspectionSource, "ImportCsvProductsScreen")
                    )
                    Text(
                        text =
                            stringResource(Res.string.description_import_csv_food_products_hint_2),
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("ImportCsvProductsScreen.Text.4", "androidx.compose.material3.Text", uiInspectionSource, "ImportCsvProductsScreen")
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.uiName("ImportCsvProductsScreen.Column.2", "androidx.compose.foundation.layout.Column", uiInspectionSource, "ImportCsvProductsScreen")) {
                    order.forEach { field ->
                        ProductFieldSelector(
                            field = field,
                            selected = fieldsMap[field],
                            onFieldSelected = { selectedField, columnName ->
                                fieldsMap =
                                    if (columnName == null) {
                                        fieldsMap - selectedField
                                    } else {
                                        fieldsMap + (selectedField to columnName)
                                    }
                            },
                            header = header,
                            required = field in required,
                            modifier = (Modifier.fillMaxSize()).uiName("ImportCsvProductsScreen.ProductFieldSelector.1", "com.maksimowiczm.foodyou.app.ui.database.importcsvproducts.ProductFieldSelector", uiInspectionSource, "ImportCsvProductsScreen"),
                        )
                    }
                }
            }
        }
    }
}

private val required =
    listOf(
        ProductField.Name,
        ProductField.Energy,
        ProductField.Proteins,
        ProductField.Carbohydrates,
        ProductField.Fats,
    )

private val order =
    listOf(
        ProductField.Name,
        ProductField.Brand,
        ProductField.Barcode,
        ProductField.Note,
        ProductField.SourceUrl,
        ProductField.PackageWeight,
        ProductField.ServingWeight,
        ProductField.Energy,
        ProductField.Proteins,
        ProductField.Carbohydrates,
        ProductField.Fats,
        ProductField.Sugars,
        ProductField.AddedSugars,
        ProductField.DietaryFiber,
        ProductField.SolubleFiber,
        ProductField.InsolubleFiber,
        ProductField.SaturatedFats,
        ProductField.TransFats,
        ProductField.MonounsaturatedFats,
        ProductField.PolyunsaturatedFats,
        ProductField.Omega3,
        ProductField.Omega6,
        ProductField.Salt,
        ProductField.Cholesterol,
        ProductField.Caffeine,
        ProductField.VitaminA,
        ProductField.VitaminB1,
        ProductField.VitaminB2,
        ProductField.VitaminB3,
        ProductField.VitaminB5,
        ProductField.VitaminB6,
        ProductField.VitaminB7,
        ProductField.VitaminB9,
        ProductField.VitaminB12,
        ProductField.VitaminC,
        ProductField.VitaminD,
        ProductField.VitaminE,
        ProductField.VitaminK,
        ProductField.Manganese,
        ProductField.Magnesium,
        ProductField.Potassium,
        ProductField.Calcium,
        ProductField.Copper,
        ProductField.Zinc,
        ProductField.Sodium,
        ProductField.Iron,
        ProductField.Phosphorus,
        ProductField.Selenium,
        ProductField.Iodine,
        ProductField.Chromium,
    )

// Normalizes header values for robust matching (trim quotes/space, case-insensitive)
private fun normalizeCsvHeader(header: String): String = header.trim().trim('"').lowercase()

// Expected CSV headers for each field (delegates to shared mapping)
private val expectedCsvHeaderByField: Map<ProductField, String> =
    ProductField.values().associateWith { it.csvHeader() }

private val expectedFieldByNormalizedHeader: Map<String, ProductField> =
    expectedCsvHeaderByField.entries.associate { (field, header) ->
        normalizeCsvHeader(header) to field
    }

@Composable
private fun ProductFieldSelector(
    field: ProductField,
    selected: String?,
    onFieldSelected: (ProductField, String?) -> Unit,
    header: List<String>,
    required: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val value = selected ?: stringResource(Res.string.neutral_no_column)
    var expanded by rememberSaveable { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions
            .filter { it is PressInteraction.Release }
            .collectLatest { expanded = !expanded }
    }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier =
            (modifier.clickable(
                indication = null,
                interactionSource = null,
                onClick = { expanded = !expanded },
            )).uiName("ProductFieldSelector.OutlinedTextField.1", "androidx.compose.material3.OutlinedTextField", uiInspectionSource, "ProductFieldSelector"),
        isError = required && selected == null,
        readOnly = true,
        label = { Text(field.stringResource(), modifier = Modifier.uiName("ProductFieldSelector.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "ProductFieldSelector")) },
        trailingIcon = {
            if (selected != null) {
                IconButton(onClick = { onFieldSelected(field, null) }, modifier = Modifier.uiName("ProductFieldSelector.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "ProductFieldSelector")) {
                    Icon(Icons.Outlined.Clear, null, modifier = Modifier.uiName("ProductFieldSelector.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "ProductFieldSelector"))
                }
            } else {
                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.uiName("ProductFieldSelector.IconButton.2", "androidx.compose.material3.IconButton", uiInspectionSource, "ProductFieldSelector")) {
                    Icon(Icons.Outlined.ArrowDropDown, null, modifier = Modifier.uiName("ProductFieldSelector.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "ProductFieldSelector"))
                }
                UiSourceScope(uiInspectionSource.element("ProductFieldSelector.DropdownMenu.1", "androidx.compose.material3.DropdownMenu", "ProductFieldSelector")) { DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.uiName("ProductFieldSelector.DropdownMenu.1.root", "androidx.compose.material3.DropdownMenu", uiInspectionSource, "ProductFieldSelector")) {
                    header.forEachIndexed { index, columnName ->
                        DropdownMenuItem(
                            text = { Text(columnName, modifier = Modifier.uiName("ProductFieldSelector.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "ProductFieldSelector")) },
                            onClick = {
                                expanded = false
                                onFieldSelected(field, columnName)
                            }, modifier = Modifier.uiName("ProductFieldSelector.DropdownMenuItem.1", "androidx.compose.material3.DropdownMenuItem", uiInspectionSource, "ProductFieldSelector")
                        )
                        if (index != header.lastIndex) {
                            HorizontalDivider( modifier = Modifier.uiName("ProductFieldSelector.HorizontalDivider.1", "androidx.compose.material3.HorizontalDivider", uiInspectionSource, "ProductFieldSelector"))
                        }
                    }
                } }
            }
        },
        interactionSource = interactionSource,
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.database.importcsvproducts",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/database/importcsvproducts/ImportCsvProductsScreen.kt",
)
