package org.openeel.shared.viewmodel.settings.sharedschooldevices

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
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
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.dataloadstate.ext.firstOrNotFound
import org.openeel.lib.dataloadstate.ext.isSettledNotFound
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.account.invite.ApproveOrDeclineInviteRequestUseCase
import org.openeel.lib.xapi.ext.getJsonAsFlow
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
    val thisDevice: DataLoadState<Person> = DataLoadingState(),
    val pendingDevices: IPagingSourceFactory<Int, Person> =
        IPagingSourceFactory { EmptyPagingSource() },
    val error: UiText? = null,
    val isPendingExpanded: Boolean = true,
    val sharedDeviceSettings: DataLoadState<SharedDeviceSettings> = DataLoadingState(),
    val newPinDialogVisible: Boolean = false,
    val showBottomSheetOptions: Boolean = false,
    val currentDeviceGuid: String? = null
) {
    fun isCurrentDevice(person: Person): Boolean = person.guid == currentDeviceGuid

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

    private val settingsDocParams = XapiActivityProfileResource.SingleDocumentParams(
        activityId = SHARED_DEVICE_SETTINGS_ACTIVITY_ID,
        profileId = SHARED_DEVICE_SETTINGS_PROFILE_ID,
    )

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
                filterByPersonRole = PersonRoleEnum.SHARED_SCHOOL_DEVICE
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
                prioritizedGuid = currentDeviceGuid
            )
        )
    }

    init {
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

        viewModelScope.launch {
            schoolDataSource.xapiResource.activityProfile.getJsonAsFlow(
                docParams = settingsDocParams,
                json = json,
                deserializer = SharedDeviceSettings.serializer(),
            ).collect { dataLoadState ->
                _uiState.update { it.copy(sharedDeviceSettings = dataLoadState) }
                if(dataLoadState.isSettledNotFound()) {
                    putSharedDeviceSettings(
                        SharedDeviceSettings(
                            pin = Random.nextInt(1000, 10000).toString()
                                .padStart(SharedDevicesSettingsUiState.PIN_LENGTH, '0'),
                            canSelectName = true,
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            if(currentDeviceGuid != null) {
                schoolDataSource.personDataSource.listAsFlow(
                    loadParams = DataLoadParams(),
                    params = PersonDataSource.GetListParams(
                        common = GetListCommonParams(guid = currentDeviceGuid),
                        filterByPersonRole = PersonRoleEnum.SHARED_SCHOOL_DEVICE,
                    )
                ).collect { thisDevice ->
                    _uiState.update { it.copy(thisDevice = thisDevice.firstOrNotFound()) }
                }
            }else {
                _uiState.update { it.copy(thisDevice = NoDataLoadedState.notFound()) }
            }
        }
    }

    private suspend fun putSharedDeviceSettings(
        sharedDeviceSettings: SharedDeviceSettings
    ) {
        schoolDataSource.xapiResource.activityProfile.putJson(
            docParams = settingsDocParams,
            document = sharedDeviceSettings,
            json = json,
            serializer = SharedDeviceSettings.serializer(),
        )
    }

    private suspend fun updateSharedDeviceSettings(
        block: (SharedDeviceSettings) -> SharedDeviceSettings
    ) {
        _uiState.value.sharedDeviceSettings.dataOrNull()?.also { sharedDeviceSettings ->
            putSharedDeviceSettings(block(sharedDeviceSettings))
        }
    }

    fun toggleSelfSelect(enabled: Boolean) {
        viewModelScope.launch {
            updateSharedDeviceSettings { it.copy(canSelectName = enabled) }
        }
    }

    fun onClickAdd() {
        val deviceGuid = settings.getStringOrNull(CURRENT_DEVICE_GUID)
        if (deviceGuid != null) {
            onClickAddAnotherDevice()
        } else {
            _uiState.update { currentState ->
                currentState.copy(showBottomSheetOptions = true)
            }
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

    fun onClickEnableOnThisDevice() {
        viewModelScope.launch {
            val inviteCode = schoolDataSource.inviteDataSource.findByGuid(
                guid = InvitePerson.NewUserInviteOptions(
                    presetRole = PersonRoleEnum.SHARED_SCHOOL_DEVICE
                ).presetRole?.newUserInviteUid
                    ?: throw IllegalStateException("no invite found for shared device role")
            ).dataOrNull()?.code ?: throw IllegalStateException("Invite code not found/error")

            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    AcceptInvite.create(
                        schoolUrl = schoolUrl,
                        code = inviteCode,
                        useActiveUserAuth = true,
                    )
                )
            )
        }
    }

    fun onShowPinDialog() {
        _uiState.update { it.copy(newPinDialogVisible = true) }
    }

    fun onDismissPinDialog() {
        _uiState.update {
            it.copy(
                newPinDialogVisible = false,
                error = null
            )
        }
    }

    fun onSavePin(pin: String) {
        if (pin.length == SharedDevicesSettingsUiState.PIN_LENGTH && pin.all { it.isDigit() }) {
            _uiState.update { it.copy(newPinDialogVisible = false) }
            viewModelScope.launch {
                updateSharedDeviceSettings { it.copy(pin = pin) }
            }
        }else {
            _uiState.update { it.copy(error = Res.string.pin_error.asUiText()) }
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

}
