package world.respect.shared.viewmodel.manageuser.otheroption

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.datalayer.RespectAppDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.libutil.ext.normalizeForEndpoint
import world.respect.shared.domain.devmode.GetDevModeEnabledUseCase
import world.respect.shared.domain.urltonavcommand.ResolveUrlToNavCommandUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.invalid_code
import world.respect.shared.generated.resources.invalid_url
import world.respect.shared.generated.resources.other_options
import world.respect.shared.navigation.LoginScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.SchoolDirectoryList
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel


data class OtherOptionsUiState(
    val link: String = "",
    val errorMessage: StringResourceUiText? = null,
    val manageDirectoriesVisible: Boolean = false,
)

class OtherOptionsViewModel(
    savedStateHandle: SavedStateHandle,
    private val respectAppDataSource: RespectAppDataSource,
    private val getDevModeEnabledUseCase: GetDevModeEnabledUseCase,
    private val resolveUrlToNavCommandUseCase: ResolveUrlToNavCommandUseCase,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(OtherOptionsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    title = Res.string.other_options.asUiText(),
                    hideBottomNavigation = true,
                    userAccountIconVisible = false,
                )
            }

            getDevModeEnabledUseCase().also { devModeEnabled ->
                _uiState.update { it.copy(manageDirectoriesVisible = devModeEnabled) }
            }
        }
    }

    fun onLinkChanged(link: String) {
        _uiState.update {
            it.copy(
                link = link,
                errorMessage = null,
            )
        }
    }

     fun onClickNext() {
         val link = uiState.value.link
         if (link.isBlank()) {
             _uiState.update {
                 it.copy(errorMessage = StringResourceUiText(Res.string.invalid_code))
             }
             return
         }

         launchWithLoadingIndicator {
             try {
                 val url = Url(link)
                 val navCommand = resolveUrlToNavCommandUseCase(url)
                 if(navCommand != null) {
                     _navCommandFlow.tryEmit(navCommand)
                     return@launchWithLoadingIndicator
                 }

                 val schoolUrl = url.normalizeForEndpoint()
                 val schoolEntry = respectAppDataSource.schoolDirectoryEntryDataSource
                     .getSchoolDirectoryEntryByUrl(schoolUrl).dataOrNull()

                 if(schoolEntry == null)
                     throw IllegalStateException()

                 _navCommandFlow.tryEmit(
                     NavCommand.Navigate(LoginScreen.create(schoolUrl))
                 )
             }catch(_: Throwable){
                 _uiState.update {
                     it.copy(errorMessage = StringResourceUiText(Res.string.invalid_url))
                 }
             }
         }
    }

    fun onClickManageSchoolDirectories() {
        _navCommandFlow.tryEmit(NavCommand.Navigate(SchoolDirectoryList.create()))
    }

}
