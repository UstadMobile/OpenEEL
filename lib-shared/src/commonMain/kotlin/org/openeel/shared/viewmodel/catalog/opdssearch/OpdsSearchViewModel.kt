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
import org.openeel.datalayer.SchoolDataSourceLocal
import org.openeel.datalayer.school.model.composites.OpdsSearchResult
import org.openeel.datalayer.school.opds.OpdsFeedDataSourceLocal
import org.openeel.lib.opds.model.LangMapStringValue
import org.openeel.lib.opds.model.OpenEelConstants
import org.openeel.libutil.ext.resolve
import org.openeel.shared.domain.account.AppAccountManager
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
 * The search text as displayed is held in the appbar search state (AppUiState.searchState).
 *
 * @property languageOptions the languages available to filter by (matching
 *           [languageOptionSearchText]).
 * @property subjectOptions the subjects available to filter by (matching
 *           [subjectOptionSearchText]).
 * @property searchParams the search params (as selected by the user) used to run the search query.
 * @property results search results for [searchParams].
 * @property languageOptionSearchText text entered to search the language filter options.
 * @property subjectOptionSearchText text entered to search the subject filter options.
 */
data class OpdsSearchUiState(
    val searchParams: OpdsFeedDataSourceLocal.SearchParams = OpdsFeedDataSourceLocal.SearchParams(),
    val results: List<OpdsSearchResult> = emptyList(),
    val languageOptions: List<String> = emptyList(),
    val subjectOptions: List<String> = emptyList(),
    val languageOptionSearchText: String = "",
    val subjectOptionSearchText: String = "",
) {

    val hasFilters: Boolean
        get() = searchParams.languages.isNotEmpty() || searchParams.subjects.isNotEmpty() ||
                searchParams.types.isNotEmpty()

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

    private val _uiState = MutableStateFlow(OpdsSearchUiState())

    val uiState = _uiState.asStateFlow()

    private val launchDebounced = LaunchDebouncer(viewModelScope)

    init {
        _appUiState.update {
            it.copy(
                showBackButton = true,
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                searchState = AppBarSearchUiState(
                    visible = true,
                    searchText = "",
                    onSearchTextChanged = ::onSearchTextChanged,
                ),
            )
        }

        viewModelScope.launch {
            uiState.map { it.searchParams }.distinctUntilChanged().collectLatest { searchParams ->
                if (searchParams.searchText.isBlank() && searchParams.languages.isEmpty() &&
                    searchParams.subjects.isEmpty() && searchParams.types.isEmpty()
                ) {
                    _uiState.update { it.copy(results = emptyList()) }
                } else {
                    schoolDataSourceLocal.opdsFeedDataSource.searchAsFlow(searchParams)
                        .collectLatest { searchResults ->
                            _uiState.update { it.copy(results = searchResults) }
                        }
                }
            }
        }

        viewModelScope.launch {
            uiState.map { it.languageOptionSearchText }.distinctUntilChanged()
                .collectLatest { searchText ->
                    schoolDataSourceLocal.opdsFeedDataSource.getLanguageOptionsAsFlow(
                        searchText
                    ).collectLatest { languageOptions ->
                        _uiState.update { it.copy(languageOptions = languageOptions) }
                    }
                }
        }

        viewModelScope.launch {
            uiState.map { it.subjectOptionSearchText }.distinctUntilChanged()
                .collectLatest { searchText ->
                    schoolDataSourceLocal.opdsFeedDataSource.getSubjectOptionsAsFlow(
                        searchText
                    ).collectLatest { subjectOptions ->
                        _uiState.update { it.copy(subjectOptions = subjectOptions) }
                    }
                }
        }
    }

    fun onSearchTextChanged(text: String) {
        _appUiState.update {
            it.copy(searchState = it.searchState.copy(searchText = text))
        }

        launchDebounced.launch(DEBOUNCE_KEY_SEARCH) {
            _uiState.update { prev ->
                prev.copy(searchParams = prev.searchParams.copy(searchText = text))
            }
        }
    }

    fun onLanguagesChanged(languages: List<String>) {
        _uiState.update { prev ->
            prev.copy(searchParams = prev.searchParams.copy(languages = languages))
        }
    }

    fun onSubjectsChanged(subjects: List<String>) {
        _uiState.update { prev ->
            prev.copy(searchParams = prev.searchParams.copy(subjects = subjects))
        }
    }

    fun onTypesChanged(types: List<String>) {
        _uiState.update { prev ->
            prev.copy(searchParams = prev.searchParams.copy(types = types))
        }
    }

    fun onLanguageOptionSearchTextChanged(text: String) {
        _uiState.update { it.copy(languageOptionSearchText = text) }
    }

    fun onSubjectOptionSearchTextChanged(text: String) {
        _uiState.update { it.copy(subjectOptionSearchText = text) }
    }

    fun onClearFilters() {
        _uiState.update { prev ->
            prev.copy(
                searchParams = prev.searchParams.copy(
                    languages = emptyList(),
                    subjects = emptyList(),
                    types = emptyList(),
                )
            )
        }
    }

    fun onClickResult(result: OpdsSearchResult) {
        val feedUrl = result.feedUrl?.let { Url(it) }
        val resultUrl = result.url?.let { Url(it) }
            ?: result.href?.let { href -> feedUrl?.resolve(href) ?: Url(href) }

        if (resultUrl == null) {
            snackBarDispatcher.showSnackBar(Snack(Res.string.not_found.asUiText()))
            return
        }

        val destination = if (result.type == OpenEelConstants.TYPE_LAUNCHABLE_APP) {
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

    }

}
