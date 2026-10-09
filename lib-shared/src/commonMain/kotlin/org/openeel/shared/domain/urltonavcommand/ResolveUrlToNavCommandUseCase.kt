package world.respect.shared.domain.urltonavcommand

import io.ktor.http.Url
import world.respect.libutil.ext.schoolUrlOrNull
import world.respect.shared.domain.createlink.CreateInviteLinkUseCase
import world.respect.shared.navigation.AcceptInvite
import world.respect.shared.navigation.NavCommand

/**
 * Given a Url (that may have come from a deep link, scanned as a qr code, etc) that
 * follows the respect school link format (See UrlExt.schoolUrlOrNull) resolve this into a
 * NavCommand.
 */
class ResolveUrlToNavCommandUseCase {

    operator fun invoke(
        url: Url,
        canGoBack: Boolean = true,
    ): NavCommand? {
        val schoolUrl = url.schoolUrlOrNull() ?: return null

        val lastSegment = url.segments.lastOrNull() ?: return null

        return when(lastSegment) {
            CreateInviteLinkUseCase.PATH -> {
                url.parameters[CreateInviteLinkUseCase.QUERY_PARAM]?.let { inviteCode ->
                    NavCommand.Navigate(
                        destination = AcceptInvite.create(
                            schoolUrl = schoolUrl,
                            code = inviteCode,
                            canGoBack = canGoBack,
                        ), clearBackStack = false
                    )
                }
            }
            else -> null
        }
    }

}