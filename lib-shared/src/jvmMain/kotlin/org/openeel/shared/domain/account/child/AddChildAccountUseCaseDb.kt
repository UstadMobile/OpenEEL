package org.openeel.shared.domain.account.child

import io.github.aakira.napier.Napier
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.datalayer.SchoolDataSourceLocal
import org.openeel.datalayer.exceptions.ForbiddenException
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.datalayer.school.ext.copyWithInviteInfo
import org.openeel.datalayer.school.model.ClassInvite
import org.openeel.datalayer.school.model.Enrollment
import org.openeel.datalayer.school.model.EnrollmentRoleEnum
import org.openeel.datalayer.school.model.Person
import org.openeel.app.userdirectory.model.PersonRoleEnum
import org.openeel.datalayer.school.model.PersonStatusEnum
import org.openeel.shared.domain.account.child.AddChildAccountUseCase.AddChildAccountRequest
import org.openeel.shared.domain.school.SchoolPrimaryKeyGenerator
import org.openeel.shared.util.toPerson
import kotlin.time.Clock

class AddChildAccountUseCaseDb(
    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val schoolDataSource: SchoolDataSourceLocal,
) : AddChildAccountUseCase {

    override suspend operator fun invoke(
        request: AddChildAccountRequest,
    ): AddChildAccountUseCase.AddChildAccountResponse {
        if(request.parentUid != authenticatedUser.guid)
            throw ForbiddenException("Cannot add child to other parents")

        Napier.d("AddChildAccountUseCase: adding child ${request.childPersonInfo.name} for parent $request.parentUid")
        val parentPerson = schoolDataSource.personDataSource.findByGuid(
            loadParams = DataLoadParams(),
            guid = request.parentUid,
        ).dataOrNull() ?: throw IllegalStateException("Parent person not found: $authenticatedUser")

        val childUid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
            Person.TABLE_ID
        ).toString()

        val timeNow = Clock.System.now()

        val parentWithRel = parentPerson.copy(
            relatedPersonUids = parentPerson.relatedPersonUids + childUid,
            lastModified = timeNow
        )

        val childPerson = request.childPersonInfo.toPerson(
            role = PersonRoleEnum.STUDENT,
            guid = childUid,
        ).copy(
            status = parentWithRel.status, //If parent still requires approval, so does child, and vice versa
            relatedPersonUids = listOf(request.parentUid),
            lastModified = timeNow,
        ).let { person ->
            if(person.status == PersonStatusEnum.PENDING_APPROVAL) {
                person.copyWithInviteInfo(invite = request.inviteRedeemRequest.invite)
            }else {
                person
            }
        }

        //Update permission is based on the invite and being the parent
        schoolDataSource.personDataSource.updateLocal(
            listOf(parentWithRel, childPerson)
        )

        val inviteInDb = schoolDataSource.inviteDataSource.findByGuid(
            request.inviteRedeemRequest.invite.uid
        ).dataOrNull()

        if (inviteInDb is ClassInvite){
            val enrollment = Enrollment(
                uid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(Enrollment.TABLE_ID)
                    .toString(),
                classUid = inviteInDb.classUid,
                personUid = childUid,
                role = if(parentPerson.status == PersonStatusEnum.PENDING_APPROVAL) {
                    EnrollmentRoleEnum.PENDING_STUDENT
                }else {
                    EnrollmentRoleEnum.STUDENT
                },
                beginDate = timeNow.toLocalDateTime(TimeZone.currentSystemDefault()).date,
            )

            schoolDataSource.enrollmentDataSource.updateLocal(listOf(enrollment))
        }

        return AddChildAccountUseCase.AddChildAccountResponse(
            childPerson = childPerson,
            parentPerson = parentWithRel,
        )
    }
}