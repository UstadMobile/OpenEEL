package org.openeel.shared.viewmodel.manageuser.learningspace

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openeel.datalayer.host.HostListDataSource
import org.openeel.datalayer.host.HostConfigSettingsDataSource
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.datalayer.respect.model.SchoolDirectory
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.shared.domain.school.LaunchSchoolSelfRegistrationUseCase
import org.openeel.shared.ext.tryOrShowSnackbarOnError
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.select_where_to_store_data
import org.openeel.shared.navigation.EnterHostLink
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.SnackBarDispatcher

data class CreateNewLearningSpaceUiState(
    val hosts: DataLoadState<List<SchoolDirectory>> = DataLoadingState(),
)

class CreateNewLearningSpaceViewModel(
    savedStateHandle: SavedStateHandle,
    private val hostListDataSource: HostListDataSource,
    private val hostConfigSettingsDataSource: HostConfigSettingsDataSource,
    private val launchSchoolSelfRegistrationUseCase: LaunchSchoolSelfRegistrationUseCase,
    private val snackBarDispatcher: SnackBarDispatcher,
) : OpenEelViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(CreateNewLearningSpaceUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.select_where_to_store_data.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
            )
        }

        viewModelScope.launch {
            hostConfigSettingsDataSource.listAsFlow(
                loadParams = DataLoadParams(),
                params = HostConfigSettingsDataSource.GetListParams(
                    key = HostConfigSettingsDataSource.KEY_HOST_LIST,
                ),
            ).collectLatest { hostConfigSettings ->
                val hostListUrl = hostConfigSettings.dataOrNull()?.firstOrNull()?.value?.let {
                    Url(it)
                } ?: return@collectLatest
                hostListDataSource.listAsFlow(
                    loadParams = DataLoadParams(),
                    params = HostListDataSource.GetListParams(url = hostListUrl),
                ).collect { hosts ->
                    _uiState.update { prev ->
                        prev.copy(hosts = hosts)
                    }
                }
            }
        }
    }

    fun onSelectHost(host: SchoolDirectory) {
        viewModelScope.launch {
            snackBarDispatcher.tryOrShowSnackbarOnError {
                launchSchoolSelfRegistrationUseCase(host.baseUrl)
            }
        }
    }

    fun onClickAddLink() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(EnterHostLink))
    }
}
