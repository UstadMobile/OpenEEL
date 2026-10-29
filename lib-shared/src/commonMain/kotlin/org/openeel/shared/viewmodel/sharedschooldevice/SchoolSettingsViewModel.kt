package org.openeel.shared.viewmodel.sharedschooldevice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
import org.openeel.lib.dataloadstate.ext.map
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.school
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.SharedDevicesSettings
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel

data class SchoolSettingsUiState(
    val schoolName: UiText? = null,
    val error: UiText? = null,
    val sharedSchoolDeviceCount: Int? = null,
)

class SchoolSettingsViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AppAccountManager,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {
    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(SchoolSettingsUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.school.asUiText(),
            )
        }
        viewModelScope.launch {
            val schoolName = accountManager.activeAccount?.school?.name?.asUiText()
            _uiState.update { prev ->
                prev.copy(
                    schoolName = schoolName
                )
            }
            val params = PersonDataSource.GetListParams(
                filterByPersonRole = PersonRoleEnum.SHARED_SCHOOL_DEVICE,
                filterByPersonStatus = PersonStatusEnum.ACTIVE,
                excludeSharedSchoolDevice = false,
            )
            schoolDataSource.personDataSource.listAsFlow(DataLoadParams(), params)
                .map { it.map { list -> list.size } }
                .distinctUntilChanged()
                .collect { count -> _uiState.update { it.copy(sharedSchoolDeviceCount = count.dataOrNull()) } }
        }
    }


    fun onClickSharedSchoolDevices() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(SharedDevicesSettings)
        )
    }
}
