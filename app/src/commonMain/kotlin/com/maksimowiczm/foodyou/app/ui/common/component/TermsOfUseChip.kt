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

@Composable
fun TermsOfUseChip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    AssistChip(
        onClick = onClick,
        modifier = (modifier).uiName("TermsOfUseChip.AssistChip.1", "androidx.compose.material3.AssistChip", uiInspectionSource, "TermsOfUseChip"),
        leadingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                modifier = (Modifier.size(AssistChipDefaults.IconSize)).uiName("TermsOfUseChip.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "TermsOfUseChip"),
            )
        },
        label = { Text(stringResource(Res.string.headline_terms_of_use), modifier = Modifier.uiName("TermsOfUseChip.Text.1", "androidx.compose.material3.Text", uiInspectionSource, "TermsOfUseChip")) },
    )
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.common.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/TermsOfUseChip.kt",
)
