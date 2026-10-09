package world.respect.app.view.catalog.bookmark

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectListSortHeader
import world.respect.app.view.catalog.opdsfeeddetail.PublicationListItem
import io.ktor.http.Url
import kotlinx.coroutines.flow.flowOf
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.ext.objectActivityOrNull
import world.respect.lib.xapi.model.XapiStatement
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.bookmark
import world.respect.shared.generated.resources.msg_see_bookmark
import world.respect.shared.generated.resources.no_bookmark
import world.respect.shared.util.SortOrderOption
import world.respect.shared.viewmodel.catalog.bookmark.BookmarkListUiState
import world.respect.shared.viewmodel.catalog.bookmark.BookmarkListViewModel

@Composable
fun BookmarkListScreen(
    viewModel: BookmarkListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    BookmarkListScreen(
        uiState = uiState,
        onClickRemoveBookmark = viewModel::onClickRemoveBookmark,
        onClickBookmark = viewModel::onClickBookmark,
        onClickSortOption = viewModel::onSortOrderChanged,
    )
}

@Composable
fun BookmarkListScreen(
    uiState: BookmarkListUiState,
    onClickRemoveBookmark: (XapiStatement) -> Unit,
    onClickBookmark: (XapiStatement) -> Unit,
    onClickSortOption: (SortOrderOption) -> Unit = { },
) {

    when {
        uiState.statements.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(Res.drawable.no_bookmark),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(200.dp)
                    )
                    Text(
                        text = stringResource(Res.string.no_bookmark),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(Res.string.msg_see_bookmark),
                        modifier = Modifier.padding(bottom = 64.dp)
                    )
                }
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item("sort_header") {
                    RespectListSortHeader(
                        activeSortOrderOption = uiState.activeSortOrderOption,
                        sortOptions = uiState.sortOptions,
                        onClickSortOption = onClickSortOption,
                    )
                }

                items(
                    uiState.statements,
                    key = { it.id ?: error("BookmarkListScreen: statement id is null") }
                ) { statement ->
                    val activityId = statement.objectActivityOrNull()?.id
                    val publicationFlow = remember(activityId) {
                        if(activityId != null) {
                            uiState.taskInfoFlow(Url(activityId))
                        } else {
                            flowOf(DataLoadingState())
                        }
                    }

                    val publicationLoadState by publicationFlow.collectAsState(DataLoadingState())
                    val publication = publicationLoadState.dataOrNull()

                    if (publication != null) {
                        PublicationListItem(
                            publication = publication,
                            onClickPublication = { onClickBookmark(statement) },
                            trailingContent = {
                                Icon(
                                    modifier = Modifier.clickable {
                                        onClickRemoveBookmark(statement)
                                    },
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = stringResource(Res.string.bookmark),
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
