package io.github.ifa.glancewidget.ui.designsystem.tokens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object AppMotion {
    const val short = 200
    const val medium = 350
    fun <T> spatial(): SpringSpec<T> = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
    fun <T> effects(): TweenSpec<T> = tween(durationMillis = short)
}
