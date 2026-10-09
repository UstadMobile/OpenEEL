package org.openeel.shared.viewmodel.catalog.bookmark

import CommonSortOptions
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.dataloadstate.ext.map
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.lib.xapi.ext.objectActivityNameOrNull
import org.openeel.lib.xapi.ext.objectActivityOrNull
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.model.XapiVerb
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.domain.bookmark.RemoveBookmarkUseCase
import org.openeel.shared.ext.resultExpected
import org.openeel.shared.ext.tryOrShowSnackbarOnError
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.home
import org.openeel.shared.generated.resources.remove_bookmark
import org.openeel.shared.generated.resources.something_went_wrong
import org.openeel.shared.navigation.BookmarkList
import org.openeel.shared.navigation.PublicationDetail
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.NavResultReturner
import org.openeel.shared.navigation.sendResultIfResultExpected
import org.openeel.shared.util.SortOrderOption
import org.openeel.shared.util.ext.appbarTitleString
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.util.ext.resolve
import org.openeel.shared.viewmodel.RespectViewModel
import org.openeel.shared.viewmodel.app.appstate.Snack
import org.openeel.shared.viewmodel.app.appstate.SnackBarDispatcher
import org.openeel.shared.viewmodel.catalog.OpdsPickType
import org.openeel.shared.viewmodel.catalog.PublicationsSelection

data class BookmarkListUiState(
    val statements: List<XapiStatement> = emptyList(),
    val taskInfoFlow: (Url) -> Flow<DataLoadState<Publication>> = {
        flowOf(DataLoadingState())
    },
    val activeSortOrderOption: SortOrderOption = CommonSortOptions.DEFAULT,
    val sortOptions: List<SortOrderOption> = CommonSortOptions.ALL_OPTIONS,
)

class BookmarkListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val snackBarDispatcher: SnackBarDispatcher,
    private val resultReturner: NavResultReturner,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    private val _uiState = MutableStateFlow(BookmarkListUiState())

    val uiState = _uiState.asStateFlow()

    private val activeSortOption = _uiState.map { it.activeSortOrderOption }.distinctUntilChanged()

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val removeBookmarkUseCase: RemoveBookmarkUseCase by inject()

    val route: BookmarkList = savedStateHandle.toRoute()

    init {
        _appUiState.update {
            it.copy(
                title = route.opdsPickType?.appbarTitleString?.asUiText() ?: Res.string.home.asUiText(),
                hideBottomNavigation = route.resultDest != null,
            )
        }

        _uiState.update {
            it.copy(
                taskInfoFlow = ::taskInfoFlowFor
            )
        }

        viewModelScope.launch {
            schoolDataSource.xapiResource.statements.getAsFlow(
                listParams = XapiStatementsResource.GetStatementParams(
                    agent = accountManager.selectedAccountAndPersonFlow.filterNotNull().first()
                        .xapiAgent,
                    verb = XapiVerb.ID_BOOKMARKED,
                    activity = OpenEelXapiConstants.CATEGORY_BOOKMARK_RECIPE,
                    relatedActivities = true,
                ),
                dataLoadParams = DataLoadParams(),
            ).combine(activeSortOption) { statements, sortOrderOption ->
                Pair(statements, sortOrderOption)
            }.collect { (statements, sortOrderOption) ->
                val stmtList = statements.dataOrNull()?.statements ?: emptyList()

                _uiState.update { prev ->
                    prev.copy(
                        statements = when(sortOrderOption.flag) {
                            CommonSortOptions.FLAG_TIME_ASC -> stmtList.sortedBy { it.timestamp }
                            CommonSortOptions.FLAG_TIME_DESC -> stmtList.sortedByDescending { it.timestamp }
                            CommonSortOptions.FLAG_TITLE_ASC -> stmtList.sortedBy {
                                it.objectActivityNameOrNull()?.entries?.firstOrNull()?.value
                            }
                            CommonSortOptions.FLAG_TITLE_DESC -> stmtList.sortedByDescending {
                                it.objectActivityNameOrNull()?.entries?.firstOrNull()?.value
                            }
                            else -> stmtList
                        }
                    )
                }
            }
        }
    }

    fun onSortOrderChanged(sortOrderOption: SortOrderOption) {
        _uiState.update {
            it.copy(activeSortOrderOption = sortOrderOption,)
        }
    }

    fun taskInfoFlowFor(url: Url): Flow<DataLoadState<Publication>> {
        return schoolDataSource.opdsPublicationDataSource.getByUrlAsFlow(
            url = url, params = DataLoadParams(), null, null
        ).map { dataLoadState ->
            dataLoadState.map { it.resolve(url) }
        }
    }

    fun onClickRemoveBookmark(statement: XapiStatement) {
        viewModelScope.launch {
            snackBarDispatcher.tryOrShowSnackbarOnError(
                logMessage = "BookmarkListViewModel: error removing bookmark"
            ) {
                removeBookmarkUseCase(statements = listOf(statement))

                _uiState.update {
                    it.copy(
                        statements = it.statements.filterNot { s -> s.id == statement.id }
                    )
                }

                snackBarDispatcher.showSnackBar(
                    Snack(message = Res.string.remove_bookmark.asUiText())
                )
            }
        }
    }

    fun onClickBookmark(statement: XapiStatement) {
        val activityId = statement.objectActivityOrNull()?.id

        if (activityId == null) {
            Napier.w("Cannot navigate to bookmark: statement object is not an Activity")
            snackBarDispatcher.showSnackBar(
                Snack(message = Res.string.something_went_wrong.asUiText())
            )
            return
        }

        when {
            /**
             * User is picking a publication - return an OpdsPublicationsSelection result
             */
            route.resultExpected && route.opdsPickType == OpdsPickType.PUBLICATION -> {
                viewModelScope.launch {
                    val manifestUrl = Url(activityId)
                    val opdsPub = schoolDataSource.opdsPublicationDataSource.getByUrl(
                        url = manifestUrl,
                        params = DataLoadParams()
                    ).dataOrNull()

                    if(opdsPub != null) {
                        resultReturner.sendResultIfResultExpected(
                            route = route,
                            navCommandFlow = _navCommandFlow,
                            result = PublicationsSelection(
                                url = manifestUrl,
                                selectedPublications = listOf(opdsPub),
                            )
                        )
                    }else {
                        snackBarDispatcher.showSnackBar(
                            Snack(message = Res.string.something_went_wrong.asUiText())
                        )
                    }
                }
            }

            /**
             * User is picking a collection, do nothing, not supoprted at present
             */
            route.resultExpected -> {
                //do nothing - pick type expected is catalog feed
            }

            else -> {
                _navCommandFlow.tryEmit(
                    value = NavCommand.Navigate(
                        PublicationDetail.create(
                            learningUnitManifestUrl = Url(activityId)
                        )
                    )
                )
            }
        }

    }
}
