package world.respect.datalayer.db.school.xapi

import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.xapi.adapters.toEntities
import world.respect.datalayer.ext.EPOCH
import world.respect.lib.xapi.resources.local.XapiAgentsResourceLocal
import world.respect.lib.xapi.XapiRequestHeaders
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.model.XapiGroup
import kotlin.time.Instant

class XapiAgentsResourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val uidNumberMapper: UidNumberMapper,
) : XapiAgentsResourceLocal{

    override suspend fun updateLocal(
        actors: List<XapiActor>,
        timestamp: Instant,
    ) {

        actors.forEach { actor ->
            val entities = actor.toEntities(
                uidNumberMapper = uidNumberMapper,
                lastModified = timestamp,
            )

            val allActorEntities = (listOf(entities.actor) + entities.groupMemberAgents)

            val updateTime = timestamp.toEpochMilliseconds()

            if(actor is XapiGroup) {
                val members = actor.member

                //An anonymous group will always have a new uid, will never be in db
                val existingEntityInDb = schoolDb.getActorDao().takeIf { !actor.isAnonymous }
                    ?.findByUidAsync(entities.actor.actorUid)

                val updateMembers = members != null &&
                    (timestamp > (existingEntityInDb?.actorGroupMembersLastUpdated ?: EPOCH))

                if(updateMembers) {
                    //If this is an identified group, we need to delete previous group member joins
                    schoolDb.takeIf { !actor.isAnonymous }?.getGroupMemberActorJoinDao()
                        ?.deleteByGroupActorUidAsync(entities.actor.actorUid)

                    schoolDb.getGroupMemberActorJoinDao().insertOrIgnoreListAsync(
                        entities.groupMemberJoins
                    )

                    schoolDb.getActorDao().updateGroupMembersLastUpdated(
                        actorUid = entities.actor.actorUid,
                        updateTime = updateTime
                    )
                }
            }

            //The name property is the only property that can change
            allActorEntities.forEach {
                schoolDb.getActorDao().updateIfNameChanged(
                    uid = it.actorUid,
                    name = it.actorName,
                    updateTime = updateTime
                )
            }
            schoolDb.getActorDao().insertOrIgnoreListAsync(allActorEntities)
        }
    }

    override suspend fun getPerson(
        actor: XapiActor,
        xapiRequestHeaders: XapiRequestHeaders
    ) {
        TODO("Not yet implemented")
    }
}