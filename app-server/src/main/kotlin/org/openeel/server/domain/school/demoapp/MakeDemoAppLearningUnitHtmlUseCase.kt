package world.respect.server.domain.school.demoapp

import io.ktor.http.Url
import kotlinx.html.HTML
import kotlinx.html.InputType
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.button
import kotlinx.html.h1
import kotlinx.html.h3
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.meta
import kotlinx.html.option
import kotlinx.html.pre
import kotlinx.html.script
import kotlinx.html.select
import kotlinx.html.stream.createHTML
import kotlinx.html.title
import world.respect.lib.xapi.model.XapiVerb
import world.respect.libutil.ext.resolve
import world.respect.server.domain.school.demoapp.MakeDemoAppLearningUnitHtmlUseCase.Companion.LEARNING_UNIT_JS_FILENAME


fun HTML.demoAppLearningUnitHtml(
    baseUrl: Url,
    gradeNum: Int,
    lessonNum: Int,
    langCode: String,
    demoStrings: DemoStringMaps,
) {
    head {
        title {
            + demoStrings.requireString(langCode, "lesson_grade")
                .replacePlaceholders(gradeNum, lessonNum)
        }

        script {
            type = "module"
            src = baseUrl.resolve("static/$LEARNING_UNIT_JS_FILENAME").toString()
        }

        meta {
            name = "viewport"
            content = "width=device-width, initial-scale=1"
        }
    }

    body {
        h1 {
            + demoStrings.requireString(langCode, "demo_learning_unit")
                .replacePlaceholders(gradeNum, lessonNum)
        }

        pre {
           id = "actor_info"
        }

        pre {
            id = "activity_id"
        }

        h3 {
            + "Send result (pass/fail)"
        }

        label {
            htmlFor = "score_text"
            + "Scaled score (must be between 0 and 1):"
        }

        br()

        input(type = InputType.text) {
            id = "score_text"
        }

        br()

        label {
            htmlFor = "verb_id"
            +"Result:"
        }
        br()

        select {
            id = "verb_id"
            listOf(XapiVerb.ID_PASSED, XapiVerb.ID_FAILED).forEach { verbId ->
                option {
                    value = verbId
                    + verbId.substringAfterLast("/")
                }
            }
        }
        br()
        br()
        button {
            id = "send_result_button"
            + "Send result statement"
        }

        pre {
            id = "send_result_result"
        }

        h3 {
            + "Send completed statement"
        }

        button {
            id = "send_completed_button"

            + "Send completed statement"
        }

        pre {
            id = "send_completed_result"
        }


        h3 {
            + "Send progressed statement"
        }

        label {
            htmlFor = "progress_text"
            + "Progress (must be between 0 and 100):"
        }

        br()

        input(type = InputType.text) {
            id = "progress_text"
        }

        br()

        button {
            id = "send_progress_button"
            + "Send progressed statement"
        }

        pre {
            id = "send_progress_result"
        }
    }
}

class MakeDemoAppLearningUnitHtmlUseCase(
    private val demoStrings: DemoStringMaps,
) {

    operator fun invoke(
        baseUrl: Url,
        gradeNum: Int,
        lessonNum: Int,
        langCode: String,
    ): String {
        return createHTML().html {
            demoAppLearningUnitHtml(
                baseUrl = baseUrl,
                gradeNum = gradeNum,
                lessonNum = lessonNum,
                langCode = langCode,
                demoStrings = demoStrings,
            )
        }
    }

    companion object {

        const val LEARNING_UNIT_HTML_FILENAME = "learningunit.html"

        const val LEARNING_UNIT_JS_FILENAME = "learning_unit_xapi.js"

        const val XAPI_MODULE_FILENAME = "xapi_module.js"

    }
}