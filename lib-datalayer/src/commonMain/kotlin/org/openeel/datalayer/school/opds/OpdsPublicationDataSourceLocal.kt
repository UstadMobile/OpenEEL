package world.respect.datalayer.school.opds

import world.respect.lib.dataloadstate.DataReadyState
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.lib.opds.model.Publication

interface OpdsPublicationDataSourceLocal: OpdsPublicationDataSource {

    val publicationNetworkValidationHelper: BaseDataSourceValidationHelper

    suspend fun updateOpdsPublication(publication: DataReadyState<Publication>)

}