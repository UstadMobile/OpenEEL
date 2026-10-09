package world.respect.app.view.sharedschooldevice

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.paging.compose.collectAsLazyPagingItems
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.app.components.uiTextStringResource
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.school.ext.getDeviceInfo
import world.respect.datalayer.school.model.Person
import world.respect.libutil.util.time.toFormattedDate
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.accept_invite
import world.respect.shared.generated.resources.add_device
import world.respect.shared.generated.resources.another_device
import world.respect.shared.generated.resources.another_device_add
import world.respect.shared.generated.resources.arrow_down_icon
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.close_icon
import world.respect.shared.generated.resources.devices
import world.respect.shared.generated.resources.dismiss_invite
import world.respect.shared.generated.resources.no_shared_devices_available
import world.respect.shared.generated.resources.no_shared_devices_available_info
import world.respect.shared.generated.resources.pending_device_requests
import world.respect.shared.generated.resources.phone_android_icon
import world.respect.shared.generated.resources.qr_code_badge
import world.respect.shared.generated.resources.save
import world.respect.shared.generated.resources.set_pin
import world.respect.shared.generated.resources.share_icon
import world.respect.shared.generated.resources.student_can_self_select_their_class_name
import world.respect.shared.generated.resources.tablet_android_last_seen
import world.respect.shared.generated.resources.teacher_admin_unlock_pin
import world.respect.shared.generated.resources.this_device
import world.respect.shared.generated.resources.this_device_enable
import world.respect.shared.generated.resources.undraw_bookmarks_i66k__1__1
import world.respect.shared.resources.UiText
import world.respect.shared.viewmodel.sharedschooldevice.SharedDevicesSettingsUiState
import world.respect.shared.viewmodel.sharedschooldevice.SharedDevicesSettingsViewmodel

@Composable
fun SharedDevicesSettingsScreen(
    viewModel: SharedDevicesSettingsViewmodel,
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
private fun SharedDevicesSettingsContent(
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

    val pager = respectRememberPager(uiState.devices)
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    val pendingPager = respectRememberPager(uiState.pendingDevices)
    val pendingItems = pendingPager.flow.collectAsLazyPagingItems()
    // Create sorted items list using remember with keys to trigger recomposition
    val sortedItems = remember(
        lazyPagingItems.itemCount,
        lazyPagingItems.itemSnapshotList,
        uiState.currentDeviceGuid
    ) {
        lazyPagingItems.itemSnapshotList.items
            .sortedWith(
                compareBy<Person> { person ->
                    // Current device first (false before true)
                    person.guid != uiState.currentDeviceGuid
                }.thenBy { it.fullName() }
            )
    }
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
                        checked = uiState.isSelfSelectClassAndName,
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
                            text = uiState.pin,
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
                respectPagingItems(
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
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                items(sortedItems.size) { index ->
                    val personDetails = sortedItems[index]
                    val isCurrentDevice = personDetails.guid == uiState.currentDeviceGuid

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
                                        "${personDetails.fullName()} (${stringResource(Res.string.this_device)})"
                                    } else {
                                        personDetails.fullName()
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = personDetails.getDeviceInfo(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${
                                        stringResource(Res.string.tablet_android_last_seen)
                                    }: ${personDetails.lastModified.toString().toFormattedDate()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        trailingContent = {
                            IconButton(
                                onClick = { onRemoveDevice(personDetails) }
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

        // PIN Dialog
        if (uiState.showPinDialog) {
            PinEntryDialog(
                onDismiss = onDismissPinDialog,
                onSave = onSavePin,
                errorMessage = uiState.error
            )
        }

        // Bottom Sheet
        if (uiState.showBottomSheetOptions && !uiState.showPinDialog) {
            AddDeviceBottomSheet(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceBottomSheet(
    onDismiss: () -> Unit,
    onAddAnotherDevice: () -> Unit,
    onEnableOnThisDevice: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(Res.string.add_device),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onEnableOnThisDevice)
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = stringResource(Res.string.phone_android_icon),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.this_device),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Res.string.this_device_enable),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddAnotherDevice)
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(Res.string.share_icon),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.another_device),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Res.string.another_device_add),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinEntryDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    errorMessage: UiText? = null,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    var currentPin by remember { mutableStateOf("") }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(),
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
            ) {
                // Title
                Text(
                    text = stringResource(Res.string.set_pin),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(24.dp))

                BasicTextField(
                    value = currentPin,
                    onValueChange = { newPin ->
                        currentPin = newPin
                    },
                    modifier = Modifier
                        .testTag("pin_text")
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.surfaceVariant)
                        .focusRequester(focusRequester)
                        .padding(8.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword
                    ),
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiTextStringResource(errorMessage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel Text
                    Text(
                        text = stringResource(Res.string.cancel),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Save Text
                    Text(
                        text = stringResource(Res.string.save),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable(
                                onClick = { onSave(currentPin) }
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}