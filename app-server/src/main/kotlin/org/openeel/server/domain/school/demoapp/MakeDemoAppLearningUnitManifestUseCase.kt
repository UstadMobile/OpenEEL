package world.respect.server.domain.school.demoapp

import com.eygraber.uri.Uri
import io.ktor.http.Url
import org.openeel.demo.demolaunchableappserver.DemoConstants
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.ReadiumContributorStringValue
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ReadiumMetadata
import world.respect.libutil.ext.resolve
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.GRADES_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.LEARNING_UNITS_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.LEARNING_UNIT_ICON_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppLearningUnitHtmlUseCase.Companion.LEARNING_UNIT_HTML_FILENAME
import world.respect.server.domain.school.demoapp.MakeDemoAppLearningUnitHtmlUseCase.Companion.LEARNING_UNIT_JS_FILENAME
import world.respect.server.domain.school.demoapp.MakeDemoAppLearningUnitHtmlUseCase.Companion.XAPI_MODULE_FILENAME
import world.respect.server.domain.school.demoapp.MakeDemoAppManifestUseCase.Companion.APP_MANIFEST_FILENAME

class MakeDemoAppLearningUnitManifestUseCase(
    private val demoStrings: DemoStringMaps,
) {

    operator fun invoke(
        demoBase: Url,
        grade: Int,
        lessonNum: Int,
        langCode: String,
        titleFn: (Int, Int) -> String = LEARNING_UNIT_TITLE_FN,
    ): Publication {
        val lessonBase = demoBase.resolve(
            "$langCode/$GRADES_DIR_NAME/$grade/$LEARNING_UNITS_DIR_NAME/$lessonNum/"
        )

        return Publication(
            metadata = ReadiumMetadata(
                title = LangMapStringValue(
                    demoStrings.requireString(
                        lang = langCode,
                        key = titleFn(grade, lessonNum),
                    ).replacePlaceholders(grade, lessonNum)
                ),
                type = Uri.parse("http://schema.org/Game"),
                author = listOf(
                    ReadiumContributorStringValue("Mullah Nasruddin")
                ),
                identifier = Uri.parse(lessonBase.toString())
            ),
            images = listOf(
                ReadiumLink(
                    href = demoBase.resolve("static/$LEARNING_UNIT_ICON_NAME").toString(),
                    type = "image/png"
                )
            ),
            links = DemoConstants.LANGUAGE_CODES.filter { it != langCode }.map { otherLang ->
                ReadiumLink(
                    rel = listOf("alternate"),
                    href = demoBase.resolve(
                        "$otherLang/$GRADES_DIR_NAME/$grade/$LEARNING_UNITS_DIR_NAME/$lessonNum/$LESSON_MANIFEST_FILENAME"
                    ).toString(),
                    type = "application/opds-publication+json",
                    language = listOf(otherLang),
                )
            } + listOf(
                ReadiumLink(
                    rel = listOf("self"),
                    href = lessonBase.resolve(LESSON_MANIFEST_FILENAME).toString(),
                    type = "application/opds-publication+json"
                ),
                ReadiumLink(
                    rel = listOf("https://id.openeel.org/rel/tincanxml"),
                    href = lessonBase.resolve("tincan.xml").toString(),
                    type = "application/xml"
                ),
                ReadiumLink(
                    rel = listOf("https://id.openeel.org/rel/launchable-app"),
                    href = demoBase.resolve("$langCode/$APP_MANIFEST_FILENAME").toString(),
                    type = "application/opds-publication+json"
                )
            ),
            resources = listOf(
                ReadiumLink(href = lessonBase.resolve(LEARNING_UNIT_HTML_FILENAME).toString()),
                ReadiumLink(href = demoBase.resolve("static/$LEARNING_UNIT_JS_FILENAME").toString()),
                ReadiumLink(href = demoBase.resolve("static/$XAPI_MODULE_FILENAME").toString()),
                ReadiumLink(href = demoBase.resolve("static/$LEARNING_UNIT_ICON_NAME").toString()),
            ),
        )
    }


    companion object {

        const val LESSON_MANIFEST_FILENAME = "manifest.json"

        val LEARNING_UNIT_TITLE_FN: (gradeNum: Int, lessonNum: Int) -> String = { gradeNum, lessonNum ->
            if(gradeNum == DemoConstants.APP_ONLY_GRADE) {
                "mobile_app_only_lesson_num"
            }else {
                "lesson_grade"
            }
        }


    }
}