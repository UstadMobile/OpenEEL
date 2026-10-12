package org.openeel.datalayer.db.school.adapters

import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.school.entities.EnrollmentEntity
import org.openeel.datalayer.school.model.Enrollment

fun EnrollmentEntity.toModel(): Enrollment {
    return Enrollment(
        uid = eUid,
        status = eStatus,
        lastModified = eLastModified,
        stored = eStored,
        metadata = eMetadata,
        classUid = eClassUid,
        personUid = ePersonUid,
        role = eRole,
        beginDate = eBeginDate,
        endDate = eEndDate,
        inviteCode = eInviteCode,
        approvedByPersonUid = eApprovedByPersonUid,
    )
}

fun Enrollment.toEntities(
    uidNumberMapper: UidNumberMapper,
): EnrollmentEntity {
    val eUidNum = uidNumberMapper(uid)
    return EnrollmentEntity(
        eUid = uid,
        eUidNum = eUidNum,
        eStatus = status,
        eLastModified = lastModified,
        eStored = stored,
        eMetadata = metadata,
        eClassUid = classUid,
        eClassUidNum = uidNumberMapper(classUid),
        ePersonUid = personUid,
        ePersonUidNum = uidNumberMapper(personUid),
        eRole = role,
        eBeginDate = beginDate,
        eEndDate = endDate,
        eInviteCode = inviteCode,
        eApprovedByPersonUidNum = approvedByPersonUid?.let { uidNumberMapper(it) } ?: 0,
        eApprovedByPersonUid = approvedByPersonUid,
    )
}
