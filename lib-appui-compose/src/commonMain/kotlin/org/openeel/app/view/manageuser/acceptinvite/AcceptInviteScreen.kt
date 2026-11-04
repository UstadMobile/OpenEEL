package org.openeel.app.view.manageuser.acceptinvite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.OpenEelDetailField
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.components.langMapString
import org.openeel.app.components.uiTextStringResource
import org.openeel.datalayer.school.model.ClassInvite
import org.openeel.datalayer.school.model.NewUserInvite
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.class_name
import org.openeel.shared.generated.resources.loading
import org.openeel.shared.generated.resources.next
import org.openeel.shared.generated.resources.role
import org.openeel.shared.generated.resources.school_name
import org.openeel.shared.generated.resources.school_server_url
import org.openeel.shared.util.ext.isLoading
import org.openeel.shared.util.ext.label
import org.openeel.shared.util.ext.roleLabel
import org.openeel.shared.viewmodel.app.appstate.AppUiState
import org.openeel.shared.viewmodel.manageuser.acceptinvite.AcceptInviteUiState
import org.openeel.shared.viewmodel.manageuser.acceptinvite.AcceptInviteViewModel

@Composable
fun AcceptInviteScreen(
    viewModel: AcceptInviteViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val appUiState by viewModel.appUiState.collectAsState()

    AcceptInviteScreen(
        uiState = uiState,
        appUiState = appUiState,
        onClickNext = viewModel::onClickNext
    )
}

@Composable
fun AcceptInviteScreen(
    uiState: AcceptInviteUiState,
    appUiState: AppUiState,
    onClickNext: () -> Unit
) {
    val invite = uiState.inviteInfo?.invite
    val errorText = uiState.errorText

    Column(modifier = Modifier.fillMaxSize()) {
        when {
            appUiState.isLoading -> {
                Spacer(Modifier.size(16.dp))

                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                Spacer(Modifier.size(16.dp))

                Text(
                    text =stringResource(Res.string.loading),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            errorText != null -> {
                Spacer(Modifier.size(16.dp))

                Icon(
                    modifier = Modifier.align(Alignment.CenterHorizontally).size(64.dp),
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                )

                Spacer(Modifier.size(16.dp))

                Text(
                    text = uiTextStringResource(errorText),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            invite != null -> {
                when(invite) {
                    is NewUserInvite -> {
                        OpenEelDetailField(
                            modifier = Modifier.defaultItemPadding(),
                            label = { Text(stringResource(Res.string.role)) },
                            value = { Text(stringResource(invite.role.label)) }
                        )
                    }

                    is ClassInvite -> {
                        OpenEelDetailField(
                            modifier = Modifier.defaultItemPadding(),
                            label = { Text(stringResource(Res.string.class_name)) },
                            value = { Text(uiState.inviteInfo?.className ?: "") },
                        )

                        OpenEelDetailField(
                            modifier = Modifier.defaultItemPadding(),
                            label = { Text(stringResource(Res.string.role)) },
                            value = { Text(stringResource(invite.roleLabel)) }
                        )
                    }

                    else -> {
                        //Do nothing else
                    }
                }

                OpenEelDetailField(
                    modifier = Modifier.defaultItemPadding(),
                    label = { Text(stringResource(Res.string.school_name)) },
                    value = { Text(uiState.schoolName?.let { langMapString(it) } ?: "") }
                )

                OpenEelDetailField(
                    modifier = Modifier.defaultItemPadding(),
                    label = { Text(stringResource(Res.string.school_server_url)) },
                    value = { Text(uiState.schoolUrl?.toString() ?: "") }
                )

                Button(
                    onClick = onClickNext,
                    modifier = Modifier.fillMaxWidth().defaultItemPadding(),
                    enabled = uiState.nextButtonEnabled,
                ) {
                    Text(stringResource(Res.string.next))
                }
            }
        }



    }

}
