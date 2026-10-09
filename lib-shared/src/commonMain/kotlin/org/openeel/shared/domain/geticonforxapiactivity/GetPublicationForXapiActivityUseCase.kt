package world.respect.shared.domain.geticonforxapiactivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import world.respect.datalayer.school.opds.OpdsPublicationDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.dataloadstate.ext.map
import world.respect.lib.opds.model.Publication
import world.respect.lib.xapi.ext.webPubManifestAsUrlOrNull
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiStatement
import world.respect.shared.util.ext.resolve

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