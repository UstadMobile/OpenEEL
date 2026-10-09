package world.respect.datalayer.db.school.opds.adapters

import kotlinx.serialization.json.Json
import world.respect.datalayer.db.school.opds.OpdsParentType
import world.respect.datalayer.db.school.opds.entities.ReadiumLinkEntity
import world.respect.datalayer.db.school.opds.entities.ReadiumSubjectEntity
import world.respect.datalayer.db.shared.adapters.asEntities
import world.respect.datalayer.db.shared.adapters.toModel
import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.datalayer.db.shared.ext.takeIfNotEmpty
import world.respect.lib.opds.model.ReadiumSubject
import world.respect.lib.opds.model.ReadiumSubjectObject
import world.respect.lib.opds.model.ReadiumSubjectStringValue
import world.respect.lib.primarykeygen.PrimaryKeyGenerator

class ReadiumSubjectEntities(
    val readiumSubject: ReadiumSubjectEntity,
    val langMapEntities: List<LangMapEntity>,
    val readiumLinkEntities: List<ReadiumLinkEntity>,
)

fun ReadiumSubject.asEntities(
    primaryKeyGenerator: PrimaryKeyGenerator,
    json: Json,
    topParentType: OpdsParentType,
    topParentUid: Long,
    index: Int,
): ReadiumSubjectEntities {
    val rseUid = primaryKeyGenerator.nextId(ReadiumSubjectEntity.TABLE_ID)
    val subjectObject = this as? ReadiumSubjectObject

    return ReadiumSubjectEntities(
        readiumSubject = ReadiumSubjectEntity(
            rseUid = rseUid,
            rseStringValue = (this as? ReadiumSubjectStringValue)?.value,
            rseTopParentType = topParentType,
            rseTopParentUid = topParentUid,
            rseSubjectSortAs = subjectObject?.sortAs,
            rseSubjectCode = subjectObject?.code,
            rseSubjectScheme = subjectObject?.scheme,
            rseIndex = index,
        ),
        langMapEntities = subjectObject?.name?.asEntities(
            lmeTopParentType = topParentType.langMapTopParentType,
            lmeTopParentUid1 = topParentUid,
            lmePropType = LangMapEntity.PropType.READIUM_SUBJECT_NAME,
            lmePropFk = rseUid,
        ) ?: emptyList(),
        readiumLinkEntities = subjectObject?.links?.mapIndexed { linkIndex, link ->
            link.asEntities(
                pkGenerator = primaryKeyGenerator,
                json = json,
                opdsParentType = topParentType,
                opdsParentUid = topParentUid,
                rlePropType = ReadiumLinkEntity.PropertyType.READIUM_SUBJECT_LINKS,
                rlePropFk = rseUid,
                rleIndex = linkIndex,
            )
        }?.flatten() ?: emptyList(),
    )
}

fun ReadiumSubjectEntities.asModel(json: Json): ReadiumSubject {
    return if(this.readiumSubject.rseStringValue != null) {
        ReadiumSubjectStringValue(this.readiumSubject.rseStringValue)
    }else {
        ReadiumSubjectObject(
            name = langMapEntities.toModel(),
            sortAs = readiumSubject.rseSubjectSortAs,
            code = readiumSubject.rseSubjectCode,
            scheme = readiumSubject.rseSubjectScheme,
            links = readiumLinkEntities.asModels(
                json = json,
                propType = ReadiumLinkEntity.PropertyType.READIUM_SUBJECT_LINKS,
                propFk = readiumSubject.rseUid,
            ).takeIfNotEmpty(),
        )
    }
}

