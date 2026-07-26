package com.maksimowiczm.foodyou.app.ui.common.utility

import androidx.compose.runtime.*
import com.maksimowiczm.foodyou.settings.domain.entity.WeekLayout

val LocalWeekLayout = staticCompositionLocalOf { WeekLayout.DEFAULT }

@Composable
fun WeekLayoutProvider(weekLayout: WeekLayout, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalWeekLayout provides weekLayout) { content() }
}
