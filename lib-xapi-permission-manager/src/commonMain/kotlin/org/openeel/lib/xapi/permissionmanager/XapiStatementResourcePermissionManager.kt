package org.openeel.lib.xapi.permissionmanager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.xapi.composites.AssignmentAndProgress
import org.openeel.lib.xapi.model.AssignmentSummary
import org.openeel.lib.xapi.model.XapiAgent
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.model.XapiStatementResult
import org.openeel.lib.xapi.permissionmanager.policy.XapiStatementsResourcePolicy
import org.openeel.lib.xapi.resources.XapiStatementsResource
import kotlin.uuid.Uuid

class XapiStatementResourcePermissionManager(
    private val resource: XapiStatementsResource,
    private val policy: XapiStatementsResourcePolicy,
) : XapiStatementsResource {

    override suspend fun post(list: List<XapiStatement>): DataLoadState<List<Uuid>> {
        return resource.post(
            list = list.map { policy.transformWrite(it) }
        )
    }

    override suspend fun get(
        listParams: XapiStatementsResource.GetStatementParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<XapiStatementResult> {
        return resource.get(
            listParams = policy.transformReadParams(listParams),
            dataLoadParams = dataLoadParams,
        )
    }

    override fun getAsFlow(
        listParams: XapiStatementsResource.GetStatementParams,
        dataLoadParams: DataLoadParams
    ): Flow<DataLoadState<XapiStatementResult>> {
        return flow {
            emitAll(
                flow = resource.getAsFlow(
                    listParams = policy.transformReadParams(listParams),
                    dataLoadParams = dataLoadParams,
                )
            )
        }
    }

    override fun getAssignmentProgress(
        activityId: String,
        filterByAssigneeAgent: XapiAgent?
    ): Flow<DataLoadState<AssignmentAndProgress>> {
        return resource.getAssignmentProgress(activityId, filterByAssigneeAgent)
    }

    override fun getAssignmentListAsFlow(
        dataLoadParams: DataLoadParams,
        studentAgent: XapiAgent?
    ): Flow<DataLoadState<List<AssignmentSummary>>> {
        return resource.getAssignmentListAsFlow(dataLoadParams, studentAgent)
    }

}