package org.openeel.app.view.catalog.opdsfeedlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.view.catalog.opdsfeeddetail.NavigationListItem
import org.openeel.datalayer.school.domain.MakePlaylistOpdsFeedUseCase
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.lib.xapi.ext.objectActivityOrNull
import org.openeel.lib.xapi.ext.opdsCollectionLinkOrNull
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.created_by
import org.openeel.shared.generated.resources.empty
import org.openeel.shared.generated.resources.more_options
import org.openeel.shared.generated.resources.no_collections_yet
import org.openeel.shared.generated.resources.no_collections_yet_description
import org.openeel.shared.generated.resources.sections_and_items
import org.openeel.shared.generated.resources.unpin
import org.openeel.shared.viewmodel.catalog.opdsfeedlist.OpdsFeedListUiState
import org.openeel.shared.viewmodel.catalog.opdsfeedlist.OpdsFeedListViewModel

@Composable
fun OpdsFeedListScreen(
    viewModel: OpdsFeedListViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    OpdsFeedListScreen(
        uiState = uiState,
        onClickCollection = viewModel::onClickCollection,
        onClickUnpinCollection = viewModel::onClickUnpinCollection,
    )
}

@Composable
fun OpdsFeedListScreen(
    uiState: OpdsFeedListUiState = OpdsFeedListUiState(),
    onClickCollection: (XapiStatement) -> Unit = {},
    onClickUnpinCollection: (XapiStatement) -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.statements.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(Res.drawable.empty),
                    contentDescription = stringResource(Res.string.no_collections_yet),
                    modifier = Modifier.size(200.dp),
                    contentScale = ContentScale.Fit,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(Res.string.no_collections_yet))
                Text(stringResource(Res.string.no_collections_yet_description))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    items = uiState.statements,
                    key = { statement -> statement.id ?: error("OpdsFeedListScreen: statement id is null")  }
                ) { statement ->
                    val feedFlow = remember(statement.id) {
                        uiState.feedFlowForStatement(statement)
                    }
                    val feed = feedFlow.collectAsState(DataLoadingState()).value.dataOrNull()
                    val activityDefinition = statement.objectActivityOrNull()?.definition

                    NavigationListItem(
                        navigation = ReadiumLink(
                            href = activityDefinition?.opdsCollectionLinkOrNull().orEmpty(),
                            type = OpdsFeed.MEDIA_TYPE,
                            title = activityDefinition?.name?.entries?.firstOrNull()?.value,
                        ),
                        description = feed?.let {
                            val isCreatedByActiveUser = feed.links.any { link ->
                                link.rel?.contains(MakePlaylistOpdsFeedUseCase.REL_OWNER) == true &&
                                        link.href == uiState.activeUserOwnerHref
                            }

                            listOfNotNull(
                                stringResource(
                                    Res.string.sections_and_items,
                                    feed.groups?.size ?: 0,
                                    feed.groups?.sumOf { group ->
                                        (group.publications?.size ?: 0) + (group.navigation?.size ?: 0)
                                    } ?: 0,
                                ),
                                if (isCreatedByActiveUser) {
                                    stringResource(Res.string.created_by, uiState.activeUsername)
                                } else {
                                    null
                                },
                            ).joinToString("\n")
                        },
                        onClickNavigation = { onClickCollection(statement) },
                        onLongPress = { onClickCollection(statement) },
                        trailingContent = {
                            var menuExpanded by remember { mutableStateOf(false) }

                            Box {
                                IconButton(
                                    onClick = { menuExpanded = true },
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = stringResource(Res.string.more_options),
                                    )
                                }

                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.unpin)) },
                                        onClick = {
                                            menuExpanded = false
                                            onClickUnpinCollection(statement)
                                        },
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}
