package io.github.ifa.glancewidget.features.appusage

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.features.appusage.component.AppUsageList
import io.github.ifa.glancewidget.ui.designsystem.components.AppScaffold
import io.github.ifa.glancewidget.ui.designsystem.components.InfoBanner
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.utils.combinePadding

const val appUsageScreenRoute = "app_usage_screen_route"

fun NavGraphBuilder.appUsageScreen(
    contentPadding: PaddingValues = PaddingValues(),
) {
    composable(appUsageScreenRoute) {
        AppUsageScreen(contentPadding = contentPadding)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUsageScreen(
    viewModel: AppUsageViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        AppUsageScreenContent(
            snackbarHostState = snackbarHostState,
            uiState = uiState,
            contentPadding = contentPadding,
            onUsageRangeChange = viewModel::setUsageRange
        )
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppUsageScreenContent(
    snackbarHostState: SnackbarHostState,
    uiState: AppUsageScreenUiState,
    contentPadding: PaddingValues,
    onUsageRangeChange: (AppUsageScreenUiState.UsageRange) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
) {
    val context = LocalContext.current
    AppScaffold(
        title = stringResource(id = R.string.app_usage),
        scrollBehavior = scrollBehavior,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        actions = {
            IconButton(onClick = {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                context.startActivity(intent)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = stringResource(id = R.string.settings),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) { padding ->
        val insets = combinePadding(padding, contentPadding)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
        ) {
            if (uiState.hasAppUsagePermission) {
                AppUsageList(
                    appUsageStats = uiState.appUsageStats,
                    usageRange = uiState.usageRange,
                    onUsageRangeChange = onUsageRangeChange,
                    modifier = Modifier.fillMaxSize()
                ) { packageName ->
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", packageName, null)
                        }
                    )
                }
            } else {
                InfoBanner(
                    text = stringResource(id = R.string.should_allow_app_usage_desc),
                    icon = Icons.Rounded.Warning,
                    actionLabel = stringResource(id = R.string.request_app_usage_permission),
                    onAction = {
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.l)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@ThemePreviews
@Composable
private fun AppUsageScreenContentPreview() {
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState = AppUsageScreenUiState(hasAppUsagePermission = false)
    MaterialTheme {
        AppUsageScreenContent(
            snackbarHostState = snackbarHostState,
            uiState = uiState,
            contentPadding = PaddingValues(),
            onUsageRangeChange = {}
        )
    }
}