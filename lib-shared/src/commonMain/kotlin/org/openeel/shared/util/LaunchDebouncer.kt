package world.respect.shared.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A debouncer (used to prevent a function from being called too frequently), generally
 * used to help managing committing changes to the SavedStateHandle. We don't want to
 * commit after every keystroke, but we do want to ensure that values are committed
 * after a very short delay.
 */
class LaunchDebouncer(
    private val scope: CoroutineScope,
    private val delay: Long = DEFAULT_DELAY,
) {
    private val jobMap = mutableMapOf<String, Job>()

    fun launch(
        debounceKey: String,
        block: suspend () -> Unit
    ) {
        jobMap[debounceKey]?.cancel()
        jobMap[debounceKey] = scope.launch {
            delay(delay)
            block()
            jobMap.remove(debounceKey)
        }
    }

    companion object {

        const val DEFAULT_DELAY = 200L

    }
}