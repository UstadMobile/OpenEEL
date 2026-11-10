package org.openeel.shared.viewmodel.manageuser.learningspace

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.openeel.shared.domain.school.LaunchSchoolSelfRegistrationUseCase
import org.openeel.shared.ext.tryOrShowSnackbarOnError
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.select_where_to_store_data
import org.openeel.shared.navigation.EnterHostLink
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.SnackBarDispatcher
import org.openeel.shared.viewmodel.app.appstate.Snack

data class HostListConfig(val url: Url)

data class CreateNewLearningSpaceUiState(
    val hosts: List<CreateNewLearningSpaceViewModel.Host> = emptyList(),
)

class CreateNewLearningSpaceViewModel(
    savedStateHandle: SavedStateHandle,
    private val httpClient: HttpClient,
    private val json: Json,
    private val hostListConfig: HostListConfig,
    private val launchSchoolSelfRegistrationUseCase: LaunchSchoolSelfRegistrationUseCase,
    private val snackBarDispatcher: SnackBarDispatcher,
) : OpenEelViewModel(savedStateHandle) {

    @Serializable
    data class Host(
        val baseUrl: Url,
        val name: String,
    )

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

        launchWithLoadingIndicator(
            onShowError = { snackBarDispatcher.showSnackBar(Snack(it)) },
        ) {
            val hosts = json.decodeFromString<List<Host>>(
                httpClient.get(hostListConfig.url) {
                    expectSuccess = true
                    header(HttpHeaders.CacheControl, "no-cache, no-store")
                }.bodyAsText()
            )
            _uiState.update { it.copy(hosts = hosts) }
        }
    }

    fun onSelectHost(host: Host) {
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
