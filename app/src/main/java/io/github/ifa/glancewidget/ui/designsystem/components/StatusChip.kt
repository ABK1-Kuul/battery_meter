package io.github.ifa.glancewidget.ui.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.glass.GlassBorder
import io.github.ifa.glancewidget.ui.glass.GlassSurfaceContainerHigh
import io.github.ifa.glancewidget.ui.glass.GlassTextPrimary
import io.github.ifa.glancewidget.ui.glass.LocalIsGlassTheme
import io.github.ifa.glancewidget.ui.theme.glassLabel

data class ChipData(
    val label: String,
    val icon: ImageVector? = null
)

@Composable
fun StatusChip(
    label: String,
    icon: ImageVector?,
    modifier: Modifier = Modifier
) {
    val isGlassTheme = LocalIsGlassTheme.current
    Surface(
        modifier = modifier,
        shape = AppShape.chip,
        color = if (isGlassTheme) GlassSurfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = if (isGlassTheme) BorderStroke(1.dp, GlassBorder) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isGlassTheme) GlassTextPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
            }
            Text(
                text = if (isGlassTheme) label.uppercase() else label,
                style = if (isGlassTheme) MaterialTheme.typography.glassLabel else MaterialTheme.typography.labelMedium,
                color = if (isGlassTheme) GlassTextPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@ThemePreviews
@Composable
fun StatusChipPreview() {
    MaterialTheme {
        StatusChip(
            label = "Fast Charging",
            icon = Icons.Rounded.Bolt
        )
    }
}
