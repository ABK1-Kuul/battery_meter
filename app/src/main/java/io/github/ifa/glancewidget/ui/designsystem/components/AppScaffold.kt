package io.github.ifa.glancewidget.ui.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeState
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.glass.GlassBackdrop
import io.github.ifa.glancewidget.ui.glass.GlassBlack
import io.github.ifa.glancewidget.ui.glass.LocalHazeState
import io.github.ifa.glancewidget.ui.glass.LocalIsGlassTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppScaffold(
    title: String,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    snackbarHost: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val isGlassTheme = LocalIsGlassTheme.current
    val hazeState = remember { HazeState() }

    CompositionLocalProvider(
        LocalHazeState provides if (isGlassTheme) hazeState else null
    ) {
        val topAppBarColors = if (isGlassTheme) {
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface
            )
        } else {
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surface
            )
        }

        Scaffold(
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    subtitle = {},
                    titleHorizontalAlignment = Alignment.CenterHorizontally,
                    scrollBehavior = scrollBehavior,
                    colors = topAppBarColors,
                    actions = actions
                )
            },
            snackbarHost = snackbarHost,
            containerColor = if (isGlassTheme) GlassBlack else MaterialTheme.colorScheme.surface
        ) { innerPadding ->
            val layoutDirection = LocalLayoutDirection.current
            val combinedPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(layoutDirection) + Spacing.screenHorizontal,
                top = innerPadding.calculateTopPadding(),
                end = innerPadding.calculateEndPadding(layoutDirection) + Spacing.screenHorizontal,
                bottom = innerPadding.calculateBottomPadding()
            )

            if (isGlassTheme) {
                GlassBackdrop(hazeState = hazeState) {
                    content(combinedPadding)
                }
            } else {
                content(combinedPadding)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreviews
@Composable
fun AppScaffoldPreview() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    AppScaffold(
        title = "Battery",
        scrollBehavior = scrollBehavior,
        actions = {
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
            }
        }
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            items(10) { index ->
                Text(
                    text = "Item $index",
                    modifier = Modifier.padding(vertical = Spacing.s)
                )
            }
        }
    }
}
