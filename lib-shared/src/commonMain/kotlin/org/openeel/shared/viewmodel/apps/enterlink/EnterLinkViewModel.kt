package org.openeel.shared.viewmodel.apps.enterlink

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.parseUrl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.school.opds.OpdsFeedDataSource
import org.openeel.lib.dataloadstate.DataErrorResult
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.libutil.ext.appendEndpointSegments
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.school.LaunchSchoolSelfRegistrationUseCase
import org.openeel.shared.domain.sharelink.CreatePlaylistShareLinkUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.enter_link
import org.openeel.shared.generated.resources.invalid_url
import org.openeel.shared.navigation.AppsDetail
import org.openeel.shared.navigation.EnterLink
import org.openeel.shared.navigation.OpdsFeedDetail
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.resources.StringResourceUiText
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel

data class EnterLinkUiState(
    val linkUrl: String = "",
    val errorMessage: UiText? = null,
)

class EnterLinkViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AppAccountManager,
    private val launchSchoolSelfRegistrationUseCase: LaunchSchoolSelfRegistrationUseCase,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    private val route = savedStateHandle.toRoute<EnterLink>()

    override val scope: Scope by lazy { accountManager.requireActiveAccountScope() }

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(EnterLinkUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.enter_link.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = if (route.mode == EnterLinkModeEnum.HOST) false else it.userAccountIconVisible,
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
        if (route.mode == EnterLinkModeEnum.HOST) {
            val hostUrl = parseHostUrlOrNull(uiState.value.linkUrl) ?: run {
                _uiState.update {
                    it.copy(errorMessage = StringResourceUiText(Res.string.invalid_url))
                }
                return
            }

            launchWithLoadingIndicator(
                onShowError = { errorMessage ->
                    _uiState.update { it.copy(errorMessage = errorMessage) }
                }
            ) {
                launchSchoolSelfRegistrationUseCase(hostUrl)
            }
            return
        }

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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Napier.w(message = "EnterLinkViewModel: could not open link", throwable = e)
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

    companion object {

        fun parseHostUrlOrNull(link: String): Url? {
            val trimmed = link.trim().takeIf { it.isNotEmpty() } ?: return null
            val withProtocol = trimmed.takeIf { it.contains("://") } ?: "https://$trimmed"

            return parseUrl(withProtocol)?.takeIf { url ->
                url.protocol in listOf(URLProtocol.HTTP, URLProtocol.HTTPS) &&
                        url.host.isNotBlank()
            }
        }
    }
}