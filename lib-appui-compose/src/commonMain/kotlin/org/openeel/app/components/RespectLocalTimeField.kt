package world.respect.app.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.datetime.LocalTime
import world.respect.libutil.ext.pad0
import kotlin.math.min


class TimeVisualTransformation: VisualTransformation {

    private val mask = "hhmm"

    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if(text.length >= mask.length) text.substring(mask.indices) else text.text
        val output = buildAnnotatedString {
            for(i in mask.indices) {
                if (i < trimmed.length) {
                    append(trimmed[i])
                }else {
                    withStyle(style = SpanStyle(color = Color.LightGray)) {
                        append(mask[i])
                    }
                }

                if(i == 1){
                    withStyle(style = SpanStyle(color = Color.LightGray)) {
                        append(":")
                    }
                }

            }
        }

        val offsetmapping= object: OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return if(offset <= 2) {
                    offset
                }else if(offset <= mask.length) {
                    offset + 1
                }else {
                    mask.length + 1 //text length plus ":"
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                val delta = if(offset <= 3) {
                    offset
                }else if(offset <= mask.length + 1) {
                    offset - 1
                }else {
                    mask.length
                }

                return min(delta, trimmed.length)
            }
        }

        return TransformedText(output, offsetmapping)

    }
}

@Composable
fun RespectLocalTimeField(
    value: LocalTime?,
    label: @Composable () -> Unit,
    onValueChange: (LocalTime?) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {

    fun LocalTime.toTimeString(): String {
        return "${this.hour.pad0()}${this.minute.pad0()}"
    }

    var rawValue: String by remember(value) {
        mutableStateOf(value?.toTimeString()   ?: "")
    }

    OutlinedTextField(
        modifier = modifier,
        value = rawValue,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() }
            rawValue = filtered.substring(0, min(filtered.length, 4))
            if(filtered.length == 4) {
                try {
                    val hours = filtered.substring(0, 2).toInt()
                    val mins = filtered.substring(2, 4).toInt()
                    if(hours >= 0 && hours <= 23 && mins >= 0 && mins <= 59)
                        onValueChange(LocalTime(hours, mins))
                }catch(e: Exception) {
                    //something not a valid time
                }
            }else if(filtered.isEmpty()) {
                onValueChange(null)
            }
        },
        label = label,
        visualTransformation = TimeVisualTransformation(),
        singleLine = true,
        supportingText = supportingText,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}


