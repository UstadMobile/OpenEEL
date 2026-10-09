package org.openeel.shared.domain.enrollments

import io.github.aakira.napier.Napier
import io.ktor.http.Url
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.school.PersonDataSource
import org.openeel.datalayer.school.ext.asXapiAgent
import org.openeel.datalayer.school.model.EnrollmentRoleEnum
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.model.XapiVerb
import org.openeel.shared.ext.studentsXapiGroup
import kotlin.uuid.ExperimentalUuidApi

class UpdateClazzStudentXapiGroupUseCase(
    private val schoolDataSource: SchoolDataSource,
    private val authenticatedUserPrincipalId: AuthenticatedUserPrincipalId,
    private val schoolUrl: Url,
) {

    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        clazzUid: String,
    ) {
        val studentsInClass = schoolDataSource.personDataSource.list(
            loadParams = DataLoadParams(),
            params = PersonDataSource.GetListParams(
                filterByClazzUid = clazzUid,
                filterByEnrolmentRole = EnrollmentRoleEnum.STUDENT,
            )
        ).dataOrNull()

        val clazz = schoolDataSource.classDataSource.findByGuid(
            params = DataLoadParams(),
            guid = clazzUid,
        ).dataOrNull()

        val activePerson = schoolDataSource.personDataSource.findByGuid(
            loadParams = DataLoadParams(onlyIfCached = true),
            guid = authenticatedUserPrincipalId.guid,
        ).dataOrNull()

        if(studentsInClass == null || clazz == null || activePerson == null) {
            Napier.w("No enrollments: something bad: students=$studentsInClass clazz=$clazz")
            return
        }

        schoolDataSource.xapiResource.statements.post(
            listOf(
                XapiStatement(
                    actor = activePerson.asXapiAgent(schoolUrl),
                    verb = XapiVerb(id = XapiVerb.ID_SAVED),
                    `object` = clazz.studentsXapiGroup(
                        schoolUrl = schoolUrl
                    ).copy(
                        member = studentsInClass.map { it.asXapiAgent(schoolUrl) }
                    )
                )
            )
        )
    }

}