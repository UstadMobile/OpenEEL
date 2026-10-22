package org.openeel.shared.viewmodel.catalog.opdssearch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.lib.xapi.ext.objectActivityOrNull
import org.openeel.lib.xapi.model.XapiVerb
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.datalayer.school.model.composites.OpdsSearchFilterOptions
import org.openeel.datalayer.school.model.composites.OpdsSearchResult
import org.openeel.datalayer.school.opds.OpdsFeedDataSourceLocal
import org.openeel.lib.opds.model.LangMapStringValue
import org.openeel.libutil.ext.resolve
import org.openeel.libutil.ext.toggle
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.contentfilter.ContentFilterOption
import org.openeel.shared.domain.contentfilter.ContentFilterType
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.not_found
import org.openeel.shared.navigation.AppsDetail
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
 * @property languageOptionSearchText text entered to search the language filter options.
 * @property subjectOptionSearchText text entered to search the subject filter options.
 * @property showLanguageFilter true when cached publications have at least one language (regardless
 *           of the language option search text).
 * @property showSubjectFilter true when cached publications have at least one subject (regardless
 *           of the subject option search text).
 */
data class OpdsSearchUiState(
    val filterOptions: OpdsSearchFilterOptions = OpdsSearchFilterOptions(),
    val searchParams: OpdsFeedDataSourceLocal.SearchParams = OpdsFeedDataSourceLocal.SearchParams(),
    //ContentFilterOption should not exist like this. We have filter options, what the user can select from,
    //we have searchParam - what the user has selected.
    val selectedFilters: Set<ContentFilterOption> = emptySet(),
    val results: List<OpdsSearchResult> = emptyList(),
    val languageOptionSearchText: String = "",
    val subjectOptionSearchText: String = "",
    val showLanguageFilter: Boolean = false,
    val showSubjectFilter: Boolean = false,
)
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
     * App catalog feeds (as used by the app list). Publications found in these feeds are apps,
     * so clicking them opens the app details instead of publication details.
     */
    private val appCatalogUrls = MutableStateFlow<List<Url>>(emptyList())

    /**
     * Manifest URLs of the apps listed on the home screen (the xAPI listed-app statement activity
     * ids). Search results with these URLs are apps.
     */
    private val listedAppUrls = MutableStateFlow<Set<String>>(emptySet())

    private val _uiState = MutableStateFlow(OpdsSearchUiState())

    val uiState = _uiState.asStateFlow()

    private val launchDebounced = LaunchDebouncer(viewModelScope)

    /**
     * The current search params, or null when there is no search text and no filter selected.
     */
    private val searchParams = MutableStateFlow<OpdsFeedDataSourceLocal.SearchParams?>(null)

    /**
     * The language and subject filter option search text (debounced) used to query the options.
     */
    private val filterOptionSearchText = MutableStateFlow(Pair("", ""))

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

        viewModelScope.launch {
            uiState.map { it.searchParams }.distinctUntilChanged().collectLatest { searchParams ->
                schoolDataSourceLocal.opdsFeedDataSource.searchAsFlow(searchParams).collect { results ->
                    _uiState.update { it.copy(results = results) }
                }
            }
        }

        viewModelScope.launch {
            filterOptionSearchText.collectLatest { (languageSearchText, subjectSearchText) ->
                schoolDataSourceLocal.opdsFeedDataSource.getSearchFilterOptionsAsFlow(
                    languageSearchText = languageSearchText,
                    subjectSearchText = subjectSearchText,
                ).collect { filterOptions ->
                    _uiState.update {
                        it.copy(
                            filterOptions = filterOptions,
                            showLanguageFilter = if (languageSearchText.isBlank()) {
                                filterOptions.languages.isNotEmpty()
                            } else {
                                it.showLanguageFilter
                            },
                            showSubjectFilter = if (subjectSearchText.isBlank()) {
                                filterOptions.subjects.isNotEmpty()
                            } else {
                                it.showSubjectFilter
                            },
                        )
                    }
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
        )
    }

    private fun updateSearchParams() {
        val searchText = _appUiState.value.searchState.searchText
        val state = _uiState.value
        searchParams.value = if (searchText.isNotBlank() || state.selectedFilters.isNotEmpty()) {
            state.toSearchParams(searchText)
        } else {
            null
        }
    }

    fun onSearchTextChanged(text: String) {
        _appUiState.update {
            it.copy(searchState = it.searchState.copy(searchText = text))
        }

        launchDebounced.launch(DEBOUNCE_KEY_SEARCH) {
            _uiState.update { prev ->
                prev.copy(
                    searchParams = prev.searchParams.copy(searchText = text)
                )
            }
        }
    }

    fun onToggleFilterOption(option: ContentFilterOption) {
        _uiState.update { it.copy(selectedFilters = it.selectedFilters.toggle(option)) }
        updateSearchParams()
    }

    fun onLanguageOptionSearchTextChanged(text: String) {
        _uiState.update { it.copy(languageOptionSearchText = text) }
        launchDebounced.launch(DEBOUNCE_KEY_FILTER_OPTIONS) { updateFilterOptionSearchText() }
    }

    fun onSubjectOptionSearchTextChanged(text: String) {
        _uiState.update { it.copy(subjectOptionSearchText = text) }
        launchDebounced.launch(DEBOUNCE_KEY_FILTER_OPTIONS) { updateFilterOptionSearchText() }
    }

    private fun updateFilterOptionSearchText() {
        val state = _uiState.value
        filterOptionSearchText.value = Pair(
            state.languageOptionSearchText, state.subjectOptionSearchText
        )
    }

    fun onClearFilters() {
        _uiState.update { it.copy(selectedFilters = emptySet()) }
        updateSearchParams()
    }

    fun onClickResult(result: OpdsSearchResult) {
        val feedUrl = result.feedUrl?.let { Url(it) }
        val resultUrl = result.url?.let { Url(it) }
            ?: result.href?.let { href -> feedUrl?.resolve(href) ?: Url(href) }

        if (resultUrl == null) {
            snackBarDispatcher.showSnackBar(Snack(Res.string.not_found.asUiText()))
            return
        }

        val isApp = result.isPublication && (
            resultUrl.toString() in listedAppUrls.value ||
                (feedUrl != null && feedUrl in appCatalogUrls.value)
        )

        val destination = if (isApp) {
            AppsDetail.create(manifestUrl = resultUrl)
        } else if (result.isPublication) {
            PublicationDetail.create(
                learningUnitManifestUrl = resultUrl,
                refererUrl = feedUrl,
                expectedIdentifier = result.identifier,
                title = LangMapStringValue(result.feedTitle ?: result.title),
            )
        } else {
            OpdsFeedDetail.create(opdsFeedUrl = resultUrl)
        }

        _navCommandFlow.tryEmit(NavCommand.Navigate(destination))
    }

    companion object {

        private const val DEBOUNCE_KEY_SEARCH = "search"

        private const val DEBOUNCE_KEY_FILTER_OPTIONS = "filter_options"

    }

}
