package world.respect.shared.domain.validator

/**
 * Basic interface that handles receiving a ValidatorMessage - this could output to a file,
 * print line it, keep a count, etc.
 */
fun interface ValidatorReporter {

    fun addMessage(message: ValidatorMessage): ValidatorMessage

}