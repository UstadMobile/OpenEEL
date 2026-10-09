package world.respect.app.view.clazz.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectPersonAvatar
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.school.model.Person
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.accept_invite
import world.respect.shared.generated.resources.date_of_birth
import world.respect.shared.generated.resources.dismiss_invite
import world.respect.shared.generated.resources.gender_literal

@Composable
fun ClassPendingPersonListItem(
    person: Person?,
    pendingRole: StringResource,
    showApproveDeclineOptions: Boolean,
    onClickAcceptInvite: (Person) -> Unit,
    onClickDismissInvite: (Person) -> Unit,
) {
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        leadingContent = {
            RespectPersonAvatar(
                name = person?.fullName() ?: ""
            )
        },
        headlineContent = {
            Text(
                text = "${
                    person?.fullName().orEmpty()
                } (${stringResource(pendingRole)})"
            )
        },
        supportingContent = {
            val gender = person?.gender?.value
            val dob = person?.dateOfBirth ?: ""
            Text(
                text =
                    "${stringResource(Res.string.gender_literal)}: $gender, " +
                            "${stringResource(Res.string.date_of_birth)}: $dob"
            )

        },
        trailingContent = if(showApproveDeclineOptions) {
            {
                Row {
                    IconButton(
                        onClick = {
                            person?.also(onClickAcceptInvite)
                        },
                        enabled = person != null,
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = stringResource(resource = Res.string.accept_invite)
                        )
                    }

                    IconButton(
                        onClick = {
                            person?.also(onClickDismissInvite)
                        },
                        enabled = person != null
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = stringResource(resource = Res.string.dismiss_invite)
                        )
                    }
                }
            }
        }else {
            null
        }
    )
}