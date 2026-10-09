package org.openeel.shared.viewmodel.apps.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.shared.viewmodel.RespectViewModel
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.select_app
import org.openeel.shared.navigation.AppsDetail
import org.openeel.shared.navigation.EnterLink
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.ext.map
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.findSelfLinks
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.util.ext.resolve

data class AppListUiState(
    val appList: DataLoadState<List<Publication>> = DataReadyState(emptyList())
)

class AppListViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {
    override val scope: Scope = accountManager.requireActiveAccountScope()
    private val _uiState = MutableStateFlow(AppListUiState())
    val uiState = _uiState.asStateFlow()
    private val schoolDataSource: SchoolDataSource by inject()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.select_app.asUiText(),
            )
        }
        viewModelScope.launch {
            val feedUrl = Url(DEFAULT_APP_CATALOG_URL)
            schoolDataSource.opdsFeedDataSource.getByUrlAsFlow(
                url = feedUrl,
                params = DataLoadParams()
            ).collect { dataLoad ->
                _uiState.update { prev ->
                    prev.copy(
                        appList = dataLoad.map {
                            it.resolve(feedUrl).publications ?: emptyList()
                        }
                    )
                }
            }
        }
    }

    fun onClickAddLink() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                EnterLink
            )
        )
    }

    fun onClickApp(app: Publication) {
        val url = app.findSelfLinks().firstOrNull()?.href ?: return
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                AppsDetail.create(Url(url))
            )
        )
    }

    companion object {
        const val DEFAULT_APP_CATALOG_URL = "https://respect.directory/respect-ds/base.json"
    }
}