package world.respect.lib.xapi.resources

import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.serializers.InstantAsISO8601
import world.respect.lib.xapi.composites.AssignmentAndProgress
import world.respect.lib.xapi.ext.getUuidOrNull
import world.respect.lib.xapi.model.AssignmentSummary
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.libutil.ext.appendIfNotNull
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * The xAPI Statements Resource
 */
interface XapiStatementsResource {

    enum class GetStatementFormatEnum(val value: String) {
        IDS("ids"), EXACT("exact"), CANONICAL("canonical");

        companion object {

            fun fromValue(value: String): GetStatementFormatEnum {
                return entries.first { it.value == value }
            }
        }
    }

    /**
     * As per https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#213-get-statements
     */
    @Serializable
    data class GetStatementParams(
        val statementId: Uuid? = null,
        val voidedStatementId: Uuid? = null,
        val agent: XapiActor? = null,
        val verb: String? = null,
        val activity: String? = null,
        val registration: Uuid? = null,
        val relatedActivities: Boolean = false,
        val relatedAgents: Boolean = false,
        val since: InstantAsISO8601? = null,
        val until: InstantAsISO8601? = null,
        val limit: Int? = null,
        val format: GetStatementFormatEnum? = null,
        val attachments: Boolean = false,
        val ascending: Boolean = false,
    ) {

        fun toParameters(json: Json): Parameters {
            return ParametersBuilder().also { parameters ->
                parameters.appendIfNotNull("statementId", statementId)
                parameters.appendIfNotNull("voidedStatementId", voidedStatementId)
                agent?.also {
                    parameters.append(
                        "agent", json.encodeToString(XapiActor.serializer(),
                            it
                        )
                    )
                }
                parameters.appendIfNotNull("verb", verb)
                parameters.appendIfNotNull("activity", activity)
                parameters.appendIfNotNull("registration", registration)
                parameters.append("related_activities", relatedActivities.toString())
                parameters.append("related_agents", relatedAgents.toString())
                parameters.appendIfNotNull("since", since?.toString())
                parameters.appendIfNotNull("until", until?.toString())
                parameters.appendIfNotNull("limit", limit?.toString())
                parameters.append("format", format?.value ?: GetStatementFormatEnum.EXACT.value)
                parameters.append("attachments", attachments.toString())
                parameters.append("ascending", ascending.toString())
            }.build()
        }

        companion object {

            fun fromParams(
                params: StringValues,
                json: Json,
            ): GetStatementParams {
                return GetStatementParams(
                    statementId = params.getUuidOrNull("statementId"),
                    voidedStatementId = params.getUuidOrNull("voidedStatementId"),
                    agent = params["agent"]?.let { json.decodeFromString(it) },
                    verb = params["verb"],
                    activity = params["activity"],
                    registration = params.getUuidOrNull("registration"),
                    relatedActivities = params["related_activities"]?.toBoolean() ?: false,
                    relatedAgents = params["related_agents"]?.toBoolean() ?: false,
                    since = params["since"]?.let { Instant.parse(it) },
                    until = params["until"]?.let { Instant.parse(it) },
                    limit = params["limit"]?.toInt(),
                    format = params["format"]?.let { GetStatementFormatEnum.fromValue(it) },
                    attachments = params["attachments"]?.toBoolean() ?: false,
                    ascending = params["ascending"]?.toBoolean() ?: false,
                )

            }
        }
    }

    /**
     * When a statement is received through the put API, the id from the parameter must be put
     * into the statement itself.
     */
    suspend fun post(
        list: List<XapiStatement>
    ): DataLoadState<List<Uuid>>

    suspend fun get(
        listParams: GetStatementParams,
        dataLoadParams: DataLoadParams = DataLoadParams(),
    ): DataLoadState<XapiStatementResult>

    fun getAsFlow(
        listParams: GetStatementParams,
        dataLoadParams: DataLoadParams
    ): Flow<DataLoadState<XapiStatementResult>>


    /**
     * Get results for an assignment with a given activityId where the assignment results are stored
     * as per the ASSIGNMENT_RECIPE
     *
     * @param activityId the xAPI activity id for the assignment itself
     * @param filterByAssigneeAgent when not null, filter results to include only the given agent (eg one student)
     */
    fun getAssignmentProgress(
        activityId: String,
        filterByAssigneeAgent: XapiAgent? = null,
    ): Flow<DataLoadState<AssignmentAndProgress>>

    fun getAssignmentListAsFlow(
        dataLoadParams: DataLoadParams = DataLoadParams(),
        studentAgent: XapiAgent? = null,
    ): Flow<DataLoadState<List<AssignmentSummary>>>

    companion object {

        const val ENDPOINT_NAME = "statements"

    }
}
