package world.respect.datalayer.repository.school.writequeue

import io.github.aakira.napier.Napier
import io.ktor.http.Url
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.repository.SchoolDataSourceRepository
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.RepositoryModelDataSource
import kotlin.uuid.Uuid


class DrainRemoteWriteQueueUseCase(
    private val remoteWriteQueue: RemoteWriteQueue,
    private val dataSource: SchoolDataSource,
) {

    private suspend fun <T: Any> RepositoryModelDataSource<T>.sendToRemote(
        writeQueueItems: List<WriteQueueItem>
    ) {
        val data = local.findByUidList(writeQueueItems.map { it.uid })
        remote.store(data)
        remoteWriteQueue.markSent(writeQueueItems.map { it.queueItemId } )
    }

    suspend operator fun invoke() {
        val repository = dataSource as SchoolDataSourceRepository
        do {
            val pending = remoteWriteQueue.getPending(100)
            if(pending.isEmpty())
                break

            pending.forEach { item ->
                when(item.model) {
                    WriteQueueItem.Model.PERSON -> {
                        repository.personDataSource.sendToRemote(listOf(item))
                    }

                    WriteQueueItem.Model.CLASS -> {
                        repository.classDataSource.sendToRemote(listOf(item))
                    }

                    WriteQueueItem.Model.ENROLLMENT -> {
                        repository.enrollmentDataSource.sendToRemote(listOf(item))
                    }

                    WriteQueueItem.Model.PERSON_PASSWORD -> {
                        repository.personPasswordDataSource.sendToRemote(listOf(item))
                    }

                    WriteQueueItem.Model.PERSON_QRBADGE -> {
                        repository.personQrBadgeDataSource.sendToRemote(listOf(item))
                    }

                    WriteQueueItem.Model.SCHOOL_PERMISSION_GRANT -> {
                        repository.schoolPermissionGrantDataSource.sendToRemote(listOf(item))
                    }

                    WriteQueueItem.Model.INVITE -> {
                        repository.inviteDataSource.sendToRemote(listOf(item))
                    }

                    WriteQueueItem.Model.XAPI_STATEMENT -> {
                        val statement = repository.local.xapiResource.statements.getByUuid(
                            Uuid.parse(item.uid)
                        )

                        if(statement != null) {
                            repository.remote.xapiResource.statements.post(listOf(statement))
                        }else {
                            Napier.w("WARN: no local data for statement: ${item.uid}")
                        }

                        remoteWriteQueue.markSent(ids = listOf(item.queueItemId))
                    }
                }

            }
        } while(true)
    }

}