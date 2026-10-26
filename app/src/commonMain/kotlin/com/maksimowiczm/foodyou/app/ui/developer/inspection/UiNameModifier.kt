package com.maksimowiczm.foodyou.app.ui.developer.inspection

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalWindowInfo

internal val LocalUiInspector = staticCompositionLocalOf<UiInspectorState?> { null }
internal val LocalInspectionLongPressThreshold = staticCompositionLocalOf { 500L }

internal fun Modifier.uiName(
    name: String,
    composable: String,
    owner: String,
    source: String,
    enabled: Boolean? = null,
): Modifier = uiName(UiSourceDescriptor(name, composable, owner, source), enabled)

/** Observe a declared source target without changing accessibility semantics or disabled state. */
internal fun Modifier.uiName(
    descriptor: UiSourceDescriptor,
    enabled: Boolean? = null,
): Modifier = composed {
    val inspector = LocalUiInspector.current
    if (inspector == null) return@composed this
    val window = LocalWindowInfo.current
    val target = remember(inspector, descriptor, window) { UiInspectorState.Target(descriptor, window) }
    val logicalAncestors = LocalUiSourceAncestry.current
    val ordinaryLongPressThreshold = LocalInspectionLongPressThreshold.current
    LaunchedEffect(window.isWindowFocused) {
        if (!window.isWindowFocused) inspector.cancelWindowHold(target)
    }
    DisposableEffect(inspector, target) {
        inspector.register(target)
        onDispose { inspector.unregister(target) }
    }
    SideEffect {
        target.enabled = enabled
        target.logicalAncestors = logicalAncestors
    }
    onGloballyPositioned { target.coordinates = it }
        .drawWithContent {
            drawContent()
            if (inspector.selectedTarget === target) {
                drawRect(Color.Magenta, style = Stroke(2f * density))
            }
        }
        .pointerInput(inspector, target, ordinaryLongPressThreshold) {
            try {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    val position = target.coordinates?.takeIf { it.isAttached }?.localToWindow(down.position)
                        ?: return@awaitEachGesture
                    inspector.down(target, down.id, down.uptimeMillis, position)
                    var pressed = true
                    var moved = false
                    var multiplePointers = false
                    while (pressed) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id }
                        val inspected = inspector.captured
                        inspector.observePointers(target, event.changes)
                        val primaryPressed = change?.pressed == true
                        // Continue forwarding the remaining fingers' releases. awaitEachGesture's
                        // implicit all-up wait cannot update this inspector's cross-target session.
                        pressed = event.changes.any { it.pressed }
                        moved = moved || (change != null &&
                            (change.position - down.position).getDistance() > viewConfiguration.touchSlop)
                        multiplePointers = multiplePointers || event.changes.count { it.pressed } > 1
                        val abandonedHold = !primaryPressed && !moved && !multiplePointers && change != null &&
                            change.uptimeMillis - down.uptimeMillis >= ordinaryLongPressThreshold
                        if (inspected || abandonedHold) event.changes.forEach { it.consume() }
                        if (multiplePointers || change == null || moved || !primaryPressed
                        ) {
                            inspector.cancelTargetHold(target)
                        }
                    }
                }
            } finally {
                inspector.forgetPointerTarget(target)
            }
        }
}
