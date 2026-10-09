package world.respect.app.view.person.list

import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectPersonAvatar
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.shared.util.ext.fullName
import world.respect.shared.util.ext.label

@Composable
fun PersonListItem(
    person: PersonListDetails?,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    ListItem(
        modifier = modifier,
        leadingContent = {
            RespectPersonAvatar(person?.fullName() ?: "")
        },
        headlineContent = {
            Text(person?.fullName() ?: "")
        },
        supportingContent = {
            person?.role?.label?.also { roleLabel ->
                Text(stringResource(roleLabel))
            }
        },
        trailingContent = trailingContent,
    )
}
