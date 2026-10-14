package io.github.ifa.glancewidget.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ifa.glancewidget.features.main.MainScreenTab
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.gravity.PillWidth

import androidx.compose.foundation.BorderStroke
import io.github.ifa.glancewidget.ui.glass.GlassBorder
import io.github.ifa.glancewidget.ui.glass.GlassHighlight
import io.github.ifa.glancewidget.ui.glass.GlassSurfaceContainerHigh
import io.github.ifa.glancewidget.ui.glass.GlassTextPrimary
import io.github.ifa.glancewidget.ui.glass.GlassTextSecondary
import io.github.ifa.glancewidget.ui.glass.LocalIsGlassTheme

@Composable
fun GravityNavPill(
    selectedTab: MainScreenTab,
    onTabSelected: (MainScreenTab) -> Unit,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    onBoundsChanged: (Rect) -> Unit = {}
) {
    val isGlassTheme = LocalIsGlassTheme.current
    val containerColor = if (isGlassTheme) GlassSurfaceContainerHigh else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (isGlassTheme) GlassTextPrimary else MaterialTheme.colorScheme.onPrimaryContainer
    val borderStroke = if (isGlassTheme) BorderStroke(1.dp, GlassBorder) else null

    Surface(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                if (coordinates.isAttached) {
                    onBoundsChanged(coordinates.boundsInRoot())
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .width(PillWidth)
            .wrapContentHeight(),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        border = borderStroke,
        shadowElevation = if (isGlassTheme) 0.dp else 6.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = Spacing.m, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.CenterVertically)
        ) {
            MainScreenTab.entries.forEach { item ->
                val isSelected = item == selectedTab
                val labelText = stringResource(item.label)

                if (isSelected) {
                    val selectedBgColor = if (isGlassTheme) GlassHighlight else MaterialTheme.colorScheme.primary
                    val selectedIconColor = if (isGlassTheme) GlassTextPrimary else MaterialTheme.colorScheme.onPrimary
                    val selectedTextColor = if (isGlassTheme) GlassTextPrimary else MaterialTheme.colorScheme.onPrimary

                    Box(
                        modifier = Modifier
                            .size(width = 48.dp, height = if (isGlassTheme) 68.dp else 60.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                color = selectedBgColor,
                                shape = RoundedCornerShape(22.dp)
                            )
                            .semantics {
                                role = Role.Tab
                                selected = true
                                contentDescription = labelText
                            }
                            .clickable { onTabSelected(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(item.selectedIcon),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = selectedIconColor
                            )
                            Text(
                                text = labelText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    lineHeight = 12.sp
                                ),
                                color = selectedTextColor,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isGlassTheme) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 3.dp)
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                } else {
                    val unselectedColor = if (isGlassTheme) GlassTextSecondary else MaterialTheme.colorScheme.onPrimaryContainer
                    Box(
                        modifier = Modifier
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .semantics {
                                role = Role.Tab
                                selected = false
                                contentDescription = labelText
                            }
                            .clickable { onTabSelected(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = unselectedColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun GravityNavPillPreview() {
    MaterialTheme {
        GravityNavPill(
            selectedTab = MainScreenTab.Widget,
            onTabSelected = {}
        )
    }
}
