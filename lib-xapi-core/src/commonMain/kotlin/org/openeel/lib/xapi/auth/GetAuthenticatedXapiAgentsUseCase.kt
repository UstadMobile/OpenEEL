package org.openeel.lib.xapi.auth

import org.openeel.lib.xapi.model.XapiAgent

/**
 * An authenticated can be represented by more than one Agent as per the agents resource doc
 * documentation in the xAPI spec:
 *
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#24-agents-resource
 *
 * This use case is used to retrieve a list of [world.respect.lib.xapi.model.XapiAgent] representing
 * the currently authenticated user.
 *
 * This can be used for permission management purposes e.g. to restrict xAPI queries that may be
 * run, e.g. agents profile/state resources can use this to prevent users accessing/writing to
 * another users documents.
 */
fun interface GetAuthenticatedXapiAgentsUseCase {

    suspend operator fun invoke(): List<XapiAgent>

}