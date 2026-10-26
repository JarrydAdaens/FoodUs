package com.maksimowiczm.foodyou.app.ui.home.meals.settings

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun LayoutVertical(modifier: Modifier = Modifier) {
    Phone((modifier).uiName("LayoutVertical.Phone.1", "com.maksimowiczm.foodyou.app.ui.home.meals.settings.Phone", uiInspectionSource, "LayoutVertical")) {
        LazyColumn(
            modifier = (Modifier.fillMaxSize()).uiName("LayoutVertical.LazyColumn.1", "androidx.compose.foundation.lazy.LazyColumn", uiInspectionSource, "LayoutVertical"),
            contentPadding = PaddingValues(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            userScrollEnabled = false,
        ) {
            item { MockCard( modifier = Modifier.uiName("LayoutVertical.MockCard.1", "com.maksimowiczm.foodyou.app.ui.home.meals.settings.MockCard", uiInspectionSource, "LayoutVertical")) }

            item { MockCard( modifier = Modifier.uiName("LayoutVertical.MockCard.2", "com.maksimowiczm.foodyou.app.ui.home.meals.settings.MockCard", uiInspectionSource, "LayoutVertical")) }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.home.meals.settings",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/meals/settings/LayoutVertical.kt",
)
