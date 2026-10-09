package world.respect.app.view.manageuser.acceptinvite

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectDetailField
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.langMapString
import world.respect.app.components.uiTextStringResource
import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.NewUserInvite
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.class_name
import world.respect.shared.generated.resources.device_name
import world.respect.shared.generated.resources.enable
import world.respect.shared.generated.resources.image_shared_device
import world.respect.shared.generated.resources.loading
import world.respect.shared.generated.resources.next
import world.respect.shared.generated.resources.required_field
import world.respect.shared.generated.resources.role
import world.respect.shared.generated.resources.school_name
import world.respect.shared.generated.resources.school_server_url
import world.respect.shared.generated.resources.shared_device
import world.respect.shared.generated.resources.shared_device_description_1
import world.respect.shared.generated.resources.shared_device_description_2
import world.respect.shared.generated.resources.shared_device_description_3
import world.respect.shared.generated.resources.undraw_sync_pe2t_1
import world.respect.shared.util.ext.isLoading
import world.respect.shared.util.ext.label
import world.respect.shared.util.ext.roleLabel
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.manageuser.acceptinvite.AcceptInviteUiState
import world.respect.shared.viewmodel.manageuser.acceptinvite.AcceptInviteViewModel

@Composable
fun AcceptInviteScreen(
    viewModel: AcceptInviteViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val appUiState by viewModel.appUiState.collectAsState()
    if (!uiState.isSharedDeviceMode) {
        AcceptInviteScreen(
            uiState = uiState,
            appUiState = appUiState,
            onClickNext = viewModel::onClickNext
        )
    } else {
        SharedSchoolDeviceEnableScreenContent(
            uiState = uiState,
            onDeviceNameChange = viewModel::updateDeviceName,
            onEnableSharedDeviceMode = viewModel::enableSharedDeviceMode,
        )
    }
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
                    text = stringResource(Res.string.loading),
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
                when (invite) {
                    is NewUserInvite -> {
                        RespectDetailField(
                            modifier = Modifier.defaultItemPadding(),
                            label = { Text(stringResource(Res.string.role)) },
                            value = { Text(stringResource(invite.role.label)) }
                        )
                    }

                    is ClassInvite -> {
                        RespectDetailField(
                            modifier = Modifier.defaultItemPadding(),
                            label = { Text(stringResource(Res.string.class_name)) },
                            value = { Text(uiState.inviteInfo?.className ?: "") },
                        )

                        RespectDetailField(
                            modifier = Modifier.defaultItemPadding(),
                            label = { Text(stringResource(Res.string.role)) },
                            value = { Text(stringResource(invite.roleLabel)) }
                        )
                    }

                    else -> {
                        //Do nothing else
                    }
                }

                RespectDetailField(
                    modifier = Modifier.defaultItemPadding(),
                    label = { Text(stringResource(Res.string.school_name)) },
                    value = { Text(uiState.schoolName?.let { langMapString(it) } ?: "") }
                )

                RespectDetailField(
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

@Composable
fun SharedSchoolDeviceEnableScreenContent(
    uiState: AcceptInviteUiState,
    onDeviceNameChange: (String) -> Unit = {},
    onEnableSharedDeviceMode: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .defaultItemPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(Res.string.device_name),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        item {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("device_name_input"),
                value = uiState.deviceName,
                label = { Text("${stringResource(Res.string.device_name)} *") },
                onValueChange = onDeviceNameChange,
                singleLine = true,
                isError = uiState.errorText != null,
                supportingText = {
                    if (uiState.errorText != null) {
                        Text(text = "${stringResource(Res.string.required_field)}*")
                    }
                }
            )
        }
        item {
            SharedSchoolDeviceInfoBox(
                onClickEnableSharedSchoolDeviceMode = onEnableSharedDeviceMode
            )
        }
    }
}

@Composable
private fun SharedSchoolDeviceInfoBox(
    onClickEnableSharedSchoolDeviceMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.undraw_sync_pe2t_1),
                    contentDescription = stringResource(Res.string.image_shared_device),
                    modifier = Modifier
                        .width(120.dp)
                        .height(100.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.shared_device),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = " * ${stringResource(Res.string.shared_device_description_1)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = " * ${stringResource(Res.string.shared_device_description_2)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = " * ${stringResource(Res.string.shared_device_description_3)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Button(
                onClick = onClickEnableSharedSchoolDeviceMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("enable_button"),
                enabled = true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
            ) {
                Text(stringResource(Res.string.enable))
            }
        }
    }
}

