package org.openeel.shared.domain.launchapp.getxapilaunchparams

import org.openeel.shared.domain.xapi.getxapilaunchurl.GetXapiLaunchUrlUseCase


/**
 * Get the Xapi Params to launch a given activityId that will be appended as per the Rustici launch
 * method.
 *
 * This can require creating an xAPI session.
 *
 */
interface GetXapiLaunchParamsUseCase {

    suspend operator fun invoke(
        activityId: String,
        assignmentActivityId: String?,
        type: GetXapiLaunchUrlUseCase.LaunchType,
    ): XapiLaunchParams

}