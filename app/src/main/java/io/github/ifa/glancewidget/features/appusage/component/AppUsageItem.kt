package io.github.ifa.glancewidget.features.appusage.component

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.glass.GlassSurface
import io.github.ifa.glancewidget.ui.glass.LocalIsGlassTheme
import io.github.ifa.glancewidget.ui.theme.glassLabel
import io.github.ifa.glancewidget.ui.theme.glassMetricSmall
import io.github.ifa.glancewidget.ui.theme.metricSmall

@Composable
fun AppUsageItem(
    modifier: Modifier = Modifier,
    appName: String,
    appIcon: Drawable,
    usageTime: Long,
    progress: Float = 0f,
    onAppClick: () -> Unit
) {
    val isGlassTheme = LocalIsGlassTheme.current
    val itemContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                Image(
                    bitmap = appIcon.toBitmap().asImageBitmap(),
                    contentDescription = appName,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(AppShape.inner),
                    contentScale = ContentScale.Fit
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (progress > 0f) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.glassLabel,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = usageTime.formatDuration(),
                    style = if (isGlassTheme) MaterialTheme.typography.glassMetricSmall else MaterialTheme.typography.metricSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Spacing.xs)
                    .clip(AppShape.chip),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        }
    }

    if (isGlassTheme) {
        GlassSurface(
            shape = AppShape.card,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.l, vertical = Spacing.xs)
                .clip(AppShape.card)
                .clickable(onClick = onAppClick)
        ) {
            itemContent()
        }
    } else {
        Surface(
            onClick = onAppClick,
            shape = AppShape.card,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.l, vertical = Spacing.xs)
        ) {
            itemContent()
        }
    }
}

@ThemePreviews
@Composable
fun AppUsageItemPreview() {
    val context = LocalContext.current
    val defaultAppIcon = context.packageManager.defaultActivityIcon
    MaterialTheme {
        AppUsageItem(
            appName = "YouTube",
            appIcon = defaultAppIcon,
            usageTime = 7200000L,
            progress = 0.75f,
            onAppClick = {}
        )
    }
}