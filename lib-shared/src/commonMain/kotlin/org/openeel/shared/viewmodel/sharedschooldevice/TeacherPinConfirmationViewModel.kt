package org.openeel.shared.viewmodel.sharedschooldevice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.school.model.SHARED_DEVICE_SETTINGS_ACTIVITY_ID
import org.openeel.datalayer.school.model.SHARED_DEVICE_SETTINGS_PROFILE_ID
import org.openeel.datalayer.school.model.SharedDeviceSettings
import org.openeel.lib.dataloadstate.DataErrorResult
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.xapi.ext.getJson
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.invalid
import org.openeel.shared.generated.resources.teacher_admin_login
import org.openeel.shared.navigation.LoginScreen
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel

data class TeacherPinConfirmationUiState(
    val errorMessage: UiText? = null,
    val pin: String = "",
)

class TeacherPinConfirmationViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: AppAccountManager,
    private val json: Json
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(TeacherPinConfirmationUiState())

    val uiState = _uiState.asStateFlow()


    init {
        _appUiState.update {
            it.copy(
                title = Res.string.teacher_admin_login.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }
    }

    fun onPinChanged(pin: String) {
        _uiState.update { it.copy(pin = pin, errorMessage = null) }
    }

    fun onClickNext() {
        viewModelScope.launch {
            if (verifyTeacherPin(_uiState.value.pin)) {
                val schoolUrl = accountManager.activeAccount?.school?.self
                schoolUrl?.let { url ->
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(LoginScreen.create(url, true))
                    )
                }
            } else {
                _uiState.update { it.copy(errorMessage = Res.string.invalid.asUiText()) }
            }
        }
    }

    private suspend fun verifyTeacherPin(enteredPin: String): Boolean {
        val correctPin = when (val result = schoolDataSource.xapiResource.activityProfile.getJson(
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
        return enteredPin == correctPin.pin
    }
}
