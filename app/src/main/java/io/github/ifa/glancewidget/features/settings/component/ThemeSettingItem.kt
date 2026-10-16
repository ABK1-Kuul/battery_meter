package io.github.ifa.glancewidget.features.settings.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.model.ThemeType
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.glass.GlassSurface
import io.github.ifa.glancewidget.ui.glass.LocalIsGlassTheme
import io.github.ifa.glancewidget.ui.theme.glassLabel

@Composable
fun ThemeSettingItem(
    modifier: Modifier = Modifier,
    themeType: ThemeType,
    onSelectTheme: (ThemeType) -> Unit,
    selected: Boolean,
) {
    val isGlassTheme = LocalIsGlassTheme.current

    val itemContent = @Composable {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .then(
                    if (isGlassTheme && selected) {
                        Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    } else {
                        Modifier
                    }
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    painter = themeType.themeIcon(),
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = if (isGlassTheme) themeType.themeName().uppercase() else themeType.themeName(),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = if (isGlassTheme) MaterialTheme.typography.glassLabel else MaterialTheme.typography.labelLarge,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    if (isGlassTheme) {
        GlassSurface(
            shape = AppShape.inner,
            modifier = modifier
                .height(92.dp)
                .fillMaxWidth()
                .clip(AppShape.inner)
                .clickable { onSelectTheme(themeType) }
        ) {
            itemContent()
        }
    } else {
        Surface(
            onClick = { onSelectTheme(themeType) },
            shape = AppShape.inner,
            modifier = modifier.height(92.dp).fillMaxWidth(),
            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            itemContent()
        }
    }
}

@Composable
private fun ThemeType.themeIcon(): Painter = when (this) {
    ThemeType.FOLLOW_SYSTEM -> painterResource(id = R.drawable.ic_brightness_auto)
    ThemeType.DARK_THEME -> painterResource(id = R.drawable.ic_dark_mode)
    ThemeType.LIGHT_THEME -> painterResource(id = R.drawable.ic_light_mode)
}

@Composable
private fun ThemeType.themeName(): String = when (this) {
    ThemeType.FOLLOW_SYSTEM -> stringResource(R.string.follow_system)
    ThemeType.DARK_THEME -> stringResource(R.string.dark_theme)
    ThemeType.LIGHT_THEME -> stringResource(R.string.light_theme)
}

@ThemePreviews
@Composable
fun ThemeSettingItemSelectedPreview() {
    MaterialTheme {
        ThemeSettingItem(themeType = ThemeType.FOLLOW_SYSTEM, onSelectTheme = {}, selected = true)
    }
}

@ThemePreviews
@Composable
fun ThemeSettingItemPreview() {
    MaterialTheme {
        ThemeSettingItem(themeType = ThemeType.FOLLOW_SYSTEM, onSelectTheme = {}, selected = false)
    }
}