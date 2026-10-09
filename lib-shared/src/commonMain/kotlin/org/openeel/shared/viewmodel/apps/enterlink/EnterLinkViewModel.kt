package world.respect.shared.viewmodel.apps.enterlink

import androidx.lifecycle.SavedStateHandle
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.opds.OpdsFeedDataSource
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.sharelink.CreatePlaylistShareLinkUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.enter_link
import world.respect.shared.generated.resources.invalid_url
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.navigation.OpdsFeedDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class EnterLinkUiState(
    val linkUrl: String = "",
    val errorMessage: UiText? = null,
)

class EnterLinkViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(EnterLinkUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.enter_link.asUiText(),
                hideBottomNavigation = true,
            )
        }
    }

    fun onLinkChanged(link: String) {
        _uiState.update {
            it.copy(
                linkUrl = link,
                errorMessage = null,
            )
        }
    }
    fun onClickNext() {
        launchWithLoadingIndicator {
            try {
                val linkUrl = Url(uiState.value.linkUrl)

                val playlistUrl = playlistUrlFromShareLinkOrNull(linkUrl)
                if (playlistUrl != null) {
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            OpdsFeedDetail.create(opdsFeedUrl = playlistUrl)
                        )
                    )
                    return@launchWithLoadingIndicator
                }

                val appResult = schoolDataSource.opdsPublicationDataSource.getByUrl(
                    url = linkUrl,
                    params = DataLoadParams(),
                    referrerUrl = null,
                    expectedPublicationId = null,
                )

                if (appResult is DataReadyState) {
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            AppsDetail.create(linkUrl)
                        )
                    )
                } else {
                    throw (appResult as? DataErrorResult)?.error ?: IllegalStateException()
                }
            } catch (_: Throwable) {
                _uiState.update {
                    it.copy(
                        errorMessage = StringResourceUiText(Res.string.invalid_url)
                    )
                }
            }
        }
    }
    /**
     * @return the OPDS feed url for the playlist that the given share link refers to, or null if
     *         the given url is not a playlist share link.
     */
    private fun playlistUrlFromShareLinkOrNull(url: Url): Url? {
        val shareLink = CreatePlaylistShareLinkUseCase.parseOrNull(url) ?: return null

        return shareLink.schoolUrl.appendEndpointSegments(
            OpdsFeedDataSource.PLAYLIST_ENDPOINT_NAME,
            shareLink.playlistUuid,
        )
    }
}