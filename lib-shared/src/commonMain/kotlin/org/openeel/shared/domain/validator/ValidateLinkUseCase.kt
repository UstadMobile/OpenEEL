package org.openeel.domain.validator

import org.openeel.lib.opds.model.ReadiumLink


/**
 * Validate a link - this can be :
 *
 * An OPDS feed or publication as per https://drafts.opds.io/opds-2.0#appendix-a-json-schema .
 * Images or resources
 *
 * The implementation will generally use multiple implementations of the Validator interface (e.g.
 * one implementation per type being validated).
 *
 */
interface ValidateLinkUseCase {

    /**
     * Options to run a validation which can be passed through (e.g. from an OpdsValidator to
     * a media resource validator, etc)
     *
     * @param followLinks follow links to other feeds and resources
     * @param skipRespectChecks ignore RESPECT-specific requirements on OPDS: runs only the json
     *        validator and checks links return a successful (OK Http/200) response.
     */
    data class ValidatorOptions(
        val followLinks: Boolean = true,
        val skipRespectChecks: Boolean = false,
    )

    /**
     * @param link ReadiumLink - Uses the href, and type (if available)
     * @param refererUrl Absolute URL of the referer in which the link was found
     * @param visitedUrls a list of URLs already visited; used to avoid validating the same url more
     *        than once
     * @param options Validation options
     * @return a list of ValidatorMessage containing any errors and warnings (if any).
     */
    suspend operator fun invoke(
        link: ReadiumLink,
        refererUrl: String,
        options: ValidatorOptions,
        reporter: ValidatorReporter,
        visitedUrls: MutableList<String>,
    )

}