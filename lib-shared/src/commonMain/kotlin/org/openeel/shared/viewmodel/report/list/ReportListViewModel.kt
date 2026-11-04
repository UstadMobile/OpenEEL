package org.openeel.shared.viewmodel.report.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
import org.openeel.datalayer.db.school.domain.report.query.RunReportUseCase
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.dataloadstate.ext.map
import org.openeel.lib.xapi.OpenEelXapiConstants.CATEGORY_REPORT_QUERY_RECIPE
import org.openeel.lib.xapi.ext.distinctByMostRecentTimestampForActivityId
import org.openeel.lib.xapi.ext.objectActivityOrNull
import org.openeel.lib.xapi.ext.objectStatementRefOrNull
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.model.XapiStatementRef
import org.openeel.lib.xapi.model.XapiVerb
import org.openeel.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import org.openeel.shared.domain.account.RespectAccountManager
import org.openeel.shared.domain.report.formatter.CreateGraphFormatterUseCase
import org.openeel.shared.domain.report.formatter.GraphFormatter
import org.openeel.shared.domain.xapi.asRunReportRequest
import org.openeel.shared.domain.xapi.toStatementReportRows
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.report
import org.openeel.shared.generated.resources.reports
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.ReportDetail
import org.openeel.shared.navigation.ReportTemplateList
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.RespectViewModel
import org.openeel.shared.viewmodel.app.appstate.FabUiState
import kotlin.time.Instant

data class ReportListUiState(
    val reportRequests: DataLoadState<List<XapiStatement>> = DataLoadingState(),
    val reportResults: Map<String, RunReportUseCase.RunReportResult> = emptyMap(),
    val xAxisFormatters: Map<String, GraphFormatter<String>> = emptyMap(),
    val yAxisFormatters: Map<String, GraphFormatter<Double>> = emptyMap()
)


class ReportListViewModel(
    savedStateHandle: SavedStateHandle,
    private val createGraphFormatterUseCase: CreateGraphFormatterUseCase,
    private val json: Json,
    private val accountManager: RespectAccountManager,
    private val uidNumberMapper: UidNumberMapper
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()
    private val _uiState = MutableStateFlow(ReportListUiState())
    val uiState: Flow<ReportListUiState> = _uiState.asStateFlow()
    private val schoolDataSource: SchoolDataSource by inject()

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    navigationVisible = true,
                    title = Res.string.reports.asUiText(),
                    fabState = FabUiState(
                        text = Res.string.report.asUiText(),
                        icon = FabUiState.FabIcon.ADD,
                        onClick = { this@ReportListViewModel.onClickAdd() },
                        visible = true
                    ),
                    showBackButton = false,
                )
            }
        }

        val queryRequestStatementsFlow = schoolDataSource.xapiResource.statements.getAsFlow(
            listParams = GetStatementParams(
                activity = CATEGORY_REPORT_QUERY_RECIPE,
                relatedActivities = true,
                verb = XapiVerb.ID_REPORT_QUERY_REQUEST,
            ),
            dataLoadParams = DataLoadParams(),
        )

        val queryResponseStatementsFlow = schoolDataSource.xapiResource.statements.getAsFlow(
            listParams = GetStatementParams(
                verb = XapiVerb.ID_REPORT_QUERY_RESPONSE,
            ),
            dataLoadParams = DataLoadParams(),
        )

        combine(
            queryRequestStatementsFlow,
            queryResponseStatementsFlow,
            accountManager.selectedAccountAndPersonFlow
        ) { requestsState, responsesState, sessionAndPerson ->

            val requests = requestsState.dataOrNull()?.statements ?: emptyList()
            val responses = responsesState.dataOrNull()?.statements ?: emptyList()
            val activeUserPersonUid =
                sessionAndPerson?.person?.guid?.let { uidNumberMapper(it) } ?: 0L

            val responsesByRequestId = responses.groupBy {
                it.objectStatementRefOrNull()?.id
            }

            val reportResults = mutableMapOf<String, RunReportUseCase.RunReportResult>()
            val xAxisFormatters = mutableMapOf<String, GraphFormatter<String>>()
            val yAxisFormatters = mutableMapOf<String, GraphFormatter<Double>>()

            val distinctRequests = requests.distinctByMostRecentTimestampForActivityId()
                .filter { it.id != null }

            distinctRequests.forEach { request ->
                val requestId = request.id.toString()
                val latestResponse = responsesByRequestId[requestId]
                    ?.maxByOrNull { it.timestamp ?: it.stored ?: Instant.DISTANT_PAST }

                latestResponse?.let { response ->
                    val result = RunReportUseCase.RunReportResult(
                        timestamp = response.timestamp?.toEpochMilliseconds() ?: 0L,
                        request = request.asRunReportRequest(
                            json = json,
                            accountPersonUid = activeUserPersonUid,
                            timeZone = TimeZone.currentSystemDefault()
                        ),
                        results = response.toStatementReportRows(json)
                    )
                    reportResults[requestId] = result

                    createGraphFormatterUseCase(
                        reportResult = result,
                        options = CreateGraphFormatterUseCase.FormatterOptions(
                            paramType = String::class,
                            axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.X_AXIS_VALUES
                        )
                    ).let { xAxisFormatters[requestId] = it }

                    createGraphFormatterUseCase(
                        reportResult = result,
                        options = CreateGraphFormatterUseCase.FormatterOptions(
                            paramType = Double::class,
                            axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.Y_AXIS_VALUES
                        )
                    ).let { yAxisFormatters[requestId] = it }
                }
            }

            ReportListUiState(
                reportRequests = requestsState.map { distinctRequests },
                reportResults = reportResults,
                xAxisFormatters = xAxisFormatters,
                yAxisFormatters = yAxisFormatters
            )
        }.onEach { newState ->
            _uiState.update { newState }
        }.launchIn(viewModelScope)
    }

    fun onClickAdd() {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ReportTemplateList
            )
        )
    }

    fun onClickEntry(request: XapiStatement) {
        val activityId = request.objectActivityOrNull()?.id ?: return
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ReportDetail(activityId)
            )
        )
    }

    fun onRemoveReport(request: XapiStatement) {
        val statementId = request.id ?: return
        launchWithLoadingIndicator {
            val actor = accountManager.selectedAccountAndPersonFlow.first()?.xapiAgent
                ?: return@launchWithLoadingIndicator
            val voidingStatement = XapiStatement(
                actor = actor,
                verb = XapiVerb(id = XapiVerb.ID_VOIDED),
                `object` = XapiStatementRef(id = statementId.toString())
            )
            schoolDataSource.xapiResource.statements.post(listOf(voidingStatement))
        }
    }
}