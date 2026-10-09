package world.respect.app.view.statement.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectDetailField
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.langMapString
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.ext.objectActivityNameOrNull
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.account_homepage
import world.respect.shared.generated.resources.account_name
import world.respect.shared.generated.resources.actor
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.duration
import world.respect.shared.generated.resources.interaction_type
import world.respect.shared.generated.resources.name
import world.respect.shared.generated.resources._object
import world.respect.shared.generated.resources.result
import world.respect.shared.generated.resources.score_scaled
import world.respect.shared.generated.resources.timestamp
import world.respect.shared.generated.resources.verb
import world.respect.shared.util.formatToShortString
import world.respect.shared.util.rememberFormattedDateTime
import world.respect.shared.viewmodel.statement.detail.StatementDetailUiState
import world.respect.shared.viewmodel.statement.detail.StatementDetailViewModel

@Composable
fun StatementDetailScreen(
    viewModel: StatementDetailViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    StatementDetailScreen(
        uiState = uiState
    )
}

@Composable
fun StatementDetailScreen(
    uiState: StatementDetailUiState,
) {
    val statement = uiState.statements.dataOrNull() ?: return

    val timestampStr = rememberFormattedDateTime(
        timeInMillis = statement.timestamp?.toEpochMilliseconds() ?: 0,
        timeZoneId = TimeZone.currentSystemDefault().id
    )

    val xapiObject = statement.objectActivityOrNull()

    val objectName = statement.objectActivityNameOrNull()?.let { langMapString(it) }
        ?: xapiObject?.id?.substringAfterLast("/") ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .defaultItemPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Timestamp
        RespectDetailField(
            label = { Text("${stringResource(Res.string.timestamp)}:") },
            value = { Text(timestampStr) }
        )

        // Verb
        RespectDetailField(
            label = { Text("${stringResource(Res.string.verb)}:") },
            value = {
                Text(
                    statement.verb.display?.let { langMapString(it) }
                        ?: statement.verb.id.substringAfterLast("/")
                )
            }
        )

        // Object
        RespectDetailField(
            label = { Text("${stringResource(Res.string._object)}:") },
            value = {
                Column {
                    Text("${stringResource(Res.string.name)}: $objectName")

                    xapiObject?.definition?.description?.let {
                        Text("${stringResource(Res.string.description)}: ${langMapString(it)}")
                    }
                    xapiObject?.definition?.interactionType?.let {
                        Text("${stringResource(Res.string.interaction_type)}: ${it.jsonFieldValue}")
                    }
                }
            }
        )

        // Actor
        RespectDetailField(
            label = { Text("${stringResource(Res.string.actor)}:") },
            value = {
                Column {
                    statement.actor.name?.let { Text("${stringResource(Res.string.name)}: $it") }
                    statement.actor.account?.let { account ->
                        Text("${stringResource(Res.string.account_homepage)}: ${account.homePage}")
                        Text("${stringResource(Res.string.account_name)}: ${account.name}")
                    }
                }
            }
        )

        // Result
        statement.result?.also { result ->
            RespectDetailField(
                label = { Text("${stringResource(Res.string.result)}:") },
                value = {
                    Column {
                        result.score?.scaled?.also {
                            Text("${stringResource(Res.string.score_scaled)}: ${(it * 100).toInt()}%")
                        }
                        result.duration?.also { duration ->
                            Text("${stringResource(Res.string.duration)}: ${duration.formatToShortString()}")
                        }
                    }
                }
            )
        }
    }
}
