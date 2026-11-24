package org.openeel.lib.xapi.p2psync.push

import kotlin.uuid.Uuid

/**
 * This should be run when statements are written locally. It should put the statement uuids in its
 * database and then schedule a run of PushStatementsToPeersUseCase
 */
interface EnqueuePushStatementsToPeerUseCase {

    suspend operator fun invoke(
        statementUuids: List<Uuid>
    )

}