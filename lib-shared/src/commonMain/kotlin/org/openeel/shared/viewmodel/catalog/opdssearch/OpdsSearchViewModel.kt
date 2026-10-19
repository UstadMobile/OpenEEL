package org.openeel.shared.viewmodel.catalog.opdssearch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.SchoolDataSourceLocal
import org.openeel.datalayer.school.SchoolConfigSettingDataSource
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.datalayer.school.model.composites.OpdsSearchFilterOptions
import org.openeel.datalayer.school.model.composites.OpdsSearchResult
import org.openeel.datalayer.school.opds.OpdsFeedDataSourceLocal
import org.openeel.datalayer.shared.paging.EmptyPagingSourceFactory
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.paging.PagingSourceFactoryHolder
import org.openeel.libutil.ext.resolve
import org.openeel.libutil.ext.toggle
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.contentfilter.ContentFilterOption
import org.openeel.shared.domain.contentfilter.ContentFilterType
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.not_found
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.OpdsFeedDetail
import org.openeel.shared.navigation.PublicationDetail
import org.openeel.shared.util.LaunchDebouncer
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.AppBarSearchUiState
import org.openeel.shared.viewmodel.app.appstate.Snack
import org.openeel.shared.viewmodel.app.appstate.SnackBarDispatcher

/**
 * The search text itself is held in the appbar search state (AppUiState.searchState).
 *
 * @property filterOptions the languages and subjects available to filter by.
 * @property selectedFilters the filter options selected by the user.
 * @property results search results; empty until the user enters text or selects a filter.
 */
data class OpdsSearchUiState(
    val filterOptions: OpdsSearchFilterOptions = OpdsSearchFilterOptions(),
    val selectedFilters: Set<ContentFilterOption> = emptySet(),
    val results: IPagingSourceFactory<Int, OpdsSearchResult> = EmptyPagingSourceFactory(),
) {
}

/**
 * Search all available (cached) OPDS publications and feeds. Searching and filtering is done by
 * the local database.
 */
class OpdsSearchViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AppAccountManager,
    private val snackBarDispatcher: SnackBarDispatcher,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSourceLocal: SchoolDataSourceLocal by inject()

    private val schoolDataSource: SchoolDataSource by inject()

    /**
     * App catalog feeds: apps are not lessons, so these are excluded from search.
     */
    private val appCatalogUrls = MutableStateFlow<List<Url>>(emptyList())

    private val _uiState = MutableStateFlow(OpdsSearchUiState())

    val uiState = _uiState.asStateFlow()

    private val launchDebounced = LaunchDebouncer(viewModelScope)

    private val pagingSourceFactoryHolder = PagingSourceFactoryHolder {
        val searchText = _appUiState.value.searchState.searchText
        val state = _uiState.value
        if (searchText.isNotBlank() || state.selectedFilters.isNotEmpty()) {
            schoolDataSourceLocal.opdsFeedDataSource.searchAsPagingSource(
                state.toSearchParams(searchText)
            )
        } else {
            EmptyPagingSourceFactory()
        }
    }

    init {
        _appUiState.update {
            it.copy(
                showBackButton = true,
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                searchState = AppBarSearchUiState(
                    visible = true,
                    expandedByDefault = true,
                    searchText = "",
                    onSearchTextChanged = ::onSearchTextChanged,
                ),
            )
        }

        _uiState.update { it.copy(results = pagingSourceFactoryHolder) }

        viewModelScope.launch {
            schoolDataSource.schoolConfigSettingDataSource.listAsFlow(
                loadParams = DataLoadParams(),
                params = SchoolConfigSettingDataSource.GetListParams(
                    key = SchoolConfigSettingDataSource.KEY_APP_CATALOGS
                )
            ).collect { configs ->
                val urls = configs.dataOrNull()?.mapNotNull { setting ->
                    runCatching { Url(setting.value) }.getOrNull()
                } ?: return@collect
                if (urls != appCatalogUrls.value) {
                    appCatalogUrls.value = urls
                    pagingSourceFactoryHolder.invalidate()
                }
            }
        }

        viewModelScope.launch {
            appCatalogUrls.collectLatest { urls ->
                schoolDataSourceLocal.opdsFeedDataSource.getSearchFilterOptionsAsFlow(urls)
                    .collect { filterOptions ->
                        _uiState.update { it.copy(filterOptions = filterOptions) }
                    }
            }
        }
    }

    private fun OpdsSearchUiState.toSearchParams(
        searchText: String,
    ): OpdsFeedDataSourceLocal.SearchParams {
        val selectedTypes = selectedFilters.filterIsInstance<ContentFilterOption.Type>()
            .map { it.type }

        return OpdsFeedDataSourceLocal.SearchParams(
            searchText = searchText,
            languages = selectedFilters.filterIsInstance<ContentFilterOption.Language>()
                .map { it.langCode },
            subjects = selectedFilters.filterIsInstance<ContentFilterOption.Subject>()
                .map { it.subject },
            types = selectedTypes.mapNotNull { it.schemaOrgId },
            includePublications = selectedTypes.isEmpty() ||
                selectedTypes.any { it != ContentFilterType.COLLECTION },
            includeNavigation = selectedTypes.isEmpty() ||
                ContentFilterType.COLLECTION in selectedTypes,
            appCatalogUrls = appCatalogUrls.value,
        )
    }

    fun onSearchTextChanged(text: String) {
        _appUiState.update {
            it.copy(searchState = it.searchState.copy(searchText = text))
        }
        launchDebounced.launch(DEBOUNCE_KEY_SEARCH) {
            pagingSourceFactoryHolder.invalidate()
        }
    }

    fun onToggleFilterOption(option: ContentFilterOption) {
        _uiState.update { it.copy(selectedFilters = it.selectedFilters.toggle(option)) }
        pagingSourceFactoryHolder.invalidate()
    }

    fun onClearFilters() {
        _uiState.update { it.copy(selectedFilters = emptySet()) }
        pagingSourceFactoryHolder.invalidate()
    }

    fun onClickResult(result: OpdsSearchResult) {
        val feedUrl = result.feedUrl?.let { Url(it) }
        val resultUrl = result.url?.let { Url(it) }
            ?: result.href?.let { href -> feedUrl?.resolve(href) ?: Url(href) }

        if (resultUrl == null) {
            snackBarDispatcher.showSnackBar(Snack(Res.string.not_found.asUiText()))
            return
        }

        val destination = if (result.isPublication) {
            PublicationDetail.create(
                learningUnitManifestUrl = resultUrl,
                refererUrl = feedUrl,
                expectedIdentifier = result.identifier,
            )
        } else {
            OpdsFeedDetail.create(opdsFeedUrl = resultUrl)
        }

        _navCommandFlow.tryEmit(NavCommand.Navigate(destination))
    }

    companion object {

        private const val DEBOUNCE_KEY_SEARCH = "search"

    }

}
