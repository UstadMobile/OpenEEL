package org.openeel.shared.domain.opds.validator

import com.networknt.schema.ValidationMessage as SchemaValidatorMessage
import org.openeel.shared.domain.validator.ValidatorMessage

fun SchemaValidatorMessage.toValidatorMessage(sourceUri: String) = ValidatorMessage(
    level = ValidatorMessage.Level.ERROR,
    sourceUri = sourceUri,
    message = toString()
)
