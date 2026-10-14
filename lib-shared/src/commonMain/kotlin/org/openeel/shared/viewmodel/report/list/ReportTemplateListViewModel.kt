package org.openeel.shared.viewmodel.report.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
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
import org.openeel.lib.xapi.extensions.reportoptions.ReportOptions
import org.openeel.datalayer.school.model.Report
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.report.formatter.CreateGraphFormatterUseCase
import org.openeel.shared.domain.report.model.RunReportResultAndFormatters
import org.openeel.shared.domain.report.query.RunReportUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.select_template
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.ReportEdit
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class ReportTemplateListUiState(
    val templates: DataLoadState<List<Report>> = DataLoadingState(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val activeUserPersonUid: Long = 0L,
)

class ReportTemplateListViewModel(
    savedStateHandle: SavedStateHandle,
    private val runReportUseCase: RunReportUseCase,
    private val createGraphFormatterUseCase: CreateGraphFormatterUseCase,
    accountManager: AppAccountManager
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()
    private val _uiState = MutableStateFlow(ReportTemplateListUiState())
    val uiState = _uiState.asStateFlow()
    private val activeUserPersonUid: Long = 0
    private val schoolDataSource: SchoolDataSource by inject()


    init {
        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = true,
                title = Res.string.select_template.asUiText(),
            )
        }

        viewModelScope.launch {
            schoolDataSource.reportDataSource.allReportsAsFlow(template = true).collect {
                _uiState.update { state ->
                    state.copy(templates = it)
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun runReport(report: Report): Flow<RunReportResultAndFormatters> {
        if (report.guid == "0") {
            return flow {
                emit(
                    RunReportResultAndFormatters(
                        reportResult = RunReportUseCase.RunReportResult(
                            timestamp = Clock.System.now().toEpochMilliseconds(),
                            request = RunReportUseCase.RunReportRequest(
                                reportUid = 0L,
                                reportOptions = ReportOptions(),
                                accountPersonUid = activeUserPersonUid,
                                timeZoneId = TimeZone.currentSystemDefault().id
                            ),
                            results = emptyList()
                        ),
                        xAxisFormatter = null,
                        yAxisFormatter = null
                    )
                )
            }
        } else {
            val request = RunReportUseCase.RunReportRequest(
                reportUid = report.guid.toLong(),
                reportOptions = report.reportOptions,
                accountPersonUid = activeUserPersonUid,
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
    }

    fun onTemplateSelected(template: Report) {
        if (template.guid == "0") { // blank template
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    ReportEdit(null)
                )
            )
        } else {
            _navCommandFlow.tryEmit(
                NavCommand.Navigate(
                    ReportEdit(template.guid)
                )
            )
        }
    }
}