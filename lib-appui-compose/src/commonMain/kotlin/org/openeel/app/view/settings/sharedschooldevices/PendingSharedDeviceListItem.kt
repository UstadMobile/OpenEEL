package org.openeel.app.view.settings.sharedschooldevices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.datalayer.school.model.Person
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.accept_invite
import org.openeel.shared.generated.resources.dismiss_invite

@Composable
fun PendingSharedDeviceListItem(
    device: Person,
    onClickAcceptOrDismissInvite: (Person, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SharedDeviceListItem(
        person = device,
        modifier = modifier,
        trailingContent = {
            Row {
                Icon(
                    modifier = Modifier.size(24.dp)
                        .clickable {
                            onClickAcceptOrDismissInvite(
                                device, true
                            )
                        },
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = stringResource(resource = Res.string.accept_invite)
                )

                Spacer(Modifier.width(16.dp))

                Icon(
                    modifier = Modifier.size(24.dp).clickable {
                        onClickAcceptOrDismissInvite(device, false)
                    },
                    imageVector = Icons.Outlined.Cancel,
                    contentDescription = stringResource(resource = Res.string.dismiss_invite)
                )
            }
        }
    )
}