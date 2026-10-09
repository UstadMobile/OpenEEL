package world.respect.datalayer.db.school.domain.xapi

import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.xapi.entities.XapiActivityEntity
import world.respect.lib.xapi.model.XapiActivity
import world.respect.libutil.util.time.systemTimeInMillis
import kotlin.time.Instant


/**
 * Handle storing activities and managing the canonical definition of activities.
 *
 * There will be an update local function for activity _because_ there is a get endpoint for activity
 *
 */
class StoreActivitiesUseCase(
    private val schoolDatabase: RespectSchoolDatabase,
) {

    private val XapiActivityEntity.isIdOnly: Boolean
        get() {
            return actType == null
                    && actMoreInfo == null
                    && actInteractionType == null
                    && actCorrectResponsePatterns == null
        }

    suspend operator fun invoke(
        activities: List<XapiActivity>,
        timestamp: Instant,
    ) {
        val timeNow = systemTimeInMillis()



        /*
        schoolDatabase.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val activities = activityEntities.map {
                    it.activityEntity.copy(
                        actNamesLastChanged = timeNow
                    )
                }
                schoolDatabase.getActivityEntityDao().insertOrIgnoreAsync(activities)

                /**
                 * A statement might have an object where the objectType is activity and it only includes
                 * the id (e.g. it does not include the definition).
                 *
                 * We shouldn't attempt to update the canonical definition if that's the case. Spec says:
                 * An LRS SHOULD NOT make significant changes to its canonical definition for the Activity based on an updated definition e.g. changes to correct responses.
                 */
                activities.filter { !it.isIdOnly }.forEach {
                    schoolDatabase.getActivityEntityDao().updateIfMoreInfoChanged(
                        activityUid = it.actUid,
                        actMoreInfo = it.actMoreInfo,
                        actLct = timeNow
                    )

                    /**
                     * Where an activity is not id only, then it might be that this is the first time we
                     * have seen the definition e.g. if it was recorded for the first time when only the
                     * id was present. If that is the case, then we should now record the
                     * canonical definition.
                     */
                    schoolDatabase.getActivityEntityDao().updateIfNotYetDefined(
                        actUid = it.actUid,
                        actType = it.actType,
                        actMoreInfo = it.actMoreInfo,
                        actInteractionType = it.actInteractionType?.dbFlag ?: 0,
                        actCorrectResponsePatterns = it.actCorrectResponsePatterns,
                        actLct = timeNow,
                    )
                }

                val allLangMapEntries = activityEntities.flatMap {
                    it.activityLangMapEntries
                }

                /*
                 * To avoid 'significantly changing' the canonical definition, interaction entities
                 * will only be inserted if no other interaction entities exist for that activity.
                 */
                val activityInteractionEntities = activityEntities.flatMap {
                    it.activityInteractionEntities
                }

                val activityUidsWithExistingInteractions = schoolDatabase.getActivityInteractionDao()
                    .findActivityUidsWithInteractionEntitiesAsync(
                        activityUids = activityInteractionEntities.map { it.aieActivityUid }.distinct().toList()
                    ).toSet()

                schoolDatabase.getActivityInteractionDao().insertOrIgnoreAsync(
                    entities = activityInteractionEntities.filter {
                        it.aieActivityUid !in activityUidsWithExistingInteractions
                    }
                )

                /**
                 * On handling lang map entities:
                 *   Entities for the name and description property will always be upserted e.g. as per
                 *   the spec activities canonical definition update can include changing spelling etc.
                 *
                 *   Entities for langmaps that are part of interaction properties should only be inserted
                 *   if those interaction entities exist.
                 */
                val (nameAndDescriptionLangMapEntities, interactionLangMapEntities) =
                    allLangMapEntries.partition { it.almeProperty.isActivityProp }

                schoolDatabase.getActivityLangMapEntryDao().upsertList(nameAndDescriptionLangMapEntities)
                interactionLangMapEntities.forEach {
                    schoolDatabase.getActivityLangMapEntryDao().upsertIfInteractionEntityExists(
                        almeActivityUid = it.almeActivityUid,
                        almeProperty = it.almeProperty.flag,
                        almeValue = it.almeValue,
                        almeLangCode = it.almeLangCode,
                        aieProp = it.almeProperty.interactionProp?.flag
                    )
                }

                allLangMapEntries.forEach {
                    schoolDatabase.getActivityLangMapEntryDao().updateIfChanged(
                        almeActivityUid = it.almeActivityUid,
                        almeValue = it.almeValue,
                        almeProperty = it.almeProperty.flag,
                        almeInteractionId = it.almeInteractionId,
                    )
                }

                schoolDatabase.getActivityExtensionDao().upsertListAsync(
                    activityEntities.flatMap { it.activityExtensionEntities }
                )
            }
        }*/
    }

}