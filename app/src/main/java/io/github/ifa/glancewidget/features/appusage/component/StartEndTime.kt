package io.github.ifa.glancewidget.features.appusage.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.glass.GlassSurface
import io.github.ifa.glancewidget.ui.glass.LocalIsGlassTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangePicker(
    modifier: Modifier = Modifier,
    initialStartTime: LocalTime = LocalTime.of(9, 0),
    initialEndTime: LocalTime = LocalTime.of(17, 0),
    onTimeRangeSelected: (startTime: LocalTime, endTime: LocalTime) -> Unit = { _, _ -> }
) {
    var startTime by remember { mutableStateOf(initialStartTime) }
    var endTime by remember { mutableStateOf(initialEndTime) }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val isInvalidTimeRange by remember(startTime, endTime) {
        derivedStateOf { endTime.isBefore(startTime) || endTime == startTime }
    }

    val startTimePickerState = rememberTimePickerState(
        initialHour = startTime.hour,
        initialMinute = startTime.minute
    )

    val endTimePickerState = rememberTimePickerState(
        initialHour = endTime.hour,
        initialMinute = endTime.minute
    )

    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }
    val isGlassTheme = LocalIsGlassTheme.current

    val cardContent = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.l)
        ) {
            Text(
                text = stringResource(R.string.usage_time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Spacing.l))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeButton(
                    time = startTime.format(timeFormatter),
                    label = stringResource(R.string.today),
                    modifier = Modifier.weight(1f),
                    onClick = { showStartTimePicker = true }
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.s)
                )

                TimeButton(
                    time = endTime.format(timeFormatter),
                    label = stringResource(R.string.today),
                    modifier = Modifier.weight(1f),
                    isError = isInvalidTimeRange,
                    onClick = { showEndTimePicker = true }
                )
            }

            if (isInvalidTimeRange) {
                Spacer(modifier = Modifier.height(Spacing.s))
                Text(
                    text = stringResource(R.string.force_reload),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (isGlassTheme) {
        GlassSurface(
            modifier = modifier.fillMaxWidth(),
            shape = AppShape.card
        ) {
            cardContent()
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = AppShape.card
        ) {
            cardContent()
        }
    }

    if (showStartTimePicker) {
        TimePickerDialog(
            onCancel = { showStartTimePicker = false },
            onConfirm = {
                val newStartTime = LocalTime.of(startTimePickerState.hour, startTimePickerState.minute)
                startTime = newStartTime

                if (endTime.isBefore(newStartTime) || endTime == newStartTime) {
                    endTime = newStartTime.plusMinutes(30)
                }

                onTimeRangeSelected(startTime, endTime)
                showStartTimePicker = false
            },
            title = stringResource(R.string.usage_time)
        ) {
            TimePicker(
                state = startTimePickerState,
                colors = TimePickerDefaults.colors(
                    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                    timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                    timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            onCancel = { showEndTimePicker = false },
            onConfirm = {
                val newEndTime = LocalTime.of(endTimePickerState.hour, endTimePickerState.minute)
                endTime = newEndTime

                onTimeRangeSelected(startTime, endTime)
                showEndTimePicker = false
            },
            title = stringResource(R.string.usage_time)
        ) {
            TimePicker(
                state = endTimePickerState,
                colors = TimePickerDefaults.colors(
                    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                    timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                    timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun TimeButton(
    time: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = AppShape.inner,
        border = BorderStroke(
            width = 1.dp,
            color = if (isError) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.outlineVariant
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(Spacing.m),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(Spacing.l)
                )

                Spacer(modifier = Modifier.width(Spacing.xs))

                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(text = title) },
        text = { content() },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(R.string.allow))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(stringResource(R.string.not_now))
            }
        }
    )
}

@ThemePreviews
@Composable
fun TimeRangePickerPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.padding(Spacing.l),
            color = MaterialTheme.colorScheme.background
        ) {
            TimeRangePicker()
        }
    }
}