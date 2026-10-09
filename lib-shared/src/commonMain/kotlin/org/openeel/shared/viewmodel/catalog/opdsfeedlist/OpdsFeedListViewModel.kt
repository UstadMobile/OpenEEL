package org.openeel.shared.viewmodel.catalog.opdsfeedlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.db.school.ext.isAdmin
import org.openeel.datalayer.school.domain.MakePlaylistOpdsFeedUseCase
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.lib.xapi.ext.distinctByMostRecentTimestampForActivityId
import org.openeel.lib.xapi.ext.idAsStringOrNull
import org.openeel.lib.xapi.ext.objectActivityOrNull
import org.openeel.lib.xapi.ext.opdsCollectionLinkAsUrlOrNull
import org.openeel.lib.xapi.ext.opdsCollectionLinkOrNull
import org.openeel.lib.xapi.model.XapiActivity
import org.openeel.lib.xapi.model.XapiContext
import org.openeel.lib.xapi.model.XapiContextActivities
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.model.XapiStatementRef
import org.openeel.lib.xapi.model.XapiVerb
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.xapi.createPinCollectionStatement
import org.openeel.shared.ext.resultExpected
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_from_a_link
import org.openeel.shared.generated.resources.add_new
import org.openeel.shared.generated.resources.home
import org.openeel.shared.generated.resources.collection
import org.openeel.shared.generated.resources.something_went_wrong
import org.openeel.shared.generated.resources.undo
import org.openeel.shared.generated.resources.unpinned_1_collection
import org.openeel.shared.navigation.EnterLink
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.OpdsFeedDetail
import org.openeel.shared.navigation.OpdsFeedEdit
import org.openeel.shared.navigation.PlaylistList
import org.openeel.shared.util.ext.appbarTitleString
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.ExpandableFabIcon
import org.openeel.shared.viewmodel.app.appstate.ExpandableFabItem
import org.openeel.shared.viewmodel.app.appstate.ExpandableFabUiState
import org.openeel.shared.viewmodel.app.appstate.Snack
import org.openeel.shared.viewmodel.app.appstate.SnackBarDispatcher
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * @param statements the most recent pin-collection statement for each pinned collection as per the
 *        Collections Listing recipe (README_COLLECTIONS_LISTING_RECIPE.md), with unpinned
 *        collections filtered out.
 * @param feedFlowForStatement loads the OPDS collection linked by the statement's
 *        opds-collection-link extension via the xAPI Activity Profile Resource
 *        (README_OPDS_COLLECTIONS_ACTIVITY_PROFILE.md), used to show the section/item count.
 */

data class OpdsFeedListUiState(
    val statements: List<XapiStatement> = emptyList(),
    val feedFlowForStatement: (XapiStatement) -> Flow<DataLoadState<OpdsFeed>> = {
        flowOf(DataLoadingState())
    },
    val isTeacherOrAdmin: Boolean = false,
    val activeUserOwnerHref: String = "",
    val activeUsername: String = "",

)

class OpdsFeedListViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: AppAccountManager,
    private val snackBarDispatcher: SnackBarDispatcher,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(OpdsFeedListUiState())

    val uiState = _uiState.asStateFlow()

    private val route: PlaylistList = savedStateHandle.toRoute()

    private val pinStatements = MutableStateFlow<List<XapiStatement>>(emptyList())

    private val unpinnedCollections = MutableStateFlow<Map<String, Instant>>(emptyMap())

    init {
        _appUiState.update {
            it.copy(
                title = route.opdsPickType?.appbarTitleString?.asUiText() ?: Res.string.home.asUiText(),
                hideBottomNavigation = route.resultExpected,
            )
        }

        _uiState.update {
            it.copy(
                feedFlowForStatement = { statement ->
                    statement.objectActivityOrNull()?.definition?.opdsCollectionLinkAsUrlOrNull()
                        ?.let { opdsCollectionLink ->
                            schoolDataSource.opdsFeedDataSource.getByUrlAsFlow(
                                url = opdsCollectionLink,
                                params = DataLoadParams(),
                            )
                        } ?: flowOf(NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND))
                }
            )
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { sessionAndPerson ->
                val isTeacherOrAdmin = sessionAndPerson?.person?.isAdmin() == true

                val username = sessionAndPerson?.person?.username.orEmpty()
                val activeUserOwnerHref = sessionAndPerson?.let {
                    MakePlaylistOpdsFeedUseCase.getUserProfileUrl(
                        schoolUrl = it.session.account.school.self,
                        username = username,
                    )
                }.orEmpty()

                _uiState.update {
                    it.copy(
                        isTeacherOrAdmin = isTeacherOrAdmin,
                        activeUserOwnerHref = activeUserOwnerHref,
                        activeUsername = username,
                    )
                }

                _appUiState.update {
                    it.copy(
                        title = Res.string.home.asUiText(),
                        expandableFabState = ExpandableFabUiState(
                            visible = isTeacherOrAdmin && !route.resultExpected,
                            text = Res.string.collection.asUiText(),
                            items = listOf(
                                ExpandableFabItem(
                                    icon = ExpandableFabIcon.ADD,
                                    text = Res.string.add_new.asUiText(),
                                    onClick = ::onClickAddNew,
                                ),
                                ExpandableFabItem(
                                    icon = ExpandableFabIcon.LINK,
                                    text = Res.string.add_from_a_link.asUiText(),
                                    onClick = ::onClickAddFromLink,
                                ),
                            )
                        )
                    )
                }
            }
        }

        viewModelScope.launch {

        /** Pinned collections are retrieved using pin-collection statements for the
        * collection-listing recipe category, including related activities.
        *
        * Since statements are immutable, saving or editing a collection creates a new
        * statement. We keep only the latest statement per collection activity so edits
        * update the existing collection instead of creating duplicates.
        */

            schoolDataSource.xapiResource.statements.getAsFlow(
                listParams = XapiStatementsResource.GetStatementParams(
                    verb = XapiVerb.ID_PIN_COLLECTION,
                    activity = OpenEelXapiConstants.CATEGORY_COLLECTION_LISTING_RECIPE,
                    relatedActivities = true,
                ),
                dataLoadParams = DataLoadParams(),
            ).collect { state ->
                pinStatements.value = state.dataOrNull()?.statements
                    ?.filter { statement -> statement.id != null }
                    ?: emptyList()
            }
        }

        viewModelScope.launch {
            combine(pinStatements, unpinnedCollections) { allPinStatements, unpinned ->
                allPinStatements.distinctByMostRecentTimestampForActivityId().filterNot { statement ->
                    val unpinnedTime = unpinned[statement.`object`.idAsStringOrNull()]
                    unpinnedTime != null && (statement.timestamp ?: Instant.DISTANT_PAST) <= unpinnedTime
                }.filter { statement ->
                    val activity = statement.objectActivityOrNull()
                    val isValid = activity?.id != null &&
                            activity.definition?.opdsCollectionLinkAsUrlOrNull() != null

                    if (!isValid) {
                        Napier.w("OpdsFeedListViewModel: skipping pin-collection statement " +
                                    "${statement.id}: missing activity id or opds-collection-link")
                    }

                    isValid
                }
            }.distinctUntilChanged().collect { visibleStatements ->
                _uiState.update { it.copy(statements = visibleStatements) }
            }
        }
    }


    fun onClickUnpinCollection(statement: XapiStatement) {
        val collectionActivityId = statement.`object`.idAsStringOrNull()

        if (collectionActivityId == null) {
            Napier.w("OpdsFeedListViewModel: cannot unpin statement ${statement.id}: no activity id")
            snackBarDispatcher.showSnackBar(
                Snack(message = Res.string.something_went_wrong.asUiText())
            )
            return
        }

        unpinnedCollections.update { it + (collectionActivityId to Clock.System.now()) }

        viewModelScope.launch {
            try {
                val actor = accountManager.selectedAccountAndPersonFlow.first()?.xapiAgent
                    ?: throw IllegalStateException("Cannot unpin collection: no active account")

                val pinStatementIds = pinStatements.value.filter {
                    it.`object`.idAsStringOrNull() == collectionActivityId
                }.mapNotNull { it.id?.toString() }

                if (pinStatementIds.isNotEmpty()) {
                    schoolDataSource.xapiResource.statements.post(
                        pinStatementIds.map { pinStatementId ->
                            XapiStatement(
                                actor = actor,
                                verb = XapiVerb(id = XapiVerb.ID_VOIDED),
                                `object` = XapiStatementRef(id = pinStatementId),
                                context = XapiContext(
                                    contextActivities = XapiContextActivities(
                                        category = listOf(
                                            XapiActivity(
                                                id = OpenEelXapiConstants.CATEGORY_COLLECTION_LISTING_RECIPE,
                                            )
                                        )
                                    )
                                )
                            )
                        }
                    )
                }

                snackBarDispatcher.showSnackBar(
                    Snack(
                        message = Res.string.unpinned_1_collection.asUiText(),
                        action = Res.string.undo.asUiText(),
                        onAction = { restoreCollectionPin(statement, collectionActivityId) },
                    )
                )
            } catch (e: Exception) {
                Napier.e("Could not unpin collection $collectionActivityId", e)

                unpinnedCollections.update { it - collectionActivityId }

                snackBarDispatcher.showSnackBar(
                    Snack(message = Res.string.something_went_wrong.asUiText())
                )
            }
        }
    }

    private fun restoreCollectionPin(
        statement: XapiStatement,
        collectionActivityId: String,
    ) {
        val definition = statement.objectActivityOrNull()?.definition

        viewModelScope.launch {
            try {
                val actor = accountManager.selectedAccountAndPersonFlow.first()?.xapiAgent
                    ?: throw IllegalStateException("Cannot restore collection pin: no active account")
                val opdsCollectionLink = definition?.opdsCollectionLinkOrNull()
                    ?: throw IllegalStateException("Cannot restore collection pin: no opds-collection-link")

                schoolDataSource.xapiResource.statements.post(
                    listOf(
                        createPinCollectionStatement(
                            collectionActivityId = collectionActivityId,
                            collectionName = definition.name.orEmpty(),
                            collectionDescription = definition.description,
                            opdsCollectionLink = opdsCollectionLink,
                            actor = actor,
                        )
                    )
                )

                unpinnedCollections.update { it - collectionActivityId }

            } catch (e: Exception) {

                Napier.e("Could not restore pin for collection $collectionActivityId", e)

                snackBarDispatcher.showSnackBar(
                    Snack(message = Res.string.something_went_wrong.asUiText())
                )
            }
        }
    }

    fun onClickCollection(statement: XapiStatement) {
        val opdsCollectionLink = statement.objectActivityOrNull()?.definition
            ?.opdsCollectionLinkAsUrlOrNull()

        if (opdsCollectionLink == null) {
            Napier.w("OpdsFeedListViewModel: cannot open statement ${statement.id}: no opds-collection-link")
            snackBarDispatcher.showSnackBar(
                Snack(message = Res.string.something_went_wrong.asUiText())
            )
            return
        }

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                OpdsFeedDetail.create(
                    opdsFeedUrl = opdsCollectionLink,
                    resultDest = route.resultDest,
                    opdsPickType = route.opdsPickType,
                )
            )
        )
    }

    fun onClickAddNew() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(OpdsFeedEdit.create()))
    }

    fun onClickAddFromLink() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(EnterLink))
    }
}