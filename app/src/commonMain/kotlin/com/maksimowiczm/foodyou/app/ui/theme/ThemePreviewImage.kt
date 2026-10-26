package com.maksimowiczm.foodyou.app.ui.theme

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
internal fun ThemePreviewImage(modifier: Modifier = Modifier) {
    ReadYouImage((modifier).uiName("ThemePreviewImage.ReadYouImage.1", "com.maksimowiczm.foodyou.app.ui.theme.ReadYouImage", uiInspectionSource, "ThemePreviewImage"))
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.theme",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/theme/ThemePreviewImage.kt",
)
