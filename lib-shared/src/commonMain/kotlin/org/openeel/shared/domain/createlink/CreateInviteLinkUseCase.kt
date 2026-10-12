package org.openeel.shared.domain.createlink

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import org.openeel.libutil.ext.RESPECT_SCHOOL_LINK_SEGMENT
import org.openeel.libutil.ext.appendEndpointPathSegments

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