package org.openeel.shared.viewmodel.catalog.opdsfeededit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.school.domain.MakePlaylistOpdsFeedUseCase
import org.openeel.datalayer.school.opds.ext.requireSelfUrl
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.dataloadstate.ext.mapAsync
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.OpdsFeedMetadata
import org.openeel.lib.opds.model.OpdsGroup
import org.openeel.lib.opds.model.util.NavigationItemItemIndex
import org.openeel.lib.opds.model.util.OpdsFeedGroupItemItemIndex
import org.openeel.lib.opds.model.util.OpdsFeedItemIndex
import org.openeel.lib.opds.model.util.PublicationItemItemIndex
import org.openeel.libutil.ext.moveItem
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.catalog.saveopdsfeed.SaveOpdsFeedUseCase
import org.openeel.shared.ext.addSelection
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_collection
import org.openeel.shared.generated.resources.collections_section
import org.openeel.shared.generated.resources.edit_collection
import org.openeel.shared.generated.resources.learning_item_section
import org.openeel.shared.generated.resources.remix_collection
import org.openeel.shared.generated.resources.required_field
import org.openeel.shared.generated.resources.save
import org.openeel.shared.navigation.OpdsFeedEditAddLink
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.NavResultReturner
import org.openeel.shared.navigation.OpdsFeedDetail
import org.openeel.shared.navigation.OpdsFeedEdit
import org.openeel.shared.navigation.OpenEelAppLauncher
import org.openeel.shared.navigation.RouteResultDest
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.util.ext.groupType
import org.openeel.shared.util.ext.isNewFeedOrNewRemix
import org.openeel.shared.util.ext.resolve
import org.openeel.shared.util.ext.resolveAndAddSelection
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.ActionBarButtonUiState
import org.openeel.shared.viewmodel.app.appstate.Snack
import org.openeel.shared.viewmodel.app.appstate.SnackBarDispatcher
import org.openeel.shared.viewmodel.catalog.OpdsFeedSelection
import org.openeel.shared.viewmodel.catalog.OpdsPickType
import org.openeel.shared.viewmodel.catalog.PublicationsSelection
import kotlin.uuid.Uuid

/**
 * As per the OPDS spec:
 * https://specs.opds.io/opds-2.0.html#25-groups
 * "Groups are meant to contain:
 *
 *     either a single navigation collection
 *     or a single publications collection"
 */
enum class OpdsGroupType {
    NAVIGATION,
    PUBLICATION,
}

data class MovingItemState(
    val fromSectionIndex: Int,
    val itemIndex: Int,
    val compatibleSections: List<CompatibleSection>,
) {
    data class CompatibleSection(
        val sectionIndex: Int,
    )
}

data class OpdsFeedEditUiState(
    val feed: OpdsFeed? = null,
    val addGroupTypeDialogVisible: Boolean = false,
    val titleError: UiText? = null,
    val movingItem: MovingItemState? = null,
    val isAddItemTypeBottomSheetVisible: Boolean = false,
) {
    val title: String
        get() = feed?.metadata?.title ?: ""

    val description: String
        get() = feed?.metadata?.description ?: ""

    val groups: List<OpdsGroup>
        get() = feed?.groups ?: emptyList()

    val hasErrors: Boolean
        get() = titleError != null

    val canMovePublicationItemToOtherGroup: Boolean
        get() = (feed?.groups?.count { it.groupType == OpdsGroupType.PUBLICATION } ?: 0) > 1

    val canMoveNavigationItemToOtherGroup: Boolean
        get() = (feed?.groups?.count { it.groupType == OpdsGroupType.NAVIGATION } ?: 0) > 1

}

class OpdsFeedEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: AppAccountManager,
    private val resultReturner: NavResultReturner,
    private val snackBarDispatcher: SnackBarDispatcher,
    private val json: Json,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()
    private val makePlaylistOpdsFeedUseCase: MakePlaylistOpdsFeedUseCase by inject()

    private val route: OpdsFeedEdit = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(OpdsFeedEditUiState())

    val uiState = _uiState.asStateFlow()

    private val saveOpdsFeedUseCase: SaveOpdsFeedUseCase by inject()

    private var pendingAddItemGroupIndex: Int?
        get() = savedStateHandle.get<Int>(KEY_PENDING_ADD_ITEM_GROUP_INDEX)
        set(value) { savedStateHandle[KEY_PENDING_ADD_ITEM_GROUP_INDEX] = value }


    init {
        _appUiState.update { prev ->
            prev.copy(
                title = when {
                    route.isCopy -> Res.string.remix_collection.asUiText()
                    route.url == null -> Res.string.add_collection.asUiText()
                    else -> Res.string.edit_collection.asUiText()
                },
                userAccountIconVisible = false,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = Res.string.save.asUiText(),
                    onClick = ::onClickSave,
                ),
                hideBottomNavigation = true,
            )
        }

        launchWithLoadingIndicator(
            onShowError = { snackBarDispatcher.showSnackBar(Snack(it)) }
        ) {
            if(route.url != null) {
                loadEntity(
                    json = json,
                    serializer = OpdsFeed.serializer(),
                    loadFn = { params ->
                        schoolDataSource.opdsFeedDataSource.getByUrl(
                            url = route.url,
                            params = params,
                        ).let { dataLoadState ->
                            if(route.isCopy) {
                                dataLoadState.mapAsync {
                                    makePlaylistOpdsFeedUseCase(
                                        base = it.resolve(route.url),
                                        username = accountManager.activeAccount?.userGuid ?: ""
                                    ).copy(
                                        metadata = it.metadata.copy(
                                            title = "Remix of ${it.metadata.title}",
                                        )
                                    )
                                }
                            }else {
                                dataLoadState
                            }
                        }
                    },
                    uiUpdateFn = { state ->
                        _uiState.update { it.copy(feed = state.dataOrNull()) }
                    }
                )
            }else {
                val newFeed = makePlaylistOpdsFeedUseCase(
                    base = OpdsFeed(
                        metadata = OpdsFeedMetadata(title = ""),
                        links = emptyList(),
                        publications = emptyList(),
                        groups = emptyList(),
                    ),
                    username = accountManager.activeAccount?.userGuid ?: ""
                )
                _uiState.update { it.copy(feed = newFeed) }
            }

            launch {
                resultReturner.filteredResultFlowForKey(KEY_PUBLICATION).collect { result ->
                    val groupIndex = pendingAddItemGroupIndex ?: return@collect
                    val publicationSelection = result.result as? PublicationsSelection ?: return@collect

                    pendingAddItemGroupIndex = null

                    _uiState.update { prev ->
                        prev.copy(
                            feed = if(groupIndex == OpdsFeedItemIndex.NO_GROUP) {
                                prev.feed?.copy(
                                    publications = prev.feed.publications.orEmpty()
                                        .resolveAndAddSelection(publicationSelection)
                                )
                            }else {
                                prev.feed?.copy(
                                    groups = if(groupIndex >= 0) {
                                        prev.feed.groups.orEmpty().toMutableList().also { groupList ->
                                            groupList[groupIndex] = groupList[groupIndex].copy(
                                                publications = groupList[groupIndex].publications
                                                    .orEmpty().resolveAndAddSelection(publicationSelection),
                                            )
                                        }
                                    }else {
                                        prev.feed.groups
                                    }
                                )
                            }
                        )
                    }
                }
            }

            launch {
                resultReturner.filteredResultFlowForKey(KEY_COLLECTION).collect { result ->
                    val groupIndex = pendingAddItemGroupIndex ?: return@collect
                    val feedSelection = result.result as? OpdsFeedSelection ?: return@collect
                    pendingAddItemGroupIndex = null

                    _uiState.update { prev ->
                        prev.copy(
                            feed = if(groupIndex == OpdsFeedItemIndex.NO_GROUP) {
                                prev.feed?.copy(
                                    navigation = prev.feed.navigation.orEmpty()
                                        .addSelection(feedSelection)
                                )
                            }else {
                                prev.feed?.copy(
                                    groups = prev.feed.groups.orEmpty().toMutableList().also { groupList ->
                                        val groupToAddTo = groupList[groupIndex]
                                        groupList[groupIndex] = groupToAddTo.copy(
                                            navigation = groupToAddTo.navigation.orEmpty()
                                                .addSelection(feedSelection)
                                        )
                                    }.toList()
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { prev ->
            prev.copy(
                feed = prev.feed?.copy(
                    metadata = prev.feed.metadata.copy(title = title)
                ),
                titleError = null,
            )
        }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { prev ->
            prev.copy(
                feed = prev.feed?.copy(
                    metadata = prev.feed.metadata.copy(description = description)
                )
            )
        }
    }

    fun onGroupTitleChanged(groupIndex: Int, title: String) {
        _uiState.update { prev ->
            val sections = prev.feed?.groups.orEmpty().toMutableList()
            val section = sections.getOrNull(groupIndex)
                ?: throw IllegalStateException("No group at index $groupIndex")
            sections[groupIndex] = section.copy(
                metadata = section.metadata.copy(title = title)
            )
            prev.copy(feed = prev.feed?.copy(groups = sections))
        }
    }

    fun onClickAddGroup() {
        _uiState.update { it.copy(addGroupTypeDialogVisible = true) }
    }

    fun onDismissAddGroupTypeDialog() {
        _uiState.update { it.copy(addGroupTypeDialogVisible = false) }
    }

    fun onClickAddGroupType(groupType: OpdsGroupType) {
        viewModelScope.launch {
            val sectionTitle = when (groupType) {
                OpdsGroupType.NAVIGATION -> getString(Res.string.collections_section)
                OpdsGroupType.PUBLICATION -> getString(Res.string.learning_item_section)
            }

            _uiState.update { prev ->
                val newGroup = OpdsGroup(
                    metadata = OpdsFeedMetadata(
                        title = sectionTitle,
                        identifier = prev.feed?.metadata?.identifier?.buildUpon()
                            ?.appendPath("/group/${Uuid.random()}")
                            ?.build()
                    ),
                    navigation = if (groupType == OpdsGroupType.NAVIGATION) emptyList() else null,
                    publications = if (groupType == OpdsGroupType.PUBLICATION) emptyList() else null,
                )

                prev.copy(
                    feed = prev.feed?.copy(
                        groups = prev.feed.groups.orEmpty() + newGroup
                    ),
                    addGroupTypeDialogVisible = false,
                )
            }
        }
    }

    fun onClickDeleteSection(sectionIndex: Int) {
        _uiState.update { prev ->
            val sections = (prev.feed?.groups ?: emptyList()).toMutableList()
            sections.removeAt(sectionIndex)
            prev.copy(feed = prev.feed?.copy(groups = sections))
        }
    }

    fun onGroupMoved(fromIndex: Int, toIndex: Int) {
        _uiState.update { prev ->
            prev.copy(
                feed = prev.feed?.copy(
                    groups = prev.feed.groups?.moveItem(fromIndex, toIndex)
                )
            )
        }
    }

    fun onItemReorderSettle(
        fromItemIndex: OpdsFeedItemIndex,
        toIndex: Int,
    ) {
        _uiState.update { prev ->
            prev.copy(
                feed = when(fromItemIndex) {
                    is OpdsFeedGroupItemItemIndex -> {
                        prev.feed?.copy(
                            groups = prev.feed.groups?.toMutableList()?.also { groupList ->
                                val group = groupList[fromItemIndex.groupIndex]

                                groupList[fromItemIndex.groupIndex] = group.copy(
                                    navigation = if(group.groupType == OpdsGroupType.NAVIGATION) {
                                        group.navigation?.moveItem(fromItemIndex.index, toIndex)
                                    }else {
                                        group.navigation
                                    },
                                    publications = if(group.groupType == OpdsGroupType.PUBLICATION) {
                                        group.publications?.moveItem(fromItemIndex.index, toIndex)
                                    }else {
                                        group.publications
                                    }
                                )
                            }
                        )
                    }

                    is NavigationItemItemIndex -> {
                        prev.feed?.copy(
                            navigation = prev.feed.navigation?.moveItem(fromItemIndex.index, toIndex)
                        )
                    }

                    is PublicationItemItemIndex -> {
                        prev.feed?.copy(
                            publications = prev.feed.publications?.moveItem(fromItemIndex.index, toIndex)
                        )
                    }
                }
            )
        }
    }

    fun onClickDeleteItem(itemIndex: OpdsFeedItemIndex) {
        _uiState.update { prev ->
            prev.copy(
                feed = when (itemIndex) {
                    is OpdsFeedGroupItemItemIndex -> {
                        prev.feed?.copy(
                            groups = prev.feed.groups.orEmpty().toMutableList().also { groups ->
                                val groupType = groups[itemIndex.groupIndex].groupType
                                if(groupType == OpdsGroupType.NAVIGATION) {
                                    groups[itemIndex.groupIndex] = groups[itemIndex.groupIndex].copy(
                                        navigation = groups[itemIndex.groupIndex].navigation.orEmpty()
                                            .toMutableList().also { it.removeAt(itemIndex.index) }
                                    )
                                }else {
                                    groups[itemIndex.groupIndex] = groups[itemIndex.groupIndex].copy(
                                        publications = groups[itemIndex.groupIndex].publications.orEmpty()
                                            .toMutableList().also { it.removeAt(itemIndex.index) }
                                    )
                                }
                            }.toList()
                        )
                    }

                    is NavigationItemItemIndex -> {
                        prev.feed?.copy(
                            navigation = prev.feed.navigation.orEmpty().toMutableList().also {
                                it.removeAt(itemIndex.index)
                            }.toList()
                        )
                    }

                    is PublicationItemItemIndex -> {
                        prev.feed?.copy(
                            publications = prev.feed.publications.orEmpty().toMutableList().also {
                                it.removeAt(itemIndex.index)
                            }
                        )
                    }
                }
            )
        }
    }

    fun onClickMoveItem(sectionIndex: Int, itemIndex: Int) {
        val sections = _uiState.value.feed?.groups ?: emptyList()
        val fromSection = sections.getOrNull(sectionIndex)
            ?: throw IllegalStateException("No section at index $sectionIndex")

        val compatibleSections = sections.mapIndexedNotNull { index, section ->
            if (index == sectionIndex) return@mapIndexedNotNull null
            val isCompatible = if (fromSection.navigation != null) {
                section.navigation != null
            } else {
                section.publications != null
            }
            if (!isCompatible) return@mapIndexedNotNull null
            MovingItemState.CompatibleSection(
                sectionIndex = index,
            )
        }

        if (compatibleSections.size == 1) {
            moveItemToSection(sectionIndex, itemIndex, compatibleSections.first().sectionIndex)
        } else {
            _uiState.update {
                it.copy(
                    movingItem = MovingItemState(
                        fromSectionIndex = sectionIndex,
                        itemIndex = itemIndex,
                        compatibleSections = compatibleSections,
                    )
                )
            }
        }
    }

    fun onClickMoveItemToSection(targetSectionIndex: Int) {
        val moving = _uiState.value.movingItem
            ?: throw IllegalStateException(
                "onClickMoveItemToSection called but no item is being moved"
            )
        _uiState.update { it.copy(movingItem = null) }
        moveItemToSection(moving.fromSectionIndex, moving.itemIndex, targetSectionIndex)
    }

    fun onDismissMoveDialog() {
        _uiState.update { it.copy(movingItem = null) }
    }

    private fun moveItemToSection(sectionIndex: Int, itemIndex: Int, targetSectionIndex: Int) {
        _uiState.update { prev ->
            val sections = (prev.feed?.groups ?: emptyList()).toMutableList()
            val fromSection = sections.getOrNull(sectionIndex)
                ?: throw IllegalStateException("No section at index $sectionIndex")
            val toSection = sections.getOrNull(targetSectionIndex)
                ?: throw IllegalStateException("No section at index $targetSectionIndex")

            sections[sectionIndex] = if (fromSection.navigation != null) {
                val items = (fromSection.navigation ?: emptyList()).toMutableList()
                val item = items.removeAt(itemIndex)
                sections[targetSectionIndex] = toSection.copy(
                    navigation = (toSection.navigation ?: emptyList()) + item
                )
                fromSection.copy(navigation = items)
            } else {
                val items = (fromSection.publications ?: emptyList()).toMutableList()
                val item = items.removeAt(itemIndex)
                sections[targetSectionIndex] = toSection.copy(
                    publications = (toSection.publications ?: emptyList()) + item
                )
                fromSection.copy(publications = items)
            }
            prev.copy(feed = prev.feed?.copy(groups = sections))
        }
    }

    fun onClickAddItem(groupIndex: Int) {
        pendingAddItemGroupIndex = groupIndex
        _uiState.update { it.copy(isAddItemTypeBottomSheetVisible = true) }
    }

    fun onDismissAddItemTypeBottomSheet() {
        _uiState.update { it.copy(isAddItemTypeBottomSheetVisible = false) }
    }

    fun onClickAddItemBrowse() {
        onDismissAddItemTypeBottomSheet()

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = OpenEelAppLauncher.create(
                    resultDest = RouteResultDest(
                        resultPopUpTo = route,
                        resultKey = KEY_PUBLICATION,
                    ),
                    opdsPickType = OpdsPickType.PUBLICATION,
                ),
            )
        )
    }

    fun onClickAddItemUseLink() {
        onDismissAddItemTypeBottomSheet()
        
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = OpdsFeedEditAddLink.create(
                    resultDest = RouteResultDest(
                        resultPopUpTo = route,
                        resultKey = KEY_PUBLICATION,
                    ),
                    opdsPickType = OpdsPickType.PUBLICATION,
                ),
            )
        )
    }

    fun onClickAddPlaylist(sectionIndex: Int) {
        pendingAddItemGroupIndex = sectionIndex
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = OpenEelAppLauncher.create(
                    resultDest = RouteResultDest(
                        resultPopUpTo = route,
                        resultKey = KEY_COLLECTION,
                    ),
                    opdsPickType = OpdsPickType.CATALOG_FEED,
                ),
            )
        )
    }

    fun onClickSave() {
        val feed = _uiState.value.feed
            ?: throw IllegalStateException("onClickSave called but feed is null")
        if (feed.metadata.title.isBlank()) {
            _uiState.update {
                it.copy(
                    titleError = Res.string.required_field.asUiText()
                )
            }
            if (uiState.value.hasErrors)
                return
        }

        viewModelScope.launch {
            val actor = accountManager.selectedAccountAndPersonFlow.first()?.xapiAgent
                ?: throw IllegalStateException("onClickSave: no active account, cannot save")

            saveOpdsFeedUseCase(feed, actor)
            if(route.isNewFeedOrNewRemix) {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        destination = OpdsFeedDetail.create(
                            opdsFeedUrl = feed.requireSelfUrl(),
                        ),
                        popUpTo = route,
                        popUpToInclusive = true,
                    )
                )
            }else {
                _navCommandFlow.tryEmit(NavCommand.PopUp())
            }
        }
    }

    companion object {
        const val KEY_PUBLICATION = "result_learning_unit"
        const val KEY_COLLECTION = "result_collection"
        private const val KEY_PENDING_ADD_ITEM_GROUP_INDEX = "pending_add_item_section_index"
    }
}