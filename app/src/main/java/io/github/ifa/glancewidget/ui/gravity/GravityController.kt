package io.github.ifa.glancewidget.ui.gravity

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class GravityPhase {
    Idle,
    Absorbing,
    Emerging
}

class GravityItemState {
    var center: Offset = Offset.Zero
    val translationX = Animatable(0f)
    val translationY = Animatable(0f)
    val scale = Animatable(1f)
    val alpha = Animatable(1f)

    suspend fun animateAbsorb(anchor: Offset, delayMs: Int) {
        if (delayMs > 0) delay(delayMs.toLong())
        val targetTx = anchor.x - center.x
        val targetTy = anchor.y - center.y
        val spec = tween<Float>(durationMillis = AbsorbDurationMs, easing = AbsorbEasing)

        coroutineScope {
            launch { translationX.animateTo(targetTx, spec) }
            launch { translationY.animateTo(targetTy, spec) }
            launch { scale.animateTo(AbsorbScale, spec) }
            launch { alpha.animateTo(AbsorbAlpha, spec) }
        }
    }

    suspend fun animateEmerge(anchor: Offset, delayMs: Int) {
        translationX.snapTo(anchor.x - center.x)
        translationY.snapTo(anchor.y - center.y)
        scale.snapTo(AbsorbScale)
        alpha.snapTo(AbsorbAlpha)

        if (delayMs > 0) delay(delayMs.toLong())
        val spec = tween<Float>(durationMillis = EmergeDurationMs, easing = EmergeEasing)

        coroutineScope {
            launch { translationX.animateTo(0f, spec) }
            launch { translationY.animateTo(0f, spec) }
            launch { scale.animateTo(1f, spec) }
            launch { alpha.animateTo(1f, spec) }
        }
    }

    suspend fun animateCrossfadeAbsorb() {
        val spec = tween<Float>(durationMillis = ReduceMotionCrossfadeMs)
        alpha.animateTo(0f, spec)
    }

    suspend fun animateCrossfadeEmerge() {
        translationX.snapTo(0f)
        translationY.snapTo(0f)
        scale.snapTo(1f)
        alpha.snapTo(0f)
        val spec = tween<Float>(durationMillis = ReduceMotionCrossfadeMs)
        alpha.animateTo(1f, spec)
    }

    suspend fun reset() {
        translationX.snapTo(0f)
        translationY.snapTo(0f)
        scale.snapTo(1f)
        alpha.snapTo(1f)
    }
}

class GravityController(
    private val scope: CoroutineScope,
    private val pillBoundsProvider: () -> Rect,
    private val reduceMotionProvider: () -> Boolean
) {
    private val _phase = mutableStateOf(GravityPhase.Idle)
    val phase: State<GravityPhase> = _phase

    val pillScale = Animatable(1f)

    private val items = mutableMapOf<Any, GravityItemState>()

    fun register(id: Any, state: GravityItemState) {
        items[id] = state
    }

    fun unregister(id: Any) {
        items.remove(id)
    }

    fun requestNavigate(action: () -> Unit) {
        if (_phase.value != GravityPhase.Idle) return
        val reduceMotion = reduceMotionProvider()
        val pillCenter = pillBoundsProvider().center

        scope.launch {
            if (reduceMotion) {
                _phase.value = GravityPhase.Absorbing
                val activeItems = items.values.toList()
                val absorbJobs = activeItems.map { item ->
                    async { item.animateCrossfadeAbsorb() }
                }
                absorbJobs.awaitAll()

                action()

                _phase.value = GravityPhase.Emerging
                delay(32)
                val newActiveItems = items.values.toList()
                val emergeJobs = newActiveItems.map { item ->
                    async { item.animateCrossfadeEmerge() }
                }
                emergeJobs.awaitAll()
                _phase.value = GravityPhase.Idle
            } else {
                _phase.value = GravityPhase.Absorbing

                val activeItems = items.values.toList()
                val centers = activeItems.map { it.center }
                val order = gravityOrder(centers, pillCenter)
                val delays = gravityDelays(order.size, StaggerMs, CapMs)

                val absorbJobs = order.mapIndexed { rank, itemIndex ->
                    val item = activeItems[itemIndex]
                    val staggerDelay = delays[rank]
                    async {
                        item.animateAbsorb(
                            anchor = pillCenter,
                            delayMs = staggerDelay
                        )
                    }
                }
                absorbJobs.awaitAll()

                pillScale.animateTo(
                    targetValue = PillPulseScale,
                    animationSpec = tween(PulseDurationMs / 2, easing = FastOutSlowInEasing)
                )
                pillScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(PulseDurationMs / 2, easing = FastOutSlowInEasing)
                )

                action()

                _phase.value = GravityPhase.Emerging
                delay(40)

                val newActiveItems = items.values.toList()
                val newCenters = newActiveItems.map { it.center }
                val newOrder = gravityOrder(newCenters, pillCenter)
                val newDelays = gravityDelays(newOrder.size, StaggerMs, CapMs)

                val emergeJobs = newOrder.mapIndexed { rank, itemIndex ->
                    val item = newActiveItems[itemIndex]
                    val staggerDelay = newDelays[rank]
                    async {
                        item.animateEmerge(
                            anchor = pillCenter,
                            delayMs = staggerDelay
                        )
                    }
                }
                emergeJobs.awaitAll()
                _phase.value = GravityPhase.Idle
            }
        }
    }
}
