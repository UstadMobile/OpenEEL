package org.openeel.shared.viewmodel.sharedschooldevice.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.db.school.ext.fullName
import org.openeel.datalayer.school.ClassDataSource
import org.openeel.datalayer.school.PersonDataSource
import org.openeel.datalayer.school.model.Clazz
import org.openeel.datalayer.school.model.SHARED_DEVICE_SETTINGS_ACTIVITY_ID
import org.openeel.datalayer.school.model.SHARED_DEVICE_SETTINGS_PROFILE_ID
import org.openeel.datalayer.school.model.SharedDeviceSettings
import org.openeel.datalayer.shared.paging.EmptyPagingSourceFactory
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.paging.PagingSourceFactoryHolder
import org.openeel.datalayer.shared.params.GetListCommonParams
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.xapi.ext.getJsonAsFlow
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.account.shareddevice.enableshareddevicemode.EnableSharedDeviceModeUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.login
import org.openeel.shared.generated.resources.select_class
import org.openeel.shared.navigation.LoginSelectStudent
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.ScanQRCode
import org.openeel.shared.navigation.TeacherPinConfirmation
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel

data class LoginSelectClassUiState(
    val error: UiText? = null,
    val classes: IPagingSourceFactory<Int, Clazz> = EmptyPagingSourceFactory(),
    val settings: DataLoadState<SharedDeviceSettings> = DataLoadingState(),
    val deviceName: String = ""
) {
    val isSelfSelectClassAndName: Boolean
        get() = settings.dataOrNull()?.canSelectName ?: false
}

class LoginSelectClassViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AppAccountManager,
    private val json: Json,
    private val settings: Settings,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    val schoolUrl = accountManager.activeAccount?.school?.self
        ?: throw IllegalStateException("No active school")
    private val pagingSourceHolder = PagingSourceFactoryHolder {
        schoolDataSource.classDataSource.listAsPagingSource(
            loadParams = DataLoadParams(),
            params = ClassDataSource.GetListParams()
        )
    }

    private val _uiState = MutableStateFlow(
        LoginSelectClassUiState(classes = pagingSourceHolder)
    )

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                showBackButton = false
            )
        }

        viewModelScope.launch {
            schoolDataSource.xapiResource.activityProfile.getJsonAsFlow(
                docParams = XapiActivityProfileResource.SingleDocumentParams(
                    activityId = SHARED_DEVICE_SETTINGS_ACTIVITY_ID,
                    profileId = SHARED_DEVICE_SETTINGS_PROFILE_ID,
                ),
                json = json,
                deserializer = SharedDeviceSettings.serializer()
            ).collect { settingsData ->
                _uiState.update {
                    it.copy(settings = settingsData)
                }

                _appUiState.update {
                    it.copy(
                        title = if (settingsData.dataOrNull()?.canSelectName == true)
                            Res.string.select_class.asUiText()
                        else
                            Res.string.login.asUiText(),
                    )
                }
            }
        }

        viewModelScope.launch {
            schoolDataSource.personDataSource.listAsFlow(
                loadParams = DataLoadParams(),
                params = PersonDataSource.GetListParams(
                    common = GetListCommonParams(
                        guid = settings.getString(
                            key = EnableSharedDeviceModeUseCase.CURRENT_DEVICE_GUID, "0"
                        )
                    )
                )
            ).collect { deviceData ->
                _uiState.update {
                    it.copy(deviceName = deviceData.dataOrNull()?.firstOrNull()?.fullName() ?: "")
                }
            }
        }
    }

    fun onClickScanQrCode() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ScanQRCode.create(isSharedDevice = true)
            )
        )
    }

    fun onClickTeacherAdminLogin() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(TeacherPinConfirmation)
        )
    }

    fun onClickClazz(clazz: Clazz) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LoginSelectStudent.create(
                    guid = clazz.guid,
                )
            )
        )
    }
}
