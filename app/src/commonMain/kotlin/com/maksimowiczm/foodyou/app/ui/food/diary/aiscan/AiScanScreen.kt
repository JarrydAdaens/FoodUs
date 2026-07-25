package com.maksimowiczm.foodyou.app.ui.food.diary.aiscan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.maksimowiczm.foodyou.app.ui.common.component.ArrowBackIconButton
import foodyou.app.generated.resources.*
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

/**
 * Placeholder entry point for the AI scanning flow (Milestone 2, Story 6). This story only wires the
 * per-meal robot button to a reachable destination; Story 6 replaces this stub with the real capture
 * and Ask-AI experience. [date] and [mealId] identify the diary meal the scan will log into.
 */
@Composable
fun AiScanScreen(
    date: LocalDate,
    mealId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_ai_scanning)) },
                navigationIcon = { ArrowBackIconButton(onBack) },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(Res.string.headline_ai_scanning))
        }
    }
}
