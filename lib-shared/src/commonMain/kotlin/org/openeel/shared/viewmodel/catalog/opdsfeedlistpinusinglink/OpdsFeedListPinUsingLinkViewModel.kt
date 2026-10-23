package org.openeel.shared.viewmodel.catalog.opdsfeedlistpinusinglink

import androidx.lifecycle.SavedStateHandle
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.catalog.PinOpdsUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_from_a_link
import org.openeel.shared.generated.resources.could_not_load_link
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel

data class OpdsFeedListPinUsingLinkUiState(
    val linkUrlStr: String = "",
    val errorMessage: UiText? = null,
)

/**
 * On this screen the user will enter a link to an [org.openeel.lib.opds.model.OpdsFeed]. If the
 * feed is loaded successfully, then it is pinned using PinOpdsUseCase and the navigation will pop
 * back.
 */
class OpdsFeedListPinUsingLinkViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: AppAccountManager,
): OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val pinOpdsUseCase: PinOpdsUseCase by inject()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(OpdsFeedListPinUsingLinkUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.add_from_a_link.asUiText(),
                hideBottomNavigation = true,
            )
        }
    }

    fun onLinkUrlChanged(linkUrlStr: String) {
        _uiState.update {
            it.copy(
                linkUrlStr = linkUrlStr,
                errorMessage = null,
            )
        }
    }

    /**
     * Try loading an OpdsFeed using the given linkUrlStr. If successful, then pin it using
     * PinOpdsUseCase. If not, then show an error message.
     */
    fun onClickPinCollection() {
        val linkUrlStr = _uiState.value.linkUrlStr.trim()

        launchWithLoadingIndicator(
            onShowError = { errorUiText ->
                _uiState.update {
                    it.copy(errorMessage = errorUiText)
                }
            }
        ) {
            val feedResult = schoolDataSource.opdsFeedDataSource.getByUrl(
                url = Url(linkUrlStr),
                params = DataLoadParams(),
            )

            if (feedResult is DataReadyState) {
                pinOpdsUseCase(
                    opdsFeed = feedResult.data,
                    contextCategoryId = OpenEelXapiConstants.CATEGORY_COLLECTION_LISTING_RECIPE,
                )

                _navCommandFlow.tryEmit(NavCommand.PopUp())
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = Res.string.could_not_load_link.asUiText(),
                    )
                }
            }
        }
    }

}