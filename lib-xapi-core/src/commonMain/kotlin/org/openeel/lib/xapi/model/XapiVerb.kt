package world.respect.lib.xapi.model

import kotlinx.serialization.Serializable


const val VERB_COMPLETED = "http://adlnet.gov/expapi/verbs/completed"

const val VERB_PROGRESSED = "http://adlnet.gov/expapi/verbs/progressed"

@Serializable
data class XapiVerb(
    val id: String,
    val display: Map<String, String>? = null,
) {

    companion object {
        const val ID_VOIDED = "http://adlnet.gov/expapi/verbs/voided"

        const val ID_EXPERIENCED = "http://adlnet.gov/expapi/verbs/experienced"

        // As per https://github.com/AICC/CMI-5_Spec_Current/blob/quartz/cmi5_spec.md#933-completed
        const val ID_COMPLETED = "http://adlnet.gov/expapi/verbs/completed"

        const val ID_PASSED = "http://adlnet.gov/expapi/verbs/passed"

        const val ID_FAILED = "http://adlnet.gov/expapi/verbs/failed"

        const val ID_SAVED = "http://activitystrea.ms/schema/1.0/saved"

        /**
         * See https://registry.tincanapi.com/#uri/verb/88
         */
        const val ID_ASSIGN = "http://activitystrea.ms/schema/1.0/assign"

        /**
         * See https://registry.tincanapi.com/#uri/verb/12
         */
        const val ID_BOOKMARKED = "http://id.tincanapi.com/verb/bookmarked"

        /**
         * As per https://id.openeel.org/verb/listed-app
         */
        const val ID_LISTED_APP = "https://id.openeel.org/verb/listed-app"

    }

}
