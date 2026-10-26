package com.maksimowiczm.foodyou.app.ui.food.diary.add

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.common.compose.component.unorderedList
import com.maksimowiczm.foodyou.common.compose.utility.LocalDateFormatter
import com.maksimowiczm.foodyou.food.domain.entity.NutritionalItemHistory
import foodyou.app.generated.resources.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NutritionalItemHistory(events: List<NutritionalItemHistory>, modifier: Modifier = Modifier.Companion) {
    val dateFormatter = LocalDateFormatter.current

    val dates =
        remember(events) {
            events.map { it.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date }
        }
    val strings =
        events.zip(dates) { event, date ->
            event.stringResource() + ", " + dateFormatter.formatDate(date)
        }
    val list = unorderedList(strings)

    Column((modifier).uiName("NutritionalItemHistory.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "NutritionalItemHistory")) {
        Text(
            text = stringResource(Res.string.headline_history),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary, modifier = Modifier.uiName("NutritionalItemHistory.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "NutritionalItemHistory")
        )
        Spacer(Modifier.Companion.height(8.dp))
        Text(
            text = list,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.uiName("NutritionalItemHistory.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "NutritionalItemHistory")
        )
    }
}

@Composable
private fun NutritionalItemHistory.stringResource(): String =
    when (this) {
        is NutritionalItemHistory.Created -> stringResource(Res.string.headline_created)
        is NutritionalItemHistory.Downloaded -> stringResource(Res.string.headline_downloaded)
        is NutritionalItemHistory.Imported -> stringResource(Res.string.headline_imported)
        is NutritionalItemHistory.Edited -> stringResource(Res.string.headline_edited)
        is NutritionalItemHistory.ImportedFromFoodYou2 ->
            stringResource(Res.string.headline_imported_from_food_you_2)
    }


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.food.diary.add",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/food/diary/add/FoodEvents.kt",
)
