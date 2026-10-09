package world.respect.app.view.person.passkeyList

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectBasicAlertDialog
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.datalayer.school.model.PersonPasskey
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.at
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.delete_this_passkey
import world.respect.shared.generated.resources.key_created_on
import world.respect.shared.generated.resources.loss_access_passkey_dialog
import world.respect.shared.util.rememberFormattedDateTime
import world.respect.shared.viewmodel.person.passkeylist.PasskeyListUiState
import world.respect.shared.viewmodel.person.passkeylist.PasskeyListViewModel

@Composable
fun PasskeyListScreen(
    viewModel: PasskeyListViewModel
) {

    val uiState by viewModel.uiState.collectAsState()
    PasskeyListScreen(
        uiState = uiState,
        onClickRevokePasskey = viewModel::onClickRevokePasskey
    )

    if (uiState.showRevokePasskeyDialog) {
        RespectBasicAlertDialog(
            headlineText = stringResource(Res.string.delete_this_passkey),
            bodyText = stringResource(Res.string.loss_access_passkey_dialog),
            onConfirm = viewModel::onConfirmRevokePasskey,
            onDismissRequest = viewModel::onDismissRevokePasskeyDialog,
        )
    }
}

@Composable
fun PasskeyListScreen(
    uiState: PasskeyListUiState = PasskeyListUiState(),
    onClickRevokePasskey: (PersonPasskey) -> Unit
) {
    val passkeyList = uiState.passkeys.dataOrNull() ?: emptyList()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            items = passkeyList,
            key = { it.credentialId }
        ) { personPasskey ->
            val createdAtStr = rememberFormattedDateTime(
                timeInMillis = personPasskey.timeCreated.toEpochMilliseconds(),
                timeZoneId = TimeZone.currentSystemDefault().id,
            )

            ListItem(
                headlineContent = {
                    Text(
                        text = "${personPasskey.providerName}",
                        maxLines = 1,
                    )
                },
                supportingContent = {
                    Text(
                        text = "${stringResource(Res.string.key_created_on)}: ${personPasskey.deviceName} " +
                                "${stringResource(Res.string.at)}: $createdAtStr",
                        maxLines = 3,
                    )
                },
                leadingContent = {
                    val iconUrl = if (isSystemInDarkTheme())
                        personPasskey.iconDark
                    else
                        personPasskey.iconLight

                    if(iconUrl != null) {
                        AsyncImage(
                            model = iconUrl,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(40.dp),
                            contentDescription = ""
                        )
                    }
                },
                trailingContent = {
                    IconButton(
                        onClick = { onClickRevokePasskey(personPasskey) },
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.delete),
                        )
                    }
                },
            )
        }
    }
}