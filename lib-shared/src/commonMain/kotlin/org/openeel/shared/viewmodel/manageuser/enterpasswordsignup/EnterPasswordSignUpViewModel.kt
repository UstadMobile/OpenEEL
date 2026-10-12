package org.openeel.shared.viewmodel.manageuser.enterpasswordsignup

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.credentials.passkey.OpenEelPasswordCredential
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.navigation.onaccountcreated.NavigateOnAccountCreatedUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.create_account
import org.openeel.shared.generated.resources.required_field
import org.openeel.shared.navigation.EnterPasswordSignup
import org.openeel.shared.resources.StringResourceUiText
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.di.SchoolDirectoryEntryScopeId
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel

data class EnterPasswordSignupUiState(
    val password: String = "",
    val passwordError: UiText? = null,
    val generalError: UiText? = null,
)

class EnterPasswordSignupViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: AppAccountManager,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {
    private val route: EnterPasswordSignup = savedStateHandle.toRoute()


    override val scope: Scope
        get() = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            SchoolDirectoryEntryScopeId(route.schoolUrl, null).scopeId
        )

    private val navigateOnAccountCreatedUseCase: NavigateOnAccountCreatedUseCase by inject()

    private val _uiState = MutableStateFlow(EnterPasswordSignupUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.create_account.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }
    }

    fun onPasswordChanged(newValue: String) {
        _uiState.update {
            it.copy(
                password = newValue,
                passwordError = null,
                generalError = null
            )
        }
    }

    fun onClickSignup() {
        val password = _uiState.value.password

        _uiState.update {
            it.copy(
                passwordError = if (password.isBlank())
                    StringResourceUiText(Res.string.required_field)
                else
                    null
            )
        }

        if (password.isBlank())
            return

        launchWithLoadingIndicator(
            onShowError = { errMsg ->
                _uiState.update {
                    it.copy(
                        generalError = errMsg,
                    )
                }
            }
        ) {
            val redeemRequest = route.redeemInviteRequest.copy(
                account = route.redeemInviteRequest.account.copy(
                    credential = OpenEelPasswordCredential(
                        username = route.redeemInviteRequest.account.username,
                        password = password,
                    )
                )
            )

            val personRegistered = accountManager.register(
                redeemInviteRequest = redeemRequest,
                schoolUrl = route.schoolUrl,
            )

            navigateOnAccountCreatedUseCase(
                personRegistered = personRegistered,
                navCommandFlow = _navCommandFlow,
                inviteRequest = redeemRequest,
            )
        }
    }
}