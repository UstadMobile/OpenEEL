package world.respect.shared.domain.report.query

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * Client side implementation of RunReportUseCase. The flow will return cached results from the
 * local database immediately when available. If cached results are not available, or no longer
 * fresh, then a request will be made to the server. Those updated results will be cached and then
 * emitted from the flow.
 */
class RunReportUseCaseClientImpl(
    private val clientNodeId: Long,
    private val clientNodeAuth: String,
    private val httpClient: HttpClient,
    private val json: Json,
) : RunReportUseCase {
    override fun invoke(
        request: RunReportUseCase.RunReportRequest
    ): Flow<RunReportUseCase.RunReportResult> {
        return flow {


        }
    }
}