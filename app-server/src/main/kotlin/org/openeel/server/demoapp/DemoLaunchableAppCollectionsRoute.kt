package org.openeel.demo.demolaunchableappserver

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.getKoin
import world.respect.server.demoapp.ext.demoAppBaseUrl
import world.respect.server.domain.school.demoapp.MakeDemoAppCollectionUseCase
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase


fun Route.DemoLaunchableAppCollectionsRoute() {
    val koin = getKoin()

    get("{lang}/${MakeDemoAppCollectionUseCase.DEFAULT_COLLECTION_NAME}") {
        call.respond(
            koin.get<MakeDemoAppCollectionUseCase>().invoke(
                baseUrl = call.demoAppBaseUrl(),
                langCode = call.parameters["lang"]!!
            )
        )
    }

    get("{lang}/${MakeDemoAppGradeCollectionsUseCase.GRADES_DIR_NAME}/{grade}/${MakeDemoAppGradeCollectionsUseCase.COLLECTION_FILE_NAME}") {
        val baseUrl = call.demoAppBaseUrl()
        val grade = call.parameters["grade"]!!

        call.respond(
            koin.get<MakeDemoAppGradeCollectionsUseCase>().invoke(
                baseUrl = baseUrl,
                gradeNum = grade.toInt(),
                langCode = call.parameters["lang"]!!,
            )
        )
    }

}