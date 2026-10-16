package io.github.ifa.glancewidget.features.appusage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.features.appusage.component.AppUsageList
import io.github.ifa.glancewidget.ui.designsystem.components.AppScaffold
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.glass.GlassSurface
import io.github.ifa.glancewidget.ui.glass.LocalIsGlassTheme
import io.github.ifa.glancewidget.ui.gravity.gravityItem
import io.github.ifa.glancewidget.ui.gravity.pillAvoidance
import io.github.ifa.glancewidget.ui.theme.glassLabel
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

    LifecycleResumeEffect(Unit) {
        viewModel.checkPermission()
        onPauseOrDispose {}
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        AppUsageScreenContent(
            snackbarHostState = snackbarHostState,
            uiState = uiState,
            contentPadding = contentPadding,
            onUsageRangeChange = viewModel::setUsageRange
        )
    }
}

private fun launchUsageAccessSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
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
            IconButton(onClick = { launchUsageAccessSettings(context) }) {
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
                            data = Uri.fromParts("package", packageName, null)
                        }
                    )
                }
            } else {
                AppUsagePermissionCard(
                    onGrantClick = { launchUsageAccessSettings(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.l)
                        .pillAvoidance()
                        .gravityItem("usage_permission_card")
                )
            }
        }
    }
}

@Composable
private fun AppUsagePermissionCard(
    onGrantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGlassTheme = LocalIsGlassTheme.current
    val cardContent = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isGlassTheme) stringResource(R.string.should_allow_app_usage).uppercase()
                else stringResource(R.string.should_allow_app_usage),
                style = if (isGlassTheme) MaterialTheme.typography.glassLabel else MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Spacing.l))

            Icon(
                imageVector = Icons.Rounded.Security,
                contentDescription = null,
                modifier = Modifier.size(Spacing.xxl),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            Text(
                text = stringResource(R.string.app_usage),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.s))

            Text(
                text = stringResource(R.string.should_allow_app_usage_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            Button(
                onClick = onGrantClick,
                shape = AppShape.chip,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isGlassTheme) stringResource(R.string.request_app_usage_permission).uppercase()
                    else stringResource(R.string.request_app_usage_permission),
                    style = if (isGlassTheme) MaterialTheme.typography.glassLabel else MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    if (isGlassTheme) {
        GlassSurface(
            modifier = modifier.fillMaxWidth(),
            shape = AppShape.hero
        ) {
            cardContent()
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = AppShape.hero,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            cardContent()
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