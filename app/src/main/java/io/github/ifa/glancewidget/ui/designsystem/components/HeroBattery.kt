package io.github.ifa.glancewidget.ui.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.ifa.glancewidget.ui.component.AnimatedCounter
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppMotion
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.model.BatteryTone
import io.github.ifa.glancewidget.ui.model.BatteryUiModel
import io.github.ifa.glancewidget.ui.model.UiText
import io.github.ifa.glancewidget.ui.model.asString
import io.github.ifa.glancewidget.ui.model.colors
import io.github.ifa.glancewidget.ui.theme.metricLarge

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import io.github.ifa.glancewidget.ui.glass.DotMatrixText
import io.github.ifa.glancewidget.ui.glass.GlassBorder
import io.github.ifa.glancewidget.ui.glass.GlassSurface
import io.github.ifa.glancewidget.ui.glass.GlassTextPrimary
import io.github.ifa.glancewidget.ui.glass.GlassTextSecondary
import io.github.ifa.glancewidget.ui.glass.LocalIsGlassTheme
import io.github.ifa.glancewidget.ui.theme.glassLabel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun HeroBattery(
    model: BatteryUiModel,
    chips: List<ChipData>,
    modifier: Modifier = Modifier
) {
    val isGlassTheme = LocalIsGlassTheme.current
    val toneColors = model.tone.colors()
    val animatedProgress by animateFloatAsState(
        targetValue = (model.level / 100f).coerceIn(0f, 1f),
        animationSpec = AppMotion.spatial(),
        label = "heroProgress"
    )

    val heroDescription = "${model.level}% ${model.statusLine.asString()}"

    if (isGlassTheme) {
        GlassSurface(
            modifier = modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = heroDescription
                },
            shape = AppShape.hero
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(Spacing.m))
                DotMatrixText(
                    text = "${model.level}%",
                    dotSize = 4.dp,
                    dotSpacing = 1.5.dp,
                    charSpacing = 4.dp,
                    tint = GlassTextPrimary
                )
                Spacer(modifier = Modifier.height(Spacing.l))
                val totalDots = 22
                val litDots = (animatedProgress * totalDots).toInt().coerceIn(0, totalDots)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until totalDots) {
                        val isLit = i < litDots
                        val isHead = isLit && (i == litDots - 1)
                        val dotColor = when {
                            isHead -> MaterialTheme.colorScheme.primary
                            isLit -> io.github.ifa.glancewidget.ui.glass.GlassAccentMono
                            else -> GlassBorder
                        }
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.m))
                Text(
                    text = model.statusLine.asString().uppercase(),
                    style = MaterialTheme.typography.glassLabel,
                    color = GlassTextSecondary
                )
                if (chips.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.m))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        chips.forEach { chip ->
                            StatusChip(label = chip.label, icon = chip.icon)
                        }
                    }
                }
            }
        }
    } else {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = heroDescription
                },
            shape = AppShape.hero,
            color = toneColors.container
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.padding(Spacing.m),
                    contentAlignment = Alignment.Center
                ) {
                    CircularWavyProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(180.dp),
                        color = toneColors.accent,
                        trackColor = toneColors.onContainer.copy(alpha = 0.12f),
                        stroke = Stroke(
                            width = 16f,
                            cap = StrokeCap.Round
                        ),
                        wavelength = 32.dp,
                        amplitude = if (model.isCharging) { _ -> 1f } else { _ -> 0f }
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            AnimatedCounter(
                                count = model.level,
                                style = MaterialTheme.typography.metricLarge,
                                fontWeight = FontWeight.Bold,
                                color = toneColors.onContainer
                            )
                            Text(
                                text = "%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = toneColors.onContainer,
                                modifier = Modifier.padding(bottom = Spacing.s)
                            )
                        }

                    }
                }

                Spacer(modifier = Modifier.height(Spacing.s))
                Text(
                    text = model.statusLine.asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = toneColors.onContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                if (chips.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.m))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(Spacing.s),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        chips.forEach { chip ->
                            StatusChip(label = chip.label, icon = chip.icon)
                        }
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun HeroBatteryPreview() {
    MaterialTheme {
        HeroBattery(
            model = BatteryUiModel(
                level = 82,
                tone = BatteryTone.Charging,
                statusLine = UiText.Plain("32 minutes to full"),
                chargerChip = UiText.Plain("AC"),
                powerText = "18.5 W",
                temperatureText = "31.2 °C",
                voltageText = "4.15 V",
                currentText = "2400 mA",
                isCharging = true
            ),
            chips = listOf(
                ChipData(label = "AC", icon = Icons.Rounded.Bolt),
                ChipData(label = "18.5 W", icon = null),
                ChipData(label = "31.2 °C", icon = Icons.Rounded.Thermostat)
            )
        )
    }
}
