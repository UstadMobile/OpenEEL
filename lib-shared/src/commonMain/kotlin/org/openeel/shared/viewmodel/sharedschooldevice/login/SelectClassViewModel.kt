package org.openeel.shared.viewmodel.sharedschooldevice.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.school.ClassDataSource
import org.openeel.datalayer.school.model.Clazz
import org.openeel.datalayer.school.model.SHARED_DEVICE_SETTINGS_ACTIVITY_ID
import org.openeel.datalayer.school.model.SHARED_DEVICE_SETTINGS_PROFILE_ID
import org.openeel.datalayer.school.model.SharedDeviceSettings
import org.openeel.datalayer.shared.paging.EmptyPagingSourceFactory
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.paging.PagingSourceFactoryHolder
import org.openeel.lib.dataloadstate.DataErrorResult
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.xapi.ext.getJson
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.login
import org.openeel.shared.generated.resources.select_class
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.ScanQRCode
import org.openeel.shared.navigation.SelectClass
import org.openeel.shared.navigation.StudentList
import org.openeel.shared.navigation.TeacherPinConfirmation
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel

data class SelectClassUiState(
    val error: UiText? = null,
    val classes: IPagingSourceFactory<Int, Clazz> = EmptyPagingSourceFactory(),
    val isSelfSelectClassAndName: Boolean = true,
    val deviceName: String = ""
)

class SelectClassViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AppAccountManager,
    private val json: Json
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val route: SelectClass = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(SelectClassUiState())
    val uiState = _uiState.asStateFlow()

    val schoolUrl = accountManager.activeAccount?.school?.self
        ?: throw IllegalStateException("No active school")
    private val pagingSourceHolder = PagingSourceFactoryHolder {
        schoolDataSource.classDataSource.listAsPagingSource(
            loadParams = DataLoadParams(),
            params = ClassDataSource.GetListParams()
        )
    }

    init {
        viewModelScope.launch {
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
            val selfEnableValue = settings.canSelectName
            _uiState.update {
                it.copy(isSelfSelectClassAndName = selfEnableValue)
            }
            _appUiState.update {
                it.copy(
                    title = if (_uiState.value.isSelfSelectClassAndName) Res.string.select_class.asUiText() else Res.string.login.asUiText(),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false,
                    showBackButton = false
                )
            }
        }
        viewModelScope.launch {
            val device =
                schoolDataSource.personDataSource.findByGuid(DataLoadParams(), route.deviceGuid)

            _uiState.update { prev ->
                prev.copy(
                    classes = pagingSourceHolder,
                    deviceName = device.dataOrNull()?.givenName ?: ""
                )
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
                StudentList.create(
                    className = clazz.title,
                    guid = clazz.guid,
                )
            )
        )
    }
}
