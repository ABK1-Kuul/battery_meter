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

    val isPureBlack = LocalIsPureBlack.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val containerColor = MaterialTheme.colorScheme.primaryContainer

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
        if (!isPureBlack) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                val primaryBrush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.20f), Color.Transparent),
                    center = Offset(width * 0.20f, height * 0.15f),
                    radius = width * 0.60f
                )
                drawRect(brush = primaryBrush)

                val tertiaryBrush = Brush.radialGradient(
                    colors = listOf(tertiaryColor.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(width * 0.85f, height * 0.45f),
                    radius = width * 0.60f
                )
                drawRect(brush = tertiaryBrush)

                val containerBrush = Brush.radialGradient(
                    colors = listOf(containerColor.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(width * 0.25f, height * 0.80f),
                    radius = width * 0.55f
                )
                drawRect(brush = containerBrush)
            }
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
