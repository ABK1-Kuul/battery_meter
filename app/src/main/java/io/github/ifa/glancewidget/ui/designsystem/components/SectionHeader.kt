package io.github.ifa.glancewidget.ui.designsystem.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing

@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(vertical = Spacing.s)
    )
}

@ThemePreviews
@Composable
fun SectionHeaderPreview() {
    MaterialTheme {
        SectionHeader(text = "Connected Devices")
    }
}
