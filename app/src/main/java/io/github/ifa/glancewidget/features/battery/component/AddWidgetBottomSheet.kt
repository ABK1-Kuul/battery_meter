package io.github.ifa.glancewidget.features.battery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.features.battery.BatteryMonitorViewModel
import io.github.ifa.glancewidget.model.AddWidgetParams
import io.github.ifa.glancewidget.model.ChartRecord
import io.github.ifa.glancewidget.model.MyDevice
import io.github.ifa.glancewidget.model.WidgetSetting
import io.github.ifa.glancewidget.model.wrapper.BatteryDataWrapper
import io.github.ifa.glancewidget.ui.component.SelectableChips
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWidgetBottomSheet(
    uiState: BatteryMonitorViewModel.BatteryMonitorScreenUiState,
    onDisMiss: () -> Unit,
    onClickAddWidget: (AddWidgetParams) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()
    var selectedStyleIndex by remember { mutableIntStateOf(0) }
    var selectedTransparencyIndex by remember { mutableIntStateOf(0) }

    val styleOptions = listOf(
        stringResource(R.string.widget_style_horizontal),
        stringResource(R.string.widget_style_circle)
    )
    val transparencyOptions = listOf(
        stringResource(R.string.widget_transparency_opaque),
        stringResource(R.string.widget_transparency_transparent)
    )

    val selectedStyle = if (selectedStyleIndex == 0) WidgetSetting.Style.Horizontal else WidgetSetting.Style.Circle
    val isTransparent = selectedTransparencyIndex == 1

    ModalBottomSheet(
        onDismissRequest = {
            onDisMiss()
            scope.launch { sheetState.hide() }
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            Text(
                text = stringResource(id = R.string.add_your_widget),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.l),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(horizontal = Spacing.l)
                    .clip(AppShape.card)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                val myDevice = uiState.batteryOverall.batteryData.myDevice
                if (selectedStyle == WidgetSetting.Style.Horizontal) {
                    BatteryItem(
                        deviceType = myDevice.deviceType,
                        percent = myDevice.level,
                        isCharging = myDevice.isCharging,
                        deviceName = myDevice.name,
                        isTransparent = isTransparent,
                        modifier = Modifier
                            .height(84.dp)
                            .fillMaxWidth(0.85f)
                    )
                } else {
                    CirCleBatteryItem(
                        batteryLevel = myDevice.level,
                        isCharging = myDevice.isCharging,
                        deviceType = myDevice.deviceType,
                        transparent = isTransparent
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.l),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = stringResource(R.string.widget_tab),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                SelectableChips(
                    options = styleOptions,
                    selected = selectedStyleIndex,
                    onSelect = { selectedStyleIndex = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.l),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = stringResource(R.string.show_transparent),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                SelectableChips(
                    options = transparencyOptions,
                    selected = selectedTransparencyIndex,
                    onSelect = { selectedTransparencyIndex = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            Button(
                onClick = {
                    onClickAddWidget(
                        AddWidgetParams(
                            isTransparent = isTransparent,
                            widgetStyle = selectedStyle
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.l)
                    .height(56.dp),
                shape = AppShape.card
            ) {
                Text(
                    text = stringResource(id = R.string.add_widget),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@ThemePreviews
@Composable
fun AddWidgetBottomSheetPreview() {
    MaterialTheme {
        AddWidgetBottomSheet(
            uiState = BatteryMonitorViewModel.BatteryMonitorScreenUiState(
                batteryOverall = BatteryDataWrapper(
                    batteryData = io.github.ifa.glancewidget.model.BatteryData(
                        myDevice = MyDevice(
                            level = 80,
                            isCharging = true,
                            name = "Pixel 8"
                        ),
                        batteryConnectedDevices = emptyList()
                    )
                ),
                chartTrackingData = ChartRecord()
            ),
            onDisMiss = {},
            onClickAddWidget = {}
        )
    }
}