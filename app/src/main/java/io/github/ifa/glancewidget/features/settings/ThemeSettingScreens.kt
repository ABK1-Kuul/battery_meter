package io.github.ifa.glancewidget.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.features.settings.component.ThemeSetting
import io.github.ifa.glancewidget.model.ThemeType
import io.github.ifa.glancewidget.model.ThemeTypeColor
import io.github.ifa.glancewidget.ui.component.AnimatedTextTopAppBar
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import androidx.compose.runtime.CompositionLocalProvider
import dev.chrisbanes.haze.HazeState
import io.github.ifa.glancewidget.model.GlassAccent
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.glass.GlassBackdrop
import io.github.ifa.glancewidget.ui.glass.GlassBlack
import io.github.ifa.glancewidget.ui.glass.LocalHazeState
import io.github.ifa.glancewidget.ui.glass.LocalIsGlassTheme

@Composable
fun ThemeSettingsScreen(
    uiState: SettingsViewModel.SettingsScreenUiState,
    showNavigationIcon: Boolean,
    onNavigationIconClick: () -> Unit,
    onSelectTheme: (ThemeType) -> Unit,
    onSelectThemeColor: (ThemeTypeColor) -> Unit,
    onEnableBlackDark: (Boolean) -> Unit,
    onEnableGlassTheme: (Boolean) -> Unit = {},
    onSelectGlassAccent: (GlassAccent) -> Unit = {},
    onSetGlassFrost: (Int) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    ThemeSettingsScreenLayout(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        showNavigationIcon = showNavigationIcon,
        onNavigationIconClick = onNavigationIconClick,
        onSelectTheme = onSelectTheme,
        onSelectThemeColor = onSelectThemeColor,
        onEnableBlackDark = onEnableBlackDark,
        onEnableGlassTheme = onEnableGlassTheme,
        onSelectGlassAccent = onSelectGlassAccent,
        onSetGlassFrost = onSetGlassFrost,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ThemeSettingsScreenLayout(
    uiState: SettingsViewModel.SettingsScreenUiState,
    snackbarHostState: SnackbarHostState,
    showNavigationIcon: Boolean,
    onSelectTheme: (ThemeType) -> Unit,
    onSelectThemeColor: (ThemeTypeColor) -> Unit,
    onEnableBlackDark: (Boolean) -> Unit,
    onNavigationIconClick: () -> Unit,
    onEnableGlassTheme: (Boolean) -> Unit = {},
    onSelectGlassAccent: (GlassAccent) -> Unit = {},
    onSetGlassFrost: (Int) -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isGlassTheme = LocalIsGlassTheme.current
    val hazeState = remember { HazeState() }

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                AnimatedTextTopAppBar(
                    title = stringResource(id = R.string.theme),
                    navigationIcon = {
                        if (showNavigationIcon) IconButton(
                            onClick = { onNavigationIconClick() },
                            colors = IconButtonDefaults.iconButtonColors().copy(
                                containerColor = if (isGlassTheme) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBackIosNew,
                                contentDescription = stringResource(id = R.string.back),
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            containerColor = if (isGlassTheme) GlassBlack else MaterialTheme.colorScheme.surface,
        ) { padding ->
            val contentList: @Composable () -> Unit = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.l),
                    contentPadding = padding,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = Spacing.l)
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                ) {
                    item {
                        ThemeSetting(
                            onSelectTheme = onSelectTheme,
                            onSelectThemeColor = onSelectThemeColor,
                            onEnableBlackDark = onEnableBlackDark,
                            uiState = uiState,
                            onEnableGlassTheme = onEnableGlassTheme,
                            onSelectGlassAccent = onSelectGlassAccent,
                            onSetGlassFrost = onSetGlassFrost,
                        )
                    }
                }
            }

            if (isGlassTheme) {
                GlassBackdrop(hazeState = hazeState) {
                    contentList()
                }
            } else {
                contentList()
            }
        }
    }
}

@ThemePreviews
@Composable
fun ThemeSettingsScreenPreview() {
    MaterialTheme {
        ThemeSettingsScreen(
            uiState = SettingsViewModel.SettingsScreenUiState(),
            showNavigationIcon = true,
            onNavigationIconClick = {},
            onSelectTheme = {},
            onSelectThemeColor = {},
            onEnableBlackDark = {}
        )
    }
}