package world.respect.shared.viewmodel.manageuser.otheroptionsignup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.credentials.passkey.CheckPasskeySupportUseCase
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.credentials.passkey.RespectPasskeyCredential
import world.respect.datalayer.RespectAppDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.other_options
import world.respect.shared.generated.resources.passkey_not_supported
import world.respect.shared.navigation.EnterPasswordSignup
import world.respect.shared.navigation.HowPasskeyWorks
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.OtherOptionsSignup
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class OtherOptionsSignupUiState(
    val passkeyError: String? = null,
    val generalError: StringResourceUiText? = null,
    val showPasskeyOption: Boolean = false,
)

class OtherOptionsSignupViewModel(
    savedStateHandle: SavedStateHandle,
    private val respectAppDataSource: RespectAppDataSource,
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    private val route: OtherOptionsSignup = savedStateHandle.toRoute()

    override val scope: Scope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
        SchoolDirectoryEntryScopeId(route.schoolUrl, null).scopeId
    )

    private val checkPasskeySupportUseCase: CheckPasskeySupportUseCase? by lazy {
        scope.getOrNull()
    }

    private val _uiState = MutableStateFlow(OtherOptionsSignupUiState())

    val uiState = _uiState.asStateFlow()

    private val createPasskeyUseCase: CreatePasskeyUseCase? by lazy {
        scope.getOrNull()
    }

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.other_options.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }

        _uiState.update { prev ->
            prev.copy(
                generalError = if (createPasskeyUseCase == null)
                    StringResourceUiText(Res.string.passkey_not_supported)
                else null
            )
        }

        viewModelScope.launch {
            val passkeySupportedVal = checkPasskeySupportUseCase?.invoke() ?: false
            _uiState.update { prev ->
                prev.copy(
                    showPasskeyOption = passkeySupportedVal
                )
            }
        }
    }


    fun onClickSignupWithPasskey() {
        val createPasskeyUseCaseVal = createPasskeyUseCase
        viewModelScope.launch {
            try {
                val schoolDirEntry = respectAppDataSource.schoolDirectoryEntryDataSource
                    .getSchoolDirectoryEntryByUrl(route.schoolUrl).dataOrNull() ?: throw IllegalStateException()
                val rpId = schoolDirEntry.rpId
                val username = route.respectRedeemInviteRequest.account.username

                if (createPasskeyUseCaseVal == null || rpId==null){
                    _uiState.update {
                        it.copy(
                            generalError = StringResourceUiText(Res.string.passkey_not_supported)
                        )
                    }
                }else {
                    val createPasskeyResult = createPasskeyUseCaseVal(
                        CreatePasskeyUseCase.Request(
                            personUid = route.respectRedeemInviteRequest.account.guid,
                            username = username,
                            rpId = rpId
                        )
                    )

                    when (createPasskeyResult) {
                        is CreatePasskeyUseCase.PasskeyCreatedResult -> {
                            val redeemRequest = route.respectRedeemInviteRequest.copy(
                                account = route.respectRedeemInviteRequest.account.copy(
                                    credential = RespectPasskeyCredential(
                                        createPasskeyResult.authenticationResponseJSON
                                    )
                                )
                            )

                            accountManager.register(
                                redeemInviteRequest = redeemRequest,
                                schoolUrl = route.schoolUrl
                            )

                            /*
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(
                                    destination = if(
                                        route.respectRedeemInviteRequest.role == PersonRoleEnum.PARENT
                                    ) {
                                        SignupScreen.create(
                                            schoolUrl = route.schoolUrl,
                                            profileType = ProfileType.CHILD,
                                            inviteRequest = redeemRequest
                                        )
                                    }else {
                                        if (redeemRequest.invite.forClassGuid == null &&
                                            redeemRequest.invite.forFamilyOfGuid == null){
                                            RespectAppLauncher()
                                        }else{
                                            WaitingForApproval()

                                        }
                                    },
                                    clearBackStack = true,
                                )
                            )
                            */
                        }

                        is CreatePasskeyUseCase.Error -> {
                            _uiState.update { prev ->
                                prev.copy(
                                    passkeyError = createPasskeyResult.message,
                                )
                            }
                        }

                        is CreatePasskeyUseCase.UserCanceledResult -> {
                            // do nothing
                        }
                    }
                }

            } catch (e: Exception) {
                println(e.message.toString())
            }
        }
    }

    fun onClickSignupWithPassword() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                EnterPasswordSignup.create(
                    schoolUrl = route.schoolUrl,
                    inviteRequest = route.respectRedeemInviteRequest
                )
            )
        )
    }

    fun onClickHowPasskeysWork() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(HowPasskeyWorks))
    }
}