package io.github.ifa.glancewidget.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
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
import io.github.ifa.glancewidget.features.battery.component.BonedDeviceItem
import io.github.ifa.glancewidget.features.settings.component.WidgetSettings
import io.github.ifa.glancewidget.model.BonedDevice
import io.github.ifa.glancewidget.model.BonnedDeviceSettings
import io.github.ifa.glancewidget.ui.component.AnimatedTextTopAppBar
import io.github.ifa.glancewidget.ui.designsystem.components.SettingsGroup
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.utils.combinePadding

@Composable
fun WidgetSettingsScreen(
    uiState: SettingsViewModel.SettingsScreenUiState,
    contentPadding: PaddingValues,
    showNavigationIcon: Boolean,
    onSetDeviceShowInWidgetChanged: (String, Boolean) -> Unit,
    onNavigationIconClick: () -> Unit,
    onSetNotificationEnabled: (Boolean) -> Unit,
    onSetShowPairedDevice: (Boolean) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    WidgetSettingsScreenLayout(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        contentPadding = contentPadding,
        showNavigationIcon = showNavigationIcon,
        onSetDeviceShowInWidgetChanged = onSetDeviceShowInWidgetChanged,
        onNavigationIconClick = onNavigationIconClick,
        onSetNotificationEnabled = onSetNotificationEnabled,
        onSetShowPairedDevice = onSetShowPairedDevice
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun WidgetSettingsScreenLayout(
    uiState: SettingsViewModel.SettingsScreenUiState,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
    showNavigationIcon: Boolean,
    onSetDeviceShowInWidgetChanged: (String, Boolean) -> Unit,
    onSetNotificationEnabled: (Boolean) -> Unit,
    onSetShowPairedDevice: (Boolean) -> Unit,
    onNavigationIconClick: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AnimatedTextTopAppBar(
                title = stringResource(id = R.string.widget_tab),
                navigationIcon = {
                    if (showNavigationIcon) IconButton(
                        onClick = { onNavigationIconClick() },
                        colors = IconButtonDefaults.iconButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        val insets = combinePadding(padding, contentPadding)
        LazyColumn(
            contentPadding = insets,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = Spacing.l)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            item {
                WidgetSettings(
                    notificationSetting = uiState.notificationSetting,
                    syncColorEnabled = uiState.syncColorEnabled,
                    onSetNotificationEnabled = onSetNotificationEnabled,
                    onSetShowPairedDevice = onSetShowPairedDevice,
                )
            }

            connectedDevices(
                batteryConnectedDevices = uiState.batteryConnectedDevices,
                batteryDeviceSettings = uiState.bonedDeviceSettings,
                onShowInWidgetChanged = onSetDeviceShowInWidgetChanged
            )
        }
    }
}

private fun LazyListScope.connectedDevices(
    batteryConnectedDevices: List<BonedDevice>,
    batteryDeviceSettings: BonnedDeviceSettings,
    onShowInWidgetChanged: (String, Boolean) -> Unit,
) {
    if (batteryConnectedDevices.isNotEmpty()) {
        item {
            SettingsGroup(
                title = stringResource(R.string.bluetooth_device_setting)
            ) {
                batteryConnectedDevices.forEach { device ->
                    val showInWidget = batteryDeviceSettings.settings[device.address]?.showInWidget ?: true
                    BonedDeviceItem(
                        device = device,
                        showInWidget = showInWidget,
                        onShowInWidgetChanged = onShowInWidgetChanged
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun WidgetSettingsScreenPreview() {
    MaterialTheme {
        WidgetSettingsScreen(
            uiState = SettingsViewModel.SettingsScreenUiState(),
            contentPadding = PaddingValues(),
            showNavigationIcon = true,
            onSetDeviceShowInWidgetChanged = { _, _ -> },
            onNavigationIconClick = {},
            onSetNotificationEnabled = {},
            onSetShowPairedDevice = {}
        )
    }
}