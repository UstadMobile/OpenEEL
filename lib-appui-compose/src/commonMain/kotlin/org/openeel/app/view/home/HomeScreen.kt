package org.openeel.app.view.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.view.apps.launcher.AppLauncherScreen
import org.openeel.app.view.catalog.bookmark.BookmarkListScreen
import org.openeel.app.view.catalog.opdsfeedlist.OpdsFeedListScreen
import org.openeel.app.viewmodel.openEelViewModel
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.apps
import org.openeel.shared.generated.resources.bookmarks
import org.openeel.shared.generated.resources.collections
import org.openeel.shared.navigation.OpenEelComposeNavController
import org.openeel.shared.viewmodel.app.appstate.AppUiState

enum class HomeScreenTabs(val label: StringResource) {
    APPS(Res.string.apps),
    BOOKMARK(Res.string.bookmarks),
    COLLECTIONS(Res.string.collections)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    respectNavController: OpenEelComposeNavController,
    onSetAppUiState: (AppUiState) -> Unit,
) {
    val pagerState = rememberPagerState { HomeScreenTabs.entries.size }
    val scope = rememberCoroutineScope()
    val selectedTab = HomeScreenTabs.entries[pagerState.currentPage]

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if(HomeScreenTabs.entries.size > 1) {
            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
            ) {
                HomeScreenTabs.entries.forEach { tab ->
                    Tab(
                        selected = pagerState.currentPage == tab.ordinal,
                        onClick = {
                            scope.launch {
                                pagerState.scrollToPage(tab.ordinal)
                            }
                        },
                        text = {
                            Text(stringResource(tab.label))
                        }
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState
        ) {
            when(selectedTab) {
                HomeScreenTabs.APPS -> {
                    AppLauncherScreen(
                        viewModel = openEelViewModel(
                            onSetAppUiState = onSetAppUiState,
                            navController = respectNavController,
                        )
                    )
                }

                HomeScreenTabs.BOOKMARK -> {
                    BookmarkListScreen(
                        viewModel = openEelViewModel(
                            onSetAppUiState = onSetAppUiState,
                            navController = respectNavController,
                        )
                    )
                }

                HomeScreenTabs.COLLECTIONS -> {
                    OpdsFeedListScreen(
                        viewModel = openEelViewModel(
                            onSetAppUiState = onSetAppUiState,
                            navController = respectNavController
                        )
                    )
                }
            }
        }
    }
}