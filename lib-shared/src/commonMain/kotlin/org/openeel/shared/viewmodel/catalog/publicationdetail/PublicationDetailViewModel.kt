package org.openeel.shared.viewmodel.catalog.publicationdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.ustadmobile.libcache.PublicationPinState
import com.ustadmobile.libcache.UstadCache
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.db.school.ext.isAdminOrTeacher
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.dataloadstate.ext.isReadyAndSettled
import org.openeel.lib.dataloadstate.ext.map
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.lib.opds.model.findLaunchableAppLink
import org.openeel.lib.opds.model.findLicenseLink
import org.openeel.lib.opds.model.findSelfLinks
import org.openeel.lib.xapi.model.XapiStatementResult
import org.openeel.lib.xapi.model.XapiVerb
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.libutil.ext.resolve
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.domain.bookmark.AddBookmarkUseCase
import org.openeel.shared.domain.bookmark.RemoveBookmarkUseCase
import org.openeel.shared.domain.launchapp.gotoappstore.GoToAppStoreUseCase
import org.openeel.shared.domain.launchapp.LaunchAppUseCase
import org.openeel.shared.domain.license.GetLicenseLabelUseCase
import org.openeel.shared.domain.license.GetLicenseLabelUseCase.LicenseLabelResult
import org.openeel.shared.domain.school.LaunchCustomTabUseCase
import org.openeel.shared.ext.tryOrShowSnackbarOnError
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.app_not_found
import org.openeel.shared.generated.resources.something_went_wrong
import org.openeel.shared.navigation.AppsDetail
import org.openeel.shared.navigation.AssignmentEdit
import org.openeel.shared.navigation.PublicationDetail
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.util.exception.getUiTextOrGeneric
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.util.ext.resolve
import org.openeel.shared.viewmodel.RespectViewModel
import org.openeel.shared.viewmodel.app.appstate.Snack
import org.openeel.shared.viewmodel.app.appstate.SnackBarDispatcher
import org.openeel.shared.viewmodel.catalog.PublicationsSelection

data class PublicationDetailUiState(
    val learningUnit: DataLoadState<Publication> = DataLoadingState(),
    val appDetail: DataLoadState<Publication> = DataLoadingState(),
    val pinState: PublicationPinState = PublicationPinState(
        PublicationPinState.Status.NOT_PINNED, 0, 0
    ),
    val showAssignButton: Boolean = false,
    val licenseLabel: LicenseLabelResult? = null,
    val bookmarks: DataLoadState<XapiStatementResult> = DataLoadingState(),
) {
    val openButtonEnabled: Boolean
        get() = learningUnit.dataOrNull() != null

    //The bookmark state is a separate API call.
    val bookmarkButtonEnabled: Boolean
        get() = bookmarks.isReadyAndSettled()

    val isBookmarked: Boolean
        get() = bookmarks.dataOrNull()?.statements?.isNotEmpty() == true
}

class PublicationDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val ustadCache: UstadCache,
    val accountManager: RespectAccountManager,
    private val snackBarDispatcher: SnackBarDispatcher,
    private val goToAppStoreUseCase: GoToAppStoreUseCase,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {


    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val _uiState = MutableStateFlow(PublicationDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val route: PublicationDetail = savedStateHandle.toRoute()

    private val schoolDataSource: SchoolDataSource by inject()

    private val launchAppUseCase: LaunchAppUseCase by inject()

    private val getLicenseLabelUseCase: GetLicenseLabelUseCase by inject()

    private val launchCustomTabUseCase: LaunchCustomTabUseCase by inject()


    private val addBookmarkUseCase: AddBookmarkUseCase by inject()

    private val removeBookmarkUseCase: RemoveBookmarkUseCase by inject()

    init {
        _appUiState.update {
            it.copy(
                title = route.title?.asUiText() ?: it.title
            )
        }

        val learningUnitFlow = schoolDataSource.opdsPublicationDataSource.getByUrlAsFlow(
            url = route.learningUnitManifestUrl,
            params = DataLoadParams(),
            referrerUrl = route.learningUnitManifestUrl,
            expectedPublicationId = route.expectedIdentifier
        ).shareIn(viewModelScope, SharingStarted.Lazily)

        viewModelScope.launch {
            learningUnitFlow.collect { result ->
                _uiState.update { prev ->
                    prev.copy(
                        learningUnit = result.map {
                            it.resolve(route.learningUnitManifestUrl)
                        }
                    )
                }
            }
        }

        /*
         *
         */
        viewModelScope.launch {
            learningUnitFlow.map { learningUnit ->
                learningUnit.dataOrNull()?.findLaunchableAppLink()?.href
            }.distinctUntilChanged().collectLatest { appManifestHref ->
                if(appManifestHref != null) {
                    val appManifestUrl = route.learningUnitManifestUrl.resolve(appManifestHref)

                    schoolDataSource.opdsPublicationDataSource.getByUrlAsFlow(
                        url = appManifestUrl,
                        params = DataLoadParams(),
                        referrerUrl = null,
                        expectedPublicationId = null,
                    ).collect { launchableApp ->
                        _uiState.update { prev ->
                            prev.copy(
                                appDetail = launchableApp.map { it.resolve(appManifestUrl) }
                            )
                        }

                        val licenseLabelResult = launchableApp.dataOrNull()?.findLicenseLink()?.let { licenseLink ->
                            try {
                                getLicenseLabelUseCase(
                                    appManifestUrl.resolve(licenseLink.href).toString()
                                )
                            } catch (e: Exception) {
                                Napier.e("Error fetching license label", e)
                                null
                            }
                        }

                        _uiState.update { it.copy(licenseLabel = licenseLabelResult) }
                    }
                }else {
                    _uiState.update {
                        it.copy(
                            appDetail = NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            ustadCache.publicationPinState(route.learningUnitManifestUrl).collect { pinState ->
                _uiState.update { it.copy(pinState = pinState) }
            }
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { selectedAccount ->
                _uiState.update {
                    it.copy(showAssignButton = selectedAccount?.person?.isAdminOrTeacher() == true)
                }
            }
        }

        viewModelScope.launch {
            schoolDataSource.xapiResource.statements.getAsFlow(
                dataLoadParams = DataLoadParams(),
                listParams = XapiStatementsResource.GetStatementParams(
                    agent = accountManager.selectedAccountAndPersonFlow.filterNotNull()
                        .first().xapiAgent,
                    verb = XapiVerb.ID_BOOKMARKED,
                    activity = route.learningUnitManifestUrl.toString(),
                )
            ).collect { bookmarks ->
                _uiState.update { it.copy(bookmarks = bookmarks) }
            }
        }
    }


    fun onClickOpen() {
        Napier.d("LauncherAppViewModel: onClickOpen")

        //If app is null, then UiState.buttonsEnabled is false, so fallback return should never happen
        viewModelScope.launch {
            try {
                val lessonPublication =
                    _uiState.value.learningUnit.dataOrNull() ?: throw IllegalStateException("Not ready")

                val result = launchAppUseCase(
                    LaunchAppUseCase.LaunchAppRequest(
                        publicationUrl = route.learningUnitManifestUrl,
                        publication = lessonPublication,
                        assignmentActivityId = route.assignmentActivityId,
                    )
                )

                if(result is LaunchAppUseCase.LaunchAppInstallRequired) {
                    val launchableAppToInstall = result.launchableApp
                    if(launchableAppToInstall != null) {
                        goToAppStoreUseCase(
                            GoToAppStoreUseCase.Request(
                                launchableApp = launchableAppToInstall,
                                referrer = result.referrerUrl.toString(),
                            )
                        )
                    }else {
                        snackBarDispatcher.showSnackBar(Snack(Res.string.app_not_found.asUiText()))
                    }
                }else if(result is LaunchAppUseCase.LaunchAppFailed) {
                    snackBarDispatcher.showSnackBar(
                        Snack(
                            result.cause?.getUiTextOrGeneric() ?: Res.string.something_went_wrong.asUiText()
                        )
                    )
                }
            } catch (e: Throwable) {
                Napier.w("Something wrong opening learning unit", e)
                snackBarDispatcher.showSnackBar(Snack(e.getUiTextOrGeneric()))
            }
        }
    }

    fun onClickDownload() {
        viewModelScope.launch {
            snackBarDispatcher.tryOrShowSnackbarOnError {
                when (uiState.value.pinState.status) {
                    PublicationPinState.Status.NOT_PINNED -> {
                        ustadCache.pinPublication(route.learningUnitManifestUrl)
                    }

                    PublicationPinState.Status.READY -> {
                        ustadCache.unpinPublication(route.learningUnitManifestUrl)
                    }

                    else -> {
                        //Do nothing
                    }
                }
            }
        }
    }

    fun onClickAssign() {
        val publicationVal = uiState.value.learningUnit.dataOrNull() ?: return

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = AssignmentEdit.create(
                    assignmentActivityId = null,
                    learningUnitSelected = PublicationsSelection(
                        url = route.learningUnitManifestUrl,
                        selectedPublications = listOf(publicationVal),
                    )
                )
            )
        )
    }

    fun onClickApp(app: Publication) {
        val url = app.findSelfLinks().firstOrNull()?.href ?: return
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = AppsDetail.create(manifestUrl = Url(url))
            )
        )
    }

    fun onClickLicense(app: Publication) {
        val licenseHref = app.findLicenseLink()?.href ?: return

        try {
            launchCustomTabUseCase(Url(licenseHref))
        } catch (e: Throwable) {
            Napier.w("Something wrong opening license", e)
            snackBarDispatcher.showSnackBar(Snack(e.getUiTextOrGeneric()))
        }
    }

    fun onClickBookmark() {
        viewModelScope.launch {
            snackBarDispatcher.tryOrShowSnackbarOnError(
                logMessage = "LearningUnitDetailViewModel: error toggling bookmark"
            ) {
                val bookmarksStmts = uiState.value.bookmarks.dataOrNull()?.statements ?: emptyList()

                if(bookmarksStmts.isEmpty()) {
                    addBookmarkUseCase(
                        agent = accountManager.selectedAccountAndPersonFlow.filterNotNull()
                            .first().xapiAgent,
                        url = route.learningUnitManifestUrl,
                        title = uiState.value.learningUnit.dataOrNull()?.metadata?.title,
                    )
                }else {
                    removeBookmarkUseCase(statements = bookmarksStmts)
                }
            }
        }
    }

    fun onClickAlternativeLangVersion(
        link: ReadiumLink
    ) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = PublicationDetail.create(
                    learningUnitManifestUrl = route.learningUnitManifestUrl.resolve(link.href)
                )
            )
        )
    }

}
