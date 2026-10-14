package io.github.ifa.glancewidget.ui.glass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews

@Composable
fun DotMatrixText(
    text: String,
    modifier: Modifier = Modifier,
    dotSize: Dp = 3.dp,
    dotSpacing: Dp = 1.dp,
    charSpacing: Dp = 3.dp,
    tint: Color = LocalContentColor.current,
    contentDescription: String? = text
) {
    if (text.isEmpty()) return

    val charWidthDp = (dotSize * DotGlyphs.COL_COUNT) + (dotSpacing * (DotGlyphs.COL_COUNT - 1))
    val totalWidthDp = (charWidthDp * text.length) + (charSpacing * (text.length - 1))
    val totalHeightDp = (dotSize * DotGlyphs.ROW_COUNT) + (dotSpacing * (DotGlyphs.ROW_COUNT - 1))

    val density = LocalDensity.current
    val dotSizePx = with(density) { dotSize.toPx() }
    val dotSpacingPx = with(density) { dotSpacing.toPx() }
    val charSpacingPx = with(density) { charSpacing.toPx() }
    val radiusPx = dotSizePx / 2f
    val charWidthPx = (dotSizePx * DotGlyphs.COL_COUNT) + (dotSpacingPx * (DotGlyphs.COL_COUNT - 1))
    val ghostTint = tint.copy(alpha = 0.10f)

    Canvas(
        modifier = modifier
            .size(totalWidthDp, totalHeightDp)
            .semantics {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            }
    ) {
        for (charIndex in text.indices) {
            val char = text[charIndex]
            val charStartX = charIndex * (charWidthPx + charSpacingPx)
            for (row in 0 until DotGlyphs.ROW_COUNT) {
                val centerY = row * (dotSizePx + dotSpacingPx) + radiusPx
                for (col in 0 until DotGlyphs.COL_COUNT) {
                    val centerX = charStartX + col * (dotSizePx + dotSpacingPx) + radiusPx
                    val isLit = DotGlyphs.isLit(char, row, col)
                    drawCircle(
                        color = if (isLit) tint else ghostTint,
                        radius = radiusPx,
                        center = Offset(centerX, centerY)
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun DotMatrixTextPreview() {
    MaterialTheme {
        DotMatrixText(text = "98%")
    }
}
