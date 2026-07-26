package com.maksimowiczm.foodyou.app.ui.home.goals

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.min

/** A single macronutrient slice of the macro breakdown pie: its consumed amount and color. */
@Immutable data class MacroPieSlice(val value: Int, val color: Color)

/**
 * Converts the consumed macronutrient amounts into slice sweep angles in degrees.
 *
 * Each slice takes a share of the full 360 degrees proportional to its value. When nothing has been
 * logged (total is zero or negative) every slice is empty, which the chart renders as an empty
 * track.
 */
fun macroPieSweepDegrees(values: List<Int>): List<Float> {
    val total = values.sumOf { it.coerceAtLeast(0) }
    if (total <= 0) return List(values.size) { 0f }
    return values.map { it.coerceAtLeast(0).toFloat() / total * 360f }
}

/**
 * Macro breakdown pie chart. Presents the same proteins/carbohydrates/fats data as [MacroBar] using
 * the same nutrient colors, sliced proportionally by consumed amount.
 */
@Composable
internal fun MacroPieChart(
    slices: List<MacroPieSlice>,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
    val sweeps = macroPieSweepDegrees(slices.map { it.value })
    val animatedSweeps =
        sweeps.map {
            animateFloatAsState(
                    targetValue = it,
                    animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                )
                .value
        }

    Canvas(modifier = modifier.size(64.dp)) {
        val diameter = min(size.width, size.height)
        val topLeft =
            Offset(x = (size.width - diameter) / 2f, y = (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)

        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = true,
            topLeft = topLeft,
            size = arcSize,
        )

        var startAngle = -90f
        slices.zip(animatedSweeps).forEach { (slice, sweep) ->
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = topLeft,
                size = arcSize,
            )
            startAngle += sweep
        }
    }
}
