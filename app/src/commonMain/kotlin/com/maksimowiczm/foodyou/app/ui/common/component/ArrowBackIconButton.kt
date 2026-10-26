package com.maksimowiczm.foodyou.app.ui.common.component

import com.maksimowiczm.foodyou.app.ui.developer.inspection.UiSourceFile
import com.maksimowiczm.foodyou.app.ui.developer.inspection.uiName

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import foodyou.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArrowBackIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = IconButtonDefaults.standardShape,
) {
    IconButton(
        onClick = onClick,
        modifier = (modifier).uiName("ArrowBackIconButton.IconButton.1", "androidx.compose.material3.IconButton", uiInspectionSource, "ArrowBackIconButton"),
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        shape = shape,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(Res.string.action_go_back), modifier = Modifier.uiName("ArrowBackIconButton.Icon.1", "androidx.compose.material3.Icon", uiInspectionSource, "ArrowBackIconButton")
        )
    }
}


private val uiInspectionSource = UiSourceFile(
    "com.maksimowiczm.foodyou.app.ui.common.component",
    "app/src/commonMain/kotlin/com/maksimowiczm/foodyou/app/ui/common/component/ArrowBackIconButton.kt",
)
