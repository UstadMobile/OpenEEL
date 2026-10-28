package org.openeel.lib.directory.resources

import kotlinx.coroutines.flow.Flow
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.directory.model.EnrollmentRoleEnum
import org.openeel.lib.directory.model.Person2
import org.openeel.lib.directory.model.PersonRoleEnum

interface PersonDirectoryResource {

    /**
     * @param includeRelated if true, then include all Persons related (as per
     *        Person.relatedPersonUids) to those that match the other criteria.
     */
    data class GetListParams(
        val filterByClazzUid: String? = null,
        val filterByEnrolmentRole: EnrollmentRoleEnum? = null,
        val filterByName: String? = null,
        val filterByPersonRole: PersonRoleEnum? = null,
        val includeRelated: Boolean = false,
    )

    suspend fun list(
        loadParams: DataLoadParams,
        params: GetListParams = GetListParams(),
    ): DataLoadState<List<Person2>>

    fun listAsFlow(
        loadParams: DataLoadParams,
        params: GetListParams = GetListParams(),
    ): Flow<DataLoadState<List<Person2>>>

    suspend fun store(
        list: List<Person2>
    )
}