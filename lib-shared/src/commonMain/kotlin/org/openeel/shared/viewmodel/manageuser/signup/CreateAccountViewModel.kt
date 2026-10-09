package world.respect.shared.viewmodel.manageuser.signup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.credentials.passkey.CheckPasskeySupportUseCase
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.credentials.passkey.RespectPasskeyCredential
import world.respect.datalayer.RespectAppDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.domain.navigation.onaccountcreated.NavigateOnAccountCreatedUseCase
import world.respect.shared.domain.account.username.UsernameSuggestionUseCase
import world.respect.shared.domain.account.username.checkusernameunique.CheckUsernameUniqueUseCase
import world.respect.shared.domain.account.username.filterusername.FilterUsernameUseCase
import world.respect.shared.domain.account.username.validateusername.ValidateUsernameUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.create_account
import world.respect.shared.generated.resources.passkey_not_supported
import world.respect.shared.generated.resources.something_went_wrong
import world.respect.shared.generated.resources.username_already_taken
import world.respect.shared.navigation.CreateAccount
import world.respect.shared.navigation.EnterPasswordSignup
import world.respect.shared.navigation.HowPasskeyWorks
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.OtherOptionsSignup
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.UiText
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.shared.util.exception.getUiTextOrGeneric
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.LoadingUiState

data class CreateAccountViewModelUiState(
    val username: String = "",
    val usernameError: UiText? = null,
    val generalError: UiText? = null,
    val signupError: UiText? = null,
    val inviteInfo: RespectInviteInfo? = null,
    val passkeySupported : Boolean = false,
    val fieldsEnabled: Boolean = true,
)

class CreateAccountViewModel(
    savedStateHandle: SavedStateHandle,
    private val respectAppDataSource: RespectAppDataSource,
    private val accountManager: RespectAccountManager,
    private val filterUsernameUseCase: FilterUsernameUseCase,
    private val validateUsernameUseCase: ValidateUsernameUseCase,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    private val route: CreateAccount = savedStateHandle.toRoute()

    override val scope: Scope
        get() = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            SchoolDirectoryEntryScopeId(route.schoolUrl, null).scopeId
        )

    private val checkPasskeySupportUseCase: CheckPasskeySupportUseCase by lazy {
        scope.get()
    }

    private val navigateOnAccountCreatedUseCase: NavigateOnAccountCreatedUseCase by inject()

    private val createPasskeyUseCase: CreatePasskeyUseCase? = scope.getOrNull()

    private val inviteInfoUseCase: GetInviteInfoUseCase by inject()
    private val usernameSuggestionUseCase: UsernameSuggestionUseCase by inject()

    private val checkUsernameUniqueUseCase: CheckUsernameUniqueUseCase by inject()


    private val _uiState = MutableStateFlow(CreateAccountViewModelUiState())

    val uiState = _uiState.asStateFlow()

    private val passkeySupported = CompletableDeferred<Boolean>()

    private val schoolDirectoryEntry = CompletableDeferred<SchoolDirectoryEntry>()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.create_account.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }

        viewModelScope.launch {
            try {
                val suggestion = usernameSuggestionUseCase.invoke(
                    name = route.respectRedeemInviteRequest.accountPersonInfo.name
                )

                onUsernameChanged(suggestion)
            }catch(t: Throwable) {
                Napier.w("Failed to get username suggestion", t)
            }

            val inviteInfo = inviteInfoUseCase(route.respectRedeemInviteRequest.code)
            respectAppDataSource.schoolDirectoryEntryDataSource
                .getSchoolDirectoryEntryByUrl(route.schoolUrl).dataOrNull()?.also {
                    schoolDirectoryEntry.complete(it)
                } ?: throw IllegalStateException()

            val passkeySupportedVal = createPasskeyUseCase != null && checkPasskeySupportUseCase()

            passkeySupported.complete(passkeySupportedVal)

            _uiState.update { prev ->
                prev.copy(
                    inviteInfo = inviteInfo,
                    passkeySupported = passkeySupportedVal,
                    generalError = if (!passkeySupportedVal)
                        StringResourceUiText(Res.string.passkey_not_supported)
                    else null
                )
            }
        }
    }

    fun onUsernameChanged(newValue: String) {
        val filteredValue = filterUsernameUseCase(
            username = newValue,
            invalidCharReplacement = ""
        )

        _uiState.update {
            it.copy(
                username = filteredValue,
                usernameError = null,
                generalError = null
            )
        }
    }

    /**
     * Check the username: needs done both when the user is going to use other options (eg set a
     * password) and when the user is signing up with passkey.
     */
    private suspend fun checkUsernameOk() : String? {
        val usernameVal = _uiState.value.username
        val validationResult = validateUsernameUseCase(usernameVal)
        _uiState.update {
            it.copy(
                usernameError = validationResult.errorMessage?.asUiText()
            )
        }

        if(validationResult.errorMessage != null)
            return null

        try {
            _uiState.update { it.copy(fieldsEnabled = false) }
            _appUiState.update { it.copy(loadingState = LoadingUiState.INDETERMINATE) }

            val usernameUnique = checkUsernameUniqueUseCase(usernameVal)

            if(usernameUnique) {
                _uiState.update { it.copy(fieldsEnabled = true) }
                return usernameVal
            }else {
                _uiState.update {
                    it.copy(
                        usernameError = Res.string.username_already_taken.asUiText(),
                        fieldsEnabled = true,
                    )
                }
            }
        }catch(e: Throwable) {
            Napier.w("Something wrong checking username unique", e)
            _uiState.update {
                it.copy(
                    usernameError = e.getUiTextOrGeneric(),
                    fieldsEnabled = true,
                )
            }
        }finally {
            _appUiState.update { it.copy(loadingState = LoadingUiState.NOT_LOADING) }
        }

        return null
    }


    fun onClickSignupWithPasskey() {
        viewModelScope.launch {
            try {
                val usernameVal = checkUsernameOk() ?: return@launch

                val rpIdVal = schoolDirectoryEntry.await().rpId

                if (createPasskeyUseCase != null && rpIdVal != null && passkeySupported.await()) {
                    val createPasskeyResult = createPasskeyUseCase(
                        CreatePasskeyUseCase.Request(
                            personUid = route.respectRedeemInviteRequest.account.guid,
                            username = usernameVal,
                            rpId = rpIdVal
                        )
                    )

                    when (createPasskeyResult) {
                        is CreatePasskeyUseCase.PasskeyCreatedResult -> {
                            val redeemRequest = route.respectRedeemInviteRequest.copy(
                                account = route.respectRedeemInviteRequest.account.copy(
                                    username = usernameVal,
                                    credential = RespectPasskeyCredential(
                                        passkeyWebAuthNResponse = createPasskeyResult.authenticationResponseJSON
                                    ),
                                )
                            )

                            val personRegistered = accountManager.register(
                                redeemInviteRequest = redeemRequest,
                                schoolUrl = route.schoolUrl
                            )

                            navigateOnAccountCreatedUseCase(
                                personRegistered = personRegistered,
                                navCommandFlow = _navCommandFlow,
                                inviteRequest = redeemRequest,
                            )
                        }

                        is CreatePasskeyUseCase.Error -> {
                            _uiState.update { prev ->
                                prev.copy(
                                    signupError = createPasskeyResult.message?.asUiText()
                                        ?: Res.string.something_went_wrong.asUiText(),
                                )
                            }
                        }

                        is CreatePasskeyUseCase.UserCanceledResult -> {
                            // do nothing
                        }
                    }
                }else{
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(
                            EnterPasswordSignup.create(
                                schoolUrl = route.schoolUrl,
                                inviteRequest = route.respectRedeemInviteRequest.copy(
                                    account = route.respectRedeemInviteRequest.account.copy(
                                        username = usernameVal,
                                    )
                                )
                            )
                        )
                    )
                }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        signupError = t.getUiTextOrGeneric()
                    )
                }
                Napier.w("Signup error", t)
            }
        }
    }

    fun onClickHowPasskeysWork() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(HowPasskeyWorks)
        )
    }

    fun onOtherOptionsClick() {
        viewModelScope.launch {
            val username = checkUsernameOk() ?: return@launch

            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    OtherOptionsSignup.create(
                        schoolUrl = route.schoolUrl,
                        inviteRequest = route.respectRedeemInviteRequest.copy(
                            account = route.respectRedeemInviteRequest.account.copy(
                                username = username
                            )
                        )
                    )
                )
            )
        }
    }

}
