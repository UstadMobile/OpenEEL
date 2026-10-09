package world.respect.datalayer.db.school.xapi.adapters

import kotlinx.serialization.json.Json
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntityObjectTypeEnum
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiObjectType
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

val XapiObjectType.entityObjectTypeEnum: XapiStatementEntityObjectTypeEnum
    get() = when(this) {
        XapiObjectType.StatementRef -> XapiStatementEntityObjectTypeEnum.STATEMENT_REF
        XapiObjectType.SubStatement -> XapiStatementEntityObjectTypeEnum.SUBSTATEMENT
        XapiObjectType.Activity -> XapiStatementEntityObjectTypeEnum.ACTIVITY
        XapiObjectType.Agent -> XapiStatementEntityObjectTypeEnum.AGENT
        XapiObjectType.Group -> XapiStatementEntityObjectTypeEnum.GROUP
        else -> {
            throw IllegalArgumentException(
                "Statement cannot be an object type for a statement itself, can only be substatement"
            )
        }
    }


@OptIn(ExperimentalUuidApi::class)
fun List<XapiActivity>.toEntities(
    uidNumberMapper: UidNumberMapper,
    json: Json,
    lastModified: Instant,
) : List<XapiActivityEntities> {
    return mapNotNull { contextActivityObj ->
        contextActivityObj.toEntities(
            uidNumberMapper = uidNumberMapper,
            json = json,
            lastModified = lastModified,
        )
    }
}