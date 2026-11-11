package org.openeel.app.view.settings.sharedschooldevices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.defaultItemPadding
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_device
import org.openeel.shared.generated.resources.another_device
import org.openeel.shared.generated.resources.another_device_add
import org.openeel.shared.generated.resources.this_device
import org.openeel.shared.generated.resources.this_device_enable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedDeviceAddBottomSheet(
    onDismiss: () -> Unit,
    onAddAnotherDevice: () -> Unit,
    onEnableOnThisDevice: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Text(
            text = stringResource(Res.string.add_device),
            modifier = Modifier.defaultItemPadding(),
            style = MaterialTheme.typography.titleMedium,
        )
        HorizontalDivider()

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                )
            },
            headlineContent = { Text(text = stringResource(Res.string.this_device)) },
            supportingContent = { Text(text = stringResource(Res.string.this_device_enable)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_device_this_device")
                .clickable(onClick = onEnableOnThisDevice),
        )

        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                )
            },
            headlineContent = { Text(text = stringResource(Res.string.another_device)) },
            supportingContent = { Text(text = stringResource(Res.string.another_device_add)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_device_another_device")
                .clickable(onClick = onAddAnotherDevice),
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
