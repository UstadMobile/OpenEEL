package org.openeel.shared.viewmodel.apps.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.shared.navigation.AppsDetail
import org.openeel.shared.navigation.PublicationDetail
import org.openeel.shared.navigation.OpdsFeedDetail
import org.openeel.shared.viewmodel.RespectViewModel
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.opds.model.OpdsGroup
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.libutil.ext.resolve
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.util.ext.asUiText
import org.openeel.datalayer.db.school.ext.isAdmin
import org.openeel.lib.dataloadstate.ext.map
import org.openeel.lib.opds.model.findCollection
import org.openeel.lib.opds.model.findHighlightCardLinks
import org.openeel.lib.opds.model.findLicenseLink
import org.openeel.lib.opds.model.findAppStoreAndroidLink
import org.openeel.lib.opds.model.findTermsOfServiceLink
import org.openeel.lib.opds.model.toStringMap
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.lib.xapi.ext.objectActivityOrNull
import org.openeel.lib.xapi.model.XapiVerb
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.shared.util.exception.getUiTextOrGeneric
import org.openeel.shared.util.exception.withUiText
import org.openeel.shared.util.ext.resolve
import org.openeel.shared.util.ext.firstSelfLinkOrNull
import org.openeel.shared.viewmodel.app.appstate.Snack
import org.openeel.shared.viewmodel.app.appstate.SnackBarDispatcher
import org.openeel.shared.domain.xapi.createBlankAppListingStatement
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.apps_detail
import org.openeel.shared.generated.resources.invalid_link
import org.openeel.shared.domain.license.GetLicenseLabelUseCase
import org.openeel.shared.domain.license.GetLicenseLabelUseCase.LicenseLabelResult
import org.openeel.shared.domain.school.LaunchCustomTabUseCase

data class AppsDetailUiState(
    val appDetail: DataLoadState<Publication>? = null,
    val publications: List<Publication> = emptyList(),
    val navigation: List<ReadiumLink> = emptyList(),
    val group: List<OpdsGroup> = emptyList(),
    val highlightCards: List<ReadiumLink> = emptyList(),
    val licenseLink: ReadiumLink? = null,
    val googlePlayLink: ReadiumLink? = null,
    val termsOfServiceLink: ReadiumLink? = null,
    val isAdded: Boolean = false,
    val showAddRemoveButton: Boolean = false,
    val licenseLabelResult: LicenseLabelResult? = null,
)

class AppsDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
    private val snackBarDispatcher: SnackBarDispatcher,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val _uiState = MutableStateFlow(AppsDetailUiState())

    val uiState = _uiState.asStateFlow()

    private val route: AppsDetail = savedStateHandle.toRoute()

    private val getLicenseLabelUseCase: GetLicenseLabelUseCase by inject()

    private val launchCustomTabUseCase: LaunchCustomTabUseCase by inject()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.apps_detail.asUiText()
            )
        }

        viewModelScope.launch {
            schoolDataSource.opdsPublicationDataSource.getByUrlAsFlow(
                url = route.manifestUrl,
                params = DataLoadParams(),
                referrerUrl = null,
                expectedPublicationId = null,
            ).collectLatest { result ->
                _uiState.update { prev ->
                    prev.copy(
                        appDetail = result.map { it.resolve(route.manifestUrl) },
                        highlightCards = result.dataOrNull()?.findHighlightCardLinks().orEmpty(),
                        licenseLink = result.dataOrNull()?.findLicenseLink(),
                        googlePlayLink = result.dataOrNull()?.findAppStoreAndroidLink(),
                        termsOfServiceLink = result.dataOrNull()?.findTermsOfServiceLink(),
                    )
                }

                launch {
                    result.dataOrNull()?.findLicenseLink()?.also { licenseLink ->
                        val licenseLabelResult = getLicenseLabelUseCase(
                            route.manifestUrl.resolve(licenseLink.href).toString()
                        )

                        _uiState.update { it.copy(licenseLabelResult = licenseLabelResult) }
                    }
                }


                val collectionLink = result.dataOrNull()?.findCollection() ?: return@collectLatest

                schoolDataSource.opdsFeedDataSource.getByUrlAsFlow(
                    url = route.manifestUrl.resolve(collectionLink.href),
                    params = DataLoadParams()
                ).collect { result ->
                    when (result) {
                        is DataReadyState -> {
                            _uiState.update {
                                val resolvedFeed = result.data.resolve(route.manifestUrl)

                                it.copy(
                                    navigation = resolvedFeed.navigation ?: emptyList(),
                                    publications = resolvedFeed.publications ?: emptyList(),
                                    group = resolvedFeed.groups ?: emptyList()
                                )
                            }
                        }


                        else -> {}
                    }

                }
            }
        }

        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { selected ->
                _uiState.update {
                    it.copy(showAddRemoveButton = selected?.person?.isAdmin() == true)
                }
            }
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
                val routeActivityId = route.manifestUrl.toString()
                val appIsAdded = state.dataOrNull()?.statements
                    ?.any { it.objectActivityOrNull()?.id == routeActivityId } == true
                _uiState.update { it.copy(isAdded = appIsAdded) }
            }
        }
    }

    fun onClickLessonList() {
        val appManifest = uiState.value.appDetail?.dataOrNull()
        appManifest?.findCollection()?.also { defaultLessonListLink ->
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    OpdsFeedDetail.create(
                        opdsFeedUrl = route.manifestUrl.resolve(defaultLessonListLink.href),
                        resultDest = route.resultDest,
                    )
                )
            )
        }
    }

    fun onClickPublication(publication: Publication) {
        try {
            val publicationHref = publication.links.firstSelfLinkOrNull()?.href
                ?: throw IllegalArgumentException().withUiText(Res.string.invalid_link.asUiText())

            val refererUrl = uiState.value.appDetail?.dataOrNull()
                ?.findCollection()?.href

            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    PublicationDetail.create(
                        learningUnitManifestUrl = route.manifestUrl.resolve(publicationHref),
                        refererUrl = refererUrl?.let { Url(it) },
                        expectedIdentifier = publication.metadata.identifier?.toString()
                    )
                )
            )
        }catch(e: Throwable) {
            Napier.w("Something wrong opening publication", e)
            snackBarDispatcher.showSnackBar(Snack(e.getUiTextOrGeneric()))
        }
    }

    fun onClickNavigation(navigation: ReadiumLink) {

        val navigationHref = navigation.href

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                OpdsFeedDetail.create(
                    opdsFeedUrl = route.manifestUrl.resolve(navigationHref),
                )
            )
        )
    }

    fun onClickAdd() {
        viewModelScope.launch {
            val actor = accountManager.selectedAccountAndPersonFlow.first()?.xapiAgent ?: run {
                Napier.w("AppsDetailViewModel: cannot add app, no actor for selected account")
                return@launch
            }

            val statement = createBlankAppListingStatement(
                appActivityId = route.manifestUrl.toString(),
                appTitle = uiState.value.appDetail?.dataOrNull()?.metadata?.title?.toStringMap()
                    ?: emptyMap(),
                actor = actor,
                manifestUrl = route.manifestUrl.toString()
            )
            schoolDataSource.xapiResource.statements.post(listOf(statement))
        }
    }

    fun onClickHighlightCard(hrefLink: String) {
        launchCustomTabUseCase(Url(hrefLink))
    }

    fun onClickLicense(hrefLink: String) {
        launchCustomTabUseCase(Url(hrefLink))
    }

    fun onClickGooglePlay(hrefLink: String) {
        launchCustomTabUseCase(Url(hrefLink))
    }

    fun onClickAlternativeLangVersion(link: ReadiumLink) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = AppsDetail.create(
                    manifestUrl = route.manifestUrl.resolve(link.href)
                )
            )
        )
    }
}