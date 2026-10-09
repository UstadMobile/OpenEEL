package world.respect.lib.xapi.auth

import world.respect.lib.xapi.model.XapiAgent

/**
 * An authenticated can be represented by more than one Agent as per the agents resource doc
 * documentation in the xAPI spec:
 *
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#24-agents-resource
 *
 * This use case is used to retrieve a list of [world.respect.lib.xapi.model.XapiAgent] representing
 * the currently authenticated user.
 */
fun interface GetAuthenticatedXapiAgentsUseCase {

    suspend operator fun invoke(): List<XapiAgent>

}