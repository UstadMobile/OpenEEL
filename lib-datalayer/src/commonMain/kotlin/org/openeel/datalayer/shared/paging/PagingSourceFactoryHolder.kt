package world.respect.datalayer.shared.paging

import androidx.paging.PagingSource
import io.github.aakira.napier.Napier
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

/**
 * The PagingSourceFactoryHolder is used by ViewModel to:
 * a) Hold a single PagingSourceFactory reference. In order to avoid flicker when a list changes
 *    (e.g. because a user is typing in a search etc) Compose code must use a not
 *    change the instance of Pager that is used. Each Pager accepts one and only one PagingSource
 *    Factory. Instead of changing the PagingSourceFactory, we nee to invalidate the last PagingSource
 *    produced by the factory (and ensure that the that the pagingsource factory will produce a
 *    new result e.g. by using an anonymous implementation in viewmodel that uses the uistate
 *    properties).
 * b) Invalidate the PagingSource when input parameters change (e.g. user types in search, etc)
 */
class PagingSourceFactoryHolder<Key: Any, Value: Any>(
    val src: () -> IPagingSourceFactory<Key, Value>
) : IPagingSourceFactory<Key, Value>{

    private val logPrefix = "RPaging/PagingSourceFactoryHolder:"

    private val pagingSourceFactory = atomic(src())
    private val currentPagingSource = atomic<PagingSource<Key, Value>?>(null)

    private val lock = ReentrantLock()

    override fun invoke(): PagingSource<Key, Value> {
        Napier.d("$logPrefix invoke()")
        return lock.withLock {
            val newPagingSource = pagingSourceFactory.value()
            val oldPagingSource = currentPagingSource.getAndSet(newPagingSource)
//            oldPagingSource?.invalidate()
            newPagingSource
        }
    }


    fun invalidate() {
        val newFactory = src().also {
            Napier.d("$logPrefix invalidate invoke src()")
        }

        lock.withLock {
            pagingSourceFactory.value = newFactory
            currentPagingSource.value?.invalidate()
        }
    }

}