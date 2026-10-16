package io.github.ifa.glancewidget.features.appusage.component

import android.graphics.Typeface
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.cartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.grouped
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.domain.UsageStatsWrapper
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.glass.GlassSurface
import io.github.ifa.glancewidget.ui.glass.LocalIsGlassTheme
import io.github.ifa.glancewidget.ui.theme.glassLabel
import io.github.ifa.glancewidget.utils.timeMillisToHours
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppUsageChart(
    modifier: Modifier = Modifier,
    appUsageStats: List<UsageStatsWrapper>,
) {
    val isGlassTheme = LocalIsGlassTheme.current
    val modelProducer = remember { CartesianChartModelProducer() }
    val totalTimeVisible = remember(appUsageStats) {
        appUsageStats.map { it.usageStats?.totalTimeVisible?.timeMillisToHours() ?: 0f }
    }
    val totalTimeForeground = remember(appUsageStats) {
        appUsageStats.map { it.usageStats?.totalTimeInForeground?.timeMillisToHours() ?: 0f }
    }

    val columnColor = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary
    )

    LaunchedEffect(appUsageStats) {
        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                columnSeries {
                    series(totalTimeVisible)
                    series(totalTimeForeground)
                }
            }
        }
    }

    val column = rememberLineComponent(
        color = MaterialTheme.colorScheme.primary,
        thickness = 14.dp,
        shape = CorneredShape.rounded(topLeftDp = 6f, topRightDp = 6f)
    )
    val resources = LocalResources.current
    val typeface = resources.getFont(R.font.google_sans_flex_400)
    val label = rememberTextComponent(
        typeface = Typeface.create(typeface, Typeface.BOLD),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        minWidth = TextComponent.MinWidth.fixed(20f),
    )
    val legendColumnKeys = listOf(
        stringResource(R.string.screen_time),
        stringResource(R.string.foreground)
    )

    val content = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.l)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isGlassTheme) stringResource(R.string.usage_breakdown).uppercase()
                    else stringResource(R.string.usage_breakdown),
                    style = if (isGlassTheme) MaterialTheme.typography.glassLabel else MaterialTheme.typography.titleMedium,
                    color = if (isGlassTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(R.string.usage_time),
                    style = MaterialTheme.typography.glassLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberColumnCartesianLayer(
                            columnProvider = object : ColumnCartesianLayer.ColumnProvider {
                                override fun getColumn(
                                    entry: ColumnCartesianLayerModel.Entry,
                                    seriesIndex: Int,
                                    extraStore: ExtraStore,
                                ) = column.copy(color = columnColor[seriesIndex % columnColor.size].toArgb())

                                override fun getWidestSeriesColumn(seriesIndex: Int, extraStore: ExtraStore) = column
                            },
                            mergeMode = {
                                ColumnCartesianLayer.MergeMode.grouped(
                                    columnSpacing = 2.dp
                                )
                            },
                        ),
                        startAxis = VerticalAxis.rememberStart(
                            label = label,
                            guideline = rememberAxisGuidelineComponent(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            )
                        ),
                        bottomAxis = HorizontalAxis.rememberBottom(
                            valueFormatter = object : CartesianValueFormatter {
                                override fun format(
                                    context: CartesianMeasuringContext,
                                    value: Double,
                                    verticalAxisPosition: Axis.Position.Vertical?,
                                ): String {
                                    val appName = appUsageStats.getOrNull(value.toInt())?.appName.orEmpty()
                                    return appName.split(" ").firstOrNull()?.take(8).orEmpty()
                                }
                            },
                            guideline = null,
                            label = label
                        ),
                        layerPadding = cartesianLayerPadding(
                            scalableStartPadding = Spacing.s,
                            scalableEndPadding = Spacing.s
                        ),
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier.fillMaxWidth(),
                    zoomState = rememberVicoZoomState(zoomEnabled = false)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                legendColumnKeys.forEachIndexed { index, key ->
                    LegendItem(
                        label = key,
                        color = columnColor[index % columnColor.size]
                    )
                }
            }
        }
    }

    if (isGlassTheme) {
        GlassSurface(
            modifier = modifier.fillMaxWidth(),
            shape = AppShape.card
        ) {
            content()
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = AppShape.card,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            content()
        }
    }
}

@Composable
private fun LegendItem(
    label: String,
    color: Color
) {
    Row(
        modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Spacing.s)
                .background(color, shape = AppShape.chip)
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.glassLabel,
            modifier = Modifier.padding(start = Spacing.xs)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@ThemePreviews
@Composable
fun AppUsageChartPreview() {
    MaterialTheme {
        AppUsageChart(
            appUsageStats = emptyList()
        )
    }
}
