package com.maksimowiczm.foodyou.app.ui.common.utility

import androidx.compose.runtime.*
import com.maksimowiczm.foodyou.settings.domain.entity.GraphStyle

val LocalGraphStyle = staticCompositionLocalOf { GraphStyle.DEFAULT }

@Composable
fun GraphStyleProvider(graphStyle: GraphStyle, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalGraphStyle provides graphStyle) { content() }
}
