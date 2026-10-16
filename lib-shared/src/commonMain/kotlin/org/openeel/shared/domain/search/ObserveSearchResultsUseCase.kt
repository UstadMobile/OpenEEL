package org.openeel.shared.domain.search

import androidx.sqlite.SQLiteException
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.something_went_wrong
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.app.appstate.AppUiState
import org.openeel.shared.viewmodel.app.appstate.Snack
import org.openeel.shared.viewmodel.app.appstate.SnackBarDispatcher

/**
 * Runs a local datasource search for the text entered in the appbar search box, keeping only the
 * results of the most recent query.
 */
class ObserveSearchResultsUseCase(
    private val snackBarDispatcher: SnackBarDispatcher,
) {

    /**
     * @param appUiStateFlow the ViewModel appbar state, used as the source of the search text.
     * @param searchFn runs the datasource search for a non-empty query.
     * @return flow of search results, where null means that there is no active search and the
     *         caller should show unfiltered content.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun <T> invoke(
        appUiStateFlow: StateFlow<AppUiState>,
        searchFn: (String) -> Flow<T>,
    ): Flow<T?> {
        fun isCurrentQuery(searchQuery: String) =
            searchQuery == appUiStateFlow.value.searchState.searchText

        return appUiStateFlow.map { it.searchState.searchText }
            .distinctUntilChanged()
            .flatMapLatest { searchQuery ->
                val resultFlow = if (searchQuery.isEmpty()) {
                    flowOf<T?>(null)
                } else {
                    searchFn(searchQuery).catch { error ->
                        if (error !is SQLiteException)
                            throw error

                        // Show the error only if this query is still current.
                        Napier.e(ERROR_MESSAGE, throwable = error)
                        if (isCurrentQuery(searchQuery)) {
                            snackBarDispatcher.showSnackBar(
                                Snack(Res.string.something_went_wrong.asUiText())
                            )
                        }
                    }
                }

                // Ignore results already superseded by a newer query.
                resultFlow.filter { isCurrentQuery(searchQuery) }
            }
    }

    companion object {

        private const val ERROR_MESSAGE = "Error searching cached titles"

    }
}
