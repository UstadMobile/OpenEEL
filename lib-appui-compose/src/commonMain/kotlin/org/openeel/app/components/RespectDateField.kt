package world.respect.app.components


import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.ok
import world.respect.shared.generated.resources.select_date
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RespectDateField(
    value: Long,
    label: @Composable () -> Unit,
    timeZoneId: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValueChange: (Long) -> Unit = {},
    unsetDefault: Long = 0,
    supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val timeZone = remember(timeZoneId) {
        TimeZone.of(timeZoneId)
    }

    val dateFormatter = remember(timeZoneId) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).also {
            it.timeZone = timeZone.toJavaTimeZone()
        }
    }

    fun Long.toDateString(): String {
        return if (isDateSet()) {
            dateFormatter.format(Date(this))
        } else {
            ""
        }
    }

    var rawValue: String by remember(value) {
        mutableStateOf(value.toDateString())
    }

    val datePickerState = rememberDatePickerState()
    var isDialogOpen by remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = modifier,
        value = rawValue,
        enabled = enabled,
        isError = isError,
        onValueChange = {
            // Allow manual input if needed
            rawValue = it
            if (it.length == 10) { // DD/MM/YYYY format
                try {
                    dateFormatter.parse(it)?.time?.also { time ->
                        onValueChange(time)
                    }
                } catch (e: Exception) {
                    // Invalid date format
                }
            } else if (it.isEmpty()) {
                onValueChange(unsetDefault)
            }
        },
        label = label,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailingIcon = {
            IconButton(
                onClick = { isDialogOpen = true },
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = stringResource(Res.string.select_date),
                )
            }
        },
        supportingText = supportingText,
    )

    if (isDialogOpen) {
        DatePickerDialog(
            onDismissRequest = { isDialogOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { time ->
                            onValueChange(time)
                        }
                        isDialogOpen = false
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { isDialogOpen = false }
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = stringResource(Res.string.select_date),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                },
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    headlineContentColor = MaterialTheme.colorScheme.onSurface,
                    weekdayContentColor = MaterialTheme.colorScheme.onSurface,
                    subheadContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    }
}

private fun Long.isDateSet(): Boolean {
    return this != 0L
}

private fun TimeZone.toJavaTimeZone(): java.util.TimeZone {
    return java.util.TimeZone.getTimeZone(this.id)
}