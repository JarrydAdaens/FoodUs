package com.maksimowiczm.foodyou.app.ui.common.component

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun PrivacyPolicyChip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    AssistChip(
        onClick = onClick,
        modifier = (modifier).uiName("PrivacyPolicyChip.AssistChip.1", "androidx.compose.material3.AssistChip", uiInspectionSource, "PrivacyPolicyChip"),
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.PrivacyTip,
                contentDescription = null,
                modifier = (Modifier.size(AssistChipDefaults.IconSize)).uiName("PrivacyPolicyChip.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "PrivacyPolicyChip"),
            )
        },
        label = { Text(stringResource(Res.string.headline_privacy_policy), modifier = Modifier.uiName("PrivacyPolicyChip.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "PrivacyPolicyChip")) },
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.common.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/PrivacyPolicyChip.kt",
)
