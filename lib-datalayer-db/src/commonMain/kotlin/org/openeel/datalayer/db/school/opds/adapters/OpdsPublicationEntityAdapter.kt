package world.respect.datalayer.db.school.opds.adapters

import kotlinx.serialization.json.Json
import world.respect.lib.dataloadstate.DataLoadMetaInfo
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.opds.OpdsParentType
import world.respect.datalayer.db.school.opds.entities.OpdsPublicationEntity
import world.respect.datalayer.db.school.opds.entities.ReadiumLinkEntity
import world.respect.datalayer.db.school.opds.entities.ReadiumSubjectEntity
import world.respect.datalayer.db.shared.adapters.asEntities
import world.respect.datalayer.db.shared.adapters.toModel
import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.datalayer.db.shared.ext.takeIfNotEmpty
import world.respect.lib.opds.model.LangMap
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ReadiumMetadata
import world.respect.lib.primarykeygen.PrimaryKeyGenerator

data class OpdsPublicationEntities(
    val opdsPublicationEntity: OpdsPublicationEntity,
    val langMapEntities: List<LangMapEntity>,
    val linkEntities: List<ReadiumLinkEntity>,
    val subjectEntities: List<ReadiumSubjectEntity>,
)

fun Publication.asEntities(
    dataLoadResult: DataReadyState<*>?,
    primaryKeyGenerator: PrimaryKeyGenerator,
    json: Json,
    uidNumberMapper: UidNumberMapper,
    feedUid: Long,
    groupUid: Long,
    feedIndex: Int,
): OpdsPublicationEntities {
    val opeUid = primaryKeyGenerator.nextId(OpdsPublicationEntity.TABLE_ID)

    fun List<ReadiumLink>?.toEntitiesSub(
        propType: ReadiumLinkEntity.PropertyType,
    ): List<ReadiumLinkEntity> {
        return this?.mapIndexed { linkIndex, link ->
            link.asEntities(
                pkGenerator = primaryKeyGenerator,
                json = json,
                opdsParentType = OpdsParentType.OPDS_PUBLICATION,
                opdsParentUid = opeUid,
                rlePropType = propType,
                rlePropFk = opeUid,
                rleIndex = linkIndex,
            )
        }?.flatten() ?: emptyList()
    }

    fun LangMap?.toEntitiesSub(
        propType: LangMapEntity.PropType
    ): List<LangMapEntity> {
        return this?.asEntities(
            lmeTopParentType = LangMapEntity.TopParentType.OPDS_PUBLICATION,
            lmeTopParentUid1 = opeUid,
            lmePropType = propType,
            lmePropFk = 0,
        ) ?: emptyList()
    }

    val subjectEntities = metadata.subject?.mapIndexed { index, subject ->
        subject.asEntities(
            primaryKeyGenerator = primaryKeyGenerator,
            json = json,
            topParentType = OpdsParentType.OPDS_PUBLICATION,
            topParentUid = opeUid,
            index = index
        )
    }.orEmpty()

    return OpdsPublicationEntities(
        opdsPublicationEntity = OpdsPublicationEntity(
            opeUid = opeUid,
            opeOfeUid = feedUid,
            opeOgeUid = groupUid,
            opeIndex = feedIndex,
            opeUrl = dataLoadResult?.metaInfo?.url,
            opeUrlHash = dataLoadResult?.metaInfo?.url?.toString()?.let { uidNumberMapper(it) } ?: 0,
            opeLastModified = dataLoadResult?.metaInfo?.lastModified ?: 0,
            opeEtag = dataLoadResult?.metaInfo?.etag,
            opeMdIdentifier = metadata.identifier,
            opeMdLanguage = metadata.language,
            opeMdType = metadata.type,
            opeMdDescription = metadata.description,
            opeMdNumberOfPages = metadata.numberOfPages,
            opeMdDuration = metadata.duration,
        ),
        langMapEntities = metadata.title.toEntitiesSub(LangMapEntity.PropType.OPDS_PUB_TITLE) +
            metadata.sortAs.toEntitiesSub(LangMapEntity.PropType.OPDS_PUB_SORT_AS) +
            metadata.subtitle.toEntitiesSub(LangMapEntity.PropType.OPDS_PUB_SUBTITLE) +
            subjectEntities.flatMap { it.langMapEntities },
        linkEntities = links.toEntitiesSub(ReadiumLinkEntity.PropertyType.OPDS_PUB_LINKS) +
                images.toEntitiesSub(ReadiumLinkEntity.PropertyType.OPDS_PUB_IMAGES) +
                readingOrder.toEntitiesSub(ReadiumLinkEntity.PropertyType.OPDS_PUB_READING_ORDER) +
                resources.toEntitiesSub(ReadiumLinkEntity.PropertyType.OPDS_PUB_RESOURCES) +
                toc.toEntitiesSub(ReadiumLinkEntity.PropertyType.OPDS_PUB_TOC) +
                subjectEntities.flatMap { it.readiumLinkEntities },
        subjectEntities = subjectEntities.map { it.readiumSubject },
    )
}

/**
 * Convert a list of database entities to OpdsPublication model class.
 *
 * Caveat: when a model has a nullable list there is no differentiation made between the list value
 * being null and an empty list. The model entity will be restored as null if the list is empty.
 */
fun OpdsPublicationEntities.asModel(
    json: Json,
): DataReadyState<Publication> {
    fun List<LangMapEntity>.toModelSub(
        propType: LangMapEntity.PropType
    ): LangMap {
        return this.filter {
            it.lmePropType == propType && it.lmeTopParentUid1 == opdsPublicationEntity.opeUid
        }.toModel()
    }

    fun List<LangMapEntity>.toModelSubOrNull(
        propType: LangMapEntity.PropType
    ): LangMap? {
        return this.filter {
            it.lmePropType == propType && it.lmeTopParentUid1 == opdsPublicationEntity.opeUid
        }.takeIf { it.isNotEmpty() }?.toModel()
    }

    fun List<ReadiumLinkEntity>.asModelsSub(
        propType: ReadiumLinkEntity.PropertyType
    ): List<ReadiumLink> {
        return asModels(
            json = json,
            propType = propType,
            propFk = opdsPublicationEntity.opeUid
        )
    }

    return DataReadyState(
        data = Publication(
            metadata = ReadiumMetadata(
                type = opdsPublicationEntity.opeMdType,
                title = langMapEntities.toModelSub(
                    LangMapEntity.PropType.OPDS_PUB_TITLE
                ),
                sortAs = langMapEntities.toModelSubOrNull(LangMapEntity.PropType.OPDS_PUB_SORT_AS),
                subtitle = langMapEntities.toModelSubOrNull(LangMapEntity.PropType.OPDS_PUB_SUBTITLE),
                identifier = opdsPublicationEntity.opeMdIdentifier,
                language = opdsPublicationEntity.opeMdLanguage,
                description = opdsPublicationEntity.opeMdDescription,
                numberOfPages = opdsPublicationEntity.opeMdNumberOfPages,
                duration = opdsPublicationEntity.opeMdDuration,
                subject = subjectEntities.map { subjectEntity ->
                    ReadiumSubjectEntities(
                        readiumSubject = subjectEntity,
                        langMapEntities = langMapEntities.filter {
                            it.lmePropFk == subjectEntity.rseUid &&
                                it.lmePropType == LangMapEntity.PropType.READIUM_SUBJECT_NAME
                        },
                        readiumLinkEntities = linkEntities,//Will be filtered/handled by asModel fn
                    ).asModel(json)
                }.takeIfNotEmpty(),
            ),
            links = linkEntities.asModelsSub(ReadiumLinkEntity.PropertyType.OPDS_PUB_LINKS),
            images = linkEntities.asModelsSub(
                ReadiumLinkEntity.PropertyType.OPDS_PUB_IMAGES
            ).takeIfNotEmpty(),
            readingOrder = linkEntities.asModelsSub(
                ReadiumLinkEntity.PropertyType.OPDS_PUB_READING_ORDER
            ).takeIfNotEmpty(),
            resources = linkEntities.asModelsSub(
                ReadiumLinkEntity.PropertyType.OPDS_PUB_RESOURCES
            ).takeIfNotEmpty(),
            toc = linkEntities.asModelsSub(
                ReadiumLinkEntity.PropertyType.OPDS_PUB_TOC
            ).takeIfNotEmpty(),
        ),
        metaInfo = DataLoadMetaInfo(
            url = opdsPublicationEntity.opeUrl,
            lastModified = opdsPublicationEntity.opeLastModified,
            etag = opdsPublicationEntity.opeEtag,
        )
    )
}
