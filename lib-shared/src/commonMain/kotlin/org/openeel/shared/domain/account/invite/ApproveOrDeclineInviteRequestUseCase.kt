package world.respect.shared.domain.account.invite

import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.db.school.ext.isStudent
import world.respect.datalayer.db.school.ext.isTeacher
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.ext.copyAsApproved
import world.respect.datalayer.school.ext.inviteCodeOrNull
import world.respect.datalayer.school.ext.relatedPersonRoleEnum
import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.datalayer.school.model.StatusEnum
import world.respect.datalayer.shared.params.GetListCommonParams
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.shared.domain.enrollments.UpdateClazzStudentXapiGroupUseCase
import kotlin.time.Clock


/**
 * Could use the AccountManager as a dependency: any API calls need to be linked to a specific
 * account
 */
class ApproveOrDeclineInviteRequestUseCase(
    private val schoolDataSource: SchoolDataSource,
    private val updateClazzStudentXapiGroupUseCase: UpdateClazzStudentXapiGroupUseCase,
) {

    /**
     * @param approved true/false
     */
    suspend operator fun invoke(
        personUid: String,
        approved: Boolean,
    ) {
        val persons = schoolDataSource.personDataSource.list(
            loadParams = DataLoadParams(),
            params = PersonDataSource.GetListParams(
                common = GetListCommonParams(
                    guid = personUid
                ),
                includeRelated = true,
            )
        ).dataOrNull() ?: throw IllegalArgumentException("Can't find person")

        if(persons.isEmpty())
            throw IllegalArgumentException("Could not find persons")

        val inviteCode = persons.first().inviteCodeOrNull()

        val invite = if(inviteCode != null) {
            schoolDataSource.inviteDataSource.findByCode(code = inviteCode).dataOrNull()
        }else {
            null
        }

        val timeNow = Clock.System.now()
        val studentOrTeacherPerson = persons.firstOrNull { it.isStudent() || it.isTeacher() }

        when {
            invite is ClassInvite -> {
                if(studentOrTeacherPerson == null)
                    throw IllegalStateException("No student or teacher found for class invitation acceptance")

                val enrollmentsToUpdate = schoolDataSource.enrollmentDataSource.list(
                    loadParams = DataLoadParams(),
                    listParams = EnrollmentDataSource.GetListParams(
                        personUid = studentOrTeacherPerson.guid
                    )
                ).dataOrNull() ?: emptyList()

                schoolDataSource.enrollmentDataSource.store(
                    enrollmentsToUpdate.map { enrollment ->
                        if(approved) {
                            enrollment.copyAsApproved().copy(lastModified = timeNow)
                        }else {
                            enrollment.copy(
                                status = StatusEnum.TO_BE_DELETED,
                                lastModified = timeNow
                            )
                        }
                    }
                )

                val hasStudents = enrollmentsToUpdate.any {
                    it.role.relatedPersonRoleEnum == PersonRoleEnum.STUDENT
                }

                if(approved && hasStudents) {
                    updateClazzStudentXapiGroupUseCase(invite.classUid)
                }
            }

            else -> {
                //nothing more to do
            }
        }

        schoolDataSource.personDataSource.store(
            persons.map { person ->
                person.copy(
                    status = if(approved) {
                        PersonStatusEnum.ACTIVE
                    }else {
                        PersonStatusEnum.TO_BE_DELETED
                    },
                    lastModified = timeNow,
                )
            }
        )
    }

}