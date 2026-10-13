package io.github.ifa.glancewidget.ui.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun Sparkline(
    values: ImmutableList<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas

        if (values.size == 1) {
            val y = size.height / 2f
            drawLine(
                color = color,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            return@Canvas
        }

        val minVal = values.minOrNull() ?: 0f
        val maxVal = values.maxOrNull() ?: 1f
        val span = (maxVal - minVal).coerceAtLeast(0.01f)
        val paddedMin = minVal - span * 0.1f
        val paddedMax = maxVal + span * 0.1f
        val totalSpan = paddedMax - paddedMin

        val stepX = size.width / (values.size - 1)
        val path = Path()

        values.forEachIndexed { index, value ->
            val x = index * stepX
            val normalizedY = ((value - paddedMin) / totalSpan).coerceIn(0f, 1f)
            val y = size.height - normalizedY * size.height
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@ThemePreviews
@Composable
fun SparklinePreview() {
    MaterialTheme {
        Sparkline(
            values = persistentListOf(20f, 25f, 22f, 30f, 28f, 35f, 32f),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        )
    }
}
