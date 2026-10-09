package world.respect.shared.viewmodel.person.manageaccount

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.credentials.passkey.CheckPasskeySupportUseCase
import world.respect.credentials.passkey.CreatePasskeyUseCase
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.isStudent
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.datalayer.school.PersonQrBadgeDataSource
import world.respect.datalayer.school.adapters.toPersonPasskey
import world.respect.datalayer.school.findByPersonGuidAsFlow
import world.respect.datalayer.school.model.PersonPassword
import world.respect.datalayer.school.model.PersonQrBadge
import world.respect.datalayer.school.model.StatusEnum
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.RespectSessionAndPerson
import world.respect.shared.domain.getdeviceinfo.GetDeviceInfoUseCase
import world.respect.shared.domain.getdeviceinfo.toUserFriendlyString
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.error_assign_qr_code
import world.respect.shared.generated.resources.error_qr_already_assigned
import world.respect.shared.generated.resources.manage_account
import world.respect.shared.navigation.ChangePassword
import world.respect.shared.navigation.HowPasskeyWorks
import world.respect.shared.navigation.ManageAccount
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.PasskeyList
import world.respect.shared.navigation.RouteResultDest
import world.respect.shared.navigation.ScanQRCode
import world.respect.shared.resources.StringUiText
import world.respect.shared.resources.UiText
import world.respect.shared.util.UrlParser
import world.respect.shared.util.exception.getUiTextOrGeneric
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import kotlin.time.Clock

data class ManageAccountUiState(
    val accountGuid: String = "",
    val passkeyCount: Int? = null,
    val passkeySupported: Boolean = false,
    val personUsername: String = "",
    val personPassword: DataLoadState<PersonPassword> = DataLoadingState(),
    val errorText: UiText? = null,
    val selectedAccount: RespectSessionAndPerson? = null,
    val isStudent: Boolean = false,
    val qrBadge: DataLoadState<PersonQrBadge> = DataLoadingState(),
    val showBottomSheet: Boolean = false,
) {

    val showCreatePasskey: Boolean
        get() = passkeyCount == 0 && passkeySupported &&
                selectedAccount?.person?.guid == accountGuid

    val showManagePasskey: Boolean
        get() = passkeyCount != null && passkeyCount > 0

    val badgeNumber: String?
        get() = qrBadge.dataOrNull()?.qrCodeUrl?.let { UrlParser.extractBadgeNumberFromUrl(it) }

    val isQrAdded: Boolean
        get() = badgeNumber != null
}

class ManageAccountViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val getDeviceInfoUseCase: GetDeviceInfoUseCase,
    private val json: Json,
    private val navResultReturner: NavResultReturner
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val checkPasskeySupportUseCase: CheckPasskeySupportUseCase by lazy {
        scope.get()
    }

    private val createPasskeyUseCase: CreatePasskeyUseCase? by lazy {
        scope.getOrNull()
    }

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: ManageAccount = savedStateHandle.toRoute()

    private val personGuid = route.guid

    private val _uiState = MutableStateFlow(
        ManageAccountUiState(
            accountGuid = personGuid
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                userAccountIconVisible = false,
                navigationVisible = false,
                title = Res.string.manage_account.asUiText()
            )
        }

        /**
         * Handle when a badge and username is being set for the first time.
         *
         * Flow: PersonDetail (no username/credential set): go to set username, scan QR code,
         * then go to ManageAccount (pop up to CreateAccountSetUsername inclusive).
         */
        val qrCodeUrl = route.setPersonQrBadgeUrl
        val setUsername = route.setPersonQrBadgeUsername
        if(qrCodeUrl != null && setUsername != null &&
            savedStateHandle.get<String>(KEY_QRCODE_SET) == null
        ) {
            launchWithLoadingIndicator(
                onShowError = { errorMsg ->
                    _uiState.update { it.copy(errorText = errorMsg) }
                }
            ) {
                saveUsername(route.setPersonQrBadgeUsername)
                storeQrCodeForPerson(personGuid = personGuid, url = qrCodeUrl)
                savedStateHandle[KEY_QRCODE_SET] = route.setPersonQrBadgeUsername
            }
        }

        /**
         * Handle when a badge is being assigned/updated for a person where a username is already
         * set
         *
         * Flow: ManageAccount, go to Scan QR code, returns result using NavResultReturner, pops back
         * to ManageAccount.
         */
        viewModelScope.launch {
            navResultReturner.filteredResultFlowForKey(
                QR_SELECT_RESULT
            ).collect { navResult ->
                val qrUrl = navResult.result as? String ?: return@collect
                storeQrCodeForPerson(personGuid = personGuid, url = Url(qrUrl))
            }
        }

        viewModelScope.launch {
            schoolDataSource.personPasswordDataSource.findByPersonGuidAsFlow(
                route.guid
            ).collect { password ->
                _uiState.update { it.copy(personPassword = password) }
            }
        }

        viewModelScope.launch {
            schoolDataSource.personQrBadgeDataSource.findByGuidAsFlow(
                loadParams = DataLoadParams(),
                guid = route.guid
            ).collect { qrBadgeState ->
                _uiState.update {
                    it.copy(
                        qrBadge = qrBadgeState,
                    )
                }
            }
        }

        viewModelScope.launch {
            if (checkPasskeySupportUseCase()) {
                _uiState.update { prev ->
                    prev.copy(passkeySupported = true)
                }
            }
        }

        viewModelScope.launch {
            launch {
                schoolDataSource.personPasskeyDataSource.listAllAsFlow().collect {
                    _uiState.update { prev ->
                        prev.copy(
                            passkeyCount = it.dataOrNull()?.size ?: 0,
                        )
                    }
                }
            }

            launch {
                schoolDataSource.personDataSource.findByGuidAsFlow(
                    route.guid
                ).collect {
                    _uiState.update { prev ->
                        prev.copy(
                            personUsername = it.dataOrNull()?.username ?: "",
                            isStudent = it.dataOrNull()?.isStudent() == true
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { accountAndPerson ->
                _uiState.update { prev ->
                    prev.copy(selectedAccount = accountAndPerson)
                }
            }
        }

        _uiState.update { prev ->
            prev.copy(
                passkeySupported = (createPasskeyUseCase != null &&
                        accountManager.activeAccount?.userGuid == personGuid),
            )
        }
    }

    fun onClickChangePassword() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ChangePassword(guid = route.guid)
            )
        )
    }

    fun onClickManagePasskey() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                PasskeyList(
                    guid = route.guid
                )
            )
        )
    }

    fun onClickHowPasskeysWork() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(HowPasskeyWorks)
        )
    }

    fun onClickQRCodeBadge() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ScanQRCode.create(
                    resultDest = RouteResultDest(
                        resultPopUpTo = route,
                        resultKey = QR_SELECT_RESULT
                    ),
                )
            )
        )
    }

    fun onDismissBottomSheet() {
        _uiState.update { prev ->
            prev.copy(
                showBottomSheet = false,
            )
        }
    }

    fun onRemoveQRBadge() {
        launchWithLoadingIndicator {
            try {
                val currentQrBadge = uiState.value.qrBadge.dataOrNull()
                if (currentQrBadge != null) {
                    schoolDataSource.personQrBadgeDataSource.store(
                        listOf(
                            currentQrBadge.copy(
                                qrCodeUrl = null,
                                lastModified = Clock.System.now(),
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update { prev ->
                    prev.copy(
                        errorText = e.getUiTextOrGeneric()
                    )
                }
            }
        }
    }

    private suspend fun storeQrCodeForPerson(personGuid: String, url: Url) {
        try {
            val qrCodeAlreadyAssignedToAnotherPerson =
                schoolDataSource.personQrBadgeDataSource.listAll(
                    loadParams = DataLoadParams(),
                    listParams = PersonQrBadgeDataSource.GetListParams(
                        qrCodeUrl = url
                    )
                ).dataOrNull()?.firstOrNull() != null

            if (qrCodeAlreadyAssignedToAnotherPerson) {
                _uiState.update { prev ->
                    prev.copy(
                        errorText = Res.string.error_qr_already_assigned.asUiText()
                    )
                }
            } else {
                schoolDataSource.personQrBadgeDataSource.store(
                    listOf(
                        PersonQrBadge(
                            personGuid = personGuid,
                            qrCodeUrl = url,
                            lastModified = Clock.System.now(),
                            status = StatusEnum.ACTIVE,
                        )
                    )
                )
                _uiState.update { prev ->
                    prev.copy(errorText = null)
                }
            }
        } catch (e: Exception) {
            _uiState.update { prev ->
                prev.copy(
                    errorText = "${Res.string.error_assign_qr_code}: ${e.message ?: "Unknown error"}".asUiText()
                )
            }
            throw e
        }

    }

    private suspend fun saveUsername(username: String?) {
        val person = schoolDataSource.personDataSource.findByGuid(
            DataLoadParams(), route.guid
        ).dataOrNull() ?: throw IllegalStateException("Person not found")

        schoolDataSource.personDataSource.store(
            listOf(
                person.copy(
                    username = username,
                    lastModified = Clock.System.now(),
                )
            )
        )
    }

    fun onClickChangeQrBadge() {
        _uiState.update { prev ->
            prev.copy(
                showBottomSheet = true,
            )
        }
    }

    fun onCreatePasskeyClick() {
        viewModelScope.launch {
            val passkeyCreated = createPasskeyUseCase?.invoke(
                CreatePasskeyUseCase.Request(
                    personUid = uiState.value.selectedAccount?.person?.guid ?: return@launch,
                    username = uiState.value.selectedAccount?.person?.username ?: return@launch,
                    rpId = uiState.value.selectedAccount?.session?.account?.school?.rpId
                        ?: return@launch
                )
            )

            if (passkeyCreated != null) {
                when (passkeyCreated) {
                    is CreatePasskeyUseCase.PasskeyCreatedResult -> {
                        schoolDataSource.personPasskeyDataSource.store(
                            listOf(
                                passkeyCreated.toPersonPasskey(
                                    json = json,
                                    personGuid = personGuid,
                                    deviceName = getDeviceInfoUseCase().toUserFriendlyString(),
                                )
                            )
                        )
                    }

                    is CreatePasskeyUseCase.Error -> {
                        _uiState.update { prev ->
                            prev.copy(
                                errorText = StringUiText(passkeyCreated.message ?: ""),
                            )
                        }
                    }

                    is CreatePasskeyUseCase.UserCanceledResult -> {
                        //do nothing
                    }
                }
            }
        }
    }

    companion object {

        const val QR_SELECT_RESULT = "qr_select_result"

        const val KEY_QRCODE_SET = "qr_code_set"

    }
}