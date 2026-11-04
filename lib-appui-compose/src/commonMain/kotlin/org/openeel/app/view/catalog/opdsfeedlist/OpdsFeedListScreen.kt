package org.openeel.app.view.catalog.opdsfeedlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.defaultItemPadding
import org.openeel.datalayer.school.domain.MakePlaylistOpdsFeedUseCase
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.all
import org.openeel.shared.generated.resources.created_by
import org.openeel.shared.generated.resources.empty
import org.openeel.shared.generated.resources.my_collections
import org.openeel.shared.generated.resources.no_collections_yet
import org.openeel.shared.generated.resources.no_collections_yet_description
import org.openeel.shared.generated.resources.sections_and_items
import org.openeel.shared.viewmodel.catalog.opdsfeedlist.OpdsFeedListFilter
import org.openeel.shared.viewmodel.catalog.opdsfeedlist.OpdsFeedListUiState
import org.openeel.shared.viewmodel.catalog.opdsfeedlist.OpdsFeedListViewModel

@Composable
fun OpdsFeedListScreen(
    viewModel: OpdsFeedListViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    OpdsFeedListScreen(
        uiState = uiState,
        onClickFilter = viewModel::onClickFilter,
        onClickPlaylist = viewModel::onClickPlaylist,
    )
}

@Composable
fun OpdsFeedListScreen(
    uiState: OpdsFeedListUiState = OpdsFeedListUiState(),
    onClickFilter: (OpdsFeedListFilter) -> Unit = {},
    onClickPlaylist: (OpdsFeed) -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = uiState.activeFilter == OpdsFeedListFilter.ALL,
                    onClick = { onClickFilter(OpdsFeedListFilter.ALL) },
                    label = { Text(stringResource(Res.string.all)) },
                )
            }
            item {
                FilterChip(
                    selected = uiState.activeFilter == OpdsFeedListFilter.MY_PLAYLISTS,
                    onClick = { onClickFilter(OpdsFeedListFilter.MY_PLAYLISTS) },
                    label = { Text(stringResource(Res.string.my_collections)) },
                )
            }
        }

        if (uiState.showPlaylists.isEmpty()) {
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
                itemsIndexed(
                    items = uiState.showPlaylists,
                    key = { index, feed ->
                        feed.metadata.identifier?.toString()
                            ?: "${feed.metadata.title}_$index"
                    }
                ) { _, feed ->
                    PlaylistListItem(
                        feed = feed,
                        ownerUsername = if (feed.links.any {
                                it.rel?.contains(MakePlaylistOpdsFeedUseCase.REL_OWNER) == true
                                        && it.href == uiState.activeUserOwnerHref
                            }) uiState.activeUsername else null,
                        onClickFeed = { onClickPlaylist(feed) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistListItem(
    feed: OpdsFeed,
    ownerUsername: String?,
    onClickFeed: () -> Unit,
) {
    val sectionCount = feed.groups?.size ?: 0
    val itemCount = feed.groups?.sumOf { group ->
        (group.publications?.size ?: 0) +
                (group.navigation?.size ?: 0)
    } ?: 0

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickFeed() },
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Book,
                contentDescription = feed.metadata.title,
            )
        },
        headlineContent = {
            Text(feed.metadata.title)
        },
        supportingContent = {
            Column {
                Text(
                    stringResource(
                        Res.string.sections_and_items,
                        sectionCount,
                        itemCount,
                    )
                )
                if (ownerUsername != null) {
                    Text(
                        text = stringResource(Res.string.created_by, ownerUsername),
                    )
                }
            }
        },
    )
}