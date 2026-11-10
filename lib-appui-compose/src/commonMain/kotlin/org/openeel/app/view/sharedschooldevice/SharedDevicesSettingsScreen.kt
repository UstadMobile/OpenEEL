package org.openeel.app.view.sharedschooldevice

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.components.openEelPagingItems
import org.openeel.app.components.openEelRememberPager
import org.openeel.datalayer.db.school.ext.fullName
import org.openeel.datalayer.school.ext.getDeviceInfo
import org.openeel.datalayer.school.model.Person
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.libutil.util.time.toFormattedDate
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.accept_invite
import org.openeel.shared.generated.resources.arrow_down_icon
import org.openeel.shared.generated.resources.close_icon
import org.openeel.shared.generated.resources.devices
import org.openeel.shared.generated.resources.dismiss_invite
import org.openeel.shared.generated.resources.no_shared_devices_available
import org.openeel.shared.generated.resources.no_shared_devices_available_info
import org.openeel.shared.generated.resources.pending_device_requests
import org.openeel.shared.generated.resources.phone_android_icon
import org.openeel.shared.generated.resources.qr_code_badge
import org.openeel.shared.generated.resources.student_can_self_select_their_class_name
import org.openeel.shared.generated.resources.tablet_android_last_seen
import org.openeel.shared.generated.resources.teacher_admin_unlock_pin
import org.openeel.shared.generated.resources.this_device
import org.openeel.shared.generated.resources.undraw_bookmarks_i66k__1__1
import org.openeel.shared.viewmodel.sharedschooldevice.SharedDevicesSettingsUiState
import org.openeel.shared.viewmodel.sharedschooldevice.SharedDevicesSettingsViewModel

@Composable
fun SharedDevicesSettingsScreen(
    viewModel: SharedDevicesSettingsViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    SharedDevicesSettingsContent(
        uiState = uiState,
        onToggleSelfSelect = viewModel::toggleSelfSelect,
        onShowPinDialog = viewModel::onShowPinDialog,
        onTogglePendingInvites = viewModel::onTogglePendingInvites,
        onClickAcceptOrDismissInvite = viewModel::onClickAcceptOrDismissInvite,
        onRemoveDevice = viewModel::onRemoveDevice,
        onSavePin = viewModel::onSavePin,
        onDismissPinDialog = viewModel::onDismissPinDialog,
        onAddAnotherDevice = {
            viewModel.onClickAddAnotherDevice()
            viewModel.onDismissBottomSheet()
        },
        onEnableOnThisDevice = {
            viewModel.onClickEnableOnThisDevice()
            viewModel.onDismissBottomSheet()
        },
        onDismissBottomSheet = viewModel::onDismissBottomSheet,
    )
}

@Composable
fun SharedDevicesSettingsContent(
    uiState: SharedDevicesSettingsUiState,
    onToggleSelfSelect: (Boolean) -> Unit,
    onShowPinDialog: () -> Unit,
    onTogglePendingInvites: () -> Unit,
    onClickAcceptOrDismissInvite: (Person, Boolean) -> Unit,
    onRemoveDevice: (Person) -> Unit,
    onSavePin: (String) -> Unit,
    onDismissPinDialog: () -> Unit,
    onAddAnotherDevice: () -> Unit,
    onEnableOnThisDevice: () -> Unit,
    onDismissBottomSheet: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    val pager = openEelRememberPager(uiState.devices)
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    val pendingPager = openEelRememberPager(uiState.pendingDevices)
    val pendingItems = pendingPager.flow.collectAsLazyPagingItems()
    // Handle bottom sheet dismissal
    LaunchedEffect(uiState.showBottomSheetOptions) {
        if (uiState.showBottomSheetOptions) {
            focusManager.clearFocus()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .defaultItemPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsOptionRow(
                        title = stringResource(Res.string.student_can_self_select_their_class_name),
                        checked = uiState.sharedDeviceSettings.dataOrNull()?.canSelectName ?: false,
                        onCheckedChange = onToggleSelfSelect
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .clickable { onShowPinDialog() }
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.teacher_admin_unlock_pin),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = uiState.sharedDeviceSettings.dataOrNull()?.pin ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.testTag("set_pin")
                        )
                    }
                }
            }

            // Pending Requests Dropdown Section
            if (pendingItems.itemCount > 0) {
                item("pending_person_header") {
                    ListItem(
                        modifier = Modifier.clickable { onTogglePendingInvites() },
                        headlineContent = {
                            Text(
                                text = stringResource(Res.string.pending_device_requests) +
                                        " (${pendingItems.itemCount})"
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(if (uiState.isPendingExpanded) 0f else -90f),
                                contentDescription = stringResource(Res.string.arrow_down_icon)
                            )
                        }
                    )
                }
            }

            if (uiState.isPendingExpanded) {
                openEelPagingItems(
                    items = pendingItems,
                    key = { item, index -> (item?.guid ?: "") + index.toString() }
                ) { device ->
                    device?.let { device ->
                        ListItem(
                            modifier = Modifier.clickable { },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = stringResource(Res.string.phone_android_icon),
                                )
                            },
                            headlineContent = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = device.givenName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${device.getDeviceInfo()} ${
                                            stringResource(
                                                Res.string.tablet_android_last_seen
                                            )
                                        }: ${device.lastModified.toString().toFormattedDate()}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            trailingContent = {
                                Row {
                                    Icon(
                                        modifier = Modifier.size(24.dp)
                                            .clickable {
                                                device.also {
                                                    onClickAcceptOrDismissInvite(
                                                        it,
                                                        true
                                                    )
                                                }
                                            },
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = stringResource(resource = Res.string.accept_invite)
                                    )

                                    Spacer(Modifier.width(16.dp))

                                    Icon(
                                        modifier = Modifier.size(24.dp).clickable {
                                            device.also { onClickAcceptOrDismissInvite(it, false) }
                                        },
                                        imageVector = Icons.Outlined.Cancel,
                                        contentDescription = stringResource(resource = Res.string.dismiss_invite)
                                    )
                                }
                            }
                        )
                    }
                }
            }

            item {
                Text(
                    text = stringResource(Res.string.devices) + " (${lazyPagingItems.itemCount})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (lazyPagingItems.itemCount == 0) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(top = 34.dp)
                            .fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.undraw_bookmarks_i66k__1__1),
                            contentDescription = stringResource(Res.string.qr_code_badge),
                            modifier = Modifier
                        )
                        Text(
                            text = stringResource(Res.string.no_shared_devices_available),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = stringResource(Res.string.no_shared_devices_available_info),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                openEelPagingItems(
                    items = lazyPagingItems,
                    key = { item, index -> item?.guid ?: index.toString() },
                    contentType = { "active_device" },
                ) { person ->
                    person?.let {
                        val isCurrentDevice = uiState.isCurrentDevice(it)
                        ListItem(
                            modifier = Modifier.clickable { },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = stringResource(Res.string.phone_android_icon),
                                )
                            },
                            headlineContent = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = if (isCurrentDevice) {
                                            "${it.fullName()} (${stringResource(Res.string.this_device)})"
                                        } else {
                                            it.fullName()
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = it.getDeviceInfo(),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "${
                                            stringResource(Res.string.tablet_android_last_seen)
                                        }: ${it.lastModified.toString().toFormattedDate()}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = { onRemoveDevice(it) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(Res.string.close_icon),
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        // PIN Dialog
        if (uiState.newPinDialogVisible) {
            SharedDevicePinEntryDialog(
                onDismiss = onDismissPinDialog,
                onSave = onSavePin,
                errorMessage = uiState.error
            )
        }

        // Bottom Sheet
        if (uiState.showBottomSheetOptions && !uiState.newPinDialogVisible) {
            SharedDeviceAddBottomSheet(
                onDismiss = onDismissBottomSheet,
                onAddAnotherDevice = onAddAnotherDevice,
                onEnableOnThisDevice = onEnableOnThisDevice
            )
        }
    }
}

@Composable
private fun SettingsOptionRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag("self_select_their_class")
        )
    }
}
