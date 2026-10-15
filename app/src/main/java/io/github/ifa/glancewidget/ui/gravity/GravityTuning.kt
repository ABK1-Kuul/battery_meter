package io.github.ifa.glancewidget.ui.gravity

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

const val AbsorbDurationMs = 356
const val EmergeDurationMs = 436
const val StaggerMs = 40
const val CapMs = 300
const val PulseDurationMs = 260
const val PillPulseScale = 1.07f
const val AbsorbScale = 0.15f
const val AbsorbAlpha = 0f
const val ReduceMotionCrossfadeMs = 120

val AbsorbEasing = CubicBezierEasing(0.55f, 0f, 0.95f, 0.5f)
val EmergeEasing = CubicBezierEasing(0.2f, 1.27f, 0.35f, 1f)

val ExclusionBandPadding = 14.dp
val AvoidanceLead = 40.dp
val AvoidanceGap = 10.dp
const val MinInsetThreshold = 0.5f

val PillWidth = 64.dp
val PillEndMargin = 8.dp

fun avoidanceFraction(distance: Float, lead: Float): Float {
    if (lead <= 0f) {
        return if (distance <= 0f) 1f else 0f
    }
    if (distance <= 0f) return 1f
    if (distance >= lead) return 0f
    val t = (1f - (distance / lead)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

fun avoidanceInset(rowEnd: Float, pillStart: Float, gap: Float, fraction: Float): Float {
    val clearanceTarget = pillStart - gap
    val rawOverlap = rowEnd - clearanceTarget
    if (rawOverlap <= 0f) return 0f
    return rawOverlap * fraction.coerceIn(0f, 1f)
}

fun gravityOrder(centers: List<Offset>, anchor: Offset): List<Int> {
    return centers.indices.sortedBy { index ->
        val center = centers[index]
        val dx = center.x - anchor.x
        val dy = center.y - anchor.y
        dx * dx + dy * dy
    }
}

fun gravityDelays(count: Int, staggerMs: Int, capMs: Int): List<Int> {
    if (count <= 0) return emptyList()
    return List(count) { index ->
        minOf(index * staggerMs, capMs)
    }
}
