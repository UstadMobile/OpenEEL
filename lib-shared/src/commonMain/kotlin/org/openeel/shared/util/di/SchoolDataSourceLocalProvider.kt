package org.openeel.shared.util.di

import io.ktor.http.Url
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.SchoolDataSourceLocal

fun interface SchoolDataSourceLocalProvider {

    operator fun invoke(
        schoolUrl: Url,
        user: AuthenticatedUserPrincipalId,
    ): SchoolDataSourceLocal

}