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
import org.openeel.lib.opds.model.ext.getNavigationLinkByIndex
import org.openeel.lib.opds.model.ext.getPublicationByIndex
import org.openeel.lib.opds.model.ext.indexIsInNavigationCollection
import org.openeel.lib.opds.model.util.NavigationItemItemIndex
import org.openeel.lib.opds.model.util.OpdsFeedGroupItemItemIndex
import org.openeel.lib.opds.model.util.OpdsFeedItemIndex
import org.openeel.lib.opds.model.util.PublicationItemItemIndex
import org.openeel.libutil.ext.moveItem
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.catalog.saveopdsfeed.SaveOpdsFeedUseCase
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
import org.openeel.shared.util.ext.addFeedSelection
import org.openeel.shared.util.ext.addPublicationSelection
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.util.ext.groupType
import org.openeel.shared.util.ext.isNewFeedOrNewRemix
import org.openeel.shared.util.ext.removeItemAt
import org.openeel.shared.util.ext.resolve
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

/**
 * A destination where the [OpdsFeedEditUiState.movingItem] can be moved to.
 *
 * @param groupIndex a number above zero when representing a group, -1 when representing the
 *        top level collections on a feed.
 * @param group the [OpdsGroup] if the destination represents a group.
 */
data class MoveItemDestination(
    val groupIndex: Int,
    val group: OpdsGroup?,
)

data class OpdsFeedEditUiState(
    val feed: OpdsFeed? = null,
    val addGroupTypeDialogVisible: Boolean = false,
    val titleError: UiText? = null,
    val movingItem: OpdsFeedItemIndex? = null,
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
        get() = (feed?.groups?.count { it.groupType == OpdsGroupType.PUBLICATION } ?: 0) >= 1

    val canMoveNavigationItemToOtherGroup: Boolean
        get() = (feed?.groups?.count { it.groupType == OpdsGroupType.NAVIGATION } ?: 0) >= 1

    /**
     * A list of destinations where the [OpdsFeedEditUiState.movingItem] can be moved to. It will
     * include only [OpdsGroup]s where the group type matches the item type being moved. It will
     * not include a [MoveItemDestination] for the where the item is currently located.
     */
    val movingItemDestinations: List<MoveItemDestination>? by lazy {
        val movingItemInGroupNum = (movingItem as? OpdsFeedGroupItemItemIndex)?.groupIndex
        val topLevelOption = if(movingItemInGroupNum != null) {
            listOf(MoveItemDestination(OpdsFeedItemIndex.NO_GROUP, null))
        }else {
            emptyList()
        }

        when {
            feed == null || movingItem == null -> null

            feed.indexIsInNavigationCollection(movingItem) -> {
                topLevelOption + feed.groups.orEmpty().mapIndexedNotNull { index, group ->
                    if(index != movingItemInGroupNum &&
                        group.groupType == OpdsGroupType.NAVIGATION
                    ){
                        MoveItemDestination(index, group)
                    }else {
                        null
                    }
                }
            }

            else -> {
                topLevelOption + feed.groups.orEmpty().mapIndexedNotNull { index, group ->
                    if(index != movingItemInGroupNum &&
                        group.groupType == OpdsGroupType.PUBLICATION
                    ) {
                        MoveItemDestination(index, group)
                    }else {
                        null
                    }
                }
            }
        }
    }

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
                            feed = prev.feed?.addPublicationSelection(
                                selection = publicationSelection,
                                toGroupIndex = groupIndex,
                            )
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
                            feed = prev.feed?.addFeedSelection(
                                selection = feedSelection,
                                toGroupIndex = groupIndex,
                            )
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
                feed = prev.feed?.removeItemAt(itemIndex)
            )
        }
    }

    fun onClickMoveItem(index: OpdsFeedItemIndex) {
        _uiState.update {
            it.copy(movingItem = index)
        }
    }

    fun onClickMoveItemToGroup(destination: MoveItemDestination) {
        val fromIndex = _uiState.value.movingItem ?: return
        val feed = uiState.value.feed ?: return
        val isNavigationItem = feed.indexIsInNavigationCollection(fromIndex)

        if(isNavigationItem) {
            _uiState.update { prev ->
                val item = prev.feed?.getNavigationLinkByIndex(fromIndex) ?: return@update prev
                prev.copy(
                    feed = prev.feed.removeItemAt(fromIndex)
                        .addFeedSelection(
                            OpdsFeedSelection(
                                url = prev.feed.requireSelfUrl(),
                                selectedFeeds = listOf(item)
                            ),
                            toGroupIndex = destination.groupIndex,
                        ),
                    movingItem = null,
                )
            }
        }else {
            _uiState.update { prev ->
                val item = prev.feed?.getPublicationByIndex(fromIndex) ?: return@update prev
                prev.copy(
                    feed = prev.feed.removeItemAt(fromIndex)
                        .addPublicationSelection(
                            PublicationsSelection(
                                url = prev.feed.requireSelfUrl(),
                                selectedPublications = listOf(item),
                            ),
                            toGroupIndex = destination.groupIndex
                        ),
                    movingItem = null,
                )
            }
        }
    }

    fun onDismissMoveDialog() {
        _uiState.update { it.copy(movingItem = null) }
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

    fun onClickAddCollection(sectionIndex: Int) {
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