package org.openeel.shared.viewmodel.sharedschooldevice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.db.school.ext.isAdminOrTeacher
import org.openeel.datalayer.school.PersonDataSource
import org.openeel.datalayer.school.ext.newUserInviteUid
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.PersonRoleEnum
import org.openeel.datalayer.school.model.PersonStatusEnum
import org.openeel.datalayer.shared.paging.EmptyPagingSource
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.paging.PagingSourceFactoryHolder
import org.openeel.datalayer.shared.params.GetListCommonParams
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.domain.account.sharedschooldevice.GetSharedDeviceSelfSelectUseCase
import org.openeel.shared.domain.account.sharedschooldevice.SetSharedDeviceSelfSelectUseCase
import org.openeel.shared.domain.account.invite.ApproveOrDeclineInviteRequestUseCase
import org.openeel.shared.domain.account.sharedschooldevice.setpin.GetSharedDevicePINUseCase
import org.openeel.shared.domain.account.sharedschooldevice.setpin.SetSharedDevicePINUseCase
import org.openeel.shared.ext.tryOrShowSnackbarOnError
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.device
import org.openeel.shared.generated.resources.pin_error
import org.openeel.shared.generated.resources.shared_school_devices
import org.openeel.shared.navigation.AcceptInvite
import org.openeel.shared.navigation.InvitePerson
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.RespectViewModel
import org.openeel.shared.viewmodel.app.appstate.FabUiState
import org.openeel.shared.viewmodel.app.appstate.SnackBarDispatcher
import org.openeel.shared.viewmodel.sharedschooldevice.SharedDevicesSettingsUiState.Companion.CURRENT_DEVICE_GUID
import kotlin.time.Clock

data class SharedDevicesSettingsUiState(
    val devices: IPagingSourceFactory<Int, Person> = IPagingSourceFactory {
        EmptyPagingSource()
    },
    val pendingDevices: IPagingSourceFactory<Int, Person> =
        IPagingSourceFactory { EmptyPagingSource() },
    val error: UiText? = null,
    val isPendingExpanded: Boolean = true,
    val isSelfSelectClassAndName: Boolean = true,
    val showEnableDialog: Boolean = false,
    val showPinDialog: Boolean = false,
    val pin: String = "",
    val showBottomSheetOptions: Boolean = false,
    val isLoadingPin: Boolean = true,
    val currentDeviceGuid: String? = null
) {
    companion object {
        const val PIN_LENGTH = 4
        const val CURRENT_DEVICE_GUID = "current_device_guid"
    }
}

class SharedDevicesSettingsViewmodel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val snackBarDispatcher: SnackBarDispatcher,
    private val settings: Settings
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()
    private val schoolDataSource: SchoolDataSource by inject()
    private val approveOrDeclineInviteRequestUseCase: ApproveOrDeclineInviteRequestUseCase by inject()

    private val getSharedDevicePINUseCase: GetSharedDevicePINUseCase by inject()
    private val setSharedDevicePINUseCase: SetSharedDevicePINUseCase by inject()
    private val getSharedDeviceSelfSelectUseCase: GetSharedDeviceSelfSelectUseCase by inject()
    private val setSharedDeviceSelfSelectUseCase: SetSharedDeviceSelfSelectUseCase by inject()

    private val _uiState = MutableStateFlow(SharedDevicesSettingsUiState(isLoadingPin = true))
    val uiState = _uiState.asStateFlow()

    private val currentDeviceGuid = settings.getStringOrNull(CURRENT_DEVICE_GUID)

    val schoolUrl = accountManager.activeAccount?.school?.self
        ?: throw IllegalStateException("No active school")
    private val pendingPersonsPagingSource = PagingSourceFactoryHolder {
        schoolDataSource.personDataSource.listAsPagingSource(
            DataLoadParams(),
            PersonDataSource.GetListParams(
                filterByPersonStatus = PersonStatusEnum.PENDING_APPROVAL,
                filterByPersonRole = PersonRoleEnum.SHARED_SCHOOL_DEVICE,
                excludeSharedSchoolDevice = false
            )
        )
    }

    private val pagingSourceFactoryHolder = PagingSourceFactoryHolder {
        schoolDataSource.personDataSource.listAsPagingSource(
            DataLoadParams(),
            PersonDataSource.GetListParams(
                filterByName = _appUiState.value.searchState.searchText.takeIf { it.isNotBlank() },
                filterByPersonStatus = PersonStatusEnum.ACTIVE,
                filterByPersonRole = PersonRoleEnum.SHARED_SCHOOL_DEVICE,
                excludeSharedSchoolDevice = false
            )
        )
    }

    init {
        loadSchoolPin()
        loadSelfSelectSetting()
        _appUiState.update {
            it.copy(
                title = Res.string.shared_school_devices.asUiText(),
                hideBottomNavigation = true,
                fabState = FabUiState(
                    text = Res.string.device.asUiText(),
                    icon = FabUiState.FabIcon.ADD,
                    onClick = ::onClickAdd,
                    visible = true,
                ),
                showBackButton = true,
            )
        }

        _uiState.update {
            it.copy(
                devices = pagingSourceFactoryHolder,
                pendingDevices = pendingPersonsPagingSource,
                currentDeviceGuid = currentDeviceGuid
            )
        }
    }

    fun toggleSelfSelect(enabled: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(isSelfSelectClassAndName = enabled)
        }
        viewModelScope.launch {
            try {
                setSharedDeviceSelfSelectUseCase(enabled)
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(error = e.message?.asUiText())
                }
            }
        }
    }

    fun onClickAdd() {
        _uiState.update { currentState ->
            currentState.copy(showBottomSheetOptions = true)
        }
    }

    fun onClickAddAnotherDevice() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                InvitePerson.create(
                    invitePersonOptions = InvitePerson.NewUserInviteOptions(
                        presetRole = PersonRoleEnum.SHARED_SCHOOL_DEVICE
                    )
                )
            )
        )
    }

    private fun loadSchoolPin() {
        viewModelScope.launch {
            try {
                val pin = getSharedDevicePINUseCase()
                _uiState.update {
                    it.copy(
                        pin = pin,
                        isLoadingPin = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message?.asUiText(),
                        isLoadingPin = false
                    )
                }
            }
        }
    }

    private fun loadSelfSelectSetting() {
        viewModelScope.launch {
            val selfEnableValue = getSharedDeviceSelfSelectUseCase()
            _uiState.update {
                it.copy(isSelfSelectClassAndName = selfEnableValue)
            }
        }
    }


    fun onClickEnableOnThisDevice() {
        viewModelScope.launch {
            val activeAccount = accountManager.activeAccount
            val persons = schoolDataSource.personDataSource.list(
                loadParams = DataLoadParams(),
                params = PersonDataSource.GetListParams(
                    common = GetListCommonParams(
                        guid = activeAccount?.userGuid
                    ),
                    includeRelated = true,
                )
            ).dataOrNull()
            val activePerson = persons?.firstOrNull { person ->
                person.guid == (activeAccount?.userGuid)
            }
            val isTeacherOrAdmin = activePerson?.isAdminOrTeacher() ?: false

            val inviteUid = InvitePerson.NewUserInviteOptions(
                presetRole = PersonRoleEnum.SHARED_SCHOOL_DEVICE
            ).presetRole?.newUserInviteUid

            activeAccount?.school?.self?.let { url ->
                if (inviteUid != null) {
                    schoolDataSource.inviteDataSource.findByUidAsFlow(
                        uid = inviteUid,
                        loadParams = DataLoadParams()
                    ).collectLatest { invite ->
                        invite.dataOrNull()?.let { it ->
                            _navCommandFlow.tryEmit(
                                NavCommand.Navigate(
                                    AcceptInvite.create(
                                        schoolUrl = url,
                                        code = it.code,
                                        useActiveUserAuth = !isTeacherOrAdmin,
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun onShowPinDialog() {
        _uiState.update { it.copy(showPinDialog = true) }
    }

    fun onDismissPinDialog() {
        _uiState.update {
            it.copy(
                showPinDialog = false,
                error = null
            )
        }
    }

    fun onSavePin(pin: String) {
        if (pin.length == SharedDevicesSettingsUiState.PIN_LENGTH && pin.all { it.isDigit() }) {
            _uiState.update {
                it.copy(pin = pin)
            }
            viewModelScope.launch {
                try {
                    setSharedDevicePINUseCase(pin)
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            error = e.message?.asUiText()
                        )
                    }
                }
                onDismissPinDialog()
            }
        } else {
            _uiState.update {
                it.copy(error = Res.string.pin_error.asUiText())
            }
        }
    }

    fun onTogglePendingInvites() {
        _uiState.update {
            it.copy(isPendingExpanded = !it.isPendingExpanded)
        }
    }

    fun onDismissBottomSheet() {
        _uiState.update { currentState ->
            currentState.copy(showBottomSheetOptions = false)
        }
    }

    fun onClickAcceptOrDismissInvite(
        person: Person,
        approved: Boolean,
    ) {
        viewModelScope.launch {
            snackBarDispatcher.tryOrShowSnackbarOnError {
                approveOrDeclineInviteRequestUseCase(
                    personUid = person.guid,
                    approved = approved,
                )
            }
        }
    }

    fun onRemoveDevice(person: Person) {
        settings.remove(CURRENT_DEVICE_GUID)
        viewModelScope.launch {
            schoolDataSource.personDataSource.store(
                listOf(
                    person.copy(
                        status = PersonStatusEnum.TO_BE_DELETED,
                        lastModified = Clock.System.now(),
                    )
                )
            )
        }
    }
}