package org.openeel.shared.viewmodel.report.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.school.model.Report
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.report.formatter.CreateGraphFormatterUseCase
import org.openeel.shared.domain.report.formatter.GraphFormatter
import org.openeel.shared.domain.report.model.RunReportResultAndFormatters
import org.openeel.shared.domain.report.query.RunReportUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.report
import org.openeel.shared.generated.resources.reports
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.ReportDetail
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.FabUiState
import kotlin.time.ExperimentalTime

data class ReportListUiState(
    val reportList: DataLoadState<List<Report>> = DataLoadingState(),
    val activeUserPersonUid: Long = 0L,
    val xAxisFormatter: GraphFormatter<String>? = null,
    val yAxisFormatter: GraphFormatter<Double>? = null
)

class ReportListViewModel(
    savedStateHandle: SavedStateHandle,
    private val runReportUseCase: RunReportUseCase,
    private val createGraphFormatterUseCase: CreateGraphFormatterUseCase,
    accountManager: AppAccountManager
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

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

            viewModelScope.launch {
                schoolDataSource.reportDataSource.allReportsAsFlow(template = false).collect {
                    _uiState.update { state ->
                        state.copy(reportList = it)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun runReport(report: Report): Flow<RunReportResultAndFormatters> {
        val request = RunReportUseCase.RunReportRequest(
            reportUid = report.guid.toLong(),
            reportOptions = report.reportOptions,
            accountPersonUid = 0L, // TODO: Get actual user ID
            timeZoneId = TimeZone.currentSystemDefault().id
        )

        return runReportUseCase(request).map { reportResult ->
            val xAxisFormatter = createGraphFormatterUseCase(
                reportResult = reportResult,
                options = CreateGraphFormatterUseCase.FormatterOptions(
                    paramType = String::class,
                    axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.X_AXIS_VALUES
                )
            )

            val yAxisFormatter = createGraphFormatterUseCase(
                reportResult = reportResult,
                options = CreateGraphFormatterUseCase.FormatterOptions(
                    paramType = Double::class,
                    axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.Y_AXIS_VALUES
                )
            )

            RunReportResultAndFormatters(
                reportResult = reportResult,
                xAxisFormatter = xAxisFormatter,
                yAxisFormatter = yAxisFormatter
            )
        }
    }

    fun onClickAdd() {
//        _navCommandFlow.tryEmit(
//            NavCommand.Navigate(
//                ReportTemplateList
//            )
//        )
    }

    fun onClickEntry(entry: Report) {
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                ReportDetail(entry.guid)
            )
        )
    }

    fun onRemoveReport(uid: String) {
        viewModelScope.launch {
            schoolDataSource.reportDataSource.deleteReport(uid)
        }
    }
}