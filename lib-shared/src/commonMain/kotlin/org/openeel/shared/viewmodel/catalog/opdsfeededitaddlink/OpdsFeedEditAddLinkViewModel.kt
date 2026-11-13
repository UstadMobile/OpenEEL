package org.openeel.shared.viewmodel.catalog.opdsfeededitaddlink

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.externallink.ExtractWebPageMetadataUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.external_link
import org.openeel.shared.generated.resources.invalid_link
import org.openeel.shared.generated.resources.required_field
import org.openeel.shared.navigation.NavResultReturner
import org.openeel.shared.navigation.OpdsFeedEditAddLink
import org.openeel.shared.navigation.sendResultIfResultExpected
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.ActionBarButtonUiState
import org.openeel.shared.viewmodel.app.appstate.Snack
import org.openeel.shared.viewmodel.app.appstate.SnackBarDispatcher
import org.openeel.shared.viewmodel.catalog.PublicationsSelection

data class OpdsFeedEditAddLinkUiState(
    val url: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val urlError: UiText? = null,
    val titleError: UiText? = null,
    val step: Step = Step.URL,
    val isLoading: Boolean = false,
) {
    enum class Step {
        URL,
        METADATA
    }
}

class OpdsFeedEditAddLinkViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: AppAccountManager,
    private val resultReturner: NavResultReturner,
    private val extractWebPageMetadataUseCase: ExtractWebPageMetadataUseCase,
    private val snackBarDispatcher: SnackBarDispatcher,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()
    private val route: OpdsFeedEditAddLink = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(OpdsFeedEditAddLinkUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.external_link.asUiText(),
                userAccountIconVisible = false,
                hideBottomNavigation = true,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = false,
                )
            )
        }
    }

    fun onUrlChanged(url: String) {
        _uiState.update {
            it.copy(
                url = url,
                urlError = null
            )
        }
    }

    /**
     * Loads the metadata for the URL that the user has entered so that the title, description, and
     * thumbnail can be shown and edited before the link is added.
     */
    fun onClickNext() {
        val urlStr = _uiState.value.url.trim()

        if (urlStr.isBlank()) {
            _uiState.update {
                it.copy(urlError = Res.string.required_field.asUiText())
            }
            return
        }

        launchWithLoadingIndicator(
            onShowError = {
                snackBarDispatcher.showSnackBar(
                    Snack(Res.string.invalid_link.asUiText())
                )
            }
        ) {
            val urlVal = Url(urlStr)

            resultReturner.sendResultIfResultExpected(
                route = route,
                navCommandFlow = _navCommandFlow,
                result = PublicationsSelection(
                    url = urlVal,
                    selectedPublications = listOf(
                        schoolDataSource.opdsPublicationDataSource.getByUrl(
                            url = urlVal,
                            params = DataLoadParams(),
                        ).dataOrNull().also {
                            println("loaded ${it?.metadata?.title}")
                        } ?: throw IllegalStateException()
                    )
                )
            )
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update {
            it.copy(
                title = title,
                titleError = null
            )
        }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update {
            it.copy(description = description)
        }
    }

}