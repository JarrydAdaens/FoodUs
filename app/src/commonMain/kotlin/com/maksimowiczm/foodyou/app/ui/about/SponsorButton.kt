package com.maksimowiczm.foodyou.app.ui.about

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SponsorButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = (modifier.height(96.dp)).uiName("SponsorButton.Button.1", "androidx.compose.material3.Button", uiInspectionSource, "SponsorButton"),
        shapes = ButtonDefaults.shapes(),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        contentPadding = PaddingValues(horizontal = 48.dp),
    ) {
        Row(
            modifier = (Modifier.fillMaxWidth()).uiName("SponsorButton.Row.1", "androidx.compose.foundation.layout.Row", uiInspectionSource, "SponsorButton"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.VolunteerActivism,
                contentDescription = null,
                modifier = (Modifier.size(32.dp)).uiName("SponsorButton.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "SponsorButton"),
            )
            Column (modifier = Modifier.uiName("SponsorButton.Column.1", "androidx.compose.foundation.layout.Column", uiInspectionSource, "SponsorButton")) {
                Text(
                    text = stringResource(Res.string.headline_sponsor),
                    style = MaterialTheme.typography.headlineSmall, modifier = Modifier.uiName("SponsorButton.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "SponsorButton")
                )
                Text(
                    text = stringResource(Res.string.description_sponsor_short_2),
                    style = MaterialTheme.typography.bodyLarge, modifier = Modifier.uiName("SponsorButton.Text.2", "androidx.compose.material3.Text", uiInspectionSource, "SponsorButton")
                )
            }
        }
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.about",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/about/SponsorButton.kt",
)
