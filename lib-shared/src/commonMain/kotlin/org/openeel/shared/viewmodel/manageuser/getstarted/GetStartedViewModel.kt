package world.respect.shared.viewmodel.manageuser.getstarted

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.ext.isReadyAndSettled
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.shared.domain.getwarnings.GetWarningsUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.lets_get_started
import world.respect.shared.generated.resources.school_not_found
import world.respect.shared.navigation.GetStartedScreen
import world.respect.shared.navigation.LoginScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.OtherOption
import world.respect.shared.navigation.ScanQRCode
import world.respect.shared.navigation.SchoolDirectoryList
import world.respect.shared.resources.UiText
import world.respect.shared.util.LaunchDebouncer
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.LoadingUiState
import world.respect.shared.viewmodel.schooldirectory.list.SchoolDirectoryMode


data class GetStartedUiState(
    val schoolName: String = "",
    val errorText: UiText? = null,
    val showButtons: Boolean = true,
    val errorMessage: UiText? = null,
    val directoryOptions: List<RespectSchoolDirectory> = emptyList(),
    val selectedDirectory: RespectSchoolDirectory? = null,
    val suggestions: List<SchoolDirectoryEntry> = emptyList(),
    val warning: UiText? = null,
    val showAddMySchool: Boolean = false
)


class GetStartedViewModel(
    savedStateHandle: SavedStateHandle,
    val respectAppDataSource: RespectAppDataSource,
    private val getWarningsUseCase: GetWarningsUseCase? = null,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(GetStartedUiState())
    val uiState = _uiState.asStateFlow()
    private val debouncer = LaunchDebouncer(viewModelScope)

    private val route: GetStartedScreen = savedStateHandle.toRoute()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = Res.string.lets_get_started.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                showBackButton = route.canGoBack,
            )
        }

        viewModelScope.launch {
            respectAppDataSource.schoolDirectoryDataSource.allDirectoriesAsFlow().collect { directories ->
                _uiState.update {
                    it.copy(
                        directoryOptions = directories,
                        selectedDirectory = it.selectedDirectory ?: directories.firstOrNull()
                    )
                }
            }
        }

        viewModelScope.launch {
            val warning = getWarningsUseCase?.invoke()
            _uiState.takeIf { warning != null }?.update { it.copy(warning = warning) }
        }
    }

    fun onSchoolNameChanged(name: String) {
        _uiState.update { it.copy(schoolName = name) }

        debouncer.launch(RESPECT_REALMS) {
            val nameIsNotBlank = name.isNotBlank()
            val flow = if(nameIsNotBlank) {
                respectAppDataSource.schoolDirectoryEntryDataSource.listAsFlow(
                    loadParams = DataLoadParams(),
                    listParams = SchoolDirectoryEntryDataSource.GetListParams(
                        name = name,
                        directoryUrl = _uiState.value.selectedDirectory?.baseUrl,
                    )
                )
            }else {
                flowOf(DataReadyState(emptyList()))
            }

            flow.collect { dataState ->
                dataState.dataOrNull()?.also { dataLoaded ->
                    val hasSchoolNotFoundError =
                        nameIsNotBlank && dataLoaded.isEmpty() && dataState.isReadyAndSettled()
                    _uiState.update {
                        it.copy(
                            suggestions = dataLoaded,
                            errorText = Res.string.school_not_found.asUiText()
                                .takeIf { hasSchoolNotFoundError },
                        )
                    }
                }

                _appUiState.update {
                    it.copy(
                        loadingState = if(dataState.remoteState is DataLoadingState) {
                            LoadingUiState.INDETERMINATE
                        }else {
                            LoadingUiState.NOT_LOADING
                        }
                    )
                }
            }
        }
    }


    fun onSchoolSelected(school: SchoolDirectoryEntry) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                LoginScreen.create(school.self)
            )
        )
    }

    fun onClickAddMySchool() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                SchoolDirectoryList.create(SchoolDirectoryMode.SELECT)
            )
        )
    }

    fun onClickScanQRBadge() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(ScanQRCode()))
    }
    fun onClickOtherOptions() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(OtherOption))
    }

    fun onDirectorySelected(directory: RespectSchoolDirectory) {
        _uiState.update { it.copy(selectedDirectory = directory) }
    }

    companion object {

        const val RESPECT_REALMS = "respectRealms"

    }
}
