package world.respect.app.components


import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import world.respect.libutil.ext.pad0
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.select_date
import world.respect.shared.generated.resources.ok
import kotlin.math.min
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.TimeZone as TimeZoneKt

/**
 * As per https://developer.android.com/reference/kotlin/androidx/compose/ui/text/input/VisualTransformation
 */
class DateVisualTransformation: VisualTransformation {
    private val mask = "ddmmyyyy"

    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if(text.length >= mask.length) text.substring(0..7) else text.text
        val output = buildAnnotatedString {
            for (i in 0 until 8) {
                if(i < trimmed.length) {
                    append(trimmed[i])
                }else {
                    withStyle(style = SpanStyle(color = Color.LightGray)) {
                        append(mask[i])
                    }
                }

                //Add separators
                if(i == 1 || i == 3) {
                    withStyle(style = SpanStyle(color = Color.LightGray)) {
                        append("/")
                    }
                }
            }

        }

        val offsetMapping = object: OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return if(offset <= 2)
                    offset
                else if(offset <= 4)
                    offset + 1
                else if(offset <= 8)
                    offset + 2
                else
                    10

            }

            override fun transformedToOriginal(offset: Int): Int {
                val delta = if(offset <= 3)
                    offset
                else if(offset <= 5)
                    offset -1
                else if(offset <= 9)
                    offset - 2
                else
                    8


                return min(delta, trimmed.length)
            }
        }

        return TransformedText(output, offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun RespectLocalDateField(
    value: LocalDate?,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    onValueChange: (LocalDate?) -> Unit = { },
    supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var rawValue: String by remember(value) {
        mutableStateOf(
            if(value != null) {
                "${value.day.pad0()}${value.month.number.pad0()}${value.year}"
            }else {
                ""
            }
        )
    }

    val datePickerState = rememberDatePickerState()
    var isDialogOpen by remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = modifier,
        value = rawValue,
        enabled = enabled,
        isError = isError,
        onValueChange = {
            val filtered = it.filter { it.isDigit() }
            rawValue = filtered.substring(0, min(filtered.length, 8))
            if(filtered.length == 8) {
                try {
                    onValueChange(
                        LocalDate(
                            year = filtered.substring(4, 8).toInt(),
                            month = filtered.substring(2, 4).toInt(),
                            day = filtered.substring(0, 2).toInt()
                        )
                    )
                }catch(_: Exception) {
                    //Probably not valid numbers for day/month - do nothing
                }
            }else if(filtered.isEmpty()) {
                onValueChange(null)
            }
        },
        label = label,
        visualTransformation = DateVisualTransformation(),
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailingIcon = {
            IconButton(
                onClick = {
                    isDialogOpen = !isDialogOpen
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Event,
                    contentDescription = "",
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
                        datePickerState
                        datePickerState.selectedDateMillis?.let { time ->
                            //As per
                            //https://developer.android.com/develop/ui/compose/components/datepickers
                            //use system default locale
                            val instant = Instant.fromEpochMilliseconds(time)
                            val localDate = instant.toLocalDateTime(TimeZoneKt.currentSystemDefault())
                                .date
                            onValueChange(localDate)
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
                title = {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = stringResource(Res.string.select_date)
                    )
                },
                state = datePickerState
            )
        }
    }

}