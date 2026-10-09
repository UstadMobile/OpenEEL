package world.respect.shared.domain.createlink

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import world.respect.libutil.ext.RESPECT_SCHOOL_LINK_SEGMENT
import world.respect.libutil.ext.appendEndpointPathSegments

class CreateInviteLinkUseCase(
    private val schoolUrl: Url
) {
    operator fun invoke(
        code: String
    ): Url {
        return URLBuilder(schoolUrl).apply {
            appendEndpointPathSegments(listOf(RESPECT_SCHOOL_LINK_SEGMENT, PATH))
            parameters[QUERY_PARAM] = code
        }.build()
    }

    companion object {
        const val PATH = "AcceptInvite"
        const val QUERY_PARAM = "inviteCode"
    }
}