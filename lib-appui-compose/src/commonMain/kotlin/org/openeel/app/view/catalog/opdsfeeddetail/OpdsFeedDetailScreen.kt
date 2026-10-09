package world.respect.app.view.catalog.opdsfeeddetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.langMapString
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ext.OpdsFeedItemIndex
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.assign
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.copy
import world.respect.shared.generated.resources.copy_of
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.make_a_copy
import world.respect.shared.generated.resources.name
import world.respect.shared.generated.resources.permanently_delete
import world.respect.shared.generated.resources.permanently_delete_description
import world.respect.shared.generated.resources.select_count_items
import world.respect.shared.generated.resources.select_this_collection
import world.respect.shared.util.SortOrderOption
import world.respect.shared.viewmodel.catalog.opdsfeeddetail.OpdsFeedDetailUiState
import world.respect.shared.viewmodel.catalog.opdsfeeddetail.OpdsFeedDetailViewModel
import world.respect.shared.viewmodel.catalog.opdsfeeddetail.OpdsFeedDetailViewModel.Companion.ICON

@Composable
fun OpdsFeedDetailScreen(
    viewModel: OpdsFeedDetailViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val copyOfTemplate = stringResource(Res.string.copy_of)

    LaunchedEffect(uiState.showCopyDialog) {
        if (uiState.showCopyDialog) {
            viewModel.onCopyDialogNameChanged(copyOfTemplate.format(uiState.copyDialogName))
        }
    }

    OpdsFeedDetailScreen(
        uiState = uiState,
        onSortOrderChanged = viewModel::onSortOrderChanged,
        onClickPublication = viewModel::onClickPublication,
        onLongPressPublication = viewModel::onLongPressPublication,
        onClickNavigation = viewModel::onClickNavigation,
        onLongPressNavigation = viewModel::onLongPressNavigation,
        onClickConfirmSelection = viewModel::onClickSelectBottomButton,
        onClickSelectPlaylist = viewModel::onClickSelectThisFeed,
        onClickToggleGroup = viewModel::onClickToggleGroup,
        onClickShare = viewModel::onClickShare,
        onClickCopy = viewModel::onClickCopy,
        onClickDelete = viewModel::onClickDelete,
        onClickAssignSection = { /* TODO */ },
        onClickAssignQuickActionButton = viewModel::onClickAssignQuickActionButton,
        onCopyDialogDismiss = viewModel::onCopyDialogDismiss,
        onCopyDialogNameChanged = viewModel::onCopyDialogNameChanged,
        onCopyDialogConfirm = viewModel::onCopyDialogConfirm,
        onDeleteDialogDismiss = viewModel::onDeleteDialogDismiss,
        onDeleteDialogConfirm = viewModel::onDeleteDialogConfirm,
    )
}

@Composable
fun OpdsFeedDetailScreen(
    uiState: OpdsFeedDetailUiState = OpdsFeedDetailUiState(),
    @Suppress("unused")
    onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onClickPublication: (OpdsFeedItemIndex) -> Unit = {},
    onLongPressPublication: (OpdsFeedItemIndex) -> Unit = {},
    onClickNavigation: (OpdsFeedItemIndex) -> Unit = {},
    onLongPressNavigation: (OpdsFeedItemIndex) -> Unit = {},
    onClickConfirmSelection: () -> Unit = {},
    onClickSelectPlaylist: () -> Unit = {},
    onClickToggleGroup: (Int) -> Unit = {},
    onClickShare: () -> Unit = {},
    onClickCopy: () -> Unit = {},
    onClickDelete: () -> Unit = {},
    onClickAssignSection: (Int) -> Unit = {},
    onClickAssignQuickActionButton: () -> Unit = {},
    onCopyDialogDismiss: () -> Unit = {},
    onCopyDialogNameChanged: (String) -> Unit = {},
    onCopyDialogConfirm: () -> Unit = {},
    onDeleteDialogDismiss: () -> Unit = {},
    onDeleteDialogConfirm: () -> Unit = {},
) {
    val catalog = uiState.feed.dataOrNull()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = if (
                uiState.showSelectPlaylistButton ||
                (uiState.isMultiSelectMode && uiState.selectedCount > 0)
            ) {
                PaddingValues(bottom = 72.dp)
            } else {
                PaddingValues()
            },
        ) {

            /* Disabled for respect-update 1/Oct/26
            item(key = "feed_header") {
                OpdsFeedDetailHeader(
                    uiState = uiState,
                    onClickShare = onClickShare,
                    onClickCopy = onClickCopy,
                    onClickDelete = onClickDelete,
                    onClickAssign = onClickAssignQuickActionButton,
                )
            }
            */

            catalog?.navigation?.also { navigation ->
                itemsIndexed(
                    items = navigation,
                    key = { index, _ -> "top_nav_$index" }
                ) { index, navigationItem ->
                    val feedIndex = OpdsFeedItemIndex(groupIndex = -1, index)
                    NavigationListItem(
                        navigation = navigationItem,
                        showCheckbox = uiState.showNavigationCheckboxes,
                        isSelected = uiState.isNavigationSelected(feedIndex),
                        onClickNavigation = { onClickNavigation(feedIndex) },
                        onLongPress = {
                            onLongPressNavigation(feedIndex)
                        }
                    )
                }
            }

            catalog?.publications?.also { publications ->
                itemsIndexed(
                    items = publications,
                    key = { index, _ -> "top_pub_$index" }
                ) { index, publication ->
                    val feedItemIndex = OpdsFeedItemIndex(groupIndex = -1, index)
                    PublicationListItem(
                        publication = publication,
                        showCheckbox = uiState.showPublicationCheckboxes,
                        isSelected = uiState.isPublicationSelected(feedItemIndex),
                        onClickPublication = { onClickPublication(feedItemIndex) },
                        onLongPressPublication = { onLongPressPublication(feedItemIndex) },
                    )
                }
            }

            catalog?.groups?.forEachIndexed { groupIndex, group ->
                item(key = "section_$groupIndex") {
                    FeedSectionHeader(
                        title = group.metadata.title,
                        isCollapsed = uiState.isGroupCollapsed(groupIndex),
                        showAssignButton = group.publications?.isNotEmpty() == true,
                        onClickToggle = { onClickToggleGroup(groupIndex) },
                        onClickAssign = { onClickAssignSection(groupIndex) },
                    )
                }

                if (!uiState.isGroupCollapsed(groupIndex)) {
                    itemsIndexed(
                        items = group.navigation ?: emptyList(),
                        key = { itemIndex, _ -> "nav_${groupIndex}_$itemIndex" }
                    ) { itemIndex, navigation ->
                        val feedIndex = OpdsFeedItemIndex(groupIndex = groupIndex, index = itemIndex)
                        NavigationListItem(
                            navigation = navigation,
                            showCheckbox = uiState.showNavigationCheckboxes,
                            isSelected = uiState.isNavigationSelected(feedIndex),
                            onClickNavigation = { onClickNavigation(feedIndex) },
                            onLongPress = { onLongPressNavigation(feedIndex) },
                        )
                    }

                    itemsIndexed(
                        items = group.publications ?: emptyList(),
                        key = { itemIndex, _ -> "pub_${groupIndex}_$itemIndex" }
                    ) { itemIndex, publication ->
                        val feedItemIndex = OpdsFeedItemIndex(groupIndex = groupIndex, index = itemIndex)
                        PublicationListItem(
                            publication = publication,
                            showCheckbox = uiState.showPublicationCheckboxes,
                            isSelected = uiState.isPublicationSelected(feedItemIndex),
                            onClickPublication = { onClickPublication(feedItemIndex) },
                            onLongPressPublication = { onLongPressPublication(feedItemIndex) },
                        )
                    }
                }
            }
        }

        if (uiState.showSelectionBottomButton) {
            Button(
                onClick = onClickConfirmSelection,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .defaultItemPadding()
                    .testTag("confirm_selection_button"),
            ) {
                Text(
                    text = stringResource(
                        Res.string.select_count_items,
                        uiState.selectedCount,
                    ),
                )
            }
        }

        if (uiState.showSelectPlaylistButton) {
            Button(
                onClick = onClickSelectPlaylist,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .defaultItemPadding()
                    .testTag("select_playlist_button"),
            ) {
                Text(text = stringResource(Res.string.select_this_collection))
            }
        }
    }



    if (uiState.showCopyDialog) {
        CopyFeedDialog(
            name = uiState.copyDialogName,
            onNameChanged = onCopyDialogNameChanged,
            onDismiss = onCopyDialogDismiss,
            onConfirm = onCopyDialogConfirm,
        )
    }

    if (uiState.showDeleteDialog) {
        DeleteFeedDialog(
            onDismiss = onDeleteDialogDismiss,
            onConfirm = onDeleteDialogConfirm,
        )
    }
}


@Composable
private fun FeedSectionHeader(
    title: String,
    isCollapsed: Boolean,
    showAssignButton: Boolean,
    onClickToggle: () -> Unit,
    onClickAssign: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showAssignButton) {
                    IconButton(
                        onClick = onClickAssign,
                        modifier = Modifier.testTag("assign_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Task,
                            contentDescription = stringResource(Res.string.assign),
                        )
                    }
                }
                IconButton(
                    onClick = onClickToggle,
                    modifier = Modifier.testTag("expand_collapse_icon"),
                ) {
                    Icon(
                        imageVector = if (isCollapsed) {
                            Icons.Filled.ExpandMore
                        } else {
                            Icons.Filled.ExpandLess
                        },
                        contentDescription = null,
                    )
                }
            }
        },
    )
}

@Composable
private fun CopyFeedDialog(
    name: String,
    onNameChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(Res.string.make_a_copy)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChanged,
                label = { Text(stringResource(Res.string.name)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("copy_dialog_name_field"),
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("copy_dialog_confirm"),
            ) {
                Text(text = stringResource(Res.string.copy))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("copy_dialog_dismiss"),
            ) {
                Text(text = stringResource(Res.string.cancel))
            }
        },
    )
}

@Composable
private fun DeleteFeedDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(imageVector = Icons.Filled.Delete, contentDescription = null) },
        title = { Text(text = stringResource(Res.string.permanently_delete)) },
        text = { Text(text = stringResource(Res.string.permanently_delete_description)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("delete_dialog_confirm"),
            ) {
                Text(text = stringResource(Res.string.delete))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("delete_dialog_dismiss"),
            ) {
                Text(text = stringResource(Res.string.cancel))
            }
        },
    )
}



@Composable
fun NavigationListItem(
    navigation: ReadiumLink,
    showCheckbox: Boolean = false,
    isSelected: Boolean = false,
    onClickNavigation: (ReadiumLink) -> Unit,
    onLongPress: () -> Unit,
) {
    FeedListItem(
        title = navigation.title
            ?.takeIf { it != "null" && it.isNotBlank() }
            ?: navigation.href,
        iconUrl = navigation.alternate?.find {
            it.rel?.contains(ICON) == true
        }?.href,
        description = null,
        language = navigation.language,
        duration = navigation.duration,
        showCheckbox = showCheckbox,
        isSelected = isSelected,
        onClick = { onClickNavigation(navigation) },
        onLongPress = onLongPress,
    )
}

@Composable
fun PublicationListItem(
    publication: Publication,
    showCheckbox: Boolean,
    isSelected: Boolean,
    onClickPublication: (Publication) -> Unit,
    onLongPressPublication: (Publication) -> Unit,
) {
    FeedListItem(
        title = langMapString(publication.metadata.title),
        iconUrl = publication.images?.firstOrNull()?.href,
        language = publication.metadata.language,
        duration = publication.metadata.duration,
        description = publication.metadata.description,
        showCheckbox = showCheckbox,
        isSelected = isSelected,
        onClick = { onClickPublication(publication) },
        onLongPress = { onLongPressPublication(publication) },
    )
}