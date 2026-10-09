package world.respect.app.view.catalog.opdsfeededit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableColumn
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_section
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.sections
import world.respect.shared.generated.resources.title
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.groupType
import world.respect.shared.viewmodel.catalog.opdsfeededit.OpdsFeedEditUiState
import world.respect.shared.viewmodel.catalog.opdsfeededit.OpdsFeedEditViewModel
import world.respect.shared.viewmodel.catalog.opdsfeededit.OpdsGroupType

/**
 * The width of one IconButton as per
 * https://m3.material.io/components/icon-buttons/specs
 */
const val ICON_BUTTON_DEFAULT_WIDTH = 40

@Composable
fun OpdsFeedEditScreen(
    viewModel: OpdsFeedEditViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    OpdsFeedEditScreen(
        uiState = uiState,
        onTitleChanged = viewModel::onTitleChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onGroupTitleChanged = viewModel::onSectionTitleChanged,
        onClickAddGroup = viewModel::onClickAddGroup,
        onDismissAddGroupTypeDialog = viewModel::onDismissAddGroupTypeDialog,
        onClickAddGroupType = viewModel::onClickAddGroupType,
        onClickDeleteGroup = viewModel::onClickDeleteSection,
        onGroupMoved = viewModel::onGroupMoved,
        onClickAddItem = viewModel::onClickAddItem,
        onClickAddPlaylist = viewModel::onClickAddPlaylist,
        onClickDeleteItem = viewModel::onClickDeleteItem,
        onClickMoveItem = viewModel::onClickMoveItem,
        onClickMoveItemToGroup = viewModel::onClickMoveItemToSection,
        onDismissMoveDialog = viewModel::onDismissMoveDialog,
        onGroupItemsReordered = viewModel::onGroupItemsReordered,
        onDismissAddItemTypeBottomSheet = viewModel::onDismissAddItemTypeBottomSheet,
        onClickAddItemBrowse = viewModel::onClickAddItemBrowse,
        onClickAddItemUseLink = viewModel::onClickAddItemUseLink,
    )
}

@Composable
fun OpdsFeedEditScreen(
    uiState: OpdsFeedEditUiState = OpdsFeedEditUiState(),
    onTitleChanged: (String) -> Unit = {},
    onDescriptionChanged: (String) -> Unit = {},
    onGroupTitleChanged: (groupIndex: Int, title: String) -> Unit = { _, _ -> },
    onClickAddGroup: () -> Unit = {},
    onDismissAddGroupTypeDialog: () -> Unit = {},
    onClickAddGroupType: (OpdsGroupType) -> Unit = {},
    onClickDeleteGroup: (Int) -> Unit = {},
    onGroupMoved: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onClickAddItem: (Int) -> Unit = {},
    onClickAddPlaylist: (Int) -> Unit = {},
    onClickDeleteItem: (Int, Int) -> Unit = { _, _ -> },
    onClickMoveItem: (Int, Int) -> Unit = { _, _ -> },
    onClickMoveItemToGroup: (Int) -> Unit = {},
    onDismissMoveDialog: () -> Unit = {},
    onGroupItemsReordered: (groupIndex: Int, fromIndex: Int, toIndex: Int) -> Unit = { _, _, _ -> },
    onDismissAddItemTypeBottomSheet: () -> Unit = {},
    onClickAddItemBrowse: () -> Unit = {},
    onClickAddItemUseLink: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChanged,
            label = { Text(stringResource(Res.string.title) + "*") },
            isError = uiState.titleError != null,
            supportingText = {
                Text(uiTextStringResource(uiState.titleError ?: Res.string.required.asUiText()))
            },
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding()
                .testTag("playlist_title_field"),
            singleLine = true,
        )

        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChanged,
            label = { Text(stringResource(Res.string.description)) },
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding()
                .testTag("playlist_description_field"),
            minLines = 2,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.sections),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.defaultItemPadding(),
        )

        ListItem(
            modifier = Modifier
                .testTag("add_section_button")
                .clickable { onClickAddGroup() },
            headlineContent = {
                Text(text = stringResource(Res.string.add_section))
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp)
                )
            }
        )

        ReorderableColumn(
            list = uiState.groups,
            onSettle = onGroupMoved,
            modifier = Modifier.fillMaxWidth(),
        ) { groupIndex, group, _ ->
            key(groupIndex) {
                ReorderableItem {
                    OpdsFeedGroupEditItem(
                        groupIndex = groupIndex,
                        group = group,
                        showMoveItemOption = if(group.groupType == OpdsGroupType.NAVIGATION) {
                            uiState.canMoveNavigationItemToOtherGroup
                        }else {
                            uiState.canMovePublicationItemToOtherGroup
                        },
                        onGroupTitleChanged = { t -> onGroupTitleChanged(groupIndex, t) },
                        onClickDeleteGroup = { onClickDeleteGroup(groupIndex) },
                        onClickAddItem = { onClickAddItem(groupIndex) },
                        onClickAddPlaylist = { onClickAddPlaylist(groupIndex) },
                        onClickDeleteItem = { itemIndex ->
                            onClickDeleteItem(groupIndex, itemIndex)
                        },
                        onClickMoveItem = { itemIndex -> onClickMoveItem(groupIndex, itemIndex) },
                        onGroupItemsReordered = { fromIndex, toIndex ->
                            onGroupItemsReordered(
                                groupIndex, fromIndex, toIndex
                            )
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (uiState.addGroupTypeDialogVisible) {
        OpdsFeedEditAddGroupSelectTypeBottomSheet(
            onDismiss = onDismissAddGroupTypeDialog,
            onClickAddGroupType = onClickAddGroupType,
        )
    }

    if (uiState.isAddItemTypeBottomSheetVisible) {
        OpdsFeedEditAddItemBottomSheet(
            onDismiss = onDismissAddItemTypeBottomSheet,
            onClickBrowse = onClickAddItemBrowse,
            onClickUseLink = onClickAddItemUseLink,
        )
    }

    uiState.movingItem?.let { movingItem ->
        OpdsFeedEditMoveToGroupDialog(
            compatibleSections = movingItem.compatibleSections,
            allSections = uiState.groups,
            onClickGroup = onClickMoveItemToGroup,
            onDismiss = onDismissMoveDialog,
        )
    }
}
