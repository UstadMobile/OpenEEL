package world.respect.libutil.ext

const val EXCEPTION_CAUSE_MAX_DEPTH = 20

/**
 * Returns the first cause of the given type.
 */
inline fun <reified T: Any> Throwable.getCauseOfType(
    maxDepth: Int = EXCEPTION_CAUSE_MAX_DEPTH
): T? {
    var cause: Throwable? = this
    var depthCount = 0
    while (cause != null && depthCount++ < maxDepth) {
        if (cause is T) {
            return cause
        }
        cause = cause.cause
    }

    return null
}
