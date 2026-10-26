package com.maksimowiczm.foodyou.app.ui.common.component

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Outbound link to a food-data provider's authoritative website (Milestone 2, Story 23). Mirrors the
 * [TermsOfUseChip] / [PrivacyPolicyChip] affordance so every provider surface opens its website the
 * same way.
 */
@Composable
fun WebsiteChip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    AssistChip(
        onClick = onClick,
        modifier = (modifier).uiName("WebsiteChip.AssistChip.1", "androidx.compose.material3.AssistChip", uiInspectionSource, "WebsiteChip"),
        leadingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                modifier = (Modifier.size(AssistChipDefaults.IconSize)).uiName("WebsiteChip.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "WebsiteChip"),
            )
        },
        label = { Text(stringResource(Res.string.action_visit_website), modifier = Modifier.uiName("WebsiteChip.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "WebsiteChip")) },
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.common.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/WebsiteChip.kt",
)
