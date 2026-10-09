package world.respect.app.view.catalog.opdsfeededit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.choose_section_type
import world.respect.shared.generated.resources.learning_item_section
import world.respect.shared.generated.resources.learning_item_section_description
import world.respect.shared.generated.resources.collections_section
import world.respect.shared.generated.resources.collection_section_description
import world.respect.shared.viewmodel.catalog.opdsfeededit.OpdsGroupType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpdsFeedEditAddGroupSelectTypeBottomSheet(
    onDismiss: () -> Unit,
    onClickAddGroupType: (OpdsGroupType) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Text(
            text = stringResource(Res.string.choose_section_type),
            modifier = Modifier.defaultItemPadding(),
        )
        HorizontalDivider()

        ListItem(
            headlineContent = { Text(stringResource(Res.string.collections_section)) },
            supportingContent = { Text(stringResource(Res.string.collection_section_description)) },
            leadingContent = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                    contentDescription = stringResource(Res.string.collections_section),
                )
            },
            modifier = Modifier
                .clickable { onClickAddGroupType(OpdsGroupType.NAVIGATION) }
                .testTag("section_type_playlist")
        )
        ListItem(
            headlineContent = { Text(stringResource(Res.string.learning_item_section)) },
            supportingContent = { Text(stringResource(Res.string.learning_item_section_description)) },
            leadingContent = {
                Icon(
                    imageVector = Icons.Filled.Book,
                    contentDescription = stringResource(Res.string.learning_item_section),
                )
            },
            modifier = Modifier
                .clickable { onClickAddGroupType(OpdsGroupType.PUBLICATION) }
                .testTag("section_type_learning_item")
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}