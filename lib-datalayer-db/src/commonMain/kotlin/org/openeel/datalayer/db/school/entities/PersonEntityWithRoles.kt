package world.respect.datalayer.db.school.entities

import androidx.room.Embedded
import androidx.room.Relation

data class PersonEntityWithRoles(
    @Embedded
    val person: PersonEntity,

    @Relation(
        parentColumn = "pGuidHash",
        entityColumn = "prPersonGuidHash"
    )
    val roles: List<PersonRoleEntity>,

    @Relation(
        parentColumn = "pGuidHash",
        entityColumn = "prpPersonUidNum",
    )
    val relatedPersons: List<PersonRelatedPersonEntity>
)