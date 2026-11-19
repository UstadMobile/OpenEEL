package org.openeel.lib.directory.resources

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.openeel.datalayer.school.model.PersonStatusEnum
import org.openeel.lib.directory.model.Person
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.directory.model.EnrollmentRoleEnum
import org.openeel.lib.directory.model.PersonRoleEnum

interface PersonDirectoryResource {

    /**
     * @param filterByPersonGuid load only this person and, when supported, their related persons.
     * @param includeRelated if true, then include all Persons related (as per
     *        Person.relatedPersonUids) to those that match the other criteria.
     */
    data class GetListParams(
        val filterByClazzUid: String? = null,
        val filterByPersonGuid: String? = null,
        val filterByEnrolmentRole: EnrollmentRoleEnum? = null,
        val filterByName: String? = null,
        val filterByPersonRole: PersonRoleEnum? = null,
        val includeRelated: Boolean = false,
        val filterByPersonStatus: PersonStatusEnum? = null,
        val inClassOnDay: LocalDate? = null,
    )

    suspend fun list(
        loadParams: DataLoadParams,
        params: GetListParams = GetListParams(),
    ): DataLoadState<List<Person>>

    fun listAsFlow(
        loadParams: DataLoadParams,
        params: GetListParams = GetListParams(),
    ): Flow<DataLoadState<List<Person>>>

    suspend fun store(
        list: List<Person>
    )
}