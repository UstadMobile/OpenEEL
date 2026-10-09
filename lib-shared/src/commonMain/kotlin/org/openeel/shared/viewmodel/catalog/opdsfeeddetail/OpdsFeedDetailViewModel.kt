package world.respect.shared.viewmodel.catalog.opdsfeeddetail

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
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.isAdmin
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.ext.map
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.ext.OpdsFeedItemIndex
import world.respect.lib.opds.model.ext.allPublications
import world.respect.lib.opds.model.ext.getNavigationLinkByIndex
import world.respect.lib.opds.model.ext.getNavigationLinksByIndexes
import world.respect.lib.opds.model.ext.getPublicationByIndex
import world.respect.lib.opds.model.ext.getPublicationsByIndexes
import world.respect.lib.opds.model.findSelfLinks
import world.respect.libutil.ext.resolve
import world.respect.libutil.ext.toggle
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.ext.resultExpected
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.edit
import world.respect.shared.generated.resources.language
import world.respect.shared.generated.resources.not_found
import world.respect.shared.generated.resources.something_went_wrong
import world.respect.shared.navigation.AssignmentEdit
import world.respect.shared.navigation.PublicationDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.OpdsFeedDetail
import world.respect.shared.navigation.OpdsFeedEdit
import world.respect.shared.navigation.PlaylistShare
import world.respect.shared.navigation.sendResultIfResultExpected
import world.respect.shared.util.SortOrderOption
import world.respect.shared.util.ext.appbarTitleString
import world.respect.shared.util.ext.asUiText
import world.respect.shared.util.ext.firstSelfLinkOrNull
import world.respect.shared.util.ext.resolve
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.app.appstate.Snack
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher
import world.respect.shared.viewmodel.assignment.edit.AssignmentEditViewModel
import world.respect.shared.viewmodel.catalog.OpdsFeedSelection
import world.respect.shared.viewmodel.catalog.OpdsPickType
import world.respect.shared.viewmodel.catalog.PublicationsSelection

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
) {
    fun isGroupCollapsed(groupIndex: Int) = groupIndex in collapsedGroupIndexes

    fun isPublicationSelected(index: OpdsFeedItemIndex): Boolean = index in selectedPublications

    fun isNavigationSelected(index: OpdsFeedItemIndex): Boolean = index in selectedNavigationLinks

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
    private val accountManager: RespectAccountManager,
    private val resultReturner: NavResultReturner,
    private val snackBarDispatcher: SnackBarDispatcher,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

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
                title = route.opdsPickType?.appbarTitleString?.asUiText() ?: prev.title
            )
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
                _uiState.update { prev ->
                    prev.copy(
                        feed = result.map { it.resolve(route.opdsFeedUrl) }
                    )
                }

                if(result is DataReadyState && route.opdsPickType == null) {
                    _appUiState.update { it.copy(title = result.data.metadata.title.asUiText()) }
                }
            }
        }
    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update { it.copy(activeSortOrderOption = sortOption) }
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
                    destination = RespectAppLauncher.create()
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