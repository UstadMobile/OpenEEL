package world.respect.app.view.clazz.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import world.respect.app.components.RespectEmptyListComponent
import world.respect.app.components.RespectListSortHeader
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.SortListMode
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.defaultSortListMode
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.model.Clazz
import world.respect.shared.util.SortOrderOption
import world.respect.shared.viewmodel.clazz.list.ClazzListUiState
import world.respect.shared.viewmodel.clazz.list.ClazzListViewModel

@Composable
fun ClazzListScreen(
    viewModel: ClazzListViewModel
) {

    val uiState by viewModel.uiState.collectAsState()

    ClazzListScreen(
        uiState = uiState,
        onClickClazz = viewModel::onClickClazz,
        onClickSortOption = viewModel::onSortOrderChanged,
    )
}

@Composable
fun ClazzListScreen(
    uiState: ClazzListUiState,
    onClickClazz: (Clazz) -> Unit,
    onClickSortOption: (SortOrderOption) -> Unit = { },
    sortListMode: SortListMode = defaultSortListMode(),
) {

    val pager = respectRememberPager(uiState.classes)

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(modifier = Modifier.fillMaxSize()) {

        item("header") {
            RespectListSortHeader(
                modifier = Modifier.defaultItemPadding(),
                activeSortOrderOption = uiState.activeSortOrderOption,
                sortOptions = uiState.sortOptions,
                enabled = uiState.fieldsEnabled,
                onClickSortOption = onClickSortOption,
                mode = sortListMode,
            )
        }

        respectPagingItems(
            items = lazyPagingItems,
            key = { item, index -> item?.guid ?: index.toString() },
            contentType = { ClassDataSource.ENDPOINT_NAME },
        ) { clazz ->
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        clazz?.also(onClickClazz)
                    },

                leadingContent = {
                    RespectPersonAvatar(name = clazz?.title ?: "")
                },

                headlineContent = {
                    Text(text = clazz?.title ?: "")
                }
            )
        }

        if(lazyPagingItems.itemCount == 0) {
            item("empty_item") {
                RespectEmptyListComponent(Modifier.fillMaxWidth().defaultItemPadding())
            }
        }

    }
}