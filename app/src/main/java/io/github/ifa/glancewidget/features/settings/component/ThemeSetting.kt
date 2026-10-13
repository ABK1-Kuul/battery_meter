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

@Composable
fun ThemeSetting(
    onSelectTheme: (ThemeType) -> Unit,
    onSelectThemeColor: (ThemeTypeColor) -> Unit,
    onEnableBlackDark: (Boolean) -> Unit,
    uiState: SettingsViewModel.SettingsScreenUiState
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