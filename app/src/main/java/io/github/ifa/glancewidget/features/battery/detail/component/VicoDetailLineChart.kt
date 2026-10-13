package io.github.ifa.glancewidget.features.battery.detail.component

import android.graphics.Typeface
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.cartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.component.TextComponent
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.features.battery.component.rememberMarker
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun List<Float>.paddedRange(minSpan: Double): Pair<Double, Double> {
    val lo = minOrNull()?.toDouble() ?: 0.0
    val hi = maxOrNull()?.toDouble() ?: 1.0
    val span = (hi - lo).coerceAtLeast(minSpan)
    val mid = (hi + lo) / 2.0
    return (mid - span * 0.6) to (mid + span * 0.6)
}

@Composable
fun VicoDetailLineChart(
    values: List<Float>,
    timestamps: List<Long>,
    accentColor: Color,
    valueFormatter: (Float) -> String,
    modifier: Modifier = Modifier,
    minSpan: Double = 1.0,
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val (minY, maxY) = remember(values, minSpan) { values.paddedRange(minSpan) }

    LaunchedEffect(values) {
        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                lineSeries {
                    series(if (values.isEmpty()) listOf(0f) else values)
                }
            }
        }
    }

    val resources = LocalResources.current
    val typeface = resources.getFont(R.font.google_sans_flex_400)
    val axisLabel = rememberTextComponent(
        typeface = Typeface.create(typeface, Typeface.NORMAL),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        minWidth = TextComponent.MinWidth.fixed(24f),
    )

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val bottomFormatter = remember(timestamps, timeFormat) {
        object : CartesianValueFormatter {
            override fun format(
                context: CartesianMeasuringContext,
                value: Double,
                verticalAxisPosition: Axis.Position.Vertical?,
            ): String {
                val index = value.toInt()
                val ts = timestamps.getOrNull(index) ?: return ""
                return timeFormat.format(Date(ts))
            }
        }
    }

    val startFormatter = remember(valueFormatter) {
        object : CartesianValueFormatter {
            override fun format(
                context: CartesianMeasuringContext,
                value: Double,
                verticalAxisPosition: Axis.Position.Vertical?,
            ): String {
                return valueFormatter(value.toFloat())
            }
        }
    }

    val marker = rememberMarker()

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        remember(accentColor) { LineCartesianLayer.LineFill.single(fill(accentColor)) }
                    )
                ),
                rangeProvider = remember(minY, maxY) {
                    CartesianLayerRangeProvider.fixed(minY = minY, maxY = maxY)
                },
                pointSpacing = 8.dp
            ),
            startAxis = VerticalAxis.rememberStart(
                label = axisLabel,
                valueFormatter = startFormatter,
                guideline = rememberAxisGuidelineComponent(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                )
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = axisLabel,
                valueFormatter = bottomFormatter,
                guideline = null,
            ),
            marker = marker,
            layerPadding = cartesianLayerPadding(
                scalableStartPadding = 12.dp,
                scalableEndPadding = 12.dp
            ),
        ),
        modelProducer = modelProducer,
        modifier = modifier,
        zoomState = rememberVicoZoomState(zoomEnabled = false),
    )
}

@ThemePreviews
@Composable
fun VicoDetailLineChartPreview() {
    MaterialTheme {
        VicoDetailLineChart(
            values = listOf(30.0f, 32.5f, 31.0f, 33.2f, 34.0f),
            timestamps = listOf(1000L, 2000L, 3000L, 4000L, 5000L),
            accentColor = MaterialTheme.colorScheme.primary,
            valueFormatter = { String.format(Locale.getDefault(), "%.1f", it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )
    }
}
