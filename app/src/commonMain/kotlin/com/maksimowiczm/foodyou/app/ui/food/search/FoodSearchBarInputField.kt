package com.maksimowiczm.foodyou.app.ui.food.search


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import foodyou.app.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FoodSearchBarInputField(
    searchBarState: SearchBarState,
    textFieldState: TextFieldState,
    onSearch: (String?) -> Unit,
    onBarcodeScanner: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    SearchBarDefaults.InputField(
        textFieldState = textFieldState,
        searchBarState = searchBarState,
        onSearch = onSearch,
        modifier = (modifier).uiName("FoodSearchBarInputField.InputField.1", "androidx.compose.material3.SearchBarDefaults.InputField", uiInspectionSource, "FoodSearchBarInputField"),
        placeholder = { Text(stringResource(Res.string.action_search), modifier = Modifier.uiName("FoodSearchBarInputField.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "FoodSearchBarInputField")) },
        leadingIcon = {
            if (searchBarState.targetValue == SearchBarValue.Expanded) {
                ArrowBackIconButton(
                    onClick = { coroutineScope.launch { searchBarState.animateToCollapsed() } }, modifier = Modifier.uiName("FoodSearchBarInputField.ArrowBackIconButton.1", "com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton", uiInspectionSource, "FoodSearchBarInputField")
                )
            } else {
                Icon(imageVector = Icons.Outlined.Search, contentDescription = null, modifier = Modifier.uiName("FoodSearchBarInputField.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "FoodSearchBarInputField"))
            }
        },
        trailingIcon = {
            Row (modifier = Modifier.uiName("FoodSearchBarInputField.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "FoodSearchBarInputField")) {
                if (textFieldState.text.isEmpty()) {
                    IconButton(onBarcodeScanner, modifier = Modifier.uiName("FoodSearchBarInputField.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "FoodSearchBarInputField")) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_barcode_scanner),
                            contentDescription = stringResource(Res.string.action_scan_barcode), modifier = Modifier.uiName("FoodSearchBarInputField.Icon.2", "androidx.compose.material3.Icon", uiInspectionSource, "FoodSearchBarInputField")
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            textFieldState.setTextAndPlaceCursorAtEnd("")
                            if (searchBarState.targetValue == SearchBarValue.Collapsed) {
                                onSearch(null)
                            }
                        }, modifier = Modifier.uiName("FoodSearchBarInputField.IconButton.2", "androidx.compose.material3.IconButton", uiInspectionSource, "FoodSearchBarInputField")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = stringResource(Res.string.action_clear), modifier = Modifier.uiName("FoodSearchBarInputField.Icon.3", "androidx.compose.material3.Icon", uiInspectionSource, "FoodSearchBarInputField")
                        )
                    }
                }
            }
        },
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.search",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/search/FoodSearchBarInputField.kt",
)
