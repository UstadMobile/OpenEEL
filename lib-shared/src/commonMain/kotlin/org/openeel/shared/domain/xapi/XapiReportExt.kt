package org.openeel.shared.domain.xapi

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.doubleOrNull
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.lib.xapi.OpenEelXapiConstants.CATEGORY_REPORT_QUERY_RECIPE
import org.openeel.lib.xapi.ext.decodeFromExtensionOrNull
import org.openeel.lib.xapi.ext.encodeWithExtension
import org.openeel.lib.xapi.ext.objectActivityOrNull
import org.openeel.lib.xapi.extensions.queryresponse.XapiSqlQueryResponse
import org.openeel.lib.xapi.extensions.reportoptions.ReportOptions
import org.openeel.lib.xapi.extensions.reportoptions.StatementReportRow
import org.openeel.lib.xapi.model.XapiActivity
import org.openeel.lib.xapi.model.XapiActivityDefinition
import org.openeel.lib.xapi.model.XapiActivityDefinition.Companion.TYPE_REPORT
import org.openeel.lib.xapi.model.XapiActor
import org.openeel.lib.xapi.model.XapiContext
import org.openeel.lib.xapi.model.XapiContextActivities
import org.openeel.lib.xapi.model.XapiObjectType
import org.openeel.lib.xapi.model.XapiResult
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.model.XapiStatementRef
import org.openeel.lib.xapi.model.XapiVerb
import org.openeel.lib.xapi.model.XapiVerb.Companion.ID_REPORT_QUERY_REQUEST
import org.openeel.lib.xapi.model.XapiVerb.Companion.ID_REPORT_QUERY_RESPONSE
import org.openeel.datalayer.db.school.domain.report.query.RunReportUseCase
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

const val COLUMN_NAME_X_AXIS = "xAxis"
const val COLUMN_NAME_Y_AXIS = "yAxis"
const val COLUMN_NAME_SUBGROUP = "subgroup"


@OptIn(ExperimentalUuidApi::class)
fun createBlankReportStatement(
    reportActivityId: String,
    actor: XapiActor,
    reportOptions: ReportOptions,
    json: Json,
    reportDescription: Map<String, String> = emptyMap(),
    queries: List<String> = emptyList(),
): XapiStatement {
    val now = Clock.System.now()
    return XapiStatement(
        id = Uuid.random(),
        actor = actor,
        verb = XapiVerb(
            id = ID_REPORT_QUERY_REQUEST,
        ),
        `object` = XapiActivity(
            objectType = XapiObjectType.Activity,
            id = reportActivityId,
            definition = XapiActivityDefinition(
                type = TYPE_REPORT,
                description = reportDescription,
            ).encodeWithExtension(
                json = json,
                extensionIri = OpenEelXapiConstants.EXTENSION_REPORT_QUERY,
                serializer = ListSerializer(String.serializer()),
                value = queries
            ).encodeWithExtension(
                json = json,
                extensionIri = OpenEelXapiConstants.EXTENSION_REPORT_OPTIONS,
                serializer = ReportOptions.serializer(),
                value = reportOptions
            )
        ),
        context = XapiContext(
            instructor = actor,
            contextActivities = XapiContextActivities(
                category = listOf(
                    XapiActivity(
                        id = CATEGORY_REPORT_QUERY_RECIPE,
                        objectType = XapiObjectType.Activity
                    )
                ),
                grouping = emptyList()
            )
        ),
        timestamp = now,
        version = "1.0.0"
    )
}

fun XapiStatement.asRunReportRequest(
    json: Json,
    accountPersonUid: Long,
    timeZone: kotlinx.datetime.TimeZone
): RunReportUseCase.RunReportRequest {
    val activity = objectActivityOrNull() ?: throw IllegalArgumentException("Statement object is not an activity")
    val reportOptions = activity.definition?.decodeFromExtensionOrNull(
        json = json,
        extensionIri = OpenEelXapiConstants.EXTENSION_REPORT_OPTIONS,
        deserializer = ReportOptions.serializer()
    ) ?: ReportOptions()

    return RunReportUseCase.RunReportRequest(
        reportUid = activity.id,
        reportOptions = reportOptions,
        accountPersonUid = accountPersonUid,
        timeZone = timeZone,
    )
}

@OptIn(ExperimentalUuidApi::class)
fun createReportResponseStatement(
    requestStatement: XapiStatement,
    result: RunReportUseCase.RunReportResult,
    json: Json,
): XapiStatement {
    val requestId = requestStatement.id ?: throw IllegalArgumentException("Request statement must have an ID")

    // Map RunReportResult to XapiSqlQueryResponse
    val queryResponses = result.results.map { rows ->
        XapiSqlQueryResponse(
            columnNames = listOf(COLUMN_NAME_X_AXIS, COLUMN_NAME_Y_AXIS, COLUMN_NAME_SUBGROUP),
            rows = JsonArray(rows.map { row ->
                buildJsonArray {
                    add(JsonPrimitive(row.xAxis))
                    add(JsonPrimitive(row.yAxis))
                    add(JsonPrimitive(row.subgroup))
                }
            })
        )
    }

    return XapiStatement(
        id = Uuid.random(),
        actor = requestStatement.actor,
        verb = XapiVerb(id = ID_REPORT_QUERY_RESPONSE),
        `object` = XapiStatementRef(id = requestId.toString()),
        context = XapiContext(
            contextActivities = XapiContextActivities(
                parent = listOf(
                    XapiActivity(
                        id = requestStatement.objectActivityOrNull()?.id
                            ?: throw IllegalArgumentException("Request statement must have an ID"),
                        objectType = XapiObjectType.Activity
                    )
                )
            )
        ),
        result = XapiResult(
            extensions = mapOf(
                OpenEelXapiConstants.EXTENSION_REPORT_QUERY_RESULT to json.encodeToJsonElement(
                    ListSerializer(XapiSqlQueryResponse.serializer()),
                    queryResponses
                )
            )
        ),
        timestamp = Clock.System.now(),
        version = "1.0.0"
    )
}

fun XapiStatement.getQueryResultExtension(): JsonArray? {
    return result?.extensions?.get(OpenEelXapiConstants.EXTENSION_REPORT_QUERY_RESULT) as? JsonArray
}

fun XapiStatement.getQueryResultResponses(json: Json): List<XapiSqlQueryResponse> {
    return getQueryResultExtension()?.mapNotNull {
        try {
            json.decodeFromJsonElement(XapiSqlQueryResponse.serializer(), it)
        } catch (_: Exception) {
            null
        }
    } ?: emptyList()
}

fun XapiStatement.toStatementReportRows(json: Json): List<List<StatementReportRow>> {
    return getQueryResultResponses(json).map { response ->
        val columnNames = response.columnNames
        val rows = response.rows

        val xAxisIndex = columnNames.indexOf(COLUMN_NAME_X_AXIS)
        val yAxisIndex = columnNames.indexOf(COLUMN_NAME_Y_AXIS)
        val subgroupIndex = columnNames.indexOf(COLUMN_NAME_SUBGROUP)

        rows.map { rowElement ->
            val row = (rowElement as? JsonArray) ?: emptyList()
            StatementReportRow(
                yAxis = if (yAxisIndex in row.indices) (row[yAxisIndex] as? JsonPrimitive)?.doubleOrNull ?: 0.0 else 0.0,
                xAxis = if (xAxisIndex in row.indices) (row[xAxisIndex] as? JsonPrimitive)?.content ?: "" else "",
                subgroup = if (subgroupIndex in row.indices) (row[subgroupIndex] as? JsonPrimitive)?.content ?: "" else ""
            )
        }
    }
}


fun XapiStatement.withReportQueries(queries: List<String>, json: Json): XapiStatement {
    val activity = objectActivityOrNull() ?: return this
    val updatedActivity = activity.copy(
        definition = (activity.definition ?: XapiActivityDefinition()).encodeWithExtension(
            json = json,
            extensionIri = OpenEelXapiConstants.EXTENSION_REPORT_QUERY,
            serializer = ListSerializer(String.serializer()),
            value = queries
        )
    )
    return copy(`object` = updatedActivity)
}

/**
 * Utility function to substitute '?' placeholders in a SQL string with provided parameters.
 * This is used to create self-contained SQL queries that can be included in xAPI statements.
 */
fun fillSqlParameters(sql: String, params: Array<Any>): String {
    var resultSql = sql
    params.forEach { param ->
        val replacement = when (param) {
            is String -> "'${param.replace("'", "''")}'"
            is Boolean -> if (param) "1" else "0"
            else -> param.toString()
        }
        resultSql = resultSql.replaceFirst("\\?", replacement)
    }
    return resultSql
}