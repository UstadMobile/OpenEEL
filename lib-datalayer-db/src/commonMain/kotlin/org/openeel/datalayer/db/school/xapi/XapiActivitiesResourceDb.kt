package world.respect.datalayer.db.school.xapi

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.xapi.adapters.toEntities
import world.respect.datalayer.db.school.xapi.adapters.toModel
import world.respect.datalayer.db.school.xapi.entities.XapiActivityLangMapEntry
import world.respect.lib.xapi.resources.local.XapiActivitiesResourceLocal
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiObjectType
import kotlin.time.Clock
import kotlin.time.Instant

class XapiActivitiesResourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val uidNumberMapper: UidNumberMapper,
    private val json: Json,
) : XapiActivitiesResourceLocal{

    /**
     * As per the xAPI spec :
     *
     * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#25-activities-resource
     * "If an LRS does not have a canonical definition of the Activity to return, the LRS SHOULD*
     * still return an Activity Object when queried."
     *
     */
    override suspend fun get(activityId: String): DataLoadState<XapiActivity> {
        return DataReadyState(
            data = schoolDb.getActivityEntityDao().getEntitiesByUid(
                uidNumberMapper(activityId)
            )?.toModel(json) ?: XapiActivity(
                id = activityId,
                objectType = XapiObjectType.Activity,
            )
        )
    }

    override suspend fun updateLocal(
        activities: List<XapiActivity>,
        timestamp: Instant,
    ) {
        val timeNow = Clock.System.now()
        val timestampMillis = timestamp.toEpochMilliseconds()

        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                activities.forEach { activity ->
                    val uid = uidNumberMapper(activity.id)
                    val entities = activity.toEntities(
                        uidNumberMapper = uidNumberMapper,
                        json = json,
                        lastModified = timestamp,
                    ) ?: return@forEach

                    val activityInDb = schoolDb.getActivityEntityDao().findByUidAsync(uid)

                    /*
                     * Because
                     * An LRS SHOULD NOT make significant changes to its canonical definition for the Activity
                     * based on an updated definition e.g. changes to correct responses.
                     *
                     * To reach eventual consistency: first write wins; significant updates should be applied if
                     * they were the first ones.
                     */
                    val doSignificantCanonicalUpdate = activityInDb == null ||
                            timestamp < activityInDb.actSignificantLastModified

                    if(doSignificantCanonicalUpdate) {
                        schoolDb.getActivityEntityDao().upsert(entities.activityEntity)
                        schoolDb.getActivityInteractionDao().deleteByActivityUid(uid)
                        schoolDb.getActivityInteractionDao().insertOrIgnoreAsync(
                            entities.activityInteractionEntities
                        )
                    }

                    //For each langmap property: If it already exists, and we have newer data for that entry
                    //then update.
                    entities.activityLangMapEntries.forEach { langMapEntry ->
                        schoolDb.getActivityLangMapEntryDao().updateIfChanged(
                            almeActivityUid = uid,
                            almeKeyHash = XapiActivityLangMapEntry.keyHashFor(
                                uidNumberMapper = uidNumberMapper,
                                property = langMapEntry.almeProperty,
                                almeInteractionId = langMapEntry.almeInteractionId,
                                almeLangCode = langMapEntry.almeLangCode,
                            ),
                            almeValue = langMapEntry.almeValue,
                            changeTime = timestampMillis,
                        )
                    }

                    entities.activityExtensionEntities.forEach {
                        schoolDb.getActivityExtensionDao().updateIfNewer(
                            activityUid = uid,
                            keyHash = it.aeeKeyHash,
                            json = it.aeeJson,
                            changeTime = timestampMillis,
                        )
                    }

                    schoolDb.getActivityExtensionDao().insertOrIgnore(
                        entities.activityExtensionEntities
                    )

                    //Insert any lang map entries that don't exist.
                    schoolDb.getActivityLangMapEntryDao().insertOrIgnore(entities.activityLangMapEntries)

                    //Handle extensions - update if present and newer.
                    if(activity.definition?.extensions != null &&
                        timestamp > (activityInDb?.actNonSignificantLastModified ?: timeNow)
                    ){
                        schoolDb.takeIf { activityInDb != null }?.getActivityExtensionDao()
                            ?.deleteByActivityUid(uid)
                        schoolDb.getActivityExtensionDao().upsertListAsync(
                            entities.activityExtensionEntities
                        )
                    }
                }
            }
        }
    }

}