package io.github.ifa.glancewidget.features.appusage.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.domain.UsageStatsWrapper
import io.github.ifa.glancewidget.features.appusage.AppUsageScreenUiState
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.glass.GlassSurface
import io.github.ifa.glancewidget.ui.glass.LocalIsGlassTheme
import io.github.ifa.glancewidget.ui.gravity.gravityItem
import io.github.ifa.glancewidget.ui.gravity.pillAvoidance
import io.github.ifa.glancewidget.ui.theme.glassLabel
import io.github.ifa.glancewidget.ui.theme.glassMetricLarge
import io.github.ifa.glancewidget.ui.theme.metricLarge

fun Long.formatDuration(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return buildString {
        if (hours > 0) append("${hours}h ")
        if (minutes > 0 || hours > 0) append("${minutes}m ")
        if (hours == 0L && minutes < 10) append("${seconds}s")
    }.trim().ifEmpty { "0s" }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppUsageList(
    modifier: Modifier = Modifier,
    appUsageStats: List<UsageStatsWrapper>,
    usageRange: AppUsageScreenUiState.UsageRange,
    onUsageRangeChange: (AppUsageScreenUiState.UsageRange) -> Unit,
    onAppUsageClick: (String) -> Unit,
) {
    val options = remember {
        AppUsageScreenUiState.UsageRange.options()
    }
    val lazyListState = rememberLazyListState()
    val isGlassTheme = LocalIsGlassTheme.current

    val maxUsageTime = remember(appUsageStats) {
        val max = appUsageStats.maxOfOrNull { it.usageStats?.totalTimeInForeground ?: 0L } ?: 1L
        if (max > 0L) max else 1L
    }

    val totalForegroundTime = remember(appUsageStats) {
        appUsageStats.sumOf { it.usageStats?.totalTimeInForeground ?: 0L }
    }

    val topApp = remember(appUsageStats) {
        appUsageStats.firstOrNull()
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = lazyListState,
        contentPadding = PaddingValues(bottom = Spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.l, vertical = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                options.forEach { range ->
                    val isSelected = range == usageRange
                    val chipShape = AppShape.chip

                    if (isGlassTheme) {
                        GlassSurface(
                            shape = chipShape,
                            modifier = Modifier
                                .weight(1f)
                                .clip(chipShape)
                                .clickable { onUsageRangeChange(range) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.05f)
                                    )
                                    .padding(vertical = Spacing.s),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(range.displayName()).uppercase(),
                                    style = MaterialTheme.typography.glassLabel,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = chipShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .weight(1f)
                                .clip(chipShape)
                                .clickable { onUsageRangeChange(range) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Spacing.s),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(range.displayName()),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        if (appUsageStats.isNotEmpty()) {
            item {
                val overviewContent = @Composable {
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
                                text = if (isGlassTheme) stringResource(R.string.total_screen_time).uppercase()
                                else stringResource(R.string.total_screen_time),
                                style = if (isGlassTheme) MaterialTheme.typography.glassLabel else MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = stringResource(usageRange.displayName()).uppercase(),
                                style = MaterialTheme.typography.glassLabel,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        Text(
                            text = totalForegroundTime.formatDuration(),
                            style = if (isGlassTheme) MaterialTheme.typography.glassMetricLarge else MaterialTheme.typography.metricLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (topApp != null) {
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.top_used_app) + ": ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = topApp.appName + " (" + (topApp.usageStats?.totalTimeInForeground ?: 0L).formatDuration() + ")",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                if (isGlassTheme) {
                    GlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.l, vertical = Spacing.xs)
                            .pillAvoidance()
                            .gravityItem("usage_hero"),
                        shape = AppShape.hero
                    ) {
                        overviewContent()
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.l, vertical = Spacing.xs)
                            .pillAvoidance()
                            .gravityItem("usage_hero"),
                        shape = AppShape.hero,
                        color = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        overviewContent()
                    }
                }
            }

            item {
                AppUsageChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.l, vertical = Spacing.xs)
                        .pillAvoidance()
                        .gravityItem("usage_chart"),
                    appUsageStats = appUsageStats.take(4)
                )
            }
        }

        if (appUsageStats.isEmpty()) {
            item {
                val emptyContent = @Composable {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.xxl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.HourglassEmpty,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Spacing.xxl)
                        )

                        Spacer(modifier = Modifier.height(Spacing.m))

                        Text(
                            text = stringResource(R.string.no_usage_data),
                            style = if (isGlassTheme) MaterialTheme.typography.glassLabel else MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isGlassTheme) {
                    GlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.l)
                            .pillAvoidance()
                            .gravityItem("usage_empty"),
                        shape = AppShape.card
                    ) {
                        emptyContent()
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.l)
                            .pillAvoidance()
                            .gravityItem("usage_empty"),
                        shape = AppShape.card,
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        emptyContent()
                    }
                }
            }
        }

        items(
            items = appUsageStats,
            key = { appUsage -> appUsage.usageStats?.packageName.orEmpty() }
        ) { appUsage ->
            val usage = appUsage.usageStats?.totalTimeInForeground ?: 0L
            val progress = (usage.toFloat() / maxUsageTime.toFloat()).coerceIn(0f, 1f)
            AppUsageItem(
                appName = appUsage.appName,
                appIcon = appUsage.appIcon,
                usageTime = usage,
                progress = progress,
                modifier = Modifier
                    .pillAvoidance()
                    .gravityItem("usage_${appUsage.usageStats?.packageName}")
                    .animateItem(
                        fadeInSpec = tween(400),
                        fadeOutSpec = tween(400),
                        placementSpec = tween(400),
                    ),
                onAppClick = {
                    onAppUsageClick(appUsage.usageStats?.packageName.orEmpty())
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@ThemePreviews
@Composable
fun AppUsageListPreview() {
    MaterialTheme {
        AppUsageList(
            appUsageStats = emptyList(),
            usageRange = AppUsageScreenUiState.UsageRange.TODAY,
            onUsageRangeChange = {},
            onAppUsageClick = {}
        )
    }
}
