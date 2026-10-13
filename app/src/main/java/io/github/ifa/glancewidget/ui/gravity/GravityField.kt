package io.github.ifa.glancewidget.ui.gravity

import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Rect

data class GravityFieldContext(
    val pillBounds: State<Rect>,
    val reduceMotion: Boolean = false,
    val controller: GravityController? = null
)

val LocalGravityField = compositionLocalOf {
    GravityFieldContext(
        pillBounds = mutableStateOf(Rect.Zero),
        reduceMotion = false,
        controller = null
    )
}

val LocalGravityController = compositionLocalOf<GravityController?> { null }
