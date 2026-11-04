package org.openeel.shared.viewmodel.manageuser.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import org.openeel.credentials.passkey.CheckPasskeySupportUseCase
import org.openeel.credentials.passkey.GetCredentialUseCase
import org.openeel.credentials.passkey.RespectPasskeyCredential
import org.openeel.credentials.passkey.RespectPasswordCredential
import org.openeel.credentials.passkey.password.SavePasswordUseCase
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.datalayer.RespectAppDataSource
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.datalayer.school.model.PersonStatusEnum
import org.openeel.lib.dataloadstate.throwable.unwrapHttpStatusCode
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.domain.account.username.filterusername.FilterUsernameUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.login
import org.openeel.shared.generated.resources.required_field
import org.openeel.shared.generated.resources.something_went_wrong
import org.openeel.shared.generated.resources.invalid_username_password
import org.openeel.shared.navigation.EnterInviteCode
import org.openeel.shared.navigation.Home
import org.openeel.shared.navigation.LoginScreen
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.WaitingForApproval
import org.openeel.shared.resources.StringResourceUiText
import org.openeel.shared.resources.StringUiText
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.di.SchoolDirectoryEntryScopeId
import org.openeel.shared.util.exception.getUiText
import org.openeel.shared.util.exception.getUiTextOrGeneric
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.RespectViewModel

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val errorText: UiText? = null,
    val usernameError: StringResourceUiText? = null,
    val passwordError: StringResourceUiText? = null,
    val schoolUrl: Url,
)

class LoginViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    getCredentialUseCase: GetCredentialUseCase,
    respectAppDataSource: RespectAppDataSource,
    private val filterUsernameUseCase: FilterUsernameUseCase,
    private val savePasswordUseCase: SavePasswordUseCase
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    private val route: LoginScreen = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(LoginUiState(schoolUrl = route.schoolUrl))

    val uiState = _uiState.asStateFlow()

    override val scope: Scope
        get() = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            SchoolDirectoryEntryScopeId(route.schoolUrl, null).scopeId
        )

    private val checkPasskeySupportUseCase: CheckPasskeySupportUseCase = scope.get()

    //Short-term internal variable used so that we can avoid showing a save password prompt if/when
    //the user just used their saved password
    private var usingSavedPassword = false

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    title = Res.string.login.asUiText(),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false
                )
            }
        }
        viewModelScope.launch {
            try {
                val school = respectAppDataSource.schoolDirectoryEntryDataSource
                    .getSchoolDirectoryEntryByUrl(route.schoolUrl)
                val rpId: String? = when (school) {
                    is DataReadyState -> school.data.rpId
                    else -> null
                }

                val isPasskeySupported = checkPasskeySupportUseCase()

                if (isPasskeySupported){
                    when (val credentialResult = getCredentialUseCase(rpId?:"")) {
                        is GetCredentialUseCase.PasskeyCredentialResult -> {
                            val authResponse = accountManager.login(
                                RespectPasskeyCredential(
                                    passkeyWebAuthNResponse = credentialResult.passkeyWebAuthNResponse
                                ),
                                schoolUrl = route.schoolUrl,
                            )

                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(
                                    destination = if(authResponse.person.status == PersonStatusEnum.PENDING_APPROVAL) {
                                        WaitingForApproval()
                                    }else {
                                        Home
                                    },
                                    clearBackStack = true
                                )
                            )
                        }

                        is GetCredentialUseCase.PasswordCredentialResult -> {
                            onUsernameChanged(credentialResult.credentialUsername)
                            onPasswordChanged(credentialResult.password)

                            usingSavedPassword = true
                            onClickLogin()
                        }

                        is GetCredentialUseCase.Error -> {
                            _uiState.update { prev ->
                                prev.copy(
                                    errorText = StringUiText(credentialResult.message ?: ""),
                                )
                            }
                        }

                        is GetCredentialUseCase.NoCredentialAvailableResult,
                        is GetCredentialUseCase.UserCanceledResult -> {
                            //do nothing
                        }

                    }

                }
            } catch (t: Throwable) {
                Napier.w("LoginViewModel: Exception logging in", t)
                _uiState.update { prev ->
                    prev.copy(
                        errorText = t.getUiText() ?: StringResourceUiText(Res.string.something_went_wrong)
                    )
                }
            }
        }
    }

    fun onUsernameChanged(userId: String) {
        usingSavedPassword = false

        val filteredValue = filterUsernameUseCase(
            username = userId,
            invalidCharReplacement = ""
        )

        _uiState.update {
            it.copy(
                username = filteredValue,
                usernameError = null
            )
        }
    }

    fun onPasswordChanged(password: String) {
        usingSavedPassword = false
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null
            )
        }
    }

    fun onClickLogin() {
        launchWithLoadingIndicator {
            val username = uiState.value.username
            val password = uiState.value.password

            _uiState.update {
                it.copy(
                    usernameError = if (username.isEmpty())
                        StringResourceUiText(Res.string.required_field)
                    else
                        null,
                    passwordError = if (password.isEmpty())
                        StringResourceUiText(Res.string.required_field)
                    else
                        null,
                    errorText = null,
                )
            }

            if (uiState.value.usernameError != null || uiState.value.passwordError != null) {
                return@launchWithLoadingIndicator
            }


            try {
                val authResponse = accountManager.login(
                    credential = RespectPasswordCredential(username.trim(), password.trim()),
                    schoolUrl = route.schoolUrl
                )

               if (!usingSavedPassword){
                   savePasswordUseCase(
                       username = username,
                       password = password
                   )
               }

                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        destination = if(authResponse.person.status == PersonStatusEnum.PENDING_APPROVAL) {
                            WaitingForApproval()
                        }else {
                            Home
                        },
                        clearBackStack = true,
                    )
                )
            }catch(e: Exception) {
                _uiState.update { prev ->
                    prev.copy(
                        errorText = if(e.unwrapHttpStatusCode() == HttpStatusCode.Forbidden.value) {
                            Res.string.invalid_username_password.asUiText()
                        }else {
                            e.getUiTextOrGeneric()
                        }
                    )
                }
            }
        }
    }

    fun onClickInviteCode() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(EnterInviteCode.create(route.schoolUrl))
        )
    }

}
