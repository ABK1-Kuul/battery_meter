package io.github.ifa.glancewidget.ui.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.theme.metricMedium
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

import io.github.ifa.glancewidget.ui.glass.DotMatrixText
import io.github.ifa.glancewidget.ui.glass.GlassSurface
import io.github.ifa.glancewidget.ui.glass.GlassTextPrimary
import io.github.ifa.glancewidget.ui.glass.GlassTextSecondary
import io.github.ifa.glancewidget.ui.glass.LocalIsGlassTheme
import io.github.ifa.glancewidget.ui.theme.glassLabel
import io.github.ifa.glancewidget.ui.theme.glassMetricMedium

@Composable
fun MetricTile(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    series: ImmutableList<Float>? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null
) {
    val isGlassTheme = LocalIsGlassTheme.current
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    if (isGlassTheme) {
        GlassSurface(
            modifier = modifier
                .heightIn(min = 112.dp)
                .then(clickableModifier),
            shape = AppShape.card
        ) {
            Column(
                modifier = Modifier.padding(Spacing.l),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = accent
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.glassLabel,
                        color = GlassTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.s))

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    val canRenderDotMatrix = value.all { it.isDigit() || it in ".%:-+ /CWVAMHmh°" }
                    if (canRenderDotMatrix && value.isNotEmpty()) {
                        DotMatrixText(
                            text = value,
                            dotSize = 2.dp,
                            dotSpacing = 1.dp,
                            charSpacing = 2.dp,
                            tint = GlassTextPrimary
                        )
                    } else {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.glassMetricMedium,
                            color = GlassTextPrimary
                        )
                    }
                    if (unit.isNotBlank()) {
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = unit.uppercase(),
                            style = MaterialTheme.typography.glassLabel,
                            color = GlassTextSecondary,
                            modifier = Modifier.padding(bottom = Spacing.xs)
                        )
                    }
                }

                if (series != null) {
                    Spacer(modifier = Modifier.height(Spacing.s))
                    Sparkline(
                        values = series,
                        color = accent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    )
                }
            }
        }
    } else {
        Surface(
            modifier = modifier
                .heightIn(min = 112.dp)
                .then(clickableModifier),
            shape = AppShape.card,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier.padding(Spacing.l),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = accent
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.s))

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.metricMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (unit.isNotBlank()) {
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = Spacing.xs)
                        )
                    }
                }

                if (series != null) {
                    Spacer(modifier = Modifier.height(Spacing.s))
                    Sparkline(
                        values = series,
                        color = accent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun MetricTilePreview() {
    MaterialTheme {
        MetricTile(
            label = "Temperature",
            value = "34.5",
            unit = "°C",
            icon = Icons.Rounded.Thermostat,
            series = persistentListOf(30f, 32f, 31f, 34.5f),
            modifier = Modifier.width(180.dp)
        )
    }
}
