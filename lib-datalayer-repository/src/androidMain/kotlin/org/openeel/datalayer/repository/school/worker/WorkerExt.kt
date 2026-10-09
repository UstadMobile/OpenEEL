package world.respect.datalayer.repository.school.worker

import androidx.work.ListenableWorker
import org.koin.core.qualifier.TypeQualifier
import org.koin.core.scope.Scope
import org.koin.mp.KoinPlatform.getKoin
import world.respect.datalayer.repository.school.RepoWorkerConstants

/**
 * Get a Koin Scope for a Worker. This works by storing the scopeId and scopeQualifier class name
 * into the inputData.
 *
 * Used when we have a Worker that needs access to a given Koin scope e.g. a data sync related
 * worker that needs to access the SchoolDataSource.
 */
fun ListenableWorker.getWorkerKoinScope(): Scope {
    val scopeId = inputData.getString(RepoWorkerConstants.DATA_SCOPE_ID)
        ?: throw IllegalStateException()
    val scopeQualifierName = Class.forName(
        inputData.getString(RepoWorkerConstants.DATA_SCOPE_QUALIFIER)!!
    ).kotlin

    return getKoin().getOrCreateScope(scopeId, TypeQualifier(scopeQualifierName))
}
