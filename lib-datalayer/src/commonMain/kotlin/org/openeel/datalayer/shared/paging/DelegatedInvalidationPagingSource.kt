package world.respect.datalayer.shared.paging

import androidx.paging.PagingSource
import io.github.aakira.napier.Napier
import kotlinx.atomicfu.atomic

/**
 * Contains the logic required for a child paging source to invalidate itself when an underlying
 * source paging source is invalidated.
 *
 * The child PagingSource MUST call registerInvalidationCallbackIfNeeded in its onLoad function
 *
 * This is derived (1:1) from UstadMobile/Door's DelegatedInvalidationPagingSource
 */
abstract class DelegatedInvalidationPagingSource<Key: Any, Value: Any>(
    protected val invalidationDelegate: PagingSource<Key, *>,
    protected val tag: LogPrefixFunction = NO_TAG,
): PagingSource<Key, Value>() {

    private val logPrefix = "RPaging/DelegatedInvalidationPagingSource(extra=${tag()})"

    private val srcInvalidateCallbackRegistered = atomic(false)

    private val invalidated = atomic(false)

    override val jumpingSupported: Boolean
        get() = invalidationDelegate.jumpingSupported

    override val keyReuseSupported: Boolean
        get() = invalidationDelegate.keyReuseSupported

    private val srcInvalidatedCallback: () -> Unit = {
        onSrcInvalidated()
    }

    private fun onSrcInvalidated() {
        invalidationDelegate.unregisterInvalidatedCallback(srcInvalidatedCallback)
        if(!invalidated.getAndSet(true)) {
            Napier.d("$logPrefix src invalidated")
            invalidate()
        }
    }

    protected fun registerInvalidationCallbackIfNeeded() {
        if(!srcInvalidateCallbackRegistered.getAndSet(true)) {
            invalidationDelegate.registerInvalidatedCallback(srcInvalidatedCallback)
        }
    }

    companion object {

        val NO_TAG: () -> String = { "" }

    }
}