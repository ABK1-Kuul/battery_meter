package io.github.ifa.glancewidget.features.settings.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.FormatColorFill
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dotsdev.material3color.getLightScheme
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.model.ThemeTypeColor
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing

@Composable
fun SelectablePaletteItem(
    modifier: Modifier,
    themeTypeColor: ThemeTypeColor,
    isSelected: Boolean = false,
    onClick: (ThemeTypeColor) -> Unit = {}
) {
    if (themeTypeColor == ThemeTypeColor.System) {
        DefaultPaletteItem(modifier, themeTypeColor, isSelected, onClick)
    } else {
        PaletteItem(
            modifier
                .clip(AppShape.inner)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .clickable { onClick(themeTypeColor) }
                .padding(Spacing.m),
            getLightScheme(themeTypeColor.code),
            isSelected
        )
    }
}

@Composable
private fun DefaultPaletteItem(
    modifier: Modifier,
    themeTypeColor: ThemeTypeColor,
    isSelected: Boolean = false,
    onClick: (ThemeTypeColor) -> Unit = {}
) {
    val selectedShape by animateFloatAsState(
        targetValue = if (isSelected) 0.5f else 1.0f,
        label = "",
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    Box(
        modifier
            .clip(AppShape.inner)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable { onClick(themeTypeColor) }
            .padding(Spacing.m),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painterResource(R.drawable.ic_contrast),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            tint = MaterialTheme.colorScheme.onSurface
        )

        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)),
            exit = scaleOut(spring(Spring.DampingRatioMediumBouncy))
        ) {
            Box(
                Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun PaletteItem(
    modifier: Modifier,
    colorScheme: ColorScheme,
    isSelected: Boolean = false
) {
    val selectedShape by animateFloatAsState(
        targetValue = if (isSelected) 0.5f else 1.0f,
        label = "",
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(colorScheme.tertiary)
            )
        }

        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)),
            exit = scaleOut(spring(Spring.DampingRatioMediumBouncy)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@ThemePreviews
@Composable
fun SelectablePaletteItemPreview() {
    MaterialTheme {
        SelectablePaletteItem(
            modifier = Modifier.size(64.dp),
            themeTypeColor = ThemeTypeColor.FlowerBlue,
            isSelected = true
        )
    }
}