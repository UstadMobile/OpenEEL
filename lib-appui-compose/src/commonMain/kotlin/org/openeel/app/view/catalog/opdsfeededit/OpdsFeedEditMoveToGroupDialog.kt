package org.openeel.app.view.catalog.opdsfeededit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.cancel
import org.openeel.shared.generated.resources.move_to
import org.openeel.shared.viewmodel.catalog.opdsfeededit.MoveItemDestination


@Composable
fun OpdsFeedEditMoveToGroupDialog(
    destinations: List<MoveItemDestination>,
    topLevelDestTitle: StringResource,
    onClickGroup: (MoveItemDestination) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(Res.string.move_to)) },
        text = {
            Column {
                destinations.forEach { section ->
                    ListItem(
                        headlineContent = {
                            Text(section.group?.metadata?.title ?: stringResource(topLevelDestTitle))
                        },
                        supportingContent = {

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClickGroup(section) },
                    )
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.cancel))
            }
        },
    )
}