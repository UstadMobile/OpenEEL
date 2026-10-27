package org.openeel.shared.viewmodel.sharedschooldevice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json
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
import org.openeel.datalayer.school.model.SHARED_DEVICE_SETTINGS_ACTIVITY_ID
import org.openeel.datalayer.school.model.SHARED_DEVICE_SETTINGS_PROFILE_ID
import org.openeel.datalayer.school.model.SharedDeviceSettings
import org.openeel.datalayer.shared.paging.EmptyPagingSource
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.paging.PagingSourceFactoryHolder
import org.openeel.datalayer.shared.params.GetListCommonParams
import org.openeel.lib.dataloadstate.DataErrorResult
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.account.invite.ApproveOrDeclineInviteRequestUseCase
import org.openeel.lib.xapi.ext.getJson
import org.openeel.lib.xapi.ext.putJson
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.shared.domain.account.invite.EnableSharedDeviceModeUseCase.Companion.CURRENT_DEVICE_GUID
import org.openeel.shared.ext.tryOrShowSnackbarOnError
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.device
import org.openeel.shared.generated.resources.pin_error
import org.openeel.shared.generated.resources.shared_school_devices
import org.openeel.shared.navigation.AcceptInvite
import org.openeel.shared.navigation.InvitePerson
import org.openeel.shared.navigation.LoginSelectClass
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.FabUiState
import org.openeel.shared.viewmodel.app.appstate.SnackBarDispatcher
import kotlin.random.Random
import kotlin.time.Clock

data class SharedDevicesSettingsUiState(
    val devices: IPagingSourceFactory<Int, Person> = IPagingSourceFactory {
        EmptyPagingSource()
    },
    val pendingDevices: IPagingSourceFactory<Int, Person> =
        IPagingSourceFactory { EmptyPagingSource() },
    val error: UiText? = null,
    val isPendingExpanded: Boolean = true,
    val sharedDeviceSettings: SharedDeviceSettings = SharedDeviceSettings(),
    val showPinDialog: Boolean = false,
    val showBottomSheetOptions: Boolean = false,
    val currentDeviceGuid: String? = null
) {
    companion object {
        const val PIN_LENGTH = 4
    }
}

class SharedDevicesSettingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: AppAccountManager,
    private val snackBarDispatcher: SnackBarDispatcher,
    private val settings: Settings,
    private val json: Json
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()
    private val schoolDataSource: SchoolDataSource by inject()
    private val approveOrDeclineInviteRequestUseCase: ApproveOrDeclineInviteRequestUseCase by inject()
    private val _uiState = MutableStateFlow(SharedDevicesSettingsUiState())
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
        loadSharedDeviceSettings()
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
        val updatedSettings = _uiState.value.sharedDeviceSettings.copy(
            canSelectName = enabled,
        )
        _uiState.update { currentState ->
            currentState.copy(sharedDeviceSettings = updatedSettings)
        }
        viewModelScope.launch {
            try {
                putSharedDeviceSettings(updatedSettings)
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

    private fun loadSharedDeviceSettings() {
        viewModelScope.launch {
            try {
                val settings = when (val result = schoolDataSource.xapiResource.activityProfile.getJson(
                    docParams = XapiActivityProfileResource.SingleDocumentParams(
                        activityId = SHARED_DEVICE_SETTINGS_ACTIVITY_ID,
                        profileId = SHARED_DEVICE_SETTINGS_PROFILE_ID,
                    ),
                    json = json,
                    deserializer = SharedDeviceSettings.serializer(),
                )) {
                    is DataReadyState -> result.data
                    is NoDataLoadedState -> when (result.reason) {
                        NoDataLoadedState.Reason.NOT_FOUND -> SharedDeviceSettings()
                        NoDataLoadedState.Reason.NOT_MODIFIED ->
                            throw IllegalStateException("Shared device settings are not available")
                    }
                    is DataErrorResult -> throw result.error
                    is DataLoadingState -> result.partialData
                        ?: throw IllegalStateException("Shared device settings are still loading")
                }
                val settingsWithPin = if (settings.pin.isNotBlank()) {
                    settings
                } else {
                    settings.copy(
                        pin = Random.nextInt(1000, 10000).toString().padStart(4, '0'),
                    ).also { putSharedDeviceSettings(it) }
                }
                _uiState.update {
                    it.copy(
                        sharedDeviceSettings = settingsWithPin
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message?.asUiText()
                    )
                }
            }
        }
    }


    fun onClickEnableOnThisDevice() {
        viewModelScope.launch {
            val deviceGuid = settings.getStringOrNull(CURRENT_DEVICE_GUID)
            if (deviceGuid != null) {
                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        LoginSelectClass
                    )
                )
                return@launch
            }

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
            val updatedSettings = _uiState.value.sharedDeviceSettings.copy(pin = pin)
            _uiState.update {
                it.copy(sharedDeviceSettings = updatedSettings)
            }
            viewModelScope.launch {
                try {
                    putSharedDeviceSettings(updatedSettings)
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
        val currentDeviceGuid = settings.getStringOrNull(CURRENT_DEVICE_GUID)
        if (person.guid == currentDeviceGuid) {
            settings.remove(CURRENT_DEVICE_GUID)
        }
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

    private suspend fun putSharedDeviceSettings(settings: SharedDeviceSettings) {
        schoolDataSource.xapiResource.activityProfile.putJson(
            docParams = XapiActivityProfileResource.SingleDocumentParams(
                activityId = SHARED_DEVICE_SETTINGS_ACTIVITY_ID,
                profileId = SHARED_DEVICE_SETTINGS_PROFILE_ID,
            ),
            document = settings,
            json = json,
            serializer = SharedDeviceSettings.serializer(),
        )
    }
}
