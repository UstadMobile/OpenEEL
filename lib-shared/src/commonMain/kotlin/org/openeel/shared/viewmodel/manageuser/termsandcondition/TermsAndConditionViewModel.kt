package org.openeel.shared.viewmodel.manageuser.termsandcondition

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openeel.libutil.ext.appendEndpointSegments
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.terms_and_conditions
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.SignupScreen
import org.openeel.shared.navigation.TermsAndCondition
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel

data class TermsAndConditionUiState(
    val termsAndConditionsUrl: Url,
    val isLoading: Boolean = true
)

class TermsAndConditionViewModel(
    savedStateHandle: SavedStateHandle,
) : OpenEelViewModel(savedStateHandle) {
    private val route: TermsAndCondition = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(
        TermsAndConditionUiState(
            termsAndConditionsUrl = route.schoolUrl.appendEndpointSegments(TERMS_PATH),
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update {
                it.copy(
                    title = Res.string.terms_and_conditions.asUiText(),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false
                )
            }
        }
    }

    fun onAcceptClicked() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                SignupScreen.create(
                    schoolUrl = route.schoolUrl,
                    inviteRequest = route.redeemInviteRequest
                )
            )
        )
    }

    companion object {

        const val TERMS_PATH = ".well-known/terms.html"

    }
}
