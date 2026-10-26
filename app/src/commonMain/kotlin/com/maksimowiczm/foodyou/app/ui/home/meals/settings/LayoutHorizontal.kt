package com.maksimowiczm.foodyou.app.ui.home.meals.settings

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

@Composable
internal fun LayoutHorizontal(modifier: Modifier = Modifier) {
    Phone((modifier).uiName("LayoutHorizontal.Phone.1", "com.maksimowiczm.foodyou.app.ui.home.meals.settings.Phone", uiInspectionSource, "LayoutHorizontal")) {
        LazyRow(
            modifier = (Modifier.fillMaxSize()).uiName("LayoutHorizontal.LazyRow.1", "androidx.compose.foundation.lazy.LazyRow", uiInspectionSource, "LayoutHorizontal"),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            userScrollEnabled = false,
        ) {
            item { MockCard( modifier = Modifier.uiName("LayoutHorizontal.MockCard.1", "com.maksimowiczm.foodyou.app.ui.home.meals.settings.MockCard", uiInspectionSource, "LayoutHorizontal")) }

            item { MockCard((Modifier.scale(.9f)).uiName("LayoutHorizontal.MockCard.2", "com.maksimowiczm.foodyou.app.ui.home.meals.settings.MockCard", uiInspectionSource, "LayoutHorizontal")) }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.home.meals.settings",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/meals/settings/LayoutHorizontal.kt",
)
