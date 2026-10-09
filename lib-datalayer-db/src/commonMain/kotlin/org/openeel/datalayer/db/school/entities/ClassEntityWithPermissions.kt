package world.respect.datalayer.db.school.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ClassEntityWithPermissions(
    @Embedded
    val clazz: ClassEntity,

    @Relation(
        parentColumn = "cGuidHash",
        entityColumn = "cpeClassUidNum",
    )
    val permissions: List<ClassPermissionEntity>,

)