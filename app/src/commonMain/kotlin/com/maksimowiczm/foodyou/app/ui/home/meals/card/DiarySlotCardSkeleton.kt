package com.maksimowiczm.foodyou.app.ui.home.meals.card

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.app.ui.home.shared.FoodYouHomeCard
import com.maksimowiczm.foodyou.common.compose.extension.toDp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.shimmer

@Composable
internal fun DiarySlotCardSkeleton(shimmer: Shimmer, modifier: Modifier = Modifier) {
    FoodYouHomeCard((modifier).uiName("DiarySlotCardSkeleton.FoodYouHomeCard.1", "com.maksimowiczm.foodyou.app.ui.home.shared.FoodYouHomeCard", uiInspectionSource, "DiarySlotCardSkeleton")) {
        Column((Modifier.padding(16.dp)).uiName("DiarySlotCardSkeleton.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "DiarySlotCardSkeleton")) {
            Box(
                (Modifier.shimmer(shimmer)
                    .size(140.dp, MaterialTheme.typography.headlineMedium.toDp() - 4.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)).uiName("DiarySlotCardSkeleton.Box.1", "androidx.compose.foundation.layout.Box", uiInspectionSource, "DiarySlotCardSkeleton")
            )

            Spacer(Modifier.height(4.dp))

            Box(
                (Modifier.shimmer(shimmer)
                    .size(60.dp, MaterialTheme.typography.labelLarge.toDp())
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)).uiName("DiarySlotCardSkeleton.Box.2", "androidx.compose.foundation.layout.Box", uiInspectionSource, "DiarySlotCardSkeleton")
            )

            Spacer(Modifier.height(8.dp))

            Box(
                (Modifier.shimmer(shimmer)
                    .size(120.dp, MaterialTheme.typography.labelMedium.toDp() * 2)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)).uiName("DiarySlotCardSkeleton.Box.3", "androidx.compose.foundation.layout.Box", uiInspectionSource, "DiarySlotCardSkeleton")
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = (Modifier.fillMaxWidth()).uiName("DiarySlotCardSkeleton.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "DiarySlotCardSkeleton"),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(4) {
                    FilledIconButton(
                        onClick = {},
                        modifier = (Modifier.shimmer(shimmer)).uiName("DiarySlotCardSkeleton.FilledIconButton.1", "androidx.compose.material3.FilledIconButton", uiInspectionSource, "DiarySlotCardSkeleton"),
                        colors =
                            IconButtonDefaults.filledIconButtonColors(
                                disabledContainerColor =
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                            ),
                        shape = MaterialTheme.shapes.medium,
                        enabled = false,
                        content = {},
                    )
                }
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.home.meals.card",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/home/meals/card/DiarySlotCardSkeleton.kt",
)
