package io.github.ifa.glancewidget.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ifa.glancewidget.domain.AppSettingsRepository
import io.github.ifa.glancewidget.domain.localAppSettingsRepository
import io.github.ifa.glancewidget.model.AppSettings
import io.github.ifa.glancewidget.model.ThemeType
import io.github.ifa.glancewidget.model.ThemeTypeColor
import com.dotsdev.material3color.isSupportedDynamicColor
import com.dotsdev.material3color.getDarkScheme
import com.dotsdev.material3color.getLightScheme
import com.dotsdev.material3color.rememberColorScheme
import kotlin.text.get

import io.github.ifa.glancewidget.ui.glass.LocalGlassFrost
import io.github.ifa.glancewidget.ui.glass.LocalIsGlassTheme
import io.github.ifa.glancewidget.ui.glass.glassColorScheme

data class AppColorScheme(
    val themeTypeColor: ThemeTypeColor,
    val colorScheme: ColorScheme
)

internal val LocalAppColorSchemes = compositionLocalOf<List<AppColorScheme>> { listOf() }

@Composable
fun AppTheme(
    shapes: Shapes = MaterialTheme.shapes,
    typography: Typography = Type.typography,
    appSettingsRepository: AppSettingsRepository = localAppSettingsRepository(),
    content: @Composable () -> Unit
) {
    val settings by appSettingsRepository.get().collectAsStateWithLifecycle(AppSettings())
    val isDarkTheme = when {
        settings.isGlassThemeEnabled -> true
        settings.theme == ThemeType.DARK_THEME -> true
        settings.theme == ThemeType.LIGHT_THEME -> false
        else -> isSystemInDarkTheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    val dynamicOrPresetScheme = rememberColorScheme(
        isDarkTheme = isDarkTheme,
        isEnabledBlackDark = settings.isBlackDarkEnabled,
        isDynamicColor = settings.themeColor == ThemeTypeColor.System,
        seedColor = settings.themeColor.code,
    )
    val colorScheme = if (settings.isGlassThemeEnabled) {
        glassColorScheme(settings.glassAccent)
    } else {
        dynamicOrPresetScheme
    }
    val effectiveTypography = if (settings.isGlassThemeEnabled && typography == Type.typography) {
        Type.glassTypography
    } else {
        typography
    }
    val appColorScheme = rememberAppColorScheme(isDarkTheme, colorScheme)
    CompositionLocalProvider(
        LocalAppColorSchemes provides appColorScheme,
        LocalIsGlassTheme provides settings.isGlassThemeEnabled,
        LocalGlassFrost provides settings.glassFrost
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = shapes,
            typography = effectiveTypography,
            content = content
        )
    }
}

@Composable
fun rememberAppColorScheme(isDarkTheme: Boolean, colorScheme: ColorScheme): List<AppColorScheme> {
    val appColorSchemeMap = rememberAppColorSchemeMap(isDarkTheme)
    return remember(colorScheme) {
        ThemeTypeColor.entries.map {
            if (it == ThemeTypeColor.System) {
                AppColorScheme(it, colorScheme)
            } else {
                appColorSchemeMap[it] ?: AppColorScheme(it, colorScheme)
            }
        }
    }
}

@Composable
fun rememberAppColorSchemeMap(
    isDarkTheme: Boolean
): Map<ThemeTypeColor, AppColorScheme> {
    return remember(isDarkTheme) {
        ThemeTypeColor.entries.associateWith { themeTypeColor ->
            AppColorScheme(
                themeTypeColor = themeTypeColor,
                colorScheme = if (isDarkTheme) {
                    getDarkScheme(themeTypeColor.code, true)
                } else {
                    getLightScheme(themeTypeColor.code)
                }
            )
        }
    }
}
