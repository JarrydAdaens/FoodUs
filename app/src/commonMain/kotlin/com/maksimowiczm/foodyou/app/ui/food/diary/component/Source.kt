package com.maksimowiczm.foodyou.app.ui.food.diary.component


import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.food.component.Icon
import com.maksimowiczm.foodyou.app.ui.food.component.stringResource
import com.maksimowiczm.foodyou.common.compose.utility.LocalClipboardManager
import com.maksimowiczm.foodyou.common.domain.food.ProductSource
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun Source(source: ProductSource, modifier: Modifier = Modifier) {
    val url = source.url
    val clipboardManger = LocalClipboardManager.current
    val sourceStr = stringResource(Res.string.headline_source)

    Row(
        modifier =
            (modifier.clickable(
                interactionSource = null,
                indication = null,
                onClick = {
                    if (url != null) {
                        clipboardManger.copy(label = sourceStr, text = url)
                    }
                },
            )).uiName("Source.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "Source"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        source.type.Icon( modifier = Modifier.uiName("Source.Icon.1", "com.maksimowiczm.foodyou.app.ui.food.component.Icon", uiInspectionSource, "Source"))

        Column (modifier = Modifier.uiName("Source.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "Source")) {
            Text(text = source.type.stringResource(), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.uiName("Source.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "Source"))
            if (url != null) {
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.uiName("Source.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "Source")
                )
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.diary.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/component/Source.kt",
)
