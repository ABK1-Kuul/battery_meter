package io.github.ifa.glancewidget.features.main

import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import android.provider.Settings
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import io.github.ifa.glancewidget.features.battery.batteryMonitorScreenRoute
import io.github.ifa.glancewidget.ui.designsystem.components.GravityNavPill
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.gravity.GravityController
import io.github.ifa.glancewidget.ui.gravity.GravityFieldContext
import io.github.ifa.glancewidget.ui.gravity.LocalGravityController
import io.github.ifa.glancewidget.ui.gravity.LocalGravityField
import io.github.ifa.glancewidget.ui.gravity.PillEndMargin
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

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pillBoundsState = remember { mutableStateOf(Rect.Zero) }
    val reduceMotion = remember(context) {
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        } catch (e: Exception) {
            false
        }
    }

    val gravityController = remember(coroutineScope, reduceMotion) {
        GravityController(
            scope = coroutineScope,
            pillBoundsProvider = { pillBoundsState.value },
            reduceMotionProvider = { reduceMotion }
        )
    }

    CompositionLocalProvider(
        LocalGravityField provides GravityFieldContext(
            pillBounds = pillBoundsState,
            reduceMotion = reduceMotion,
            controller = gravityController
        ),
        LocalGravityController provides gravityController
    ) {
        Scaffold(
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
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                    ) {
                        mainNavGraph(PaddingValues(), snackbarHostState)
                    }
                }
            } else {
                val navBackStackEntry by mainTabNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val currentTab = MainScreenTab.entries.find { it.route == currentRoute } ?: MainScreenTab.Widget
                val isTabRoute = MainScreenTab.entries.any { it.route == currentRoute }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    NavHost(
                        navController = mainTabNavController,
                        startDestination = batteryMonitorScreenRoute,
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                    ) {
                        mainNavGraph(contentPadding, snackbarHostState)
                    }
                    if (isTabRoute) GravityNavPill(
                        selectedTab = currentTab,
                        onTabSelected = { tab ->
                            if (tab.route != currentRoute) {
                                gravityController.requestNavigate {
                                    mainTabNavController.navigate(tab.route) {
                                        mainTabNavController.graph.startDestinationRoute?.let { route ->
                                            popUpTo(route) { saveState = true }
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        },
                        scale = gravityController.pillScale.value,
                        onBoundsChanged = { pillBoundsState.value = it },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = PillEndMargin)
                    )
                }
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