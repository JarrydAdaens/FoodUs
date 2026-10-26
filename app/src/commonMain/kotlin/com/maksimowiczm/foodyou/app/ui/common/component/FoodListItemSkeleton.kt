package com.maksimowiczm.foodyou.app.ui.common.component

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.common.compose.extension.toDp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.shimmer

@Composable
fun FoodListItemSkeleton(
    shimmer: Shimmer,
    modifier: Modifier = Modifier.Companion,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    ListItem(
        headlineContent = {
            Column (modifier = Modifier.uiName("FoodListItemSkeleton.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "FoodListItemSkeleton")) {
                Spacer(Modifier.height(2.dp))
                Spacer(
                    Modifier.Companion.shimmer(shimmer)
                        .height(LocalTextStyle.current.toDp() - 4.dp)
                        .width(200.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
                Spacer(Modifier.height(2.dp))
            }
        },
        modifier = (modifier).uiName("FoodListItemSkeleton.ListItem.1", "androidx.compose.material3.ListItem", uiInspectionSource, "FoodListItemSkeleton"),
        overlineContent = {
            Spacer(
                Modifier.Companion.shimmer(shimmer)
                    .height(LocalTextStyle.current.toDp())
                    .width(100.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            )
        },
        supportingContent = {
            Row(
                modifier = (Modifier.fillMaxWidth()).uiName("FoodListItemSkeleton.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "FoodListItemSkeleton"),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Spacer(
                    Modifier.Companion.shimmer(shimmer)
                        .height(LocalTextStyle.current.toDp())
                        .width(125.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
                Spacer(
                    Modifier.Companion.shimmer(shimmer)
                        .height(LocalTextStyle.current.toDp())
                        .width(75.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
            }
        },
        trailingContent = trailingContent,
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.common.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/FoodListItemSkeleton.kt",
)
