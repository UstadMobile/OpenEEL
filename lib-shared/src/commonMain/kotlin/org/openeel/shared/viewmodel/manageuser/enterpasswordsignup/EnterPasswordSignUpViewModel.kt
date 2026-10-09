package world.respect.shared.viewmodel.manageuser.enterpasswordsignup

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.navigation.onaccountcreated.NavigateOnAccountCreatedUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.create_account
import world.respect.shared.generated.resources.required_field
import world.respect.shared.navigation.EnterPasswordSignup
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.UiText
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class EnterPasswordSignupUiState(
    val password: String = "",
    val passwordError: UiText? = null,
    val generalError: UiText? = null,
)

class EnterPasswordSignupViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
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
            val redeemRequest = route.respectRedeemInviteRequest.copy(
                account = route.respectRedeemInviteRequest.account.copy(
                    credential = RespectPasswordCredential(
                        username = route.respectRedeemInviteRequest.account.username,
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