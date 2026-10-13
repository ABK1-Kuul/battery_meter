package io.github.ifa.glancewidget.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ifa.glancewidget.R

val googleFlex400 = FontFamily(Font(R.font.google_sans_flex_400))
val googleFlex600 = FontFamily(Font(R.font.google_sans_flex_600))
val TYPOGRAPHY = Typography()

object Type {
    val typography = Typography(
        displayLarge = TYPOGRAPHY.displayLarge.copy(
            fontFamily = googleFlex600,
            fontFeatureSettings = "ss02, dlig"
        ),
        displayMedium = TYPOGRAPHY.displayMedium.copy(
            fontFamily = googleFlex600,
            fontFeatureSettings = "ss02, dlig"
        ),
        displaySmall = TYPOGRAPHY.displaySmall.copy(
            fontFamily = googleFlex600,
            fontFeatureSettings = "ss02, dlig"
        ),
        headlineLarge = TYPOGRAPHY.headlineLarge.copy(
            fontFamily = googleFlex600,
            fontFeatureSettings = "ss02, dlig"
        ),
        headlineMedium = TYPOGRAPHY.headlineMedium.copy(
            fontFamily = googleFlex600,
            fontFeatureSettings = "ss02, dlig"
        ),
        headlineSmall = TYPOGRAPHY.headlineSmall.copy(
            fontFamily = googleFlex600,
            fontFeatureSettings = "ss02, dlig"
        ),
        titleLarge = TYPOGRAPHY.titleLarge.copy(
            fontFamily = googleFlex600,
            fontFeatureSettings = "ss02, dlig"
        ),
        titleMedium = TYPOGRAPHY.titleMedium.copy(
            fontFamily = googleFlex600,
            fontFeatureSettings = "ss02, dlig"
        ),
        titleSmall = TYPOGRAPHY.titleSmall.copy(
            fontFamily = googleFlex600,
            fontFeatureSettings = "ss02, dlig"
        ),
        bodyLarge = TYPOGRAPHY.bodyLarge.copy(
            fontFamily = googleFlex400,
            fontFeatureSettings = "ss02"
        ),
        bodyMedium = TYPOGRAPHY.bodyMedium.copy(
            fontFamily = googleFlex400,
            fontFeatureSettings = "ss02"
        ),
        bodySmall = TYPOGRAPHY.bodySmall.copy(
            fontFamily = googleFlex400,
            fontFeatureSettings = "ss02"
        ),
        labelLarge = TYPOGRAPHY.labelLarge.copy(
            fontFamily = googleFlex600,
            fontFeatureSettings = "ss02"
        ),
        labelMedium = TYPOGRAPHY.labelMedium.copy(
            fontFamily = googleFlex400,
            fontFeatureSettings = "ss02"
        ),
        labelSmall = TYPOGRAPHY.labelSmall.copy(
            fontFamily = googleFlex400,
            fontFeatureSettings = "ss02"
        )
    )
}

val Typography.metricLarge: TextStyle
    get() = TextStyle(
        fontFamily = googleFlex600,
        fontSize = 56.sp,
        fontFeatureSettings = "tnum, ss02"
    )

val Typography.metricMedium: TextStyle
    get() = TextStyle(
        fontFamily = googleFlex600,
        fontSize = 28.sp,
        fontFeatureSettings = "tnum, ss02"
    )

val Typography.metricSmall: TextStyle
    get() = TextStyle(
        fontFamily = googleFlex600,
        fontSize = 18.sp,
        fontFeatureSettings = "tnum, ss02"
    )

@Preview(showBackground = true)
@Composable
fun TypographyPreview() {
    MaterialTheme(typography = Type.typography) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Metric Large 98%", style = MaterialTheme.typography.metricLarge)
            Text(text = "Metric Medium 45W", style = MaterialTheme.typography.metricMedium)
            Text(text = "Metric Small 32.5°C", style = MaterialTheme.typography.metricSmall)
            Text(text = "Headline Large", style = MaterialTheme.typography.headlineLarge)
            Text(text = "Title Large", style = MaterialTheme.typography.titleLarge)
            Text(text = "Body Large Content", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Body Medium Content", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Label Large Button", style = MaterialTheme.typography.labelLarge)
            Text(text = "Label Small Info", style = MaterialTheme.typography.labelSmall)
        }
    }
}