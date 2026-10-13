package io.github.ifa.glancewidget.features.main

import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import io.github.ifa.glancewidget.features.battery.batteryMonitorScreenRoute
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.localcomposition.LocalAnimatedVisibilityScope

const val mainScreenRoute = "main_screen_route"

fun NavGraphBuilder.mainTabScreens(
    mainNavGraph: NavGraphBuilder.(PaddingValues, SnackbarHostState) -> Unit,
) {
    composable(mainScreenRoute) {
        CompositionLocalProvider(
            LocalAnimatedVisibilityScope provides this@composable,
        ) {
            MainScreen(mainNavGraph = mainNavGraph)
        }
    }
}

@SuppressLint("ImplicitSamInstance")
@Composable
fun MainScreen(
    mainNavGraph: NavGraphBuilder.(PaddingValues, SnackbarHostState) -> Unit,
) {
    val mainTabNavController = rememberNavController()
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isWide = remember(windowSizeClass) {
        windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
    }

    Scaffold(
        bottomBar = {
            if (!isWide) {
                MainFloatBottomBar(
                    navController = mainTabNavController,
                    colorScheme = colorScheme,
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = colorScheme.surface
    ) { contentPadding ->
        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                MainNavigationRail(
                    navController = mainTabNavController,
                    colorScheme = colorScheme
                )
                NavHost(
                    navController = mainTabNavController,
                    startDestination = batteryMonitorScreenRoute,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    enterTransition = { materialFadeThroughIn() },
                    exitTransition = { materialFadeThroughOut() },
                ) {
                    mainNavGraph(PaddingValues(), snackbarHostState)
                }
            }
        } else {
            NavHost(
                navController = mainTabNavController,
                startDestination = batteryMonitorScreenRoute,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { materialFadeThroughIn() },
                exitTransition = { materialFadeThroughOut() },
            ) {
                mainNavGraph(contentPadding, snackbarHostState)
            }
        }
    }
}

@Composable
fun MainNavigationRail(
    navController: NavController,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = colorScheme.surface,
        contentColor = colorScheme.onSurface
    ) {
        Spacer(modifier = Modifier.weight(1f))
        MainScreenTab.entries.forEach { item ->
            val selected = navBackStackEntry?.destination?.route == item.route
            NavigationRailItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Crossfade(selected, label = "railIcon") { isSelected ->
                        Icon(
                            painter = painterResource(if (isSelected) item.selectedIcon else item.icon),
                            contentDescription = stringResource(item.label)
                        )
                    }
                },
                label = {
                    Text(
                        text = stringResource(item.label),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = colorScheme.onPrimary,
                    selectedTextColor = colorScheme.primary,
                    indicatorColor = colorScheme.primary,
                    unselectedIconColor = colorScheme.onSurfaceVariant,
                    unselectedTextColor = colorScheme.onSurfaceVariant
                )
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

private fun materialFadeThroughIn(): EnterTransition = fadeIn(
    animationSpec = tween(
        durationMillis = 195,
        delayMillis = 105,
        easing = LinearOutSlowInEasing,
    ),
) + scaleIn(
    animationSpec = tween(
        durationMillis = 195,
        delayMillis = 105,
        easing = LinearOutSlowInEasing,
    ),
    initialScale = 0.92f,
)

private fun materialFadeThroughOut(): ExitTransition = fadeOut(
    animationSpec = tween(
        durationMillis = 105,
        delayMillis = 0,
        easing = FastOutLinearInEasing,
    ),
)

@ThemePreviews
@Composable
fun MainNavigationRailPreview() {
    MaterialTheme {
        MainNavigationRail(
            navController = rememberNavController(),
            colorScheme = MaterialTheme.colorScheme
        )
    }
}

@Preview(name = "Phone - 360dp", widthDp = 360, heightDp = 800)
@Preview(name = "Medium - 700dp", widthDp = 700, heightDp = 800)
@Preview(name = "Expanded - 1000dp", widthDp = 1000, heightDp = 800)
@Composable
fun MainScreenAdaptivePreview() {
    MaterialTheme {
        MainScreen(mainNavGraph = { _, _ -> })
    }
}