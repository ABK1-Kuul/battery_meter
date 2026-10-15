package io.github.ifa.glancewidget.ui.glass

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import io.github.ifa.glancewidget.model.GlassAccent

val LocalIsGlassTheme = compositionLocalOf { false }
val LocalGlassFrost = compositionLocalOf { 18 }

val GlassBlack = Color(0xFF000000)
val GlassSurface = Color(0xFF000000)
val GlassSurfaceContainerLow = Color(0xFF0D0D0D)
val GlassSurfaceContainer = Color(0xFF141414)
val GlassSurfaceContainerHigh = Color(0xFF1C1C1C)
val GlassSurfaceContainerHighest = Color(0xFF262626)
val GlassTextPrimary = Color(0xFFF2F2F2)
val GlassTextSecondary = Color(0x99F2F2F2)
val GlassAccentRed = Color(0xFFE5232B)
val GlassAccentMono = Color(0xFFFFFFFF)
val GlassBorder = Color(0x24FFFFFF)
val GlassTintStart = Color(0x24FFFFFF)
val GlassTintEnd = Color(0x0AFFFFFF)
val GlassHighlight = Color(0x29FFFFFF)
val GlassSpecular = Color(0x4DFFFFFF)
val GlassGhostDot = Color(0x1AFFFFFF)
val GlassInnerShadow = Color(0x66000000)
val GlassMeniscus = Color(0xB3FFFFFF)
val GlassIconUnselected = Color(0x80FFFFFF)
val GlassBlobBlue = Color(0x1F2196F3)
val GlassBlobTeal = Color(0x1F009688)
val GlassShadow = Color(0x40000000)

fun glassColorScheme(accent: GlassAccent): ColorScheme {
    val accentColor = when (accent) {
        GlassAccent.Red -> GlassAccentRed
        GlassAccent.Mono -> GlassAccentMono
    }
    val onAccentColor = when (accent) {
        GlassAccent.Red -> Color(0xFFFFFFFF)
        GlassAccent.Mono -> Color(0xFF000000)
    }
    val accentContainer = when (accent) {
        GlassAccent.Red -> Color(0xFF3B0508)
        GlassAccent.Mono -> Color(0xFF262626)
    }
    val onAccentContainer = when (accent) {
        GlassAccent.Red -> Color(0xFFFFDAD8)
        GlassAccent.Mono -> Color(0xFFFFFFFF)
    }

    return darkColorScheme(
        primary = accentColor,
        onPrimary = onAccentColor,
        primaryContainer = accentContainer,
        onPrimaryContainer = onAccentContainer,
        secondary = GlassTextSecondary,
        onSecondary = GlassBlack,
        secondaryContainer = GlassSurfaceContainerHigh,
        onSecondaryContainer = GlassTextPrimary,
        tertiary = accentColor,
        onTertiary = onAccentColor,
        tertiaryContainer = accentContainer,
        onTertiaryContainer = onAccentContainer,
        background = GlassBlack,
        onBackground = GlassTextPrimary,
        surface = GlassSurface,
        onSurface = GlassTextPrimary,
        surfaceVariant = GlassSurfaceContainerHigh,
        onSurfaceVariant = GlassTextSecondary,
        surfaceContainerLowest = GlassBlack,
        surfaceContainerLow = GlassSurfaceContainerLow,
        surfaceContainer = GlassSurfaceContainer,
        surfaceContainerHigh = GlassSurfaceContainerHigh,
        surfaceContainerHighest = GlassSurfaceContainerHighest,
        outline = GlassBorder,
        outlineVariant = Color(0x1AFFFFFF),
        error = GlassAccentRed,
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFF3B0508),
        onErrorContainer = Color(0xFFFFDAD8)
    )
}
