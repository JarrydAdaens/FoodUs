package com.maksimowiczm.foodyou.app.ui.developer.inspection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Session-local registry. Pointer hit paths, rather than rectangle sorting, choose the top target. */
internal class UiInspectorState(private val scope: CoroutineScope) {
    internal class Target(val descriptor: UiSourceDescriptor, val window: Any) {
        var coordinates: LayoutCoordinates? = null
        var enabled: Boolean? = null
        var logicalAncestors: List<UiSourceDescriptor> = emptyList()
    }

    private val targets = mutableListOf<Target>()
    private var hold: Job? = null
    private var highlight: Job? = null
    private var downTime: Long? = null
    private var candidate: Target? = null
    private var candidatePoint: Offset? = null
    private val hitPath = mutableListOf<Target>()
    // Changes are filtered to each modifier's hit path. Merge observations across those paths so
    // a second finger on a different sibling cannot restart a supposedly single-finger hold.
    private val pressedPointers = mutableMapOf<PointerId, MutableSet<Target>>()
    private var gestureWindow: Any? = null
    private var gestureBlocked = false
    var captured by mutableStateOf(false)
        private set
    var selectedTarget by mutableStateOf<Target?>(null)
        private set
    var snapshot by mutableStateOf<UiSelectionSnapshot?>(null)
        private set

    fun register(target: Target) { targets.add(target) }

    fun unregister(target: Target) {
        targets.remove(target)
        forgetPointerTarget(target)
        if (selectedTarget === target) selectedTarget = null
        // Keep only the structural snapshot so modal selections can be copied after dismissal.
    }

    /** Called in Initial pass: ancestors first, then the deepest target on Compose's hit path. */
    fun down(target: Target, pointer: PointerId, uptimeMillis: Long, positionInWindow: Offset) {
        if (gestureWindow !== target.window) resetGesture()
        gestureWindow = target.window
        pressedPointers.getOrPut(pointer) { mutableSetOf() }.add(target)
        if (pressedPointers.size > 1) {
            gestureBlocked = true
            cancelHold()
        }
        if (gestureBlocked) return
        if (downTime != uptimeMillis) {
            cancelHold()
            downTime = uptimeMillis
            hold = scope.launch {
                delay(HOLD_MILLIS)
                val target = candidate ?: return@launch
                val capturedSnapshot = capture(target) ?: return@launch
                selectedTarget = target
                snapshot = capturedSnapshot
                captured = true
                highlight?.cancel()
                highlight = scope.launch {
                    delay(HIGHLIGHT_MILLIS)
                    if (selectedTarget === target) selectedTarget = null
                }
            }
        }
        candidate = target
        candidatePoint = positionInWindow
        if (target !in hitPath) hitPath.add(target)
    }

    fun cancelHold() {
        hold?.cancel()
        hold = null
        downTime = null
        candidate = null
        candidatePoint = null
        hitPath.clear()
        captured = false
    }

    fun cancelTargetHold(target: Target) {
        if (candidate === target) cancelHold()
    }

    fun observePointers(target: Target, changes: List<PointerInputChange>) {
        if (gestureWindow !== target.window) return
        changes.forEach { change ->
            if (change.pressed) pressedPointers.getOrPut(change.id) { mutableSetOf() }.add(target)
            else pressedPointers.remove(change.id)
        }
        if (pressedPointers.size > 1) {
            gestureBlocked = true
            cancelHold()
        }
        if (pressedPointers.isEmpty()) resetGesture()
    }

    fun forgetPointerTarget(target: Target) {
        cancelTargetHold(target)
        pressedPointers.values.forEach { it.remove(target) }
        pressedPointers.entries.removeAll { it.value.isEmpty() }
        if (pressedPointers.isEmpty()) resetGesture()
    }

    fun cancelWindowHold(target: Target) {
        if (gestureWindow === target.window) resetGesture()
    }

    private fun resetGesture() {
        cancelHold()
        pressedPointers.clear()
        gestureWindow = null
        gestureBlocked = false
    }

    fun clear() {
        resetGesture()
        highlight?.cancel()
        highlight = null
        targets.clear()
        selectedTarget = null
        snapshot = null
    }

    private fun capture(target: Target): UiSelectionSnapshot? {
        val coordinates = target.coordinates?.takeIf { it.isAttached } ?: return null
        val bounds = coordinates.boundsInWindow()
        if (target !in targets || bounds.isEmpty || candidatePoint?.let(bounds::contains) != true) return null
        // The observed modifier hit path preserves caller and shared-component descriptors even
        // when several named modifiers occupy one LayoutCoordinates instance.
        val ancestors = hitPath.takeWhile { it !== target }.filter {
            it in targets && it.coordinates?.isAttached == true
        }
        val screen = target.logicalAncestors.lastOrNull { it.composable.substringAfterLast('.').endsWith("Screen") }
            ?: target.logicalAncestors.firstOrNull() ?: ancestors.firstOrNull()?.descriptor ?: target.descriptor
        fun isSurface(descriptor: UiSourceDescriptor): Boolean {
            val name = descriptor.composable.substringAfterLast('.')
            return name.contains("Dialog") || name.contains("Popup") || name.contains("Sheet") ||
                name.contains("Menu") || name == "TooltipBox" || name == "ExpandedFullScreenSearchBar"
        }
        val surface = target.logicalAncestors.lastOrNull(::isSurface) ?: ancestors.lastOrNull {
            isSurface(it.descriptor)
        }?.descriptor ?: ancestors.firstOrNull()?.descriptor ?: target.descriptor
        return UiSelectionSnapshot(
            screen = screen,
            surface = surface,
            ancestors = target.logicalAncestors.map { UiElementSnapshot(it, 1) } + ancestors.map(::snapshotElement),
            selected = snapshotElement(target),
        )
    }

    private fun sourceAncestors(coordinates: LayoutCoordinates): List<Target> {
        val ancestors = mutableListOf<Target>()
        var parent = coordinates.parentLayoutCoordinates
        while (parent != null) {
            val current = parent
            targets.lastOrNull { it.coordinates === current }?.let(ancestors::add)
            parent = current.parentLayoutCoordinates
        }
        return ancestors.asReversed()
    }

    private fun snapshotElement(target: Target): UiElementSnapshot {
        val coordinates = target.coordinates!!
        val sourceParent = sourceAncestors(coordinates).lastOrNull()
        fun rootOf(coordinates: LayoutCoordinates): LayoutCoordinates {
            var root = coordinates
            while (root.parentLayoutCoordinates != null) root = root.parentLayoutCoordinates!!
            return root
        }
        val root = rootOf(coordinates)
        val siblings = targets.filter {
            val sibling = it.coordinates
            it.descriptor.name == target.descriptor.name && sibling?.isAttached == true &&
                rootOf(sibling) === root && sourceAncestors(sibling).lastOrNull() === sourceParent
        }.sortedWith(compareBy({ it.coordinates!!.boundsInWindow().top }, { it.coordinates!!.boundsInWindow().left }))
        val bounds = coordinates.boundsInWindow()
        return UiElementSnapshot(
            descriptor = descriptorWithCallSite(target),
            occurrence = siblings.indexOf(target) + 1,
            enabled = target.enabled,
            bounds = UiInspectionBounds(bounds.left, bounds.top, bounds.right, bounds.bottom),
        )
    }

    private fun descriptorWithCallSite(target: Target): UiSourceDescriptor {
        val caller = hitPath.takeWhile { it !== target }.lastOrNull {
            it.descriptor.composable == target.descriptor.owner &&
                it.descriptor.owner != target.descriptor.owner
        } ?: return target.descriptor
        return target.descriptor.copy(
            callSiteOwner = caller.descriptor.owner,
            callSiteSource = caller.descriptor.source,
        )
    }

    companion object {
        const val HOLD_MILLIS = 3_000L
        private const val HIGHLIGHT_MILLIS = 1_500L
    }
}
