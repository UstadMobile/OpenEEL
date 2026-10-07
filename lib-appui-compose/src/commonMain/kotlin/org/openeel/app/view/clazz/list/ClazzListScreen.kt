package org.openeel.app.view.clazz.list

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
import org.openeel.app.components.OpenEelEmptyListComponent
import org.openeel.app.components.OpenEelListSortHeader
import org.openeel.app.components.OpenEelPersonAvatar
import org.openeel.app.components.SortListMode
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.components.defaultSortListMode
import org.openeel.app.components.openEelPagingItems
import org.openeel.app.components.openEelRememberPager
import org.openeel.datalayer.school.ClassDataSource
import org.openeel.datalayer.school.model.Clazz
import org.openeel.shared.util.SortOrderOption
import org.openeel.shared.viewmodel.clazz.list.ClazzListUiState
import org.openeel.shared.viewmodel.clazz.list.ClazzListViewModel

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

    val pager = openEelRememberPager(uiState.classes)

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(modifier = Modifier.fillMaxSize()) {

        item("header") {
            OpenEelListSortHeader(
                modifier = Modifier.defaultItemPadding(),
                activeSortOrderOption = uiState.activeSortOrderOption,
                sortOptions = uiState.sortOptions,
                enabled = uiState.fieldsEnabled,
                onClickSortOption = onClickSortOption,
                mode = sortListMode,
            )
        }

        openEelPagingItems(
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
                    OpenEelPersonAvatar(name = clazz?.title ?: "")
                },

                headlineContent = {
                    Text(text = clazz?.title ?: "")
                }
            )
        }

        if(lazyPagingItems.itemCount == 0) {
            item("empty_item") {
                OpenEelEmptyListComponent(Modifier.fillMaxWidth().defaultItemPadding())
            }
        }

    }
}