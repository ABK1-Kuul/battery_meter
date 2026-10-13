package io.github.ifa.glancewidget.features.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AdaptStrategy
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableSupportingPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import io.github.ifa.glancewidget.BuildConfig
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.features.main.MainScreenTab
import io.github.ifa.glancewidget.features.settings.about.AboutScreen
import io.github.ifa.glancewidget.features.settings.component.LanguageSetting
import io.github.ifa.glancewidget.features.settings.component.TemperatureUnitSetting
import io.github.ifa.glancewidget.model.AppSettings
import io.github.ifa.glancewidget.model.MyDevice
import io.github.ifa.glancewidget.ui.designsystem.components.AppScaffold
import io.github.ifa.glancewidget.ui.designsystem.components.SettingsGroup
import io.github.ifa.glancewidget.ui.designsystem.components.SettingsRow
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.utils.combinePadding
import io.github.ifa.glancewidget.utils.isAppCompatLocaleDeprecated
import io.github.ifa.glancewidget.utils.navigateUrl
import io.github.ifa.glancewidget.utils.syncShowPairedDevicesToWidget
import io.github.ifa.glancewidget.utils.syncThemeColorToWidget
import io.github.ifa.glancewidget.utils.syncThemeToWidget
import kotlinx.coroutines.launch

const val settingsScreenRoute = "settings_screen_route"

fun NavGraphBuilder.settingsScreen(
    contentPadding: PaddingValues,
) {
    composable(settingsScreenRoute) {
        SettingsSupportingPaneScreen(
            contentPadding = contentPadding,
        )
    }
}

enum class SettingsPane {
    About, WidgetSettings, ThemeSettings
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingsSupportingPaneScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    contentPadding: PaddingValues
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val navigator = rememberSupportingPaneScaffoldNavigator(
        adaptStrategies = SupportingPaneScaffoldDefaults.adaptStrategies(supportingPaneAdaptStrategy = AdaptStrategy.Hide)
    )
    val scope = rememberCoroutineScope()
    val backNavigationBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange
    val context = LocalContext.current
    val expansionState = rememberPaneExpansionState(
        anchors = listOf(PaneExpansionAnchor.Proportion(0.45f)),
        initialAnchoredIndex = 0
    )
    val widthExpanded = currentWindowAdaptiveInfo()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

    NavigableSupportingPaneScaffold(
        navigator = navigator,
        mainPane = {
            AnimatedPane {
                SettingsScreenLayout(
                    uiState = uiState,
                    snackbarHostState = snackbarHostState,
                    contentPadding = contentPadding,
                    onOpenAboutScreen = {
                        scope.launch {
                            navigator.navigateTo(SupportingPaneScaffoldRole.Supporting, SettingsPane.About)
                        }
                    },
                    onOpenWidgetSettings = {
                        scope.launch {
                            navigator.navigateTo(SupportingPaneScaffoldRole.Supporting, SettingsPane.WidgetSettings)
                        }
                    },
                    onOpenThemeSettings = {
                        scope.launch {
                            navigator.navigateTo(SupportingPaneScaffoldRole.Supporting, SettingsPane.ThemeSettings)
                        }
                    },
                    onSelectLanguage = viewModel::setLanguage,
                    onSelectTemperatureUnit = viewModel::setTemperatureUnit,
                )
            }
        },
        supportingPane = {
            AnimatedPane {
                when (navigator.currentDestination?.contentKey) {
                    null -> {
                        DetailPlaceholder(R.drawable.ic_settings_filled)
                    }
                    SettingsPane.About -> {
                        AboutScreen(
                            contentPadding = contentPadding,
                            showNavigationIcon = !widthExpanded,
                            onNavigationIconClick = {
                                scope.launch { navigator.navigateBack(backNavigationBehavior) }
                            },
                            onExternalUrlClick = { navigateUrl(it) }
                        )
                    }
                    SettingsPane.ThemeSettings -> {
                        ThemeSettingsScreen(
                            uiState = uiState,
                            showNavigationIcon = !widthExpanded,
                            onNavigationIconClick = {
                                scope.launch { navigator.navigateBack(backNavigationBehavior) }
                            },
                            onSelectTheme = { theme ->
                                viewModel.setThemeType(theme)
                                context.syncThemeToWidget(theme)
                            },
                            onSelectThemeColor = { themeColor ->
                                viewModel.setThemeTypeColor(themeColor)
                                context.syncThemeColorToWidget(themeColor)
                            },
                            onEnableBlackDark = viewModel::setEnableBlackDark
                        )
                    }
                    SettingsPane.WidgetSettings -> {
                        WidgetSettingsScreen(
                            uiState = uiState,
                            showNavigationIcon = !widthExpanded,
                            contentPadding = contentPadding,
                            onNavigationIconClick = {
                                scope.launch { navigator.navigateBack(backNavigationBehavior) }
                            },
                            onSetDeviceShowInWidgetChanged = viewModel::updateDeviceShowInWidget,
                            onSetNotificationEnabled = viewModel::onBatteryAlertChanged,
                            onSetShowPairedDevice = { enabled ->
                                context.syncShowPairedDevicesToWidget(enabled)
                                viewModel.onShowPairedDeviceChanged(enabled)
                            },
                        )
                    }
                }
            }
        },
        paneExpansionState = expansionState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreenLayout(
    uiState: SettingsViewModel.SettingsScreenUiState,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
    onOpenAboutScreen: () -> Unit,
    onOpenWidgetSettings: () -> Unit,
    onOpenThemeSettings: () -> Unit,
    onSelectLanguage: (AppSettings.Language) -> Unit,
    onSelectTemperatureUnit: (MyDevice.Temperature.TemperatureUnit) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    AppScaffold(
        title = stringResource(id = MainScreenTab.Settings.label),
        scrollBehavior = scrollBehavior,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        val insets = combinePadding(padding, contentPadding)
        LazyColumn(
            contentPadding = insets,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            item {
                SettingsGroup(title = stringResource(R.string.appearance)) {
                    if (!isAppCompatLocaleDeprecated()) {
                        LanguageSetting(onSelectLanguage = onSelectLanguage, uiState = uiState)
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = Spacing.l),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                    TemperatureUnitSetting(onSelectTemperatureUnit = onSelectTemperatureUnit, uiState = uiState)
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = Spacing.l),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    SettingsRow(
                        title = stringResource(R.string.theme),
                        subtitle = stringResource(R.string.theme_settings_desc),
                        leadingPainter = painterResource(R.drawable.ic_brightness_auto),
                        trailing = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = onOpenThemeSettings
                    )
                }
            }
            item {
                SettingsGroup(title = stringResource(R.string.notification_settings)) {
                    SettingsRow(
                        title = stringResource(R.string.widget_tab),
                        subtitle = stringResource(R.string.notification_settings_desc),
                        leadingPainter = painterResource(R.drawable.ic_widgets_filled),
                        trailing = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = onOpenWidgetSettings
                    )
                }
            }
            item {
                SettingsGroup(title = stringResource(R.string.about_tab)) {
                    SettingsRow(
                        title = stringResource(R.string.about_tab),
                        subtitle = BuildConfig.VERSION_NAME,
                        leadingPainter = painterResource(R.drawable.ic_info_filled),
                        trailing = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = onOpenAboutScreen
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DetailPlaceholder(
    @DrawableRes icon: Int,
    background: Color = MaterialTheme.colorScheme.surfaceContainerLow
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Spacer(
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialShapes.Cookie12Sided.toShape()
                        )
                        .size(128.dp)
                )
                Icon(
                    painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(72.dp)
                )
            }
        }
    }
}

@ThemePreviews
@Composable
fun SettingsScreenLayoutPreview() {
    MaterialTheme {
        SettingsScreenLayout(
            uiState = SettingsViewModel.SettingsScreenUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            contentPadding = PaddingValues(),
            onOpenAboutScreen = {},
            onOpenWidgetSettings = {},
            onOpenThemeSettings = {},
            onSelectLanguage = {},
            onSelectTemperatureUnit = {}
        )
    }
}
