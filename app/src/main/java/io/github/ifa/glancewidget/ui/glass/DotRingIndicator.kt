package io.github.ifa.glancewidget.ui.glass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DotRingIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    totalDots: Int = 48,
    ringSize: Dp = 180.dp,
    dotRadius: Dp = 3.dp,
    litColor: Color = MaterialTheme.colorScheme.primary,
    unlitColor: Color = GlassBorder
) {
    val litCount = (progress.coerceIn(0f, 1f) * totalDots).toInt().coerceIn(0, totalDots)

    Canvas(modifier = modifier.size(ringSize)) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val dotRadiusPx = dotRadius.toPx()
        val orbitRadius = (size.minDimension / 2f) - dotRadiusPx

        for (i in 0 until totalDots) {
            val angleRad = (i.toFloat() / totalDots) * (2f * PI.toFloat()) - (PI.toFloat() / 2f)
            val x = centerX + orbitRadius * cos(angleRad)
            val y = centerY + orbitRadius * sin(angleRad)
            val isLit = i < litCount
            drawCircle(
                color = if (isLit) litColor else unlitColor,
                radius = dotRadiusPx,
                center = Offset(x, y)
            )
        }
    }
}

@ThemePreviews
@Composable
fun DotRingIndicatorPreview() {
    MaterialTheme {
        Box(contentAlignment = Alignment.Center) {
            DotRingIndicator(progress = 0.75f)
            DotMatrixText(text = "75%")
        }
    }
}
