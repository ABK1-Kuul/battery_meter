package io.github.ifa.glancewidget.features.settings.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.features.settings.SettingsViewModel
import io.github.ifa.glancewidget.model.ThemeType
import io.github.ifa.glancewidget.model.ThemeTypeColor
import io.github.ifa.glancewidget.ui.designsystem.components.SettingsGroup
import io.github.ifa.glancewidget.ui.designsystem.components.SettingsRow
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import io.github.ifa.glancewidget.model.GlassAccent
import io.github.ifa.glancewidget.ui.glass.GlassAccentMono
import io.github.ifa.glancewidget.ui.glass.GlassAccentRed

@Composable
fun ThemeSetting(
    onSelectTheme: (ThemeType) -> Unit,
    onSelectThemeColor: (ThemeTypeColor) -> Unit,
    onEnableBlackDark: (Boolean) -> Unit,
    uiState: SettingsViewModel.SettingsScreenUiState,
    onEnableGlassTheme: (Boolean) -> Unit = {},
    onSelectGlassAccent: (GlassAccent) -> Unit = {},
    onSetGlassFrost: (Int) -> Unit = {}
) {
    var isBlackDarkEnabled by remember(uiState.isBlackDarkEnabled) {
        mutableStateOf(uiState.isBlackDarkEnabled)
    }
    val isDarkTheme = when (uiState.theme) {
        ThemeType.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        ThemeType.DARK_THEME -> true
        ThemeType.LIGHT_THEME -> false
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.l),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.l)
        ) {
            ThemeType.entries.forEach { theme ->
                ThemeSettingItem(
                    modifier = Modifier.weight(1f),
                    themeType = theme,
                    onSelectTheme = onSelectTheme,
                    selected = theme == uiState.theme
                )
            }
        }

        SettingsGroup(
            title = null,
            modifier = Modifier.padding(horizontal = Spacing.l)
        ) {
            SettingsRow(
                title = stringResource(id = R.string.black_dark),
                subtitle = stringResource(id = R.string.black_dark_description),
                leadingIcon = Icons.Rounded.Contrast,
                trailing = {
                    Switch(
                        checked = isBlackDarkEnabled,
                        enabled = isDarkTheme,
                        onCheckedChange = { checked ->
                            isBlackDarkEnabled = checked
                            onEnableBlackDark(checked)
                        }
                    )
                }
            )
        }

        SettingsGroup(
            title = null,
            modifier = Modifier.padding(horizontal = Spacing.l)
        ) {
            SettingsRow(
                title = stringResource(id = R.string.glass_theme),
                subtitle = stringResource(id = R.string.glass_theme_desc),
                leadingIcon = Icons.Rounded.Contrast,
                trailing = {
                    Switch(
                        checked = uiState.isGlassThemeEnabled,
                        onCheckedChange = onEnableGlassTheme
                    )
                }
            )
            if (uiState.isGlassThemeEnabled) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = Spacing.l),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.l, vertical = Spacing.m)
                ) {
                    Text(
                        text = stringResource(id = R.string.glass_accent),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(Spacing.s))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                        GlassAccent.entries.forEach { accent ->
                            val isSelected = accent == uiState.glassAccent
                            val accentName = when (accent) {
                                GlassAccent.Red -> stringResource(R.string.glass_accent_red)
                                GlassAccent.Mono -> stringResource(R.string.glass_accent_mono)
                            }
                            val accentDotColor = when (accent) {
                                GlassAccent.Red -> GlassAccentRed
                                GlassAccent.Mono -> GlassAccentMono
                            }
                            Surface(
                                shape = AppShape.inner,
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .clickable { onSelectGlassAccent(accent) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(accentDotColor)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.s))
                                    Text(
                                        text = accentName,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = Spacing.l),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.l, vertical = Spacing.m)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.glass_frost),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${uiState.glassFrost} dp",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = uiState.glassFrost.toFloat(),
                        onValueChange = { onSetGlassFrost(it.toInt()) },
                        valueRange = 0f..32f,
                        steps = 31
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.theme),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = Spacing.l, bottom = Spacing.s)
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = Spacing.l),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                items(ThemeTypeColor.entries()) { item ->
                    SelectablePaletteItem(
                        modifier = Modifier.size(64.dp),
                        themeTypeColor = item,
                        onClick = onSelectThemeColor,
                        isSelected = item == uiState.colorScheme,
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun ThemeSettingPreview() {
    MaterialTheme {
        ThemeSetting(
            onSelectTheme = {},
            onSelectThemeColor = {},
            onEnableBlackDark = {},
            uiState = SettingsViewModel.SettingsScreenUiState()
        )
    }
}