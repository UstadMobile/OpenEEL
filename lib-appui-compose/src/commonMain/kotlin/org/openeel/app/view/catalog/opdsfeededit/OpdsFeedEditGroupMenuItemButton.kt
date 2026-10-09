package world.respect.app.view.catalog.opdsfeededit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.move


@Composable
fun OpdsGroupItemMenuButton(
    groupIndex: Int,
    itemIndex: Int,
    hasMoveOption: Boolean,
    onClickDelete: () -> Unit,
    onClickMove: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { menuExpanded = true },
        modifier = Modifier.testTag("item_menu_${groupIndex}_$itemIndex"),
    ) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(Res.string.move),
        )
    }
    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false },
    ) {
        if (hasMoveOption) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.move)) },
                onClick = {
                    menuExpanded = false
                    onClickMove()
                },
                modifier = Modifier.testTag("item_move_${groupIndex}_$itemIndex"),
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(Res.string.delete)) },
            onClick = { menuExpanded = false; onClickDelete() },
            modifier = Modifier.testTag("item_delete_${groupIndex}_$itemIndex"),
        )
    }
}