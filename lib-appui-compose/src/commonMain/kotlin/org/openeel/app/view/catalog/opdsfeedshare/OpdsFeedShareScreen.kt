package world.respect.app.view.catalog.opdsfeedshare

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.admins_in_my_school
import world.respect.shared.generated.resources.anyone_in_my_school
import world.respect.shared.generated.resources.anyone_with_the_link
import world.respect.shared.generated.resources.copy_link
import world.respect.shared.generated.resources.send_link_via_email
import world.respect.shared.generated.resources.send_link_via_sms
import world.respect.shared.generated.resources.share_link
import world.respect.shared.generated.resources.teachers_and_admins_in_my_school
import world.respect.shared.generated.resources.who_can_edit
import world.respect.shared.generated.resources.who_can_view
import world.respect.shared.viewmodel.catalog.opdsfeedshare.OpdsFeedShareUiState
import world.respect.shared.viewmodel.catalog.opdsfeedshare.OpdsFeedShareViewModel

@Composable
fun PlaylistShareScreen(
    viewModel: OpdsFeedShareViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    PlaylistShareScreen(
        uiState = uiState,
        onViewPermissionChanged = viewModel::onViewPermissionChanged,
        onEditPermissionChanged = viewModel::onEditPermissionChanged,
        onClickCopyLink = viewModel::onClickCopyLink,
        onClickShareLink = viewModel::onClickShareLink,
        onClickSendViaSms = viewModel::onClickSendViaSms,
        onClickSendViaEmail = viewModel::onClickSendViaEmail,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistShareScreen(
    uiState: OpdsFeedShareUiState = OpdsFeedShareUiState(),
    onViewPermissionChanged: (Int) -> Unit = {},
    onEditPermissionChanged: (Int) -> Unit = {},
    onClickCopyLink: () -> Unit = {},
    onClickShareLink: () -> Unit = {},
    onClickSendViaSms: () -> Unit = {},
    onClickSendViaEmail: () -> Unit = {},
) {

    val viewPermissionOptions = listOf(
        stringResource(Res.string.anyone_with_the_link),
        stringResource(Res.string.anyone_in_my_school),
        stringResource(Res.string.teachers_and_admins_in_my_school),
        stringResource(Res.string.admins_in_my_school),
    )
    val editPermissionOptions = listOf(
        stringResource(Res.string.teachers_and_admins_in_my_school),
        stringResource(Res.string.admins_in_my_school),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = uiState.playlistTitle,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.shareUrl.isNotBlank()) {
            Image(
                painter = rememberQrCodePainter(uiState.shareUrl),
                contentDescription = uiState.playlistTitle,
                modifier = Modifier
                    .size(200.dp)
                    .testTag("share_qr_code"),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = uiState.shareUrl,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider()

        // Who can view dropdown
        PermissionDropdown(
            label = stringResource(Res.string.who_can_view),
            options = viewPermissionOptions,
            selectedIndex = uiState.viewPermissionIndex,
            onSelectionChanged = onViewPermissionChanged,
            testTag = "view_permission_dropdown",
        )

        // Who can edit dropdown
        PermissionDropdown(
            label = stringResource(Res.string.who_can_edit),
            options = editPermissionOptions,
            selectedIndex = uiState.editPermissionIndex,
            onSelectionChanged = onEditPermissionChanged,
            testTag = "edit_permission_dropdown",
        )

        HorizontalDivider()

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = stringResource(Res.string.share_link),
                )
            },
            headlineContent = { Text(text = stringResource(Res.string.share_link)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClickShareLink() }
                .testTag("share_link_button"),
        )

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = stringResource(Res.string.copy_link),
                )
            },
            headlineContent = { Text(text = stringResource(Res.string.copy_link)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClickCopyLink() }
                .testTag("copy_link_button"),
        )

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Filled.Sms,
                    contentDescription = stringResource(Res.string.send_link_via_sms),
                )
            },
            headlineContent = { Text(text = stringResource(Res.string.send_link_via_sms)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClickSendViaSms() }
                .testTag("send_sms_button"),
        )

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = stringResource(Res.string.send_link_via_email),
                )
            },
            headlineContent = { Text(text = stringResource(Res.string.send_link_via_email)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClickSendViaEmail() }
                .testTag("send_email_button"),
        )
    }
}

/**
 * Reusable permission dropdown — used for both "Who can view" and "Who can edit".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionDropdown(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    testTag: String,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        OutlinedTextField(
            value = options.getOrElse(selectedIndex) { options.first() },
            onValueChange = {},
            readOnly = true,
            label = { Text(text = label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .testTag(testTag),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        expanded = false
                        onSelectionChanged(index)
                    },
                    modifier = Modifier.testTag("${testTag}_option_$index"),
                )
            }
        }
    }
}