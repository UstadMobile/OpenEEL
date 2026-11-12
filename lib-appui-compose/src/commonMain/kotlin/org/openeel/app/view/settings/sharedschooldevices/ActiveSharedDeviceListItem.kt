package org.openeel.app.view.settings.sharedschooldevices

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.openeel.datalayer.school.model.Person
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.remove

@Composable
fun ActiveSharedDeviceListItem(
    person: Person,
    modifier: Modifier = Modifier,
    isCurrentDevice: Boolean = false,
    onClickRemove: (Person) -> Unit,
) {
    SharedDeviceListItem(
        person = person,
        modifier = modifier,
        isCurrentDevice = isCurrentDevice,
        trailingContent = {
            IconButton(
                onClick = { onClickRemove(person) }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.remove),
                )
            }
        }
    )
}