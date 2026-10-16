package io.github.ifa.glancewidget.features.settings.about

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Support
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.ui.designsystem.components.AppScaffold
import io.github.ifa.glancewidget.ui.designsystem.components.SettingsGroup
import io.github.ifa.glancewidget.ui.designsystem.components.SettingsRow
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.glass.GlassSurface
import io.github.ifa.glancewidget.ui.glass.LocalIsGlassTheme
import io.github.ifa.glancewidget.ui.gravity.gravityItem
import io.github.ifa.glancewidget.ui.gravity.pillAvoidance
import io.github.ifa.glancewidget.utils.Constants.IFA_GITHUB_URL
import io.github.ifa.glancewidget.utils.Constants.IFA_LICENSES_URL
import io.github.ifa.glancewidget.utils.Constants.IFA_SUPPORT_URL
import io.github.ifa.glancewidget.utils.Constants.IFA_TEAM_URL
import io.github.ifa.glancewidget.utils.Constants.STORE_APP_URL
import io.github.ifa.glancewidget.utils.combinePadding
import io.github.ifa.glancewidget.utils.cookieShape
import io.github.ifa.glancewidget.utils.navigateLicencesScreen
import io.github.ifa.glancewidget.utils.navigateUrl
import io.github.ifa.glancewidget.utils.sendMail

@Composable
fun AboutScreen(
    viewModel: AboutViewModel = hiltViewModel(),
    contentPadding: PaddingValues,
    showNavigationIcon: Boolean = true,
    onNavigationIconClick: () -> Unit,
    onExternalUrlClick: Context.(String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    AboutScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        showNavigationIcon = showNavigationIcon,
        contentPadding = contentPadding,
        onNavigationIconClick = onNavigationIconClick,
        onExternalUrlClick = onExternalUrlClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AboutScreen(
    uiState: AboutViewModel.AboutScreenUiState,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
    showNavigationIcon: Boolean = true,
    onNavigationIconClick: () -> Unit,
    onExternalUrlClick: Context.(String) -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val isGlassTheme = LocalIsGlassTheme.current

    val onOpenLicensesScreen = {
        context.navigateLicencesScreen()
    }
    val onOpenPrivacyPolicy = {
        context.navigateUrl(IFA_LICENSES_URL)
    }
    val onOpenGooglePlay = {
        context.navigateUrl(STORE_APP_URL)
    }

    AppScaffold(
        title = stringResource(id = R.string.about_tab),
        scrollBehavior = scrollBehavior,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        navigationIcon = {
            if (showNavigationIcon) {
                IconButton(
                    onClick = onNavigationIconClick,
                    colors = IconButtonDefaults.iconButtonColors().copy(
                        containerColor = if (isGlassTheme) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
            }
        }
    ) { padding ->
        val insets = combinePadding(padding, contentPadding)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
            contentPadding = insets,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        ) {
            item {
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.l)
                        .pillAvoidance()
                        .gravityItem("about_app_hero"),
                    shape = AppShape.hero
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.l),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.l)
                    ) {
                        Box(modifier = Modifier.size(56.dp)) {
                            val infiniteTransition = rememberInfiniteTransition(label = "rotate")
                            val rotation by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = 4000, easing = LinearEasing)
                                ),
                                label = "rotation"
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .rotate(rotation)
                                    .clip(cookieShape())
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                            )
                            Icon(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(40.dp),
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                tint = MaterialTheme.colorScheme.secondary,
                                contentDescription = null
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(id = R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                text = stringResource(id = R.string.version, uiState.appVersion),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.l)
                        .pillAvoidance()
                        .gravityItem("about_author"),
                    shape = AppShape.card
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.l),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.l)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(AppShape.card)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .size(56.dp)
                        ) {
                            Icon(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(40.dp),
                                painter = painterResource(id = R.drawable.img_dotsdev),
                                tint = MaterialTheme.colorScheme.secondary,
                                contentDescription = null
                            )
                        }

                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Dotsdev",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                style = MaterialTheme.typography.bodySmall,
                                text = "@huyhunhngc from IFATeam",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(Spacing.m))
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                                TonalButton(
                                    onClick = { context.sendMail() },
                                    imageVector = Icons.Rounded.Mail,
                                )
                                TonalButton(
                                    onClick = { onExternalUrlClick(context, IFA_SUPPORT_URL) },
                                    imageVector = Icons.Rounded.Support,
                                )
                                TonalButton(
                                    onClick = { onExternalUrlClick(context, IFA_TEAM_URL) },
                                    icon = R.drawable.ic_globe,
                                )
                                TonalButton(
                                    onClick = { onExternalUrlClick(context, IFA_GITHUB_URL) },
                                    icon = R.drawable.ic_github,
                                )
                            }
                        }
                    }
                }
            }

            item {
                SettingsGroup(
                    title = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.l)
                        .pillAvoidance()
                        .gravityItem("about_rate")
                ) {
                    SettingsRow(
                        title = stringResource(id = R.string.rate),
                        subtitle = stringResource(id = R.string.rate_on_google_play),
                        leadingPainter = painterResource(id = R.drawable.ic_play_store),
                        trailing = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = onOpenGooglePlay
                    )
                }
            }

            item {
                SettingsGroup(
                    title = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.l)
                        .pillAvoidance()
                        .gravityItem("about_legal")
                ) {
                    SettingsRow(
                        title = stringResource(id = R.string.license),
                        leadingPainter = painterResource(id = R.drawable.ic_license),
                        trailing = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = onOpenLicensesScreen
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = Spacing.l),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    SettingsRow(
                        title = stringResource(id = R.string.font_doto_license),
                        leadingPainter = painterResource(id = R.drawable.ic_license),
                        trailing = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            onExternalUrlClick(context, "https://github.com/googlefonts/doto")
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = Spacing.l),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    SettingsRow(
                        title = stringResource(id = R.string.privacy_policy),
                        leadingPainter = painterResource(id = R.drawable.ic_policy),
                        trailing = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = onOpenPrivacyPolicy
                    )
                }
            }
        }
    }
}

@Composable
fun TonalButton(
    onClick: () -> Unit,
    icon: Int? = null,
    imageVector: ImageVector? = null,
) {
    Surface(
        onClick = onClick,
        shape = AppShape.chip,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(
                    painter = painterResource(id = icon),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = null
                )
            } else if (imageVector != null) {
                Icon(
                    imageVector = imageVector,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = null
                )
            }
        }
    }
}

@ThemePreviews
@Composable
fun AboutScreenPreview() {
    MaterialTheme {
        AboutScreen(
            uiState = AboutViewModel.AboutScreenUiState(appVersion = "2.0.0 (200001)"),
            snackbarHostState = remember { SnackbarHostState() },
            contentPadding = PaddingValues(),
            showNavigationIcon = true,
            onNavigationIconClick = {},
            onExternalUrlClick = {},
        )
    }
}