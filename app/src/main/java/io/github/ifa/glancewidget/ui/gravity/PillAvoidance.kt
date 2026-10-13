package io.github.ifa.glancewidget.ui.gravity

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

class PillAvoidanceElement : ModifierNodeElement<PillAvoidanceNode>() {
    override fun create(): PillAvoidanceNode = PillAvoidanceNode()

    override fun update(node: PillAvoidanceNode) {}

    override fun hashCode(): Int = javaClass.hashCode()

    override fun equals(other: Any?): Boolean = other is PillAvoidanceElement
}

class PillAvoidanceNode :
    Modifier.Node(),
    LayoutModifierNode,
    GlobalPositionAwareModifierNode,
    CompositionLocalConsumerModifierNode {

    private var targetInset: Float = 0f
    private var appliedInset: Float = 0f
    private var animJob: Job? = null
    private val animInset = Animatable(0f)

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        if (!isAttached || !coordinates.isAttached) return
        val field = currentValueOf(LocalGravityField)
        val pill = field.pillBounds.value
        val density = currentValueOf(LocalDensity)
        val thresholdPx = with(density) { 0.5.dp.toPx() }

        if (pill.isEmpty) {
            if (targetInset != 0f) {
                targetInset = 0f
                updateInset(0f, field.reduceMotion, thresholdPx)
            }
            return
        }

        val bandPaddingPx = with(density) { ExclusionBandPadding.toPx() }
        val leadPx = with(density) { AvoidanceLead.toPx() }
        val gapPx = with(density) { AvoidanceGap.toPx() }

        val exclusionTop = pill.top - bandPaddingPx
        val exclusionBottom = pill.bottom + bandPaddingPx

        val rowBounds = coordinates.boundsInRoot()
        val distance = when {
            rowBounds.bottom < exclusionTop -> exclusionTop - rowBounds.bottom
            rowBounds.top > exclusionBottom -> rowBounds.top - exclusionBottom
            else -> 0f
        }

        val fraction = avoidanceFraction(distance = distance, lead = leadPx)
        val newTargetInset = avoidanceInset(
            rowEnd = rowBounds.right,
            pillStart = pill.left,
            gap = gapPx,
            fraction = fraction
        )

        if (abs(newTargetInset - targetInset) >= thresholdPx) {
            targetInset = newTargetInset
            updateInset(newTargetInset, field.reduceMotion, thresholdPx)
        }
    }

    private fun updateInset(newTarget: Float, reduceMotion: Boolean, thresholdPx: Float) {
        if (!isAttached) return
        if (reduceMotion) {
            animJob?.cancel()
            if (abs(newTarget - appliedInset) >= thresholdPx || (newTarget == 0f && appliedInset != 0f)) {
                appliedInset = newTarget
                if (isAttached) {
                    invalidateMeasurement()
                }
            }
            return
        }

        animJob?.cancel()
        animJob = coroutineScope.launch {
            animInset.snapTo(appliedInset)
            animInset.animateTo(
                targetValue = newTarget,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) {
                if (!isAttached) return@animateTo
                if (abs(value - appliedInset) >= thresholdPx) {
                    appliedInset = value
                    invalidateMeasurement()
                }
            }
            if (!isAttached) return@launch
            if (appliedInset != animInset.value) {
                appliedInset = animInset.value
                invalidateMeasurement()
            }
        }
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val insetPx = appliedInset.roundToInt().coerceAtLeast(0)
        val childConstraints = if (constraints.hasBoundedWidth && insetPx > 0) {
            val newMaxWidth = (constraints.maxWidth - insetPx).coerceAtLeast(0)
            val newMinWidth = constraints.minWidth.coerceAtMost(newMaxWidth)
            constraints.copy(minWidth = newMinWidth, maxWidth = newMaxWidth)
        } else {
            constraints
        }
        val placeable = measurable.measure(childConstraints)
        val totalWidth = if (constraints.hasBoundedWidth && insetPx > 0) {
            constraints.constrainWidth(placeable.width + insetPx)
        } else {
            placeable.width
        }
        return layout(totalWidth, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }

    override fun onDetach() {
        super.onDetach()
        animJob?.cancel()
        appliedInset = 0f
        targetInset = 0f
    }
}

fun Modifier.pillAvoidance(): Modifier = this.then(PillAvoidanceElement())
