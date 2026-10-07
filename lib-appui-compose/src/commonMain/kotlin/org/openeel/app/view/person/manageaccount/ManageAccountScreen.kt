package org.openeel.app.view.person.manageaccount

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.OpenEelBottomSheetOption
import org.openeel.app.components.OpenEelPasskeySignInFasterCard
import org.openeel.app.components.appendStringRes
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.components.uiTextStringResource
import org.openeel.app.components.OpenEelQrBadgeInfoBox
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.assign_new_badge_replace
import org.openeel.shared.generated.resources.assigned
import org.openeel.shared.generated.resources.change
import org.openeel.shared.generated.resources.create_passkey
import org.openeel.shared.generated.resources.last_updated
import org.openeel.shared.generated.resources.manage
import org.openeel.shared.generated.resources.passkeys
import org.openeel.shared.generated.resources.password_label
import org.openeel.shared.generated.resources.qr_code_badge
import org.openeel.shared.generated.resources.revoke_badge
import org.openeel.shared.generated.resources.security
import org.openeel.shared.generated.resources.username_label
import org.openeel.shared.generated.resources.badge
import org.openeel.shared.generated.resources.not_set
import org.openeel.shared.generated.resources.set_password
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.util.rememberFormattedDateTime
import org.openeel.shared.viewmodel.person.manageaccount.ManageAccountUiState
import org.openeel.shared.viewmodel.person.manageaccount.ManageAccountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAccountScreen(
    viewModel: ManageAccountViewModel
) {

    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showBottomSheet) {
        RespectQRBadgeOptionsBottomSheet(
            onAssignNewBadge = viewModel::onClickQRCodeBadge,
            onRemoveBadge = viewModel::onRemoveQRBadge,
            onDismissRequest = viewModel::onDismissBottomSheet
        )
    }

    ManageAccountScreenContent(
        uiState = uiState,
        onCreatePasskeyClick = viewModel::onCreatePasskeyClick,
        onClickManagePasskey = viewModel::onClickManagePasskey,
        onClickChangePassword = viewModel::onClickChangePassword,
        onClickHowPasskeysWork = viewModel::onClickHowPasskeysWork,
        onClickAssignQrCodeBadge = viewModel::onClickQRCodeBadge,
        onClickChangeQrBadge = viewModel::onClickChangeQrBadge,
    )
}

@Composable
fun ManageAccountScreenContent(
    uiState: ManageAccountUiState,
    onCreatePasskeyClick: () -> Unit = {},
    onClickHowPasskeysWork: () -> Unit = {},
    onClickManagePasskey: () -> Unit = {},
    onClickChangePassword: () -> Unit = {},
    onClickAssignQrCodeBadge: () -> Unit = {},
    onClickChangeQrBadge: () -> Unit = {},
    onClickQrBadgeLearnMore: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        uiState.errorText?.also {
            Text(
                uiTextStringResource(it),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.defaultItemPadding(),
            )
        }

        ListItem(
            headlineContent = {
                Text(text = uiState.personUsername)
            },
            supportingContent = {
                Text(text = stringResource(Res.string.username_label))
            }
        )

        if (uiState.passkeySupported || uiState.isStudent) {
            ListItem(
                headlineContent = {
                    Text(text = stringResource(Res.string.security))
                }
            )

            if (uiState.showCreatePasskey) {
                OpenEelPasskeySignInFasterCard(
                    modifier = Modifier.fillMaxWidth().defaultItemPadding(),
                    onClickPasskeySignup = onCreatePasskeyClick,
                    onClickHowPasskeysWork = onClickHowPasskeysWork,
                    buttonText = Res.string.create_passkey.asUiText(),
                )
            }

            if (uiState.showManagePasskey) {
                ListItem(
                    modifier = Modifier.clickable {
                        onClickManagePasskey()
                    },
                    leadingContent = {
                        Icon(Icons.Default.Security, contentDescription = null)
                    },
                    headlineContent = {
                        Text(
                            text = "${uiState.passkeyCount ?: ""} ${stringResource(Res.string.passkeys)}",
                            maxLines = 1,
                        )
                    },
                    supportingContent = {
                        Text(text = stringResource(Res.string.passkeys))
                    },
                    trailingContent = {
                        Text(text = stringResource(Res.string.manage))
                    }
                )
            }
        }

        if (uiState.isStudent) {
            val personQrVal = uiState.qrBadge.dataOrNull()
            val badgeNumber = uiState.badgeNumber
            val qrLastUpdatedStr = rememberFormattedDateTime(
                timeInMillis = personQrVal?.lastModified?.toEpochMilliseconds() ?: 0,
                timeZoneId = TimeZone.currentSystemDefault().id,
            )

            if (uiState.isQrAdded) {
                // Show assigned badge
                ListItem(
                    leadingContent = {
                        Icon(Icons.Default.QrCode, contentDescription = null)
                    },
                    headlineContent = {
                        Text(
                            text = stringResource(Res.string.qr_code_badge),
                            maxLines = 1,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "${stringResource(Res.string.badge)} #$badgeNumber ${
                                stringResource(
                                    Res.string.assigned
                                )
                            } $qrLastUpdatedStr"
                        )
                    },
                    trailingContent = {
                        OutlinedButton(
                            onClick = onClickChangeQrBadge
                        ) {
                            Text(
                                text = stringResource(Res.string.change),
                                modifier = Modifier.testTag("qr_change_btn"),
                            )
                        }
                    }
                )
            } else {
                // Show QR code info box for assigning a badge
                OpenEelQrBadgeInfoBox(
                    onClickLearnMore = onClickQrBadgeLearnMore,
                    onClickAssignQrCodeBadge = onClickAssignQrCodeBadge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }

        val personPasswordVal = uiState.personPassword.dataOrNull()
        val passwordLastUpdatedStr = rememberFormattedDateTime(
            timeInMillis = personPasswordVal?.lastModified?.toEpochMilliseconds() ?: 0,
            timeZoneId = TimeZone.currentSystemDefault().id,
        )

        ListItem(
            leadingContent = {
                Icon(Icons.Default.Password, contentDescription = null)
            },
            headlineContent = {
                Text(
                    text = stringResource(Res.string.password_label),
                    maxLines = 1,
                )
            },
            supportingContent = {
                Text(
                    text = buildString {
                        if(personPasswordVal != null) {
                            appendStringRes(Res.string.last_updated)
                            append(": ")
                            append(passwordLastUpdatedStr)
                        }else {
                            appendStringRes(Res.string.not_set)
                        }
                    }
                )

                Text(
                    text = "${stringResource(Res.string.last_updated)}: $passwordLastUpdatedStr"
                )
            },
            trailingContent = {
                OutlinedButton(
                    onClick = onClickChangePassword,
                    modifier = Modifier.testTag("password_change_btn"),
                ) {
                    Text(
                        text = stringResource(
                            if(personPasswordVal != null)
                                Res.string.change
                            else
                                Res.string.set_password
                        ),
                        modifier = Modifier.testTag("password_change_btn"),
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RespectQRBadgeOptionsBottomSheet(
    onAssignNewBadge: () -> Unit = {},
    onRemoveBadge: () -> Unit = {},
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            Modifier.verticalScroll(
                state = rememberScrollState()
            ).fillMaxSize()
        ) {
            OpenEelBottomSheetOption(
                modifier = Modifier.clickable {
                    onDismissRequest()
                    onAssignNewBadge()
                },
                headlineContent = {
                    Text(stringResource(Res.string.assign_new_badge_replace))
                },
            )
            OpenEelBottomSheetOption(
                modifier = Modifier.clickable {
                    onDismissRequest()
                    onRemoveBadge()
                },
                headlineContent = {
                    Text(
                        text = stringResource(Res.string.revoke_badge),
                        color = MaterialTheme.colorScheme.error
                    )
                },
            )
        }
    }
}