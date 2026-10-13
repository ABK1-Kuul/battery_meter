package io.github.ifa.glancewidget.features.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.features.health.component.BatteryExtraInformation
import io.github.ifa.glancewidget.model.BatteryData
import io.github.ifa.glancewidget.model.ChargeDisChargeCurrent
import io.github.ifa.glancewidget.model.ExtraBatteryInfo
import io.github.ifa.glancewidget.model.MyDevice
import io.github.ifa.glancewidget.model.wrapper.BatteryDataWrapper
import io.github.ifa.glancewidget.model.wrapper.PowerDetails
import io.github.ifa.glancewidget.ui.designsystem.components.AppScaffold
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.gravity.gravityItem
import io.github.ifa.glancewidget.ui.gravity.pillAvoidance

const val healthScreenRoute = "health_screen_route"

fun NavGraphBuilder.healthScreen(
    contentPadding: PaddingValues,
) {
    composable(healthScreenRoute) {
        HealthScreen(contentPadding = contentPadding)
    }
}

@Composable
internal fun HealthScreen(
    viewModel: HealthViewModel = hiltViewModel(),
    contentPadding: PaddingValues,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    HealthScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        contentPadding = contentPadding,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthScreen(
    uiState: HealthViewModel.HealthScreenUiState,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    AppScaffold(
        title = stringResource(id = R.string.health),
        scrollBehavior = scrollBehavior,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { scaffoldPadding ->
        LazyColumn(
            contentPadding = scaffoldPadding,
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                BatteryExtraInformation(
                    batteryDataWrapper = uiState.batteryOverall,
                    modifier = Modifier
                        .pillAvoidance()
                        .gravityItem("health_extra")
                )
            }
        }
    }
}

@ThemePreviews
@Composable
fun HealthScreenPreview() {
    MaterialTheme {
        val sampleMyDevice = MyDevice(
            level = 80,
            isCharging = false,
            temperature = MyDevice.Temperature(temperature = 28.0f),
            voltage = 4.05f,
            cycleCount = 142
        )
        val sampleBatteryData = BatteryData(
            myDevice = sampleMyDevice,
            batteryConnectedDevices = emptyList()
        )
        val sampleExtraBatteryInfo = ExtraBatteryInfo(
            capacity = 5000,
            fullChargeCapacity = 4750,
            chargeCounter = 3800
        )
        val sampleUiState = HealthViewModel.HealthScreenUiState(
            batteryOverall = BatteryDataWrapper(
                batteryData = sampleBatteryData,
                extraBatteryInfo = sampleExtraBatteryInfo,
                powerDetails = PowerDetails(0f, 0f),
                chargeDisChargeCurrent = ChargeDisChargeCurrent()
            )
        )
        HealthScreen(
            uiState = sampleUiState,
            snackbarHostState = remember { SnackbarHostState() },
            contentPadding = PaddingValues(0.dp)
        )
    }
}