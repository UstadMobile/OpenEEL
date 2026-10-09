package org.openeel.domain.validator

data class ValidatorMessage(
    val level: Level = Level.ERROR,
    val sourceUri: String,
    val message: String,
) {

    @Suppress("unused") //others reserved for future use
    enum class Level {
        DEBUG, VERBOSE, INFO, WARN, ERROR
    }

    companion object {
        fun fromException(
            sourceUri: String,
            throwable: Throwable
        ) = ValidatorMessage(
            level = Level.ERROR,
            sourceUri = sourceUri,
            message = "Error processing $sourceUri : ${throwable.message}"
        )

    }

}
