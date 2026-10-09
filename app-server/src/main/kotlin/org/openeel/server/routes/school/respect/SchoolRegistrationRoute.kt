package world.respect.server.routes.school.respect

import io.github.aakira.napier.Napier
import io.ktor.http.CacheControl
import io.ktor.http.HttpStatusCode
import io.ktor.server.html.respondHtml
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.cacheControl
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.html.ButtonType
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.main
import kotlinx.html.meta
import kotlinx.html.style
import kotlinx.html.title
import org.koin.ktor.ext.inject
import world.respect.server.SchoolConfig
import world.respect.server.schoolDirsUseVirtualHost
import world.respect.server.util.ext.getStatusCode
import world.respect.server.util.ext.virtualHost
import world.respect.shared.domain.school.add.RegisterSchoolUseCase

fun Route.SchoolRegistrationRoute() {
    val registerSchoolUseCase: RegisterSchoolUseCase by inject()
    val schoolConfig: SchoolConfig by inject()

    // Show registration form
    get("/school-directory/register-school") {
        if (!schoolConfig.registration.enabled) {
            call.respondText(
                "School registration is disabled",
                status = HttpStatusCode.Forbidden
            )
            return@get
        }

        val packageName = call.request.queryParameters[RegisterSchoolUseCase.PARAM_PACKAGE_NAME]
        val virtualHost = call.request.virtualHost
        val showUserInForm = call.request.queryParameters[RegisterSchoolUseCase.PARAM_NAME_SET_USERNAME_AND_PASS]?.toBoolean() ?: false

        call.respondHtml {
            head {
                title { +"Register New School" }
                meta(name = "viewport", content = "width=device-width, initial-scale=1")
            }
            body {
                main(classes = "container") { // "container" centers the form and gives it padding
                    h1 { + "Register New School" }

                    form(method = FormMethod.post, action = "/school-directory/register-school") {
                        div("form-group") {
                            if(showUserInForm) {
                                if(environment.config.schoolDirsUseVirtualHost()){
                                    +"School in directory: ${call.virtualHost}"
                                    br()
                                }

                                label {
                                    htmlFor = RegisterSchoolUseCase.PARAM_SCHOOL_CREATION_PIN
                                    +"School Creation PIN:"
                                }

                                input(
                                    type = InputType.password,
                                    name = RegisterSchoolUseCase.PARAM_SCHOOL_CREATION_PIN,
                                ) {
                                    id = RegisterSchoolUseCase.PARAM_SCHOOL_CREATION_PIN
                                }
                                br()
                            }

                            label {
                                htmlFor = RegisterSchoolUseCase.PARAM_SCHOOL_NAME
                                +"School Name:"
                            }

                            input(type = InputType.text, name = "schoolName") {
                                id = RegisterSchoolUseCase.PARAM_SCHOOL_NAME
                                required = true
                                placeholder = "Enter school name"
                            }

                            br()

                            if(showUserInForm) {
                                label {
                                    htmlFor = RegisterSchoolUseCase.PARAM_ADMIN_USERNAME
                                    +"Admin username:"
                                }

                                input(
                                    type = InputType.text,
                                    name = RegisterSchoolUseCase.PARAM_ADMIN_USERNAME,
                                ) {
                                    id = RegisterSchoolUseCase.PARAM_ADMIN_USERNAME
                                    required = true
                                    value = "admin"
                                }

                                br()

                                label {
                                    htmlFor = RegisterSchoolUseCase.PARAM_ADMIN_PASSWORD
                                    +"Admin password:"
                                }

                                input(
                                    type = InputType.password,
                                    name = RegisterSchoolUseCase.PARAM_ADMIN_PASSWORD,
                                ) {
                                    id = RegisterSchoolUseCase.PARAM_ADMIN_PASSWORD
                                    required = true
                                }
                            }
                        }

                        when (schoolConfig.registration.mode) {
                            SchoolConfig.RegistrationConfig.RegistrationMode.SUBDOMAIN -> {
                                div("form-group") {
                                    label {
                                        htmlFor = RegisterSchoolUseCase.PARAM_SUBDOMAIN
                                        +"School Link:"
                                    }
                                    div {
                                        input(type = InputType.text, name = "schoolSubdomain") {
                                            id = RegisterSchoolUseCase.PARAM_SUBDOMAIN
                                            required = true
                                            placeholder = "schoolname"
                                        }
                                        // Show the suffix to the user
                                        +".${schoolConfig.registration.subdomainParent}"
                                    }
                                }
                            }

                            SchoolConfig.RegistrationConfig.RegistrationMode.ANY_URL -> {
                                div("form-group") {
                                    label {
                                        htmlFor = RegisterSchoolUseCase.PARAM_SCHOOL_FULL_URL
                                        +"School URL:"
                                    }

                                    input(type = InputType.text, name = "schoolUrl") {
                                        id = RegisterSchoolUseCase.PARAM_SCHOOL_FULL_URL
                                        required = true
                                        placeholder = "https://your-school.com"
                                    }
                                }
                            }

                            else -> {
                                // Should not reach here since registration is disabled
                            }
                        }

                        // Hidden package name field
                        packageName?.also { pkg ->
                            input(
                                type = InputType.hidden,
                                name = RegisterSchoolUseCase.PARAM_PACKAGE_NAME
                            ) {
                                value = pkg
                            }
                        }

                        schoolConfig.registration.subdomainParent?.also { topLevelDomain ->
                            input(
                                type = InputType.hidden,
                                name = RegisterSchoolUseCase.PARAM_SUBDOMAIN_PARENT,
                            ) {
                                value = topLevelDomain
                            }

                            input(
                                type = InputType.hidden,
                                name = RegisterSchoolUseCase.PARAM_SUBDOMAIN_PROTO,
                            ) {
                                value = virtualHost.protocol.name
                            }

                            input(
                                type = InputType.hidden,
                                name = RegisterSchoolUseCase.PARAM_SUBDOMAIN_PORT
                            ) {
                                value = schoolConfig.registration.subdomainPort.toString()
                            }
                        }

                        // Hidden redirect field for deep linking to app
                        input(
                            type = InputType.hidden,
                            name = RegisterSchoolUseCase.PARAM_NAME_REDIRECT
                        ) {
                            value = call.request.queryParameters["redirect"]
                                ?: "world.respect.app://school-registered"
                        }

                        button(type = ButtonType.submit, classes = "submit-btn") {
                            +"Next"
                        }
                    }
                }
            }
        }
    }

    post("/school-directory/register-school") {
        try {
            val parameters = call.receiveParameters()
            val registerRequest = RegisterSchoolUseCase.RegisterSchoolRequest.fromParameters(
                parameters
            ).let {
                if(environment.config.schoolDirsUseVirtualHost()) {
                    it.copy(inDirectoryUrl = call.virtualHost)
                }else {
                    it
                }
            }

            val response = registerSchoolUseCase(request = registerRequest)
            val redirectUrl = response.redirectUrl
            if(redirectUrl != null) {
                call.respondRedirect(redirectUrl)
            }else {
                call.respondHtml {
                    body {
                        h1 {
                            +"Registration successful"
                        }
                        response.schoolName?.also { name ->
                            +"School name: $name"
                            br()
                        }
                        +"School URL: ${response.schoolUrl}"
                        br()
                        "Admin username: ${response.adminUsername}"
                        br()
                        response.inDirectoryUrl?.also { dirUrl ->
                            + "In directory :$dirUrl"
                        }
                    }
                }
            }

            return@post
        }catch(e: Throwable) {
            Napier.e("Error registering school", e)
            val errorCode = e.getStatusCode() ?: HttpStatusCode.InternalServerError

            call.respondHtml(errorCode) {
                head {
                    title { +"Registration Error" }
                    meta(
                        name = "viewport",
                        content = "width=device-width, initial-scale=1"
                    )
                }
                body {
                    main(classes = "container") { // "container" centers the form and gives it padding
                        h1 { +"Registration Failed" }
                        div {
                            style =
                                "color: red; padding: 20px; background-color: #ffe6e6; border-radius: 5px;"
                            +(e.message ?: "Unknown Error")
                        }
                    }
                }
            }
        }
    }
}

