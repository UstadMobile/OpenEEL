package org.openeel.app.view.catalog.opdsfeededit

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.OpenEelExpandCollapseListItem
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.components.uiTextStringResource
import org.openeel.lib.opds.model.ext.indexIsInNavigationCollection
import org.openeel.lib.opds.model.util.NavigationItemItemIndex
import org.openeel.lib.opds.model.util.OpdsFeedGroupItemItemIndex
import org.openeel.lib.opds.model.util.OpdsFeedItemIndex
import org.openeel.lib.opds.model.util.PublicationItemItemIndex
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_section
import org.openeel.shared.generated.resources.collection_section_description
import org.openeel.shared.generated.resources.description
import org.openeel.shared.generated.resources.links_to_learning_items
import org.openeel.shared.generated.resources.required
import org.openeel.shared.generated.resources.sections
import org.openeel.shared.generated.resources.title
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.util.ext.groupType
import org.openeel.shared.viewmodel.catalog.opdsfeededit.MoveItemDestination
import org.openeel.shared.viewmodel.catalog.opdsfeededit.OpdsFeedEditUiState
import org.openeel.shared.viewmodel.catalog.opdsfeededit.OpdsFeedEditViewModel
import org.openeel.shared.viewmodel.catalog.opdsfeededit.OpdsGroupType
import sh.calvin.reorderable.ReorderableColumn

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
        onGroupTitleChanged = viewModel::onGroupTitleChanged,
        onClickAddGroup = viewModel::onClickAddGroup,
        onDismissAddGroupTypeDialog = viewModel::onDismissAddGroupTypeDialog,
        onClickAddGroupType = viewModel::onClickAddGroupType,
        onClickDeleteGroup = viewModel::onClickDeleteSection,
        onGroupMoved = viewModel::onGroupMoved,
        onClickAddItem = viewModel::onClickAddItem,
        onClickAddCollection = viewModel::onClickAddCollection,
        onClickDeleteItem = viewModel::onClickDeleteItem,
        onClickMoveItem = viewModel::onClickMoveItem,
        onClickMoveItemToGroup = viewModel::onClickMoveItemToGroup,
        onDismissMoveDialog = viewModel::onDismissMoveDialog,
        onItemReorderSettle = viewModel::onItemReorderSettle,
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
    onClickAddCollection: (Int) -> Unit = {},
    onClickDeleteItem: (OpdsFeedItemIndex) -> Unit = {},
    onClickMoveItem: (OpdsFeedItemIndex) -> Unit = { },
    onClickMoveItemToGroup: (MoveItemDestination) -> Unit = {},
    onDismissMoveDialog: () -> Unit = {},
    onItemReorderSettle: (from: OpdsFeedItemIndex, toIndex: Int) -> Unit = { _, _ -> },
    onDismissAddItemTypeBottomSheet: () -> Unit = {},
    onClickAddItemBrowse: () -> Unit = {},
    onClickAddItemUseLink: () -> Unit = {},
) {
    var collectionsExpanded by remember { mutableStateOf(true) }
    var learningItemsExpanded by remember { mutableStateOf(true) }

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

        OpenEelExpandCollapseListItem(
            expanded = collectionsExpanded,
            onExpandedChanged = { collectionsExpanded = it },
            headlineContent = {
                Text(stringResource(Res.string.collection_section_description))
            },
        )

        if (collectionsExpanded) {
            NavigationItemsReorderableColumn(
                modifier = Modifier.fillMaxWidth(),
                indentPadding = 0.dp,
                navItems = uiState.feed?.navigation.orEmpty(),
                groupIndex = OpdsFeedItemIndex.NO_GROUP,
                showMoveItemOption = uiState.canMoveNavigationItemToOtherGroup,
                onClickAdd = {
                    onClickAddCollection(OpdsFeedItemIndex.NO_GROUP)
                },
                onItemReorderSettle = onItemReorderSettle,
                onClickDeleteItem = { itemIndex ->
                    onClickDeleteItem(NavigationItemItemIndex(itemIndex))
                },
                onClickMoveItem = { index ->
                    onClickMoveItem(NavigationItemItemIndex(index))
                },
            )
        }

        OpenEelExpandCollapseListItem(
            expanded = learningItemsExpanded,
            onExpandedChanged = { learningItemsExpanded = it },
            headlineContent = {
                Text(stringResource(Res.string.links_to_learning_items))
            },
        )

        if (learningItemsExpanded) {
            OpdsFeedEditPublicationsReorderableColumn(
                indentPadding = 0.dp,
                pubItems = uiState.feed?.publications.orEmpty(),
                groupIndex = OpdsFeedItemIndex.NO_GROUP,
                onClickAddItem = {
                    onClickAddItem(OpdsFeedItemIndex.NO_GROUP)
                },
                onItemReorderSettle = onItemReorderSettle,
                showMoveItemOption = uiState.canMovePublicationItemToOtherGroup,
                onClickDeleteItem = { itemIndex ->
                    onClickDeleteItem(PublicationItemItemIndex(itemIndex))
                },
                onClickMoveItem = { index ->
                    onClickMoveItem(PublicationItemItemIndex(index))
                },
            )
        }

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
                        onClickAddPlaylist = { onClickAddCollection(groupIndex) },
                        onClickDeleteItem = { itemIndex ->
                            onClickDeleteItem(OpdsFeedGroupItemItemIndex(groupIndex, itemIndex))
                        },
                        onClickMoveItem = { itemIndex ->
                            onClickMoveItem(OpdsFeedGroupItemItemIndex(groupIndex, itemIndex))
                        },
                        onItemReorderSettle = onItemReorderSettle,
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

    uiState.movingItem?.also { movingItem ->
        OpdsFeedEditMoveToGroupDialog(
            destinations = uiState.movingItemDestinations.orEmpty(),
            topLevelDestTitle = if(uiState.feed?.indexIsInNavigationCollection(movingItem) == true) {
                Res.string.collection_section_description
            }else {
                Res.string.links_to_learning_items
            },
            onClickGroup = onClickMoveItemToGroup,
            onDismiss = onDismissMoveDialog,
        )
    }
}
