package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.PersonEntity
import world.respect.datalayer.db.school.entities.PersonEntityWithRoles
import world.respect.datalayer.db.school.entities.PersonRelatedPersonEntity
import world.respect.datalayer.db.school.entities.PersonRoleEntity
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRole
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


data class PersonEntities(
    val personEntity: PersonEntity,
    val personRoleEntities: List<PersonRoleEntity> = emptyList(),
    val relatedPersonEntities: List<PersonRelatedPersonEntity>,
)

fun PersonEntityWithRoles.toPersonEntities() = PersonEntities(
    personEntity = person,
    personRoleEntities = roles,
    relatedPersonEntities = relatedPersons,
)

@OptIn(ExperimentalTime::class)
fun PersonEntities.toModel(): Person {
    return Person(
        guid = personEntity.pGuid,
        status = personEntity.pStatus,
        userActive = personEntity.pActive,
        lastModified = Instant.fromEpochMilliseconds(personEntity.pLastModified),
        stored = Instant.fromEpochMilliseconds(personEntity.pStored),
        metadata = personEntity.pMetadata,
        username = personEntity.pUsername,
        givenName = personEntity.pGivenName,
        familyName = personEntity.pFamilyName,
        middleName = personEntity.pMiddleName,
        gender = personEntity.pGender,
        roles = personRoleEntities.map {
            PersonRole(
                isPrimaryRole = it.prIsPrimaryRole,
                roleEnum = it.prRoleEnum,
                beginDate = it.prBeginDate,
                endDate = it.prEndDate,
            )
        },
        relatedPersonUids = relatedPersonEntities.map { it.prpOtherPersonUid },
        dateOfBirth = personEntity.pDateOfBirth,
        phoneNumber = personEntity.pPhoneNumber,
        email = personEntity.pEmail,
    )
}

fun Person.toEntities(
    uidNumberMapper: UidNumberMapper
): PersonEntities {
    val pGuidHash = uidNumberMapper(guid)
    return PersonEntities(
        personEntity = PersonEntity(
            pGuid = guid,
            pGuidHash = pGuidHash,
            pActive = userActive,
            pStatus = status,
            pLastModified = lastModified.toEpochMilliseconds(),
            pStored = stored.toEpochMilliseconds(),
            pMetadata = metadata,
            pUsername = username,
            pGivenName = givenName,
            pFamilyName = familyName,
            pMiddleName = middleName,
            pGender = gender,
            pDateOfBirth = dateOfBirth,
            pEmail = email,
            pPhoneNumber = phoneNumber
        ),
        personRoleEntities = roles.map {
            PersonRoleEntity(
                prPersonGuidHash = pGuidHash,
                prIsPrimaryRole = it.isPrimaryRole,
                prRoleEnum = it.roleEnum,
                prEndDate = it.endDate,
                prBeginDate = it.beginDate,
            )
        },
        relatedPersonEntities = relatedPersonUids.map {
            PersonRelatedPersonEntity(
                prpPersonUidNum = pGuidHash,
                prpOtherPersonUid = it,
                prpOtherPersonUidNum = uidNumberMapper(it),
            )
        }
    )
}


