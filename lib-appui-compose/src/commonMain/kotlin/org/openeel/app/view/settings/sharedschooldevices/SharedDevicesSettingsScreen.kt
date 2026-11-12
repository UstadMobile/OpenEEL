package org.openeel.app.view.settings.sharedschooldevices

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.OpenEelExpandCollapseListItem
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.components.openEelPagingItems
import org.openeel.app.components.openEelRememberPager
import org.openeel.datalayer.school.model.Person
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.devices
import org.openeel.shared.generated.resources.no_shared_devices_available
import org.openeel.shared.generated.resources.no_shared_devices_available_info
import org.openeel.shared.generated.resources.pending_device_requests
import org.openeel.shared.generated.resources.qr_code_badge
import org.openeel.shared.generated.resources.student_can_self_select_their_class_name
import org.openeel.shared.generated.resources.teacher_admin_unlock_pin
import org.openeel.shared.generated.resources.undraw_bookmarks_i66k__1__1
import org.openeel.shared.viewmodel.settings.sharedschooldevices.SharedDevicesSettingsUiState
import org.openeel.shared.viewmodel.settings.sharedschooldevices.SharedDevicesSettingsViewModel

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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingsOptionRow(
                    modifier = Modifier.defaultItemPadding(),
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
                    .defaultItemPadding(),
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
                OpenEelExpandCollapseListItem(
                    expanded = uiState.isPendingExpanded,
                    onExpandedChanged = {
                        onTogglePendingInvites()
                    },
                    headlineContent = {
                        Text(
                            text = stringResource(Res.string.pending_device_requests) +
                                    " (${pendingItems.itemCount})"
                        )
                    },
                )
            }
        }

        if (uiState.isPendingExpanded) {
            openEelPagingItems(
                items = pendingItems,
                key = { item, index -> (item?.guid ?: "p_$index") }
            ) { device ->
                device?.let { device ->
                    PendingSharedDeviceListItem(
                        device = device,
                        onClickAcceptOrDismissInvite = onClickAcceptOrDismissInvite,
                    )
                }
            }
        }

        item("devices_heading") {
            Text(
                text = stringResource(Res.string.devices) + " (${lazyPagingItems.itemCount})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().defaultItemPadding(),
            )
        }

        if (lazyPagingItems.itemCount == 0) {
            item("devices_empty") {
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
            uiState.thisDevice.dataOrNull()?.also {
                item("this_device") {
                    ActiveSharedDeviceListItem(
                        person = it,
                        isCurrentDevice = true,
                        onClickRemove = onRemoveDevice
                    )
                }
            }

            openEelPagingItems(
                items = lazyPagingItems,
                key = { item, index -> item?.guid ?: "a_$index" },
                contentType = { "active_device" },
            ) { person ->
                person?.takeIf { !uiState.isCurrentDevice(it) }?.also {
                    ActiveSharedDeviceListItem(
                        person = it,
                        isCurrentDevice = uiState.isCurrentDevice(it),
                        onClickRemove = onRemoveDevice,
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
