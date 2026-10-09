package world.respect.shared.domain.opds.validator

import com.networknt.schema.ValidationMessage as SchemaValidatorMessage
import world.respect.shared.domain.validator.ValidatorMessage

fun SchemaValidatorMessage.toValidatorMessage(sourceUri: String) = ValidatorMessage(
    level = ValidatorMessage.Level.ERROR,
    sourceUri = sourceUri,
    message = toString()
)
