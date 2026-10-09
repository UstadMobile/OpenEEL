package world.respect.shared.viewmodel.apps.launcher

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
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.datalayer.SchoolDataSource
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.devmode.GetDevModeEnabledUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.app
import world.respect.shared.generated.resources.home
import world.respect.shared.generated.resources.empty_list_description_admin
import world.respect.shared.generated.resources.empty_list_description_non_admin
import world.respect.shared.navigation.AppsDetail
import world.respect.shared.navigation.OpdsFeedDetail
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.Settings
import world.respect.shared.navigation.RespectAppLauncher
import world.respect.shared.navigation.RespectAppList
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.datalayer.db.school.ext.isAdmin
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.ext.map
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.findCollection
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.ext.mostRecentByTimestampOrNull
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementRef
import world.respect.lib.xapi.model.XapiVerb
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.libutil.ext.resolve
import world.respect.shared.domain.geticonforxapiactivity.GetPublicationForXapiActivityUseCase
import world.respect.shared.util.ext.appbarTitleString
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.FabUiState

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
    private val accountManager: RespectAccountManager,
    private val getDevModeEnabledUseCase: GetDevModeEnabledUseCase,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

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
                // TODO: For now, make the settingsIcon always visible for testing.
                _appUiState.update {
                    it.copy(
                        fabState = it.fabState.copy(
                            visible = isAdmin && route.resultDest == null
                        ),
                        settingsIconVisible = true
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
