package org.openeel.shared.viewmodel.sharedschooldevice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.school.PersonDataSource
import org.openeel.datalayer.school.model.PersonRoleEnum
import org.openeel.datalayer.school.model.PersonStatusEnum
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.school
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.SharedDevicesSettings
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.RespectViewModel

data class SchoolSettingsUiState(
    val schoolName: UiText? = null,
    val error: UiText? = null,
    val sharedSchoolDeviceCount: Int? = null,
)

class SchoolSettingsViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(SchoolSettingsUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.school.asUiText(),
                hideBottomNavigation = true,
            )
        }
        viewModelScope.launch {
            val schoolName = accountManager.activeAccount?.school?.name?.asUiText()
            _uiState.update { prev ->
                prev.copy(
                    schoolName = schoolName
                )
            }
            val deviceList = schoolDataSource.personDataSource.list(
                loadParams = DataLoadParams(),
                params = PersonDataSource.GetListParams(
                    filterByPersonRole = PersonRoleEnum.SHARED_SCHOOL_DEVICE,
                    filterByPersonStatus = PersonStatusEnum.ACTIVE
                )
            )

            _uiState.update { prev ->
                prev.copy(
                    sharedSchoolDeviceCount = deviceList.dataOrNull()?.size
                )
            }
        }
    }


    fun onClickSharedSchoolDevices() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(SharedDevicesSettings)
        )
    }
}