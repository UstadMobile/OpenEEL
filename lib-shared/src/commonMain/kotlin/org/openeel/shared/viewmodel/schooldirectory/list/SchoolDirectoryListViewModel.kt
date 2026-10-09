package world.respect.shared.viewmodel.schooldirectory.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import io.ktor.http.URLBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.shared.domain.appversioninfo.GetAppVersionInfoUseCase
import world.respect.shared.domain.school.LaunchCustomTabUseCase
import world.respect.shared.ext.tryOrShowSnackbarOnError
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.school_directories
import world.respect.shared.generated.resources.school_directory
import world.respect.shared.generated.resources.select_host
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SchoolDirectoryEdit
import world.respect.shared.navigation.SchoolDirectoryList
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher

data class SchoolDirectoryListUiState(
    val schoolDirectory: List<RespectSchoolDirectory> = emptyList(),
    val mode: SchoolDirectoryMode = SchoolDirectoryMode.MANAGE
)

class SchoolDirectoryListViewModel(
    savedStateHandle: SavedStateHandle,
    private val respectAppDataSource: RespectAppDataSource,
    private val launchCustomTabUseCase: LaunchCustomTabUseCase,
    private val getAppVersionInfoUseCase: GetAppVersionInfoUseCase,
    private val snackBarDispatcher: SnackBarDispatcher,
) : RespectViewModel(savedStateHandle) {

    private val route: SchoolDirectoryList = savedStateHandle.toRoute()
    private val _uiState = MutableStateFlow(SchoolDirectoryListUiState(mode = route.mode))

    val uiState = _uiState.asStateFlow()

    init {
        // Configure the app bar based on mode
        _appUiState.update {
            it.copy(
                title = when (route.mode) {
                    SchoolDirectoryMode.MANAGE -> Res.string.school_directories.asUiText()
                    SchoolDirectoryMode.SELECT -> Res.string.select_host.asUiText()
                },
                hideBottomNavigation = true,
                fabState = when (route.mode) {
                    SchoolDirectoryMode.MANAGE -> it.fabState.copy(
                        icon = FabUiState.FabIcon.ADD,
                        text = Res.string.school_directory.asUiText(),
                        onClick = ::onClickAdd,
                        visible = true
                    )
                    SchoolDirectoryMode.SELECT -> it.fabState.copy(visible = false)
                }
            )
        }

        viewModelScope.launch {
            respectAppDataSource.schoolDirectoryDataSource.allDirectoriesAsFlow().collect { directories ->
                _uiState.update { prev ->
                    prev.copy(schoolDirectory = directories)
                }
            }
        }
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = SchoolDirectoryEdit,
                popUpTo = SchoolDirectoryList.create(SchoolDirectoryMode.MANAGE)
            )
        )
    }

    fun onDeleteDirectory(directory: RespectSchoolDirectory) {
        viewModelScope.launch {
            respectAppDataSource.schoolDirectoryDataSource.deleteDirectory(directory)
        }
    }

    fun onSelectDirectory(directory: RespectSchoolDirectory) {
        when (route.mode) {
            SchoolDirectoryMode.SELECT -> {
                viewModelScope.launch {
                    snackBarDispatcher.tryOrShowSnackbarOnError {
                        val appInfo = getAppVersionInfoUseCase()

                        launchCustomTabUseCase(
                            url = URLBuilder(
                                directory.baseUrl.appendEndpointSegments("school-directory/register-school")
                            ).apply {
                                parameters.append("packageName", appInfo.packageName)
                            }.build()
                        )
                    }
                }
            }

            else -> {
                Napier.d { "SchoolDirectoryListViewModel: onSelectDirectory called but mode is ${route.mode}, not SELECT" }
            }
        }
    }
}