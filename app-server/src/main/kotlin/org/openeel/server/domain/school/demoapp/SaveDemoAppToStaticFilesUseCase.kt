package world.respect.server.domain.school.demoapp

import io.ktor.http.Url
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML
import org.openeel.demo.demolaunchableappserver.DemoConstants
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlDocument
import world.respect.server.domain.school.demoapp.MakeDemoAppCollectionUseCase.Companion.GRADE_ICON_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.GRADES_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.LEARNING_UNITS_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.LEARNING_UNIT_ICON_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppLearningUnitHtmlUseCase.Companion.LEARNING_UNIT_HTML_FILENAME
import world.respect.server.domain.school.demoapp.MakeDemoAppLearningUnitHtmlUseCase.Companion.LEARNING_UNIT_JS_FILENAME
import world.respect.server.domain.school.demoapp.MakeDemoAppLearningUnitHtmlUseCase.Companion.XAPI_MODULE_FILENAME
import world.respect.server.domain.school.demoapp.MakeDemoAppManifestUseCase.Companion.APP_MANIFEST_FILENAME
import java.io.File

/**
 * Save the demo app to static files in a given directory (e.g. to be served using a static file
 * server such as Apache)
 */
class SaveDemoAppToStaticFilesUseCase(
    private val makeDemoAppManifestUseCase: MakeDemoAppManifestUseCase,
    private val makeDemoAppCollectionUseCase: MakeDemoAppCollectionUseCase,
    private val makeDemoAppGradeCollectionsUseCase: MakeDemoAppGradeCollectionsUseCase,
    private val makeDemoAppLearningUnitManifestUseCase: MakeDemoAppLearningUnitManifestUseCase,
    private val makeDemoAppLearningUnitTinCanXmlUseCase: MakeDemoAppLearningUnitTinCanXmlUseCase,
    private val makeDemoAppLearningUnitHtmlUseCase: MakeDemoAppLearningUnitHtmlUseCase,
    private val json: Json,
    private val xml: XML,
) {

    operator fun invoke(
        destDir: File,
        baseUrl: Url,
        languages: List<String> = DemoConstants.LANGUAGE_CODES,
    ) {
        destDir.takeIf { !it.exists() }?.mkdirs()
        val staticDir = File(destDir, "static")
        staticDir.mkdirs()

        listOf(
            GRADE_ICON_NAME, LEARNING_UNIT_ICON_NAME, LEARNING_UNIT_JS_FILENAME, XAPI_MODULE_FILENAME
        ).forEach { resourceName ->
            File(staticDir, resourceName).writeBytes(
                this::class.java.getResourceAsStream("/demoapp/$resourceName")!!.readBytes()
            )
        }

        File(destDir, MakeDemoAppManifestUseCase.APP_MANIFEST_ICON_NAME).writeBytes(
            this::class.java.getResourceAsStream("/demoapp/app_icon.png")!!.readBytes()
        )

        File(destDir, "index.html").writeBytes(
            this::class.java.getResourceAsStream("/demoapp/index.html")!!.readBytes()
        )

        languages.forEach { lang ->
            val langDir = File(destDir, lang).also { it.mkdirs() }

            File(langDir, APP_MANIFEST_FILENAME).writeText(
                json.encodeToString(
                    makeDemoAppManifestUseCase(
                        baseUrl = baseUrl,
                        langCode = lang,
                    )
                )
            )

            File(langDir, MakeDemoAppCollectionUseCase.DEFAULT_COLLECTION_NAME).writeText(
                json.encodeToString(
                    makeDemoAppCollectionUseCase(
                        baseUrl = baseUrl, langCode = lang
                    )
                )
            )

            val gradesDir = File(langDir, GRADES_DIR_NAME).also { it.mkdirs() }
            (1..DemoConstants.NUM_LESSONS).forEach { gradeNum ->
                val gradeDir = File(gradesDir, gradeNum.toString()).also {
                    it.mkdirs()
                }

                File(gradeDir, MakeDemoAppGradeCollectionsUseCase.COLLECTION_FILE_NAME).writeText(
                    json.encodeToString(
                        makeDemoAppGradeCollectionsUseCase(
                            baseUrl = baseUrl,
                            gradeNum = gradeNum,
                            langCode = lang,
                        )
                    )
                )

                val lessonsDir = File(gradeDir, LEARNING_UNITS_DIR_NAME).also { it.mkdirs() }
                (1..DemoConstants.NUM_LESSONS).forEach { lessonNum ->
                    val lessonDir = File(lessonsDir, lessonNum.toString()).also { it.mkdirs() }

                    File(
                        lessonDir, MakeDemoAppLearningUnitManifestUseCase.LESSON_MANIFEST_FILENAME
                    ).writeText(
                        json.encodeToString(
                            makeDemoAppLearningUnitManifestUseCase(
                                demoBase = baseUrl,
                                grade = gradeNum,
                                lessonNum = lessonNum,
                                langCode = lang,
                            )
                        )
                    )

                    File(lessonDir, "tincan.xml").writeText(
                        xml.encodeToString(
                            TinCanXmlDocument.serializer(),
                            makeDemoAppLearningUnitTinCanXmlUseCase(
                                baseUrl = baseUrl,
                                gradeNum = gradeNum,
                                lessonNum = lessonNum,
                                langCode = lang,
                            )
                        )
                    )

                    File(lessonDir, LEARNING_UNIT_HTML_FILENAME).writeText(
                        makeDemoAppLearningUnitHtmlUseCase(
                            baseUrl = baseUrl,
                            gradeNum = gradeNum,
                            lessonNum = lessonNum,
                            langCode = lang,
                        )
                    )
                }
            }
        }



    }
}