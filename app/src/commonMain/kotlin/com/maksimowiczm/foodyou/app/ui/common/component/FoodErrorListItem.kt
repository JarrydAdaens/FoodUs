package com.maksimowiczm.foodyou.app.ui.common.component

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun FoodErrorListItem(
    headline: String,
    errorMessage: String,
    modifier: Modifier = Modifier.Companion,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    shape: Shape = RectangleShape,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
) {
    val content =
        @Composable {
            Row(
                modifier = (Modifier.padding(contentPadding)).uiName("FoodErrorListItem.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "FoodErrorListItem"),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = (Modifier.weight(1f)).uiName("FoodErrorListItem.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "FoodErrorListItem"),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.titleMediumEmphasized
                    ) {
                        Text(headline, modifier = Modifier.uiName("FoodErrorListItem.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "FoodErrorListItem"))
                    }
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.bodyMedium
                    ) {
                        Text(errorMessage, modifier = Modifier.uiName("FoodErrorListItem.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "FoodErrorListItem"))
                    }
                }

                trailingContent?.invoke()
            }
        }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = (modifier).uiName("FoodErrorListItem.Surface.1", "androidx.compose.material3.Surface", uiInspectionSource, "FoodErrorListItem"),
            shape = shape,
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            content = content,
        )
    } else {
        Surface(
            modifier = (modifier).uiName("FoodErrorListItem.Surface.2", "androidx.compose.material3.Surface", uiInspectionSource, "FoodErrorListItem"),
            shape = shape,
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            content = content,
        )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.common.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/FoodErrorListItem.kt",
)
