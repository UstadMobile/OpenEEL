package world.respect.app.view.catalog.opdsfeededit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.browse
import world.respect.shared.generated.resources.browse_description
import world.respect.shared.generated.resources.use_link
import world.respect.shared.generated.resources.use_link_description


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpdsFeedEditAddItemBottomSheet(
    onDismiss: () -> Unit,
    onClickBrowse: () -> Unit,
    onClickUseLink: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = stringResource(Res.string.browse),
                )
            },
            headlineContent = { Text(text = stringResource(Res.string.browse)) },
            supportingContent = { Text(text = stringResource(Res.string.browse_description)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_item_type_browse")
                .clickable { onClickBrowse() },
        )
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Filled.Link,
                    contentDescription = stringResource(Res.string.use_link),
                )
            },
            headlineContent = { Text(text = stringResource(Res.string.use_link)) },
            supportingContent = { Text(text = stringResource(Res.string.use_link_description)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_item_type_use_link")
                .clickable { onClickUseLink() },
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
