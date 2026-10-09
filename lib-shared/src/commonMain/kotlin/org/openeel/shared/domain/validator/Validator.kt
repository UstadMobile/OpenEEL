package org.openeel.shared.domain.validator

import org.openeel.domain.validator.ValidateLinkUseCase

interface Validator {

    suspend operator fun invoke(
        url: String,
        options: ValidateLinkUseCase.ValidatorOptions,
        reporter: ValidatorReporter,
        visitedUrls: MutableList<String>,
        linkValidator: ValidateLinkUseCase?,
    )

}