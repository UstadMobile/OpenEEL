package world.respect.datalayer.repository.school.worker

import androidx.work.Data
import world.respect.datalayer.repository.school.RepoWorkerConstants
import kotlin.reflect.KClass

/**
 * See WorkerExt.getWorkerKoinScope .
 *
 * Used to save the scopeId and qualifier name to the worker inputData so it can be used when the
 * worker runs.
 */
fun Data.Builder.putKoinScope(
    scopeId: String,
    scopeClass: KClass<*>,
) : Data.Builder {
    return putString(RepoWorkerConstants.DATA_SCOPE_ID, scopeId)
        .putString(RepoWorkerConstants.DATA_SCOPE_QUALIFIER, scopeClass.qualifiedName)
}

