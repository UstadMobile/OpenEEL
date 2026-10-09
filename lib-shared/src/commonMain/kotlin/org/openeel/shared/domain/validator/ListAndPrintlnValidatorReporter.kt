package world.respect.shared.domain.validator

class ListAndPrintlnValidatorReporter(
    val filter: (ValidatorMessage) -> Boolean = { true }
): ValidatorReporter {

    private val _messages = mutableListOf<ValidatorMessage>()

    val messages: List<ValidatorMessage>
        get() = _messages.toList()

    override fun addMessage(message: ValidatorMessage) : ValidatorMessage {
        _messages.add(message)

        if(filter(message)) {
            println(
                buildString {
                    append(message.level.name)
                    append(": ")
                    append(message.sourceUri)
                    append(" ")
                    append(message.message)
                    append("\n")
                }
            )
        }

        return message
    }
}