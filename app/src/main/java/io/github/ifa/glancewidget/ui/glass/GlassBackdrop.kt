package io.github.ifa.glancewidget.ui.glass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews

@Composable
fun GlassBackdrop(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = LocalHazeState.current,
    content: @Composable () -> Unit
) {
    val isGlassTheme = LocalIsGlassTheme.current
    if (!isGlassTheme) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            content()
        }
        return
    }

    val primaryAccent = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val secondaryBlob = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
    val tertiaryBlob = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)

    val hazeModifier = if (hazeState != null) {
        modifier.hazeSource(state = hazeState)
    } else {
        modifier
    }

    Box(
        modifier = hazeModifier
            .fillMaxSize()
            .background(GlassBlack)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val accentBrush = Brush.radialGradient(
                colors = listOf(primaryAccent, Color.Transparent),
                center = Offset(width * 0.20f, height * 0.15f),
                radius = width * 0.55f
            )
            drawRect(brush = accentBrush)

            val secondaryBrush = Brush.radialGradient(
                colors = listOf(secondaryBlob, Color.Transparent),
                center = Offset(width * 0.85f, height * 0.45f),
                radius = width * 0.60f
            )
            drawRect(brush = secondaryBrush)

            val tertiaryBrush = Brush.radialGradient(
                colors = listOf(tertiaryBlob, Color.Transparent),
                center = Offset(width * 0.25f, height * 0.80f),
                radius = width * 0.50f
            )
            drawRect(brush = tertiaryBrush)
        }

        content()
    }
}

@ThemePreviews
@Composable
fun GlassBackdropPreview() {
    MaterialTheme {
        GlassBackdrop {
            Text(text = "Glass Backdrop Content")
        }
    }
}
