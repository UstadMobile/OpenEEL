package org.openeel.datalayer.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.ext.firstOrNotLoaded
import org.openeel.datalayer.school.model.PersonPassword
import org.openeel.datalayer.shared.params.GetListCommonParams

fun PersonPasswordDataSource.findByPersonGuidAsFlow(
    guid: String
): Flow<DataLoadState<PersonPassword>> {
    return listAllAsFlow(
        listParams = PersonPasswordDataSource.GetListParams(
            common = GetListCommonParams(guid = guid)
        )
    ).map {
        it.firstOrNotLoaded()
    }
}
