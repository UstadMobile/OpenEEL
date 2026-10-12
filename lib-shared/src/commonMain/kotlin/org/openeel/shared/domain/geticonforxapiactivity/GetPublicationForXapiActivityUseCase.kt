package org.openeel.shared.domain.geticonforxapiactivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.openeel.datalayer.school.opds.OpdsPublicationDataSource
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.ext.map
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.xapi.ext.webPubManifestAsUrlOrNull
import org.openeel.lib.xapi.model.XapiActivity
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.shared.util.ext.resolve

class GetPublicationForXapiActivityUseCase(
    private val opdsPublicationDataSource: OpdsPublicationDataSource,
) {

    operator fun invoke(
        activity: XapiActivity
    ) : Flow<DataLoadState<Publication>> {
        return activity.definition?.webPubManifestAsUrlOrNull()?.let { publicationUrl ->
            opdsPublicationDataSource.getByUrlAsFlow(
                url = publicationUrl,
                params = DataLoadParams(),
                referrerUrl = null,
                expectedPublicationId = null,
            ).map { dataLoadState ->
                dataLoadState.map { it.resolve(publicationUrl) }
            }
        } ?: flowOf(NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND))
    }

    operator fun invoke(
        statement: XapiStatement
    ): Flow<DataLoadState<Publication>>  {
        return (statement.`object` as? XapiActivity)?.let { invoke(it) }
            ?: flowOf(NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND))
    }


}