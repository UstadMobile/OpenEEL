package world.respect.shared.util.di

import io.ktor.http.Url
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSourceLocal

fun interface SchoolDataSourceLocalProvider {

    operator fun invoke(
        schoolUrl: Url,
        user: AuthenticatedUserPrincipalId,
    ): SchoolDataSourceLocal

}