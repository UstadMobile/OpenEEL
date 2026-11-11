package org.openeel.app.view.settings.sharedschooldevices

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.openeel.datalayer.db.school.ext.fullName
import org.openeel.datalayer.school.ext.getDeviceInfo
import org.openeel.datalayer.school.model.Person
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.this_device


@Composable
fun SharedDeviceListItem(
    person: Person,
    modifier: Modifier = Modifier,
    isCurrentDevice: Boolean = false,
    trailingContent: @Composable () -> Unit,
) {
    ListItem(
        modifier = modifier,
        leadingContent = {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
            )
        },
        headlineContent = {
            Text(
                text = if(isCurrentDevice) {
                    "${person.fullName()} (${stringResource(Res.string.this_device)})"
                }else {
                    person.fullName()
                }
            )
        },
        supportingContent = {
            Text(person.getDeviceInfo())
        },
        trailingContent = trailingContent,
    )
}