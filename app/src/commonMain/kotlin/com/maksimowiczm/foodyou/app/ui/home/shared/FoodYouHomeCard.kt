package com.maksimowiczm.foodyou.app.ui.home.shared

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

@Composable
internal fun FoodYouHomeCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    content: @Composable () -> Unit,
) {
    Surface(modifier = modifier.uiName("FoodYouHomeCard.Surface", "androidx.compose.material3.Surface", "com.maksimowiczm.foodyou.app.ui.home.shared.FoodYouHomeCard", "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/shared/FoodYouHomeCard.kt"), color = color, shape = MaterialTheme.shapes.medium) { content() }
}

@Composable
internal fun FoodYouHomeCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = FoodYouHomeCardDefaults.color,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Surface(modifier = (modifier).uiName("FoodYouHomeCard.Surface.2", "androidx.compose.material3.Surface", uiInspectionSource, "FoodYouHomeCard"), color = color, shape = MaterialTheme.shapes.medium) {
        Box(modifier = Modifier.uiName("FoodYouHomeCard.clickTarget", "androidx.compose.foundation.layout.Box", "com.maksimowiczm.foodyou.app.ui.home.shared.FoodYouHomeCard", "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/shared/FoodYouHomeCard.kt").combinedClickable(onLongClick = onLongClick, onClick = onClick)) {
            content()
        }
    }
}

internal object FoodYouHomeCardDefaults {

    val color: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.home.shared",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/shared/FoodYouHomeCard.kt",
)
