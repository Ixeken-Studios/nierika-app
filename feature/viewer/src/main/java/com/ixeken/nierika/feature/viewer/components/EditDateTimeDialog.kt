package com.ixeken.nierika.feature.viewer.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.ButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.FilledTonalButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.NierikaConnectedButtonGroup
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import java.util.Calendar
import java.util.TimeZone

private enum class EditDateTimeMode {
    DATE,
    TIME
}

/**
 * Diálogo modal Material 3 Expressive para ajustar interactivamente la fecha y hora de captura de un archivo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDateTimeDialog(
    initialTimestampMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialCalendar = remember(initialTimestampMillis) {
        Calendar.getInstance().apply {
            timeInMillis = if (initialTimestampMillis > 0) initialTimestampMillis else System.currentTimeMillis()
        }
    }

    // Inicializar estado del DatePicker con el día en UTC para evitar desfases de zona horaria
    val utcInitialDateMillis = remember(initialCalendar) {
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(
                initialCalendar.get(Calendar.YEAR),
                initialCalendar.get(Calendar.MONTH),
                initialCalendar.get(Calendar.DAY_OF_MONTH),
                0, 0, 0
            )
            set(Calendar.MILLISECOND, 0)
        }
        utcCal.timeInMillis
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = utcInitialDateMillis
    )

    val timePickerState = rememberTimePickerState(
        initialHour = initialCalendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = initialCalendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    var currentMode by remember { mutableStateOf(EditDateTimeMode.DATE) }
    val motionScheme = MaterialTheme.nierikaMotionScheme

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Encabezado
                Text(
                    text = stringResource(R.string.edit_date_time_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                val dateLabel = stringResource(R.string.action_change_date)
                val timeLabel = stringResource(R.string.action_change_time)

                // Selector de modo (Fecha / Hora) con NierikaConnectedButtonGroup
                NierikaConnectedButtonGroup(
                    items = listOf(EditDateTimeMode.DATE, EditDateTimeMode.TIME),
                    selectedItem = currentMode,
                    onItemSelected = { currentMode = it },
                    labelProvider = { mode ->
                        when (mode) {
                            EditDateTimeMode.DATE -> dateLabel
                            EditDateTimeMode.TIME -> timeLabel
                        }
                    },
                    iconProvider = { mode ->
                        when (mode) {
                            EditDateTimeMode.DATE -> Icons.Filled.CalendarToday
                            EditDateTimeMode.TIME -> Icons.Filled.Schedule
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Contenedor animado del selector
                AnimatedContent(
                    targetState = currentMode,
                    transitionSpec = {
                        fadeIn(animationSpec = motionScheme.fastEffectsSpec()) togetherWith
                                fadeOut(animationSpec = motionScheme.fastEffectsSpec())
                    },
                    label = "dateTimeModeTransition",
                    modifier = Modifier.fillMaxWidth()
                ) { mode ->
                    when (mode) {
                        EditDateTimeMode.DATE -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                DatePicker(
                                    state = datePickerState,
                                    title = null,
                                    headline = null,
                                    showModeToggle = false,
                                    colors = DatePickerDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                                        todayDateBorderColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                        EditDateTimeMode.TIME -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .padding(vertical = 24.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                TimePicker(
                                    state = timePickerState,
                                    colors = TimePickerDefaults.colors(
                                        clockDialColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        selectorColor = MaterialTheme.colorScheme.primary,
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButtonWithAnimatedShape(
                        onClick = onDismiss,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    ButtonWithAnimatedShape(
                        onClick = {
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = datePickerState.selectedDateMillis ?: utcInitialDateMillis
                            }
                            val localCal = Calendar.getInstance().apply {
                                set(
                                    utcCal.get(Calendar.YEAR),
                                    utcCal.get(Calendar.MONTH),
                                    utcCal.get(Calendar.DAY_OF_MONTH),
                                    timePickerState.hour,
                                    timePickerState.minute,
                                    0
                                )
                                set(Calendar.MILLISECOND, 0)
                            }
                            onConfirm(localCal.timeInMillis)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.action_save),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
