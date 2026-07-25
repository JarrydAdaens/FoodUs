package com.maksimowiczm.foodyou.app.ui.food.diary.fasttext

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
 * Placeholder entry point for the fast-text placeholder flow (Milestone 2, Story 8). This story only
 * wires the per-meal pencil button to a reachable destination; Story 8 replaces this stub with the
 * name-and-description quick capture. [date] and [mealId] identify the diary meal the entry logs into.
 */
@Composable
fun FastTextScreen(
    date: LocalDate,
    mealId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.headline_fast_text)) },
                navigationIcon = { ArrowBackIconButton(onBack) },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(Res.string.headline_fast_text))
        }
    }
}
