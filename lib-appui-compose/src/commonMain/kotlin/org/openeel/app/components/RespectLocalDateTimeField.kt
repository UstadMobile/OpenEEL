package world.respect.app.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.date
import world.respect.shared.generated.resources.time

@Composable
fun RespectLocalDateTimeField(
    modifier: Modifier = Modifier,
    dateFieldWeight: Float = 0.6f,
    value: LocalDateTime?,
    onValueChanged: (LocalDateTime?) -> Unit,
    dateLabel: (@Composable () -> Unit)? = null,
    timeLabel: (@Composable () -> Unit)? = null,
    dateTestTag: String? = null,
    timeTestTag: String? = null,
) {
    var dateVar by remember(value) {
        mutableStateOf(value?.date)
    }

    var timeVar by remember(value) {
        mutableStateOf(value?.time)
    }

    Row(modifier = modifier) {
        RespectLocalDateField(
            modifier = Modifier.weight(dateFieldWeight, true)
                .testTagIfNotNull(dateTestTag),
            value = value?.date ?: dateVar,
            onValueChange = { newDate ->
                dateVar = newDate
                val timeVal = timeVar
                if(timeVal != null && newDate != null) {
                    onValueChanged(LocalDateTime(newDate, timeVal))
                }else if(newDate == null) {
                    onValueChanged(null)
                }
            },
            label = dateLabel ?: {
                Text(stringResource(Res.string.date))
            },
        )

        Spacer(Modifier.width(16.dp))

        RespectLocalTimeField(
            modifier = Modifier.weight(1f - dateFieldWeight, true)
                .testTagIfNotNull(timeTestTag),
            value = value?.time ?: timeVar,
            onValueChange = { newTime ->
                timeVar = newTime
                val dateVal = dateVar
                if(dateVal != null && newTime != null) {
                    onValueChanged(LocalDateTime(dateVal, newTime))
                }else if(newTime == null){
                    onValueChanged(null)
                }
            },
            label = timeLabel ?: {
                Text(stringResource(Res.string.time))
            }
        )
    }
}