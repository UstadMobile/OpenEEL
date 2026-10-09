package world.respect.shared.domain.school.add

import io.ktor.http.DEFAULT_PORT
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import kotlinx.serialization.Serializable

interface RegisterSchoolUseCase {

    @Serializable
    data class RegisterSchoolRequest(
        val schoolName: String,
        val schoolUrl: String,
        val inDirectoryUrl: Url? = null,
        val adminUsername: String? = null,
        val adminPassword: String? = null,
        val schoolCreationPin: String? = null,
    ) {

        companion object {
            /**
             * Construct the RegisterSchoolRequest based on the parameters provided (which are
             * expected to be form encoded when sent to the server).
             */
            fun fromParameters(
                params: Parameters
            ): RegisterSchoolRequest {
                val schoolName = params[PARAM_SCHOOL_NAME] ?: ""
                val fullUrlParam = params[PARAM_SCHOOL_FULL_URL]

                val baseRequest = if(fullUrlParam != null) {
                    RegisterSchoolRequest(
                        schoolName = schoolName,
                        schoolUrl = fullUrlParam
                    )
                }else {
                    val subdomain = params[PARAM_SUBDOMAIN]!!

                    RegisterSchoolRequest(
                        schoolName = schoolName,
                        schoolUrl = URLBuilder(
                            protocol = URLProtocol.createOrDefault(
                                params[PARAM_SUBDOMAIN_PROTO]!!
                            ),
                            host = "$subdomain.${params[PARAM_SUBDOMAIN_PARENT]!!}",
                            pathSegments = listOf(),
                            port = params[PARAM_SUBDOMAIN_PORT]?.toInt() ?: DEFAULT_PORT,
                        ).build().toString()
                    )
                }

                return baseRequest.copy(
                    adminUsername = params[PARAM_ADMIN_USERNAME],
                    adminPassword = params[PARAM_ADMIN_PASSWORD],
                    schoolCreationPin = params[PARAM_SCHOOL_CREATION_PIN],
                )
            }

        }
    }

    @Serializable
    data class RegisterSchoolResponse(
        val schoolUrl: Url,
        val schoolName: String? = null,
        val redirectUrl: Url? = null,
        val adminUsername: String? = null,
        val inDirectoryUrl: Url? = null,
    )

    suspend operator fun invoke(
        request: RegisterSchoolRequest
    ): RegisterSchoolResponse


    companion object {

        const val PARAM_SCHOOL_NAME = "schoolName"

        const val PARAM_SUBDOMAIN = "schoolSubdomain"

        /**
         * If registration is being done by subdomain, then this is the domain it will be under
         */
        const val PARAM_SUBDOMAIN_PARENT = "subdomainParent"

        const val PARAM_SUBDOMAIN_PROTO = "subdomainProto"

        const val PARAM_SUBDOMAIN_PORT = "subdomainPort"

        const val PARAM_SCHOOL_FULL_URL = "schoolUrl"

        const val PARAM_PACKAGE_NAME = "packageName"

        const val PARAM_NAME_REDIRECT = "redirect"

        const val PARAM_ADMIN_USERNAME = "adminUsername"

        const val PARAM_ADMIN_PASSWORD = "adminPassword"

        const val PARAM_SCHOOL_CREATION_PIN = "schoolCreationPin"

        const val PARAM_NAME_SET_USERNAME_AND_PASS = "setDirect"

    }
}