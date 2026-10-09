package world.respect.shared.viewmodel.sharedschooldevice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.school
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SharedDevicesSettings
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

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