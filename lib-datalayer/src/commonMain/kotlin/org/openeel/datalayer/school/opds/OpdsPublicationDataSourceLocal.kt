package org.openeel.datalayer.school.opds

import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.datalayer.networkvalidation.BaseDataSourceValidationHelper
import org.openeel.lib.opds.model.Publication

interface OpdsPublicationDataSourceLocal: OpdsPublicationDataSource {

    val publicationNetworkValidationHelper: BaseDataSourceValidationHelper

    suspend fun updateOpdsPublication(publication: DataReadyState<Publication>)

}