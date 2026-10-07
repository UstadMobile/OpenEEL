package org.openeel.shared.viewmodel.apps.launcher

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.datalayer.SchoolDataSource
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.devmode.GetDevModeEnabledUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.app
import org.openeel.shared.generated.resources.home
import org.openeel.shared.generated.resources.empty_list_description_admin
import org.openeel.shared.generated.resources.empty_list_description_non_admin
import org.openeel.shared.navigation.AppsDetail
import org.openeel.shared.navigation.OpdsFeedDetail
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.Settings
import org.openeel.shared.navigation.RespectAppLauncher
import org.openeel.shared.navigation.RespectAppList
import org.openeel.shared.resources.UiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.datalayer.db.school.ext.isAdmin
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.dataloadstate.ext.map
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.findCollection
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.lib.xapi.ext.mostRecentByTimestampOrNull
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.model.XapiStatementRef
import org.openeel.lib.xapi.model.XapiVerb
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.libutil.ext.resolve
import org.openeel.shared.domain.geticonforxapiactivity.GetPublicationForXapiActivityUseCase
import org.openeel.shared.util.ext.appbarTitleString
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.FabUiState

data class AppLauncherUiState(
    val apps: DataLoadState<List<XapiStatement>> = DataLoadingState(),
    val respectPublicationForXapiStatement: (XapiStatement) -> Flow<DataLoadState<Publication>> = {
        emptyFlow()
    },
    val canRemove: Boolean = false,
    val emptyListDescription: UiText? = null,
    val appMustLoadToBeClickable: Boolean = false,
) {

    fun isAppClickable(appState: DataLoadState<Publication>): Boolean {
        return !appMustLoadToBeClickable || appState.dataOrNull() != null
    }

}

class AppLauncherViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: AppAccountManager,
    private val getDevModeEnabledUseCase: GetDevModeEnabledUseCase,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(AppLauncherUiState())

    val uiState = _uiState.asStateFlow()

    private val route: RespectAppLauncher = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val getPublicationForXapiActivityUseCase: GetPublicationForXapiActivityUseCase by inject()

    init {
        _appUiState.update {
            it.copy(
                title = route.opdsPickType?.appbarTitleString?.asUiText() ?: Res.string.home.asUiText(),
                onClickSettings = ::onClickSettings,
                fabState = FabUiState(
                    icon = FabUiState.FabIcon.ADD,
                    text = Res.string.app.asUiText(),
                    onClick = {
                        _navCommandFlow.tryEmit(
                            NavCommand.Navigate(
                                RespectAppList
                            )
                        )
                    }
                ),
                hideBottomNavigation = route.resultDest != null,
                showBackButton = route.resultDest != null,
            )
        }

        _uiState.update { prev ->
            prev.copy(
                respectPublicationForXapiStatement = getPublicationForXapiActivityUseCase::invoke,
                appMustLoadToBeClickable = route.resultDest != null,
            )
        }

        viewModelScope.launch {
            schoolDataSource.xapiResource.statements.getAsFlow(
                listParams = XapiStatementsResource.GetStatementParams(
                    verb = XapiVerb.ID_LISTED_APP,
                    activity = OpenEelXapiConstants.CATEGORY_APP_LISTING_RECIPE,
                    relatedActivities = true,
                ),
                dataLoadParams = DataLoadParams(),
            ).collectLatest { state ->
                _uiState.update { it.copy(apps = state.map { result -> result.statements }) }
            }
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { selected ->
                val isAdmin = selected?.person?.isAdmin() == true
                val devModeEnabled = getDevModeEnabledUseCase()
                _appUiState.update {
                    it.copy(
                        fabState = it.fabState.copy(
                            visible = isAdmin && route.resultDest == null
                        ),
                        settingsIconVisible = isAdmin && devModeEnabled,
                    )
                }
                _uiState.update {
                    it.copy(
                        canRemove = isAdmin,
                        emptyListDescription = if(isAdmin)
                            Res.string.empty_list_description_admin.asUiText()
                        else
                            Res.string.empty_list_description_non_admin.asUiText()
                    )
                }
            }
        }
    }


    fun onClickApp(app: DataLoadState<Publication>) {
        val url = app.metaInfo.url ?: return

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                if(route.resultDest != null) {
                    val defaultLessonListHref = app.dataOrNull()?.findCollection()?.href
                        ?: return
                    val defaultLessonUrl = url.resolve(defaultLessonListHref)

                    OpdsFeedDetail.create(
                        opdsFeedUrl = defaultLessonUrl,
                        resultDest = route.resultDest,
                        opdsPickType = route.opdsPickType,
                    )
                }else {
                    AppsDetail.create(
                        manifestUrl = url,
                        resultDest = route.resultDest,
                    )
                }
            )
        )
    }
    fun onClickSettings() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(Settings)
        )
    }

    fun onClickRemove(app: DataLoadState<Publication>) {
        val manifestUrl = app.metaInfo.url ?: run {
            Napier.w("app has no manifest url, cannot remove")
            return
        }

        viewModelScope.launch {
            val existing = schoolDataSource.xapiResource.statements.get(
                XapiStatementsResource.GetStatementParams(
                    verb = XapiVerb.ID_LISTED_APP,
                    activity = manifestUrl.toString(),
                ),
                DataLoadParams(),
            ).dataOrNull()?.statements?.mostRecentByTimestampOrNull() ?: run {
                Napier.w("no listed-app statement found for $manifestUrl")
                return@launch
            }

            val actor = accountManager.selectedAccountAndPersonFlow.first()?.xapiAgent ?: run {
                Napier.w("no actor for selected account, cannot void")
                return@launch
            }

            schoolDataSource.xapiResource.statements.post(
                listOf(
                    XapiStatement(
                        actor = actor,
                        verb = XapiVerb(id = XapiVerb.ID_VOIDED),
                        `object` = XapiStatementRef(id = existing.id.toString()),
                    )
                )
            )
        }
    }

}
