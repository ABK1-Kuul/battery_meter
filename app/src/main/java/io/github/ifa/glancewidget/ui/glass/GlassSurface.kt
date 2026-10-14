package io.github.ifa.glancewidget.ui.glass

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews

val LocalHazeState = compositionLocalOf<HazeState?> { null }

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    hazeState: HazeState? = LocalHazeState.current,
    content: @Composable () -> Unit
) {
    val isGlassTheme = LocalIsGlassTheme.current
    if (!isGlassTheme) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            content = content
        )
        return
    }

    val frostValue = LocalGlassFrost.current
    val isBlurSupported = Build.VERSION.SDK_INT >= 31 && frostValue > 0
    val frostDp = frostValue.coerceIn(0, 32).dp

    val baseModifier = modifier
        .clip(shape)
        .border(1.dp, GlassBorder, shape)

    val hazeModifier = if (isBlurSupported && hazeState != null) {
        baseModifier.hazeEffect(
            state = hazeState,
            style = HazeDefaults.style(
                backgroundColor = GlassBlack.copy(alpha = 0.50f),
                blurRadius = frostDp,
                noiseFactor = 0.16f
            )
        )
    } else {
        baseModifier.background(GlassSurfaceContainerHigh)
    }

    val glassBackgroundBrush = Brush.verticalGradient(
        colors = listOf(
            GlassHighlight.copy(alpha = 0.14f),
            GlassBorder.copy(alpha = 0.04f)
        )
    )

    Box(
        modifier = hazeModifier
            .background(glassBackgroundBrush)
            .drawWithContent {
                drawContent()
                drawLine(
                    color = GlassSpecular,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        content()
    }
}

@ThemePreviews
@Composable
fun GlassSurfacePreview() {
    MaterialTheme {
        GlassSurface(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Glass Surface Preview",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
