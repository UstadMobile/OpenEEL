package world.respect.app.view.statement.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.langMapString
import world.respect.app.util.rememberDayOrDate
import world.respect.lib.xapi.ext.objectActivityNameOrNull
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.model.XapiStatement
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.score
import world.respect.shared.util.ext.dayStringResource
import java.text.DateFormat
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.time.Clock

@Composable
fun StatementListItem(
    statement: XapiStatement,
    modifier: Modifier = Modifier,
    onClickListItem: (statement: XapiStatement) -> Unit = {},
) {
    val objectName = statement.objectActivityNameOrNull()?.let { langMapString(it) }
        ?: statement.objectActivityOrNull()?.id?.substringAfterLast("/") ?: ""

    val verbName = statement.verb.display?.let { langMapString(it) }
        ?: statement.verb.id.substringAfterLast("/")

    val scoreText = statement.result?.score?.scaled?.let {
        stringResource(Res.string.score) + ": " + "${(it * 100).roundToInt()}%"
    }

    // Placeholder for now
    val iconUrl: String? = null

    val timeZone = remember { TimeZone.currentSystemDefault() }
    val localDateTimeNow = remember { Clock.System.now().toLocalDateTime(timeZone) }
    val timeFormatter =
        remember { DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()) }
    val dateFormatter =
        remember { DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()) }


    val dayOfWeekStringMap = DayOfWeek.entries.associateWith { day ->
        stringResource(day.dayStringResource)
    }

    val formattedTime = statement.timestamp?.let {
        rememberDayOrDate(
            localDateTimeNow = localDateTimeNow,
            timestamp = it.toEpochMilliseconds(),
            timeZone = timeZone,
            showTimeIfToday = true,
            timeFormatter = timeFormatter,
            dateFormatter = dateFormatter,
            dayOfWeekStringMap = dayOfWeekStringMap
        )
    } ?: ""

    ListItem(
        modifier = modifier.clickable {
            onClickListItem(statement)
        },
        leadingContent = {
            RespectAsyncImage(
                uri = iconUrl,
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(36.dp)
            )
        },
        headlineContent = {
            Text(
                text = objectName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = verbName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (scoreText != null) {
                    Text(
                        text = scoreText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        trailingContent = {
            Text(
                text = formattedTime,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}
