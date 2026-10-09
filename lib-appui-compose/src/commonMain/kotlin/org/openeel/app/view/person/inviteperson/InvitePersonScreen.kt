package world.respect.app.view.person.inviteperson

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectExposedDropDownMenuField
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.ClassInviteModeEnum
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.approval_not_required_until
import world.respect.shared.generated.resources.approval_required
import world.respect.shared.generated.resources.copy_link
import world.respect.shared.generated.resources.invite_code_label
import world.respect.shared.generated.resources.invite_students_directly
import world.respect.shared.generated.resources.invite_via_email
import world.respect.shared.generated.resources.invite_via_parents
import world.respect.shared.generated.resources.invite_via_share
import world.respect.shared.generated.resources.invite_via_sms
import world.respect.shared.generated.resources.parents_register_and
import world.respect.shared.generated.resources.qr_code
import world.respect.shared.generated.resources.reset_code
import world.respect.shared.generated.resources.role
import world.respect.shared.generated.resources.students_register_themselves
import world.respect.shared.util.ext.isLoading
import world.respect.shared.util.ext.label
import world.respect.shared.util.rememberFormattedTime
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.person.inviteperson.InvitePersonUiState
import world.respect.shared.viewmodel.person.inviteperson.InvitePersonViewModel

@Composable
fun InvitePersonScreen(
    viewModel: InvitePersonViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val appUiState by viewModel.appUiState.collectAsState()

    InvitePersonScreen(
        uiState = uiState,
        appUiState = appUiState,
        onClickInviteCode = viewModel::onClickInviteCode,
        onCopyLink = viewModel::copyInviteLinkToClipboard,
        onInviteViaSms = viewModel::onSendLinkViaSms,
        onInviteViaEmail = viewModel::onSendLinkViaEmail,
        onInviteViaShare = viewModel::onShareLink,
        onApprovalRequiredChanged = viewModel::onApprovalEnabledChanged,
        onRoleChange = viewModel::onRoleChange,
        onClickResetCode = viewModel::onClickResetCode,
        onSetClassInviteMode = viewModel::onSetClassInviteMode,
    )
}

@Composable
fun InvitePersonScreen(
    uiState: InvitePersonUiState,
    appUiState: AppUiState,
    onClickInviteCode: () -> Unit,
    onCopyLink: () -> Unit,
    onInviteViaSms: () -> Unit,
    onInviteViaEmail: () -> Unit,
    onInviteViaShare: () -> Unit,
    onApprovalRequiredChanged: (Boolean) -> Unit,
    onRoleChange: (PersonRoleEnum) -> Unit,
    onClickResetCode: () -> Unit,
    onSetClassInviteMode: (ClassInviteModeEnum) -> Unit = { },
) {
    val invite = uiState.invite.dataOrNull()
    val fieldsEnabled = !appUiState.isLoading && invite != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (!uiState.isSharedDeviceMode) {
            if (uiState.showRoleSelection) {
                val selectedRole = uiState.selectedRole ?: uiState.roleOptions.firstOrNull()
                ?: PersonRoleEnum.STUDENT

                RespectExposedDropDownMenuField(
                    value = selectedRole,
                    modifier = Modifier
                        .defaultItemPadding()
                        .fillMaxWidth()
                        .testTag("role"),
                    label = {
                        Text(stringResource(Res.string.role))
                    },
                    onOptionSelected = { newRole ->
                        onRoleChange(newRole)
                    },
                    options = uiState.roleOptions,
                    itemText = { stringResource(it.label) },
                    enabled = fieldsEnabled,
                )
            }
        }
        if (uiState.isSharedDeviceMode) {
            uiState.schoolName?.let {
                Text(
                    text = uiTextStringResource(it),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)
                )
            }
        }

        uiState.inviteUrl?.also { link ->
            val linkStr = link.toString()
            Image(
                painter = rememberQrCodePainter(linkStr),
                contentDescription = stringResource(Res.string.qr_code),
                modifier = Modifier
                    .background(Color.White)
                    .size(240.dp)
                    .defaultItemPadding()
                    .align(Alignment.CenterHorizontally),
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(color = MaterialTheme.colorScheme.primary)
                    ) {
                        append(linkStr)
                    }
                },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .testTag("invite_url")
                    .clickable(enabled = fieldsEnabled) { onInviteViaShare() }
                    .defaultItemPadding()
                    .align(Alignment.CenterHorizontally)
            )

        }

        invite?.also {
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally).defaultItemPadding()
                    .clickable(enabled = fieldsEnabled) {
                        onClickInviteCode()
                    },
            ) {
                Text(
                    text = stringResource(Res.string.invite_code_label) + ": ",
                    textAlign = TextAlign.Center,
                )

                //Separated out for easier automated testing.
                Text(
                    text = it.code,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("invite_code"),
                )
            }
        }

        HorizontalDivider()

        val classInviteMode = (invite as? ClassInvite)?.inviteMode
        if(uiState.showClassInviteMode) {
            ListItem(
                modifier = Modifier.selectable(
                    selected = classInviteMode == ClassInviteModeEnum.DIRECT,
                    onClick = { onSetClassInviteMode(ClassInviteModeEnum.DIRECT) },
                    role = Role.RadioButton,
                ),
                leadingContent = {
                    RadioButton(
                        selected = classInviteMode == ClassInviteModeEnum.DIRECT,
                        onClick = null,
                    )
                },
                headlineContent = {
                    Text(stringResource(Res.string.invite_students_directly))
                },
                supportingContent = {
                    Text(stringResource(Res.string.students_register_themselves))
                }
            )

            ListItem(
                modifier = Modifier.selectable(
                    selected = classInviteMode == ClassInviteModeEnum.VIA_PARENT,
                    onClick = { onSetClassInviteMode(ClassInviteModeEnum.VIA_PARENT) },
                    role = Role.RadioButton,
                ),
                leadingContent = {
                    RadioButton(
                        selected = classInviteMode == ClassInviteModeEnum.VIA_PARENT,
                        onClick = null,
                    )
                },
                headlineContent = {
                    Text(stringResource(Res.string.invite_via_parents))
                },
                supportingContent = {
                    Text(stringResource(Res.string.parents_register_and))
                }
            )
        }

        HorizontalDivider()

        val approvalRequiredAfter = invite?.approvalRequiredAfter

        ListItem(
            modifier = Modifier.clickable(enabled = fieldsEnabled) {
                onApprovalRequiredChanged(!uiState.approvalRequired)
            },
            headlineContent = { Text(stringResource(Res.string.approval_required)) },
            trailingContent = {
                Switch(
                    checked = uiState.approvalRequired,
                    enabled = fieldsEnabled,
                    onCheckedChange = { onApprovalRequiredChanged(it) }
                )
            },
            leadingContent = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
            supportingContent = if(!uiState.approvalRequired && approvalRequiredAfter != null) {
                {
                    val approvalOffUntil = rememberFormattedTime(approvalRequiredAfter)
                    Text(stringResource(Res.string.approval_not_required_until) + " " + approvalOffUntil)
                }
            }else {
                null
            }
        )

        HorizontalDivider()

        ListItem(
            modifier = Modifier.clickable(enabled = fieldsEnabled) { onInviteViaShare() },
            leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
            headlineContent = { Text(stringResource(Res.string.invite_via_share)) }
        )

        ListItem(
            modifier = Modifier.clickable(enabled = fieldsEnabled) { onCopyLink() },
            leadingContent = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
            headlineContent = { Text(stringResource(Res.string.copy_link)) }
        )

        ListItem(
            modifier = Modifier.clickable(enabled = fieldsEnabled) { onInviteViaSms() },
            leadingContent = { Icon(Icons.Default.Sms, contentDescription = null) },
            headlineContent = { Text(stringResource(Res.string.invite_via_sms)) }
        )

        ListItem(
            modifier = Modifier.clickable(enabled = fieldsEnabled) { onInviteViaEmail() },
            leadingContent = { Icon(Icons.Default.Email, contentDescription = null) },
            headlineContent = { Text(stringResource(Res.string.invite_via_email)) }
        )


        ListItem(
            modifier = Modifier.clickable(enabled = fieldsEnabled) { onClickResetCode() },
            leadingContent = { Icon(Icons.Default.Refresh, contentDescription = null) },
            headlineContent = { Text(stringResource(Res.string.reset_code)) }
        )
    }
}
