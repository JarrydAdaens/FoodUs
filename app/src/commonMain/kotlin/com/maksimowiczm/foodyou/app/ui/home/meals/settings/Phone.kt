package com.maksimowiczm.foodyou.app.ui.home.meals.settings

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun Phone(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = (modifier.size(width = 120.dp, height = 100.dp)).uiName("Phone.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "Phone"),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        color = color,
    ) {
        content()
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.home.meals.settings",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/meals/settings/Phone.kt",
)
