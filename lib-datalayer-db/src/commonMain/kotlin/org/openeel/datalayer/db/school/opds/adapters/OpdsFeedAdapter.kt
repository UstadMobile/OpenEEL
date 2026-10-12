package org.openeel.datalayer.db.school.opds.adapters

import kotlinx.serialization.json.Json
import org.openeel.lib.dataloadstate.DataLoadMetaInfo
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.school.opds.OpdsParentType
import org.openeel.datalayer.db.school.opds.entities.OpdsFeedEntity
import org.openeel.datalayer.db.school.opds.entities.OpdsFeedMetadataEntity
import org.openeel.datalayer.db.school.opds.entities.OpdsGroupEntity
import org.openeel.datalayer.db.school.opds.entities.OpdsPublicationEntity
import org.openeel.datalayer.db.school.opds.entities.ReadiumLinkEntity
import org.openeel.datalayer.db.school.opds.entities.ReadiumLinkEntity.PropertyType.OPDS_FEED_LINKS
import org.openeel.datalayer.db.school.opds.entities.ReadiumLinkEntity.PropertyType.OPDS_FEED_NAVIGATION
import org.openeel.datalayer.db.school.opds.entities.ReadiumSubjectEntity
import org.openeel.datalayer.school.opds.ext.requireSelfUrl
import org.openeel.datalayer.db.shared.entities.LangMapEntity
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.lib.primarykeygen.PrimaryKeyGenerator
import kotlin.time.Clock
import kotlin.time.Instant

class OpdsFeedEntities(
    val opdsFeed: OpdsFeedEntity,
    val feedMetaData: List<OpdsFeedMetadataEntity>,
    val langMapEntities: List<LangMapEntity>,
    val linkEntities: List<ReadiumLinkEntity>,
    val publications: List<OpdsPublicationEntity>,
    val groups: List<OpdsGroupEntity>,
    val subjects: List<ReadiumSubjectEntity>,
)

fun OpdsFeed.asEntities(
    dataLoadMetaInfo: DataLoadMetaInfo,
    json: Json,
    primaryKeyGenerator: PrimaryKeyGenerator,
    uidNumberMapper: UidNumberMapper,
): OpdsFeedEntities {
    val url = requireSelfUrl()

    val ofeUid = uidNumberMapper(url.toString())

    fun List<ReadiumLink>?.asEntitiesSub(
        propType: ReadiumLinkEntity.PropertyType,
    ) : List<ReadiumLinkEntity> {
        return this?.mapIndexed { index, link ->
            link.asEntities(
                pkGenerator = primaryKeyGenerator,
                json = json,
                opdsParentType = OpdsParentType.OPDS_FEED,
                opdsParentUid = ofeUid,
                rlePropType = propType,
                rlePropFk = ofeUid,
                rleIndex = index,
            )
        }?.flatten() ?: emptyList()
    }

    val feedMetadata = metadata.asEntity(
        ofmeOfeUid = ofeUid,
        ofmePropType = OpdsFeedMetadataEntity.PropType.FEED_METADATA,
        ofmeRelUid = ofeUid,
    )

    val groupEntities = groups?.mapIndexed { index, group ->
        group.asEntities(
            primaryKeyGenerator = primaryKeyGenerator,
            json = json,
            uidNumberMapper = uidNumberMapper,
            ofeUid = ofeUid,
            index = index,
        )
    } ?: emptyList()

    val publicationEntities = publications?.mapIndexed { index, publication ->
        publication.asEntities(
            dataLoadResult = null,
            primaryKeyGenerator = primaryKeyGenerator,
            json = json,
            uidNumberMapper = uidNumberMapper,
            feedUid = ofeUid,
            groupUid = 0,
            feedIndex = index,
        )
    } ?: emptyList()

    return OpdsFeedEntities(
        opdsFeed = OpdsFeedEntity(
            ofeUid = ofeUid,
            ofeUrl = url,
            ofeUrlHash = ofeUid,
            ofeLastModified = this.metadata.modified ?: Clock.System.now(),
            ofeLastModifiedHeader = Instant.fromEpochMilliseconds(dataLoadMetaInfo.lastModified),
            ofeEtag = dataLoadMetaInfo.etag,
        ),
        feedMetaData = buildList {
            add(feedMetadata)
            addAll(groupEntities.map { it.metadata })
        },
        langMapEntities = buildList {
            addAll(publicationEntities.flatMap { it.langMapEntities })
            addAll(groupEntities.flatMap { it.langMapEntities })
        },
        linkEntities = buildList {
            addAll(
                links.asEntitiesSub(OPDS_FEED_LINKS)
            )
            addAll(
                navigation.asEntitiesSub(OPDS_FEED_NAVIGATION)
            )

            addAll(publicationEntities.flatMap { it.linkEntities })
            addAll(groupEntities.flatMap { it.links })
        },
        publications = buildList {
            addAll(publicationEntities.map { it.opdsPublicationEntity } )
            addAll(groupEntities.flatMap { it.publications })
        },
        groups = groupEntities.map { it.group },
        subjects = publicationEntities.flatMap { it.subjectEntities },
    )
}


fun OpdsFeedEntities.asModel(
    json: Json
): OpdsFeed{
    val feedUid = opdsFeed.ofeUid

    return OpdsFeed(
        metadata = feedMetaData.first { it.ofmeOfeUid == feedUid }.asModel(),
        links = linkEntities.asModels(json, OPDS_FEED_LINKS, feedUid),
        publications = publications.filter {
            it.opeOfeUid == feedUid && it.opeOgeUid == 0L
        }.map { publication ->
            OpdsPublicationEntities(
                opdsPublicationEntity = publication,
                langMapEntities = langMapEntities.filter {
                    it.lmeTopParentUid1 == publication.opeUid
                },
                linkEntities = linkEntities.filter { link ->
                    link.rleOpdsParentUid == publication.opeUid
                },
                subjectEntities = subjects.filter { subject ->
                    subject.rseTopParentUid == publication.opeUid
                },
            ).asModel(json).data
        },
        navigation = linkEntities.asModels(json, OPDS_FEED_NAVIGATION, feedUid),
        facets = emptyList(), //TODO
        groups = groups.map { groupEntity ->
            OpdsGroupEntities(
                group = groupEntity,
                metadata = feedMetaData.first { it.ofmePropFk == groupEntity.ogeUid },
                publications = publications.filter { it.opeOgeUid == groupEntity.ogeUid },
                links = linkEntities,
                langMapEntities = langMapEntities,
                subjectEntities = subjects,
            ).asModel(json)
        },
    )
}

