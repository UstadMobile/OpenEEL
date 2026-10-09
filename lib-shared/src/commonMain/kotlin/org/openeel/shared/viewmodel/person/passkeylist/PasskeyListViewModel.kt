package world.respect.shared.viewmodel.person.passkeylist
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.credentials.passkey.CheckPasskeySupportUseCase
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.adapters.toPersonPasskey
import world.respect.datalayer.school.model.PersonPasskey
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.getdeviceinfo.GetDeviceInfoUseCase
import world.respect.shared.domain.getdeviceinfo.toUserFriendlyString
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.passkey
import world.respect.shared.generated.resources.passkeys
import world.respect.shared.generated.resources.something_went_wrong
import world.respect.shared.resources.UiText
import world.respect.shared.util.exception.getUiTextOrGeneric
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import kotlin.time.Clock

data class PasskeyListUiState(
    val passkeys: DataLoadState<List<PersonPasskey>> = DataLoadingState(),
    val showRevokePasskeyDialog : Boolean = false,
    val passkeyPendingRevocation : PersonPasskey? = null,
    val errorMessage: UiText? = null,
)


class PasskeyListViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val json: Json,
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val createPasskeyUseCase: CreatePasskeyUseCase? by lazy {
        scope.getOrNull()
    }

    private val checkPasskeySupportUseCase: CheckPasskeySupportUseCase? by lazy {
        scope.getOrNull()
    }

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(
        PasskeyListUiState()
    )

    val uiState = _uiState.asStateFlow()


    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.passkeys.asUiText(),
                userAccountIconVisible = false,
                navigationVisible = true,
                hideBottomNavigation = true,
                fabState = FabUiState(
                    visible = false,
                    text = Res.string.passkey.asUiText(),
                    icon = FabUiState.FabIcon.ADD,
                    onClick = ::onClickAdd,
                )
            )
        }

        viewModelScope.launch {
            val passkeySupported = checkPasskeySupportUseCase?.invoke() ?: false
            _appUiState.takeIf { passkeySupported }?.update { prev ->
                prev.copy(
                    fabState = prev.fabState.copy(visible = true)
                )
            }
        }

        viewModelScope.launch {
            schoolDataSource.personPasskeyDataSource.listAllAsFlow().collect {
                _uiState.update { prev ->
                    prev.copy(
                        passkeys = it
                    )
                }
            }
        }
    }

    fun onDismissRevokePasskeyDialog(){
       _uiState.update { prev ->
           prev.copy(
               showRevokePasskeyDialog = false,
               passkeyPendingRevocation = null,
           )
       }
    }

    fun onClickAdd() {
        viewModelScope.launch {
            try {
                val accountAndPerson = accountManager.selectedAccountAndPersonFlow.first()
                val username = accountAndPerson?.person?.username ?: return@launch
                val rpId = accountAndPerson.session.account.school.rpId ?: return@launch
                val passkeyResult = createPasskeyUseCase?.invoke(
                    request = CreatePasskeyUseCase.Request(
                        personUid = accountAndPerson.person.guid,
                        username = username,
                        rpId = rpId,
                    ),
                ) ?: return@launch

                when(passkeyResult) {
                    is CreatePasskeyUseCase.PasskeyCreatedResult -> {
                        schoolDataSource.personPasskeyDataSource.store(
                            listOf(
                                passkeyResult.toPersonPasskey(
                                    json = json,
                                    personGuid = accountAndPerson.person.guid,
                                    deviceName = getDeviceInfoUseCase().toUserFriendlyString(),
                                )
                            )
                        )
                    }

                    is CreatePasskeyUseCase.Error -> {
                        _uiState.update {
                            it.copy(
                                errorMessage = passkeyResult.message?.asUiText()
                                    ?: Res.string.something_went_wrong.asUiText()
                            )
                        }
                    }

                    else -> {
                        //Do nothing - user canceled
                    }
                }
            }catch(e: Throwable) {
                Napier.w("Error creating passkey", e)
                _uiState.update {
                    it.copy(errorMessage = e.getUiTextOrGeneric())
                }
            }
        }
    }


    fun onConfirmRevokePasskey() {
        val keyToRevoke = _uiState.value.passkeyPendingRevocation ?: return

        viewModelScope.launch {
            try {
                schoolDataSource.personPasskeyDataSource.store(
                    listOf(
                        keyToRevoke.copy(
                            isRevoked = true,
                            lastModified = Clock.System.now(),
                        )
                    )
                )
                onDismissRevokePasskeyDialog()
            }catch(e: Throwable) {
                Napier.w("Error revoking passkey", e)
                _uiState.update {
                    it.copy(errorMessage = e.getUiTextOrGeneric())
                }
            }
        }

    }

    fun onClickRevokePasskey(passkey: PersonPasskey){
        _uiState.update { prev ->
            prev.copy(
                showRevokePasskeyDialog = true,
                passkeyPendingRevocation = passkey,
            )
        }
    }


}
