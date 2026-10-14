package io.github.ifa.glancewidget.features.battery.component

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.features.battery.BatteryMonitorViewModel
import io.github.ifa.glancewidget.glance.glass.GlassBitmapRenderer
import io.github.ifa.glancewidget.glance.glass.WidgetBitmapCacheKey
import io.github.ifa.glancewidget.model.AddWidgetParams
import io.github.ifa.glancewidget.model.ChartRecord
import io.github.ifa.glancewidget.model.MyDevice
import io.github.ifa.glancewidget.model.WidgetAccent
import io.github.ifa.glancewidget.model.WidgetSetting
import io.github.ifa.glancewidget.model.wrapper.BatteryDataWrapper
import io.github.ifa.glancewidget.ui.component.SelectableChips
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.glass.GlassSurface
import io.github.ifa.glancewidget.ui.glass.LocalIsGlassTheme
import io.github.ifa.glancewidget.ui.model.resolveBatteryTone
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
    var selectedAccentIndex by remember { mutableIntStateOf(0) }

    val styles = listOf(
        WidgetSetting.Style.Horizontal,
        WidgetSetting.Style.Circle,
        WidgetSetting.Style.DotRing,
        WidgetSetting.Style.GlyphBar,
        WidgetSetting.Style.Dot,
        WidgetSetting.Style.MatrixTile,
        WidgetSetting.Style.LiquidTube,
        WidgetSetting.Style.Orbit,
        WidgetSetting.Style.StatPill
    )

    val styleOptions = listOf(
        stringResource(R.string.widget_style_horizontal),
        stringResource(R.string.widget_style_circle),
        stringResource(R.string.widget_style_dot_ring),
        stringResource(R.string.widget_style_glyph_bar),
        stringResource(R.string.widget_style_dot),
        stringResource(R.string.widget_style_matrix_tile),
        stringResource(R.string.widget_style_liquid_tube),
        stringResource(R.string.widget_style_orbit),
        stringResource(R.string.widget_style_stat_pill)
    )

    val accents = listOf(
        WidgetAccent.Red,
        WidgetAccent.Mono,
        WidgetAccent.FollowTone
    )

    val accentOptions = listOf(
        stringResource(R.string.glass_accent_red),
        stringResource(R.string.glass_accent_mono),
        stringResource(R.string.widget_accent_follow_tone)
    )

    val transparencyOptions = listOf(
        stringResource(R.string.widget_transparency_opaque),
        stringResource(R.string.widget_transparency_transparent)
    )

    val selectedStyle = styles[selectedStyleIndex.coerceIn(0, styles.lastIndex)]
    val selectedAccent = accents[selectedAccentIndex.coerceIn(0, accents.lastIndex)]
    val isTransparent = selectedTransparencyIndex == 1
    val isGlassTheme = LocalIsGlassTheme.current

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

            val previewContent: @Composable () -> Unit = {
                val myDevice = uiState.batteryOverall.batteryData.myDevice
                when (selectedStyle) {
                    WidgetSetting.Style.Horizontal -> {
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
                    }
                    WidgetSetting.Style.Circle -> {
                        CirCleBatteryItem(
                            batteryLevel = myDevice.level,
                            isCharging = myDevice.isCharging,
                            deviceType = myDevice.deviceType,
                            transparent = isTransparent
                        )
                    }
                    else -> {
                        val tone = resolveBatteryTone(myDevice.level, myDevice.isCharging, 25f, false)
                        val key = remember(selectedStyle, myDevice.level, myDevice.isCharging, tone, selectedAccent, isTransparent) {
                            WidgetBitmapCacheKey.create(
                                style = selectedStyle,
                                width = 360,
                                height = 200,
                                level = myDevice.level,
                                isCharging = myDevice.isCharging,
                                tone = tone,
                                temperature = 25f,
                                voltage = 4.0f,
                                accent = selectedAccent,
                                opacity = if (isTransparent) 0.35f else 1.0f
                            )
                        }
                        val bitmap = remember(key) {
                            GlassBitmapRenderer.renderOffscreen(key)
                        }
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(120.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            if (isGlassTheme) {
                GlassSurface(
                    shape = AppShape.card,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = Spacing.l)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        previewContent()
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = Spacing.l)
                        .clip(AppShape.card)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) {
                    previewContent()
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

            if (selectedStyle !in listOf(WidgetSetting.Style.Horizontal, WidgetSetting.Style.Circle)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.l),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Text(
                        text = stringResource(R.string.widget_accent),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    SelectableChips(
                        options = accentOptions,
                        selected = selectedAccentIndex,
                        onSelect = { selectedAccentIndex = it },
                        modifier = Modifier.fillMaxWidth()
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
                            widgetStyle = selectedStyle,
                            accent = selectedAccent,
                            glassOpacity = if (isTransparent) 0.35f else 1.0f
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