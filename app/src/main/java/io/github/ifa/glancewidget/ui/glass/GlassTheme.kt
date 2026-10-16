package io.github.ifa.glancewidget.ui.glass

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import io.github.ifa.glancewidget.model.GlassAccent

val LocalIsGlassTheme = compositionLocalOf { false }
val LocalGlassFrost = compositionLocalOf { 18 }
val LocalIsPureBlack = compositionLocalOf { false }

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

fun glassColorScheme(
    accent: GlassAccent = GlassAccent.Red,
    baseScheme: ColorScheme? = null,
    isPureBlack: Boolean = false
): ColorScheme {
    val isThemeAccent = accent == GlassAccent.Theme && baseScheme != null

    val accentColor = if (isThemeAccent) {
        baseScheme!!.primary
    } else {
        when (accent) {
            GlassAccent.Red -> GlassAccentRed
            GlassAccent.Mono -> GlassAccentMono
            GlassAccent.Theme -> baseScheme?.primary ?: GlassAccentRed
        }
    }
    val onAccentColor = if (isThemeAccent) {
        baseScheme!!.onPrimary
    } else {
        when (accent) {
            GlassAccent.Red -> Color(0xFFFFFFFF)
            GlassAccent.Mono -> Color(0xFF000000)
            GlassAccent.Theme -> baseScheme?.onPrimary ?: Color(0xFFFFFFFF)
        }
    }
    val accentContainer = if (isThemeAccent) {
        baseScheme!!.primaryContainer
    } else {
        when (accent) {
            GlassAccent.Red -> Color(0xFF3B0508)
            GlassAccent.Mono -> Color(0xFF262626)
            GlassAccent.Theme -> baseScheme?.primaryContainer ?: Color(0xFF3B0508)
        }
    }
    val onAccentContainer = if (isThemeAccent) {
        baseScheme!!.onPrimaryContainer
    } else {
        when (accent) {
            GlassAccent.Red -> Color(0xFFFFDAD8)
            GlassAccent.Mono -> Color(0xFFFFFFFF)
            GlassAccent.Theme -> baseScheme?.onPrimaryContainer ?: Color(0xFFFFDAD8)
        }
    }
    val tertiaryColor = if (isThemeAccent) {
        baseScheme!!.tertiary
    } else {
        accentColor
    }
    val onTertiaryColor = if (isThemeAccent) {
        baseScheme!!.onTertiary
    } else {
        onAccentColor
    }
    val tertiaryContainerColor = if (isThemeAccent) {
        baseScheme!!.tertiaryContainer
    } else {
        accentContainer
    }
    val onTertiaryContainerColor = if (isThemeAccent) {
        baseScheme!!.onTertiaryContainer
    } else {
        onAccentContainer
    }
    val errorColor = if (isThemeAccent) {
        baseScheme!!.error
    } else {
        GlassAccentRed
    }
    val onErrorColor = if (isThemeAccent) {
        baseScheme!!.onError
    } else {
        Color(0xFFFFFFFF)
    }
    val errorContainerColor = if (isThemeAccent) {
        baseScheme!!.errorContainer
    } else {
        Color(0xFF3B0508)
    }
    val onErrorContainerColor = if (isThemeAccent) {
        baseScheme!!.onErrorContainer
    } else {
        Color(0xFFFFDAD8)
    }

    val surfaceContainerLowestColor = GlassBlack
    val surfaceContainerLowColor = if (isPureBlack) GlassBlack else GlassSurfaceContainerLow
    val surfaceContainerColor = if (isPureBlack) Color(0xFF0A0A0A) else GlassSurfaceContainer
    val surfaceContainerHighColor = if (isPureBlack) Color(0xFF101010) else GlassSurfaceContainerHigh
    val surfaceContainerHighestColor = if (isPureBlack) Color(0xFF161616) else GlassSurfaceContainerHighest

    return darkColorScheme(
        primary = accentColor,
        onPrimary = onAccentColor,
        primaryContainer = accentContainer,
        onPrimaryContainer = onAccentContainer,
        secondary = GlassTextSecondary,
        onSecondary = GlassBlack,
        secondaryContainer = surfaceContainerHighColor,
        onSecondaryContainer = GlassTextPrimary,
        tertiary = tertiaryColor,
        onTertiary = onTertiaryColor,
        tertiaryContainer = tertiaryContainerColor,
        onTertiaryContainer = onTertiaryContainerColor,
        background = GlassBlack,
        onBackground = GlassTextPrimary,
        surface = GlassSurface,
        onSurface = GlassTextPrimary,
        surfaceVariant = surfaceContainerHighColor,
        onSurfaceVariant = GlassTextSecondary,
        surfaceContainerLowest = surfaceContainerLowestColor,
        surfaceContainerLow = surfaceContainerLowColor,
        surfaceContainer = surfaceContainerColor,
        surfaceContainerHigh = surfaceContainerHighColor,
        surfaceContainerHighest = surfaceContainerHighestColor,
        outline = GlassBorder,
        outlineVariant = if (isPureBlack) Color(0x33FFFFFF) else Color(0x1AFFFFFF),
        error = errorColor,
        onError = onErrorColor,
        errorContainer = errorContainerColor,
        onErrorContainer = onErrorContainerColor
    )
}

fun lerpColor(start: Color, stop: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (stop.red - start.red) * f,
        green = start.green + (stop.green - start.green) * f,
        blue = start.blue + (stop.blue - start.blue) * f,
        alpha = start.alpha + (stop.alpha - start.alpha) * f
    )
}

fun glassFrostColor(primary: Color, isPureBlack: Boolean = false): Color {
    val factor = if (isPureBlack) 0.08f else 0.18f
    val alpha = if (isPureBlack) 0.72f else 0.58f
    return Color(
        red = primary.red * factor,
        green = primary.green * factor,
        blue = primary.blue * factor,
        alpha = alpha
    )
}

fun glassTintStart(primary: Color): Color {
    return lerpColor(GlassTintStart, primary.copy(alpha = 0.28f), 0.50f)
}

fun glassTintEnd(primary: Color): Color {
    return lerpColor(GlassTintEnd, primary.copy(alpha = 0.08f), 0.40f)
}

fun glassHighlight(primary: Color): Color {
    return lerpColor(GlassHighlight, primary.copy(alpha = 0.35f), 0.50f)
}

fun glassBorder(primary: Color): Color {
    return lerpColor(GlassBorder, primary.copy(alpha = 0.36f), 0.45f)
}

fun glassSpecular(primary: Color): Color {
    return lerpColor(GlassSpecular, primary.copy(alpha = 0.55f), 0.35f)
}

fun glassFallbackBackground(primary: Color, isPureBlack: Boolean = false): Color {
    val base = if (isPureBlack) GlassBlack else GlassSurfaceContainerHigh
    return lerpColor(base, primary.copy(alpha = 0.18f), 0.20f)
}

