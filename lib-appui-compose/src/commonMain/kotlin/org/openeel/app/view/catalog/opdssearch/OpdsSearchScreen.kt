package org.openeel.app.view.catalog.opdssearch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import io.ktor.http.Url
import org.openeel.app.view.catalog.opdsfeeddetail.FeedListItem
import org.openeel.app.components.OpenEelContentFilterRow
import org.openeel.app.components.contentFilterDefinitions
import org.openeel.app.components.openEelPagingItems
import org.openeel.app.components.openEelRememberPager
import org.openeel.datalayer.school.model.composites.OpdsSearchResult
import org.openeel.shared.domain.contentfilter.ContentFilterOption
import org.openeel.libutil.ext.resolve
import org.openeel.shared.viewmodel.catalog.opdssearch.OpdsSearchUiState
import org.openeel.shared.viewmodel.catalog.opdssearch.OpdsSearchViewModel

@Composable
fun OpdsSearchScreen(
    viewModel: OpdsSearchViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    OpdsSearchScreen(
        uiState = uiState,
        onToggleFilterOption = viewModel::onToggleFilterOption,
        onClearFilters = viewModel::onClearFilters,
        onClickResult = viewModel::onClickResult,
    )
}

@Composable
fun OpdsSearchScreen(
    uiState: OpdsSearchUiState,
    onToggleFilterOption: (ContentFilterOption) -> Unit = { },
    onClearFilters: () -> Unit = { },
    onClickResult: (OpdsSearchResult) -> Unit = { },
) {
    val pager = openEelRememberPager(uiState.results)
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    Column(modifier = Modifier.fillMaxSize()) {
        OpenEelContentFilterRow(
            filters = contentFilterDefinitions(options = uiState.filterOptions),
            isOptionSelected = { it in uiState.selectedFilters },
            onToggleOption = onToggleFilterOption,
            clearAllEnabled = uiState.selectedFilters.isNotEmpty(),
            onClickClearAll = onClearFilters,
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            openEelPagingItems(
                items = lazyPagingItems,
                key = { _, index -> index },
            ) { result ->
                val iconUrl = remember(result) {
                    result?.iconHref?.let { iconHref ->
                        (result.url ?: result.feedUrl)?.let { Url(it).resolve(iconHref).toString() }
                    }
                }

                FeedListItem(
                    title = result?.title ?: "",
                    iconUrl = iconUrl,
                    description = null,
                    language = null,
                    duration = null,
                    showCheckbox = false,
                    isSelected = false,
                    onClick = { result?.also(onClickResult) },
                    onLongPress = { },
                )
            }
        }
    }
}
