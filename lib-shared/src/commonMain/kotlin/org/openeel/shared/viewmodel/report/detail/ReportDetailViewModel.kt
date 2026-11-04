package org.openeel.shared.viewmodel.report.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.UidNumberMapper
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.xapi.ext.distinctByMostRecentTimestampForActivityId
import org.openeel.lib.xapi.ext.objectActivityNameOrNull
import org.openeel.lib.xapi.ext.objectStatementRefOrNull
import org.openeel.lib.xapi.extensions.reportoptions.ReportOptions
import org.openeel.lib.xapi.model.XapiVerb
import org.openeel.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import org.openeel.shared.domain.report.formatter.CreateGraphFormatterUseCase
import org.openeel.shared.domain.report.formatter.GraphFormatter
import org.openeel.datalayer.db.school.domain.report.query.RunReportUseCase
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.xapi.asRunReportRequest
import org.openeel.shared.domain.xapi.toStatementReportRows
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.edit
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.ReportDetail
import org.openeel.shared.navigation.ReportEdit
import org.openeel.shared.resources.LangMapUiText
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.FabUiState

data class ReportDetailUiState(
    val title: LangMapUiText? = null,
    val reportResult: RunReportUseCase.RunReportResult? = null,
    val errorMessage: String? = null,
    val reportOptions: ReportOptions = ReportOptions(),
    val xAxisFormatter: GraphFormatter<String>? = null,
    val yAxisFormatter: GraphFormatter<Double>? = null,
    val subgroupFormatter: GraphFormatter<String>? = null
)

class ReportDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val createGraphFormatterUseCase: CreateGraphFormatterUseCase,
    private val json: Json,
    private val accountManager: AppAccountManager,
    private val uidNumberMapper: UidNumberMapper,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()
    private val route: ReportDetail = savedStateHandle.toRoute()
    private val reportUid = route.reportUid
    private val schoolDataSource: SchoolDataSource by inject()
    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: Flow<ReportDetailUiState> = _uiState.asStateFlow()
    private var activeUserPersonUid: Long = 0L

    init {
        viewModelScope.launch {
            accountManager.selectedAccountAndPersonFlow.collect { sessionAndPerson ->
                activeUserPersonUid =
                    sessionAndPerson?.person?.guid?.let { uidNumberMapper(it) } ?: 0L
            }
        }

        _appUiState.update { prev ->
            prev.copy(
                fabState = FabUiState(
                    visible = true,
                    text = Res.string.edit.asUiText(),
                    icon = FabUiState.FabIcon.EDIT,
                    onClick = {
                        _navCommandFlow.tryEmit(
                            NavCommand.Navigate(
                                ReportEdit(reportActivityUid = reportUid)
                            )
                        )
                    },
                )
            )
        }
        schoolDataSource.xapiResource.statements.getAsFlow(
            listParams = GetStatementParams(
                activity = reportUid,
                relatedActivities = true,
            ),
            dataLoadParams = DataLoadParams(),
        ).onEach { loadState ->
            val statements = loadState.dataOrNull()?.statements ?: emptyList()
            val requests = statements.filter { it.verb.id == XapiVerb.ID_REPORT_QUERY_REQUEST }
            val responses = statements.filter { it.verb.id == XapiVerb.ID_REPORT_QUERY_RESPONSE }

            val statement = requests
                .distinctByMostRecentTimestampForActivityId()
                .firstOrNull()

            if (statement == null) {
                return@onEach
            }

            val requestId = statement.id.toString()
            val latestResponse = responses
                .filter { it.objectStatementRefOrNull()?.id == requestId }
                .maxByOrNull { it.timestamp ?: it.stored ?: kotlin.time.Instant.DISTANT_PAST }

            val reportResult = latestResponse?.let { response ->
                RunReportUseCase.RunReportResult(
                    timestamp = response.timestamp?.toEpochMilliseconds() ?: 0L,
                    request = statement.asRunReportRequest(
                        json = json,
                        accountPersonUid = activeUserPersonUid,
                        timeZone = TimeZone.currentSystemDefault()
                    ),
                    results = response.toStatementReportRows(json)
                )
            }

            val xAxisFormatter = reportResult?.let {
                createGraphFormatterUseCase(
                    reportResult = it,
                    options = CreateGraphFormatterUseCase.FormatterOptions(
                        paramType = String::class,
                        axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.X_AXIS_VALUES
                    )
                )
            }
            val subgroupFormatter = reportResult?.let {
                createGraphFormatterUseCase(
                    reportResult = it,
                    options = CreateGraphFormatterUseCase.FormatterOptions(
                        paramType = String::class,
                        axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.X_AXIS_VALUES,
                        forSubgroup = true
                    )
                )
            }
            val yAxisFormatter = reportResult?.let {
                createGraphFormatterUseCase(
                    reportResult = it,
                    options = CreateGraphFormatterUseCase.FormatterOptions(
                        paramType = Double::class,
                        axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.Y_AXIS_VALUES
                    )
                )
            }

            val newState = ReportDetailUiState(
                title = statement.objectActivityNameOrNull()?.let { LangMapUiText(it) },
                reportOptions = reportResult?.request?.reportOptions ?: ReportOptions(),
                reportResult = reportResult,
                xAxisFormatter = xAxisFormatter,
                yAxisFormatter = yAxisFormatter,
                subgroupFormatter = subgroupFormatter,
            )

            _uiState.update { newState }
            _appUiState.update { prev ->
                prev.copy(title = newState.title)
            }
        }.launchIn(viewModelScope)
    }
}