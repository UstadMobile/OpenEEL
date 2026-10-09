package world.respect.shared.domain.validator

import world.respect.domain.validator.ValidateLinkUseCase

interface Validator {

    suspend operator fun invoke(
        url: String,
        options: ValidateLinkUseCase.ValidatorOptions,
        reporter: ValidatorReporter,
        visitedUrls: MutableList<String>,
        linkValidator: ValidateLinkUseCase?,
    )

}