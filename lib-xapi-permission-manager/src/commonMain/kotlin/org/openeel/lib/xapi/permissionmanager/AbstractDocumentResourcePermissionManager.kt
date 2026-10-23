package org.openeel.lib.xapi.permissionmanager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.permissionmanager.policy.XapiDocumentResourcePolicy
import org.openeel.lib.xapi.resources.ISingleDocumentParams
import org.openeel.lib.xapi.resources.XapiDocumentResource

abstract class AbstractDocumentResourcePermissionManager<
        MultiDocParams: Any, SingleDocParams: ISingleDocumentParams<MultiDocParams>
>(
    private val resource: XapiDocumentResource<MultiDocParams, SingleDocParams>,
    private val policy: XapiDocumentResourcePolicy<MultiDocParams, SingleDocParams>,
) : XapiDocumentResource<MultiDocParams, SingleDocParams> {

    override suspend fun getMultipleDocuments(
        params: MultiDocParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<List<String>> {
        return resource.getMultipleDocuments(
            params = policy.transformReadMultiDocParams(params),
            dataLoadParams = dataLoadParams,
        )
    }

    override suspend fun get(
        params: SingleDocParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<XapiDocument> {
        return resource.get(
            params = policy.transformReadSingleDocParams(params),
            dataLoadParams = dataLoadParams,
        )
    }

    override fun getAsFlow(
        params: SingleDocParams,
        dataLoadParams: DataLoadParams
    ): Flow<DataLoadState<XapiDocument>> {
        return flow {
            emitAll(
                flow = resource.getAsFlow(
                    params = policy.transformReadSingleDocParams(params),
                    dataLoadParams = dataLoadParams,
                )
            )
        }
    }

    override suspend fun post(
        params: SingleDocParams,
        document: XapiDocument
    ) {
        policy.transformPostDoc(
            document = document,
            params = params,
        ).also {
            resource.post(
                params = it.params,
                document = it.document,
            )
        }
    }

    override suspend fun put(
        params: SingleDocParams,
        document: XapiDocument
    ) {
        policy.transformPutDoc(
            document = document,
            params = params,
        ).also {
            resource.put(
                params = it.params,
                document = it.document
            )
        }
    }

    override suspend fun delete(params: SingleDocParams) {
        resource.delete(
            params = policy.transformDeleteDoc(params)
        )
    }
}