package org.openeel.shared.viewmodel.catalog.opdsfeeddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.SchoolDataSourceLocal
import org.openeel.datalayer.db.school.ext.isAdmin
import org.openeel.datalayer.school.opds.OpdsFeedDataSourceLocal
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.dataloadstate.ext.map
import org.openeel.lib.opds.model.LangMapStringValue
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.ext.OpdsFeedItemIndex
import org.openeel.lib.opds.model.ext.OpdsFeedSearchMatch
import org.openeel.lib.opds.model.ext.allPublications
import org.openeel.lib.opds.model.ext.getNavigationLinkByIndex
import org.openeel.lib.opds.model.ext.getNavigationLinksByIndexes
import org.openeel.lib.opds.model.ext.getPublicationByIndex
import org.openeel.lib.opds.model.ext.getPublicationsByIndexes
import org.openeel.lib.opds.model.findSelfLinks
import org.openeel.libutil.ext.resolve
import org.openeel.libutil.ext.toggle
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.contentfilter.ContentFilterOption
import org.openeel.shared.domain.contentfilter.ContentFilterOptions
import org.openeel.shared.domain.contentfilter.ContentFilterSelection
import org.openeel.shared.domain.contentfilter.itemsMatching
import org.openeel.shared.domain.search.ObserveSearchResultsUseCase
import org.openeel.shared.ext.resultExpected
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.edit
import org.openeel.shared.generated.resources.language
import org.openeel.shared.generated.resources.not_found
import org.openeel.shared.generated.resources.something_went_wrong
import org.openeel.shared.navigation.AssignmentEdit
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.NavResultReturner
import org.openeel.shared.navigation.OpdsFeedDetail
import org.openeel.shared.navigation.OpdsFeedEdit
import org.openeel.shared.navigation.OpenEelAppLauncher
import org.openeel.shared.navigation.PlaylistShare
import org.openeel.shared.navigation.PublicationDetail
import org.openeel.shared.navigation.sendResultIfResultExpected
import org.openeel.shared.util.SortOrderOption
import org.openeel.shared.util.ext.appbarTitleString
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.util.ext.firstSelfLinkOrNull
import org.openeel.shared.util.ext.resolve
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.AppBarSearchUiState
import org.openeel.shared.viewmodel.app.appstate.FabUiState
import org.openeel.shared.viewmodel.app.appstate.Snack
import org.openeel.shared.viewmodel.app.appstate.SnackBarDispatcher
import org.openeel.shared.viewmodel.assignment.edit.AssignmentEditViewModel
import org.openeel.shared.viewmodel.catalog.OpdsFeedSelection
import org.openeel.shared.viewmodel.catalog.OpdsPickType
import org.openeel.shared.viewmodel.catalog.PublicationsSelection

/**
 * @property visibleItems items matching both the search and the selected content filters, or null
 *           when neither is active. Calculated by the ViewModel when the feed, search, or filters
 *           change.
 */
data class OpdsFeedDetailUiState(
    val feed: DataLoadState<OpdsFeed> = DataLoadingState(),
    val sortOptions: List<SortOrderOption> = emptyList(),
    val activeSortOrderOption: SortOrderOption = SortOrderOption(
        Res.string.language, 1, true
    ),
    val quickActionsVisible: Boolean = true,
    val isTeacherOrAdmin: Boolean = false,
    val collapsedGroupIndexes: Set<Int> = emptySet(),
    val selectedPublications: Set<OpdsFeedItemIndex> = emptySet(),
    val selectedNavigationLinks: Set<OpdsFeedItemIndex> = emptySet(),
    val showCopyDialog: Boolean = false,
    val copyDialogName: String = "",
    val showDeleteDialog: Boolean = false,
    val pickType: OpdsPickType? = null,
    val searchMatches: List<OpdsFeedSearchMatch>? = null,
    val contentFilterOptions: ContentFilterOptions = ContentFilterOptions(),
    val contentFilter: ContentFilterSelection = ContentFilterSelection(),
    val visibleItems: Set<OpdsFeedSearchMatch>? = null,
) {
    private val visibleGroupIndexes by lazy { visibleItems?.map { it.groupIndex }?.toSet() }

    fun isGroupCollapsed(groupIndex: Int) = groupIndex in collapsedGroupIndexes

    fun isPublicationSelected(index: OpdsFeedItemIndex): Boolean = index in selectedPublications

    fun isNavigationSelected(index: OpdsFeedItemIndex): Boolean = index in selectedNavigationLinks

    /**
     * Whether the item matches the search and selected content filters. Match both the original
     * position and item type because separate feed lists can share indexes.
     */
    fun matchesSearchFilter(index: OpdsFeedItemIndex, isPublication: Boolean): Boolean =
        visibleItems?.contains(
            OpdsFeedSearchMatch(index.groupIndex, index.index, isPublication)
        ) ?: true

    /** Keep a group visible only when at least one item in it matched the search and filters.*/
    fun groupMatchesSearchFilter(groupIndex: Int): Boolean =
        visibleGroupIndexes?.contains(groupIndex) ?: true

    val isMultiSelectMode: Boolean
        get() = selectedPublications.isNotEmpty() || selectedNavigationLinks.isNotEmpty()

    val showPublicationCheckboxes: Boolean
        get() = pickType == OpdsPickType.PUBLICATION && isMultiSelectMode

    val showNavigationCheckboxes: Boolean
        get() = pickType == OpdsPickType.CATALOG_FEED && isMultiSelectMode


    val selectedCount: Int
        get() = (selectedPublications.size + selectedNavigationLinks.size)

    val showAssignButton: Boolean
        get() = isTeacherOrAdmin && !feed.dataOrNull()?.allPublications().isNullOrEmpty()

    val showSelectPlaylistButton: Boolean
        get() = pickType == OpdsPickType.CATALOG_FEED && selectedNavigationLinks.isEmpty()

    val showSelectionBottomButton: Boolean
        get() = isMultiSelectMode && feed.dataOrNull() != null

    /**
     * When the user is in pick mode the appbar title will be select unit(s) or selection
     * collection(s) to make it clear what they need to select, so we need to show the title in the
     * content.
     */
    val showFeedTitleInContent: Boolean
        get() = pickType != null

}

/**
 * Show a list of learning units as provided by an OpdsFeed
 */
class OpdsFeedDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: AppAccountManager,
    private val resultReturner: NavResultReturner,
    private val snackBarDispatcher: SnackBarDispatcher,
    private val observeSearchResultsUseCase: ObserveSearchResultsUseCase,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()
    private val schoolDataSourceLocal: SchoolDataSourceLocal by inject()

    private val _uiState = MutableStateFlow(OpdsFeedDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val route: OpdsFeedDetail = savedStateHandle.toRoute()

    init {
        _uiState.update {
            it.copy(
                quickActionsVisible = route.resultDest == null,
                pickType = route.opdsPickType,
            )
        }

        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = route.resultDest != null,
                title = route.opdsPickType?.appbarTitleString?.asUiText() ?: prev.title,
                searchState = AppBarSearchUiState(
                    visible = true,
                    onSearchTextChanged = ::onSearchTextChanged,
                    onSearchExpandedChanged = ::setSearchExpanded,
                ),
            )
        }

        viewModelScope.launch {
            observeSearchResultsUseCase(
                appUiStateFlow = appUiState,
                searchFn = { searchQuery ->
                    schoolDataSourceLocal.opdsFeedDataSource.searchByTitleAsFlow(
                        url = route.opdsFeedUrl,
                        listParams = OpdsFeedDataSourceLocal.GetListParams(title = searchQuery),
                    )
                },
            ).collect { matchingItems ->
                updateVisibleItemsInputs { it.copy(searchMatches = matchingItems) }
            }
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { sessionAndPerson ->
                val isTeacherOrAdmin = sessionAndPerson?.person?.isAdmin() == true
                _uiState.update { it.copy(isTeacherOrAdmin = isTeacherOrAdmin) }
                _appUiState.update {
                    it.copy(
                        fabState = FabUiState(
                            visible = false,//temporarily disabled 3/Oct - isTeacherOrAdmin && route.resultDest == null,
                            icon = FabUiState.FabIcon.EDIT,
                            text = Res.string.edit.asUiText(),
                            onClick = ::onClickEdit,
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            schoolDataSource.opdsFeedDataSource.getByUrlAsFlow(
                url = route.opdsFeedUrl,
                params = DataLoadParams()
            ).collect { result ->
                updateVisibleItemsInputs { prev ->
                    prev.copy(
                        feed = result.map { it.resolve(route.opdsFeedUrl) },
                        contentFilterOptions = result.dataOrNull()?.let {
                            ContentFilterOptions.fromPublications(it.allPublications())
                        } ?: prev.contentFilterOptions,
                    )
                }

                if(result is DataReadyState && route.opdsPickType == null) {
                    _appUiState.update { it.copy(title = result.data.metadata.title.asUiText()) }
                }
            }
        }
    }

    /**
     * Update the ui state where the change affects which items are visible (the feed,
     * searchMatches, or contentFilter) and recalculate visibleItems. Other changes (e.g. selection,
     * dialogs) should use _uiState.update directly to avoid unnecessary recalculation.
     */
    private inline fun updateVisibleItemsInputs(
        transform: (OpdsFeedDetailUiState) -> OpdsFeedDetailUiState
    ) {
        _uiState.update { transform(it).withVisibleItems() }
    }

    /**
     * Calculate the items matching both the search and the selected content filters. The result is
     * null when neither search nor filters are active (e.g. everything is visible).
     */
    private fun OpdsFeedDetailUiState.withVisibleItems(): OpdsFeedDetailUiState {
        val searchMatchSet = searchMatches?.toSet()

        val filterMatches = if (contentFilter.isEmpty) {
            null
        } else {
            feed.dataOrNull()?.itemsMatching(contentFilter).orEmpty()
        }

        return copy(
            visibleItems = when {
                filterMatches == null -> searchMatchSet
                searchMatchSet == null -> filterMatches
                else -> filterMatches intersect searchMatchSet
            }
        )
    }

    fun onToggleContentFilterOption(option: ContentFilterOption) {
        updateVisibleItemsInputs {
            it.copy(contentFilter = it.contentFilter.toggle(option))
        }
    }

    override fun onSearchCollapsed() {
        onClearContentFilters()
    }

    fun onClearContentFilters() {
        updateVisibleItemsInputs { it.copy(contentFilter = ContentFilterSelection()) }
    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update { it.copy(activeSortOrderOption = sortOption) }
    }

    fun onSearchTextChanged(searchQuery: String) {
        updateSearchText(searchQuery) { query ->
            updateVisibleItemsInputs {
                it.copy(searchMatches = if (query.isEmpty()) null else emptyList())
            }
        }
    }

    fun onClickPublication(index: OpdsFeedItemIndex) {
        val publication = uiState.value.feed.dataOrNull()
            ?.getPublicationByIndex(index)
            ?: return

        when {
            route.opdsPickType == OpdsPickType.CATALOG_FEED -> {
                //do nothing: the user is expected to pick a catalog.
            }

            route.opdsPickType == OpdsPickType.PUBLICATION && uiState.value.isMultiSelectMode -> {
                togglePublicationSelection(index)
            }

            route.resultExpected -> {
                resultReturner.sendResultIfResultExpected(
                    route = route,
                    navCommandFlow = _navCommandFlow,
                    result = PublicationsSelection(
                        url = route.opdsFeedUrl,
                        selectedPublications = listOf(publication),
                    )
                )
            }

            else -> {
                val manifestUrl = publication.findSelfLinks().firstOrNull()?.href?.let {
                    route.opdsFeedUrl.resolve(it)
                } ?: return run {
                    snackBarDispatcher.showSnackBar(Snack(Res.string.not_found.asUiText()))
                }

                //Needs to handle external link.
                _navCommandFlow.tryEmit(
                    value = NavCommand.Navigate(
                        PublicationDetail.create(
                            learningUnitManifestUrl = manifestUrl,
                            refererUrl = route.opdsFeedUrl,
                            expectedIdentifier = publication.metadata.identifier?.toString(),
                            title = uiState.value.feed.dataOrNull()?.metadata?.title?.let {
                                LangMapStringValue(it)
                            }
                        )
                    )
                )
            }
        }
    }

    fun onLongPressPublication(
        opdsFeedItemIndex: OpdsFeedItemIndex
    ) {
        if(route.opdsPickType == OpdsPickType.PUBLICATION) {
            togglePublicationSelection(opdsFeedItemIndex)
        }else {
            onClickPublication(opdsFeedItemIndex)
        }
    }

    private fun togglePublicationSelection(
        index: OpdsFeedItemIndex
    ) {
        _uiState.update { prev ->
            val publicationSelections = prev.selectedPublications.toggle(index)
            prev.copy(
                selectedPublications = publicationSelections,
            )
        }
    }

    fun onClickSelectBottomButton() {
        val currentState = _uiState.value

        when(route.opdsPickType) {
            OpdsPickType.PUBLICATION -> {
                resultReturner.sendResultIfResultExpected(
                    route = route,
                    navCommandFlow = _navCommandFlow,
                    result = PublicationsSelection(
                        url = route.opdsFeedUrl,
                        selectedPublications = uiState.value.feed.dataOrNull()?.getPublicationsByIndexes(
                            currentState.selectedPublications
                        ).orEmpty()
                    )
                )
            }

            OpdsPickType.CATALOG_FEED -> {
                resultReturner.sendResultIfResultExpected(
                    route = route,
                    navCommandFlow = _navCommandFlow,
                    result = OpdsFeedSelection(
                        url = route.opdsFeedUrl,
                        selectedFeeds = uiState.value.feed.dataOrNull()?.getNavigationLinksByIndexes(
                            currentState.selectedNavigationLinks
                        ).orEmpty()
                    ),
                )
            }

            else -> {
                //do nothing - not in select mode
            }
        }
    }

    private fun toggleNavigationSelection(
        index: OpdsFeedItemIndex
    ) {
        _uiState.update { prev ->
            val navigationSelections = prev.selectedNavigationLinks.toggle(index)
            prev.copy(
                selectedNavigationLinks = navigationSelections,
            )
        }
    }

    fun onLongPressNavigation(index: OpdsFeedItemIndex) {
        if(route.opdsPickType == OpdsPickType.CATALOG_FEED) {
            toggleNavigationSelection(index)
        }else {
            onClickNavigation(index)
        }
    }


    fun onClickNavigation(index: OpdsFeedItemIndex) {
        val navigation = uiState.value.feed.dataOrNull()
            ?.getNavigationLinkByIndex(index) ?: return

        when {
            route.opdsPickType == OpdsPickType.CATALOG_FEED && uiState.value.isMultiSelectMode -> {
                toggleNavigationSelection(index)
            }

            else -> {
                val resolvedUrl = route.opdsFeedUrl.resolve(navigation.href)
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        OpdsFeedDetail.create(
                            opdsFeedUrl = resolvedUrl,
                            resultDest = route.resultDest,
                            opdsPickType = route.opdsPickType,
                        )
                    )
                )
            }
        }
    }

    fun onClickSelectThisFeed() {
        val feed = uiState.value.feed.dataOrNull() ?: return

        val navItem = feed.links.firstSelfLinkOrNull()?.let {
            it.copy(title = it.title ?: feed.metadata.title)
        }

        if(navItem == null) {
            snackBarDispatcher.showSnackBar(Snack(Res.string.something_went_wrong.asUiText()))
            return
        }

        resultReturner.sendResultIfResultExpected(
            route = route,
            navCommandFlow = _navCommandFlow,
            result = OpdsFeedSelection(
                url = route.opdsFeedUrl,
                selectedFeeds = listOf(navItem),
            )
        )
    }

    fun onClickToggleGroup(groupIndex: Int) {
        _uiState.update { prev ->
            prev.copy(
                collapsedGroupIndexes = prev.collapsedGroupIndexes.toggle(groupIndex)
            )
        }
    }

    fun onClickShare() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(PlaylistShare.create(playlistUrl = route.opdsFeedUrl))
        )
    }

    fun onClickCopy() {
        /*
        val feed = _uiState.value.feed
            ?: throw IllegalStateException("onClickCopy called but feed is null")
        _uiState.update {
            it.copy(
                showCopyDialog = true,
                copyDialogName = feed.metadata.title,
            )
        }*/
    }

    fun onCopyDialogDismiss() {
        _uiState.update { it.copy(showCopyDialog = false, copyDialogName = "") }
    }

    fun onCopyDialogNameChanged(name: String) {
        _uiState.update { it.copy(copyDialogName = name) }
    }

    fun onCopyDialogConfirm() {
        /*
        val feed = _uiState.value.feed
            ?: throw IllegalStateException("onCopyDialogConfirm called but feed is null")
        val newName = _uiState.value.copyDialogName.trim()
        if (newName.isBlank())
            throw IllegalStateException("onCopyDialogConfirm called but the new name is blank")

        viewModelScope.launch {
            val sessionAndPerson = accountManager.selectedAccountAndPersonFlow.first()
                ?: throw IllegalStateException("No active session when copying feed")

            val username = sessionAndPerson.person.username
                ?: throw IllegalStateException(
                    "Active person has no username: ${sessionAndPerson.person.guid}"
                )

            @OptIn(ExperimentalUuidApi::class)
            val copiedFeed = makePlaylistOpdsFeedUseCase.invoke(
                base = feed.copy(
                    metadata = feed.metadata.copy(title = newName)
                ),
                username = username,
            )

            schoolDataSource.opdsFeedDataSource.store(listOf(copiedFeed))

            _uiState.update { it.copy(showCopyDialog = false, copyDialogName = "") }

            val copiedUrl = copiedFeed.selfUrl()
                ?: throw IllegalStateException("Copied feed has no self URL")

            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    PlaylistEdit.create(
                        playlistUrl = copiedUrl,
                        isCopy = true,
                    )
                )
            )
        }*/
    }

    fun onClickDelete() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDeleteDialogDismiss() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun onDeleteDialogConfirm() {
        /*
        viewModelScope.launch {
            val feed = _uiState.value.feed
                ?: throw IllegalStateException("onDeleteDialogConfirm called but feed is null")
            val selfUrl = feed.selfUrl()
                ?: throw IllegalStateException("Cannot delete: feed has no self URL")

            schoolDataSource.opdsFeedDataSource.deleteByUrl(selfUrl)

            _uiState.update { it.copy(showDeleteDialog = false) }
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    destination = OpenEelAppLauncher.create()
                )
            )
        }*/
    }

    fun onClickAssignQuickActionButton() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                AssignmentEdit.create(
                    assignmentActivityId = null,
                    learningUnitSelected = PublicationsSelection(
                        url = route.opdsFeedUrl,
                        selectedPublications = uiState.value.feed.dataOrNull()?.let { feed ->
                            feed.publications.orEmpty() + feed.groups.orEmpty().flatMap {
                                it.publications.orEmpty()
                            }
                        }.orEmpty().take(AssignmentEditViewModel.MAX_UNITS_TO_ADD)
                    )
                )
            )
        )
    }

    fun onClickAssignSection(sectionIndex: Int) {
        /*
        val feed = _uiState.value.feed
            ?: throw IllegalStateException("Cannot assign: no feed loaded")
        val feedUrl = feed.selfUrl()
            ?: throw IllegalStateException("Cannot assign: feed has no self URL")

        val targetSection = if (sectionIndex == ASSIGN_HEADER_SECTION_INDEX) {
            _uiState.value.group.firstOrNull { it.publications?.isNotEmpty() == true }
                ?: throw IllegalStateException("No learning unit section with items found to assign")
        } else {
            _uiState.value.group.getOrNull(sectionIndex)
                ?: throw IllegalStateException("No section at index $sectionIndex")
        }

        val firstPublication = targetSection.publications?.firstOrNull()
            ?: throw IllegalStateException(
                "Assign clicked but section at index $sectionIndex has no learning items"
            )

        val learningUnitManifestUrl = feedUrl.resolve(
            publicationSelfLink(firstPublication).href
        )

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = AssignmentEdit.create(
                    assignmentActivityId = null,
                    learningUnitSelected = LearningUnitSelection(
                        learningUnitManifestUrl = learningUnitManifestUrl,
                        selectedPublication = firstPublication,
                    )
                )
            )
        )

         */
    }


    fun onClickEdit() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(OpdsFeedEdit.create(playlistUrl = route.opdsFeedUrl))
        )
    }

    companion object {
        const val SELF = "self"
        const val ICON = "icon"
    }
}