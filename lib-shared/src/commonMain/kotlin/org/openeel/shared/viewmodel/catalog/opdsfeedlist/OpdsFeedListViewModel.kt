package world.respect.shared.viewmodel.catalog.opdsfeedlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.getScopeId
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.isAdmin
import world.respect.datalayer.school.domain.MakePlaylistOpdsFeedUseCase
import world.respect.datalayer.school.opds.ext.requireSelfUrl
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.opds.model.OpdsFeed
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.ext.resultExpected
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_from_a_link
import world.respect.shared.generated.resources.add_new
import world.respect.shared.generated.resources.home
import world.respect.shared.generated.resources.collection
import world.respect.shared.navigation.EnterLink
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.OpdsFeedDetail
import world.respect.shared.navigation.OpdsFeedEdit
import world.respect.shared.navigation.PlaylistList
import world.respect.shared.util.di.RespectAccountScopeId
import world.respect.shared.util.ext.appbarTitleString
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ExpandableFabIcon
import world.respect.shared.viewmodel.app.appstate.ExpandableFabItem
import world.respect.shared.viewmodel.app.appstate.ExpandableFabUiState

enum class OpdsFeedListFilter {
    ALL,
    MY_PLAYLISTS,
}

data class OpdsFeedListUiState(
    val playlists: List<OpdsFeed> = emptyList(),
    val activeFilter: OpdsFeedListFilter = OpdsFeedListFilter.ALL,
    val isTeacherOrAdmin: Boolean = false,
    val activeUserOwnerHref: String = "",
    val activeUsername: String = "",
) {
    val showPlaylists: List<OpdsFeed>
        get() = when (activeFilter) {
            OpdsFeedListFilter.ALL -> playlists
            OpdsFeedListFilter.MY_PLAYLISTS -> playlists.filter { feed ->
                feed.links.any { link ->
                    link.rel?.contains(MakePlaylistOpdsFeedUseCase.REL_OWNER) == true
                            && link.href == activeUserOwnerHref
                }
            }
        }

}

class OpdsFeedListViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(OpdsFeedListUiState())

    val uiState = _uiState.asStateFlow()

    private val route: PlaylistList = savedStateHandle.toRoute()

    init {
        _appUiState.update {
            it.copy(
                title = route.opdsPickType?.appbarTitleString?.asUiText() ?: Res.string.home.asUiText(),
                hideBottomNavigation = route.resultExpected,
            )
        }

        val schoolUrl = RespectAccountScopeId.parse(scope.getScopeId()).schoolUrl

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
            /*
             * Disabled - to be replaced with using statements per recipe
            schoolDataSource.opdsFeedDataSource.getPlaylistsAsFlow(
                schoolUrl = schoolUrl,
            ).collect { result ->
                when (result) {
                    is DataReadyState -> _uiState.update { it.copy(playlists = result.data) }
                    else -> {}
                }
            }
            */
        }
    }

    fun onClickFilter(filter: OpdsFeedListFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun onClickPlaylist(feed: OpdsFeed) {
        val playlistUrl = feed.requireSelfUrl()

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                OpdsFeedDetail.create(
                    opdsFeedUrl = playlistUrl,
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