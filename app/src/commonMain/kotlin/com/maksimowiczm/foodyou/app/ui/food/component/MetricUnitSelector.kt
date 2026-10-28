package com.maksimowiczm.foodyou.app.ui.food.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.maksimowiczm.foodyou.app.ui.developer.inspection.*

@Composable
internal fun MetricUnitSelector(isLiquid: Boolean, onSelect: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Row((modifier.fillMaxWidth().selectableGroup()).uiName("MetricUnitSelector.Row.phone1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "MetricUnitSelector")) {
        listOf(false, true).forEach { liquid ->
            Row((Modifier.weight(1f).heightIn(min = 48.dp).selectable(selected = isLiquid == liquid,
                role = Role.RadioButton, onClick = { onSelect(liquid) })).uiName("MetricUnitSelector.Row.phone2", "androidx.compose.foundation.layout.Row", uiInspectionSource, "MetricUnitSelector"), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = isLiquid == liquid, onClick = null, modifier = Modifier.uiName("MetricUnitSelector.RadioButton.phone1", "androidx.compose.material3.RadioButton", uiInspectionSource, "MetricUnitSelector"))
                Text(stringResource(if (liquid) Res.string.unit_choice_millilitres else Res.string.unit_choice_grams), modifier = Modifier.uiName("MetricUnitSelector.Text.phone1", "androidx.compose.material3.Text", uiInspectionSource, "MetricUnitSelector"))
            }
        }
    }
}
private val uiInspectionSource = UiSourceFile("com.maksimowiczm.foodyou.app.ui.food.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/component/MetricUnitSelector.kt")
