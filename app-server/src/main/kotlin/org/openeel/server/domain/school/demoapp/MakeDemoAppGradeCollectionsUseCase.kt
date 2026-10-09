package world.respect.server.domain.school.demoapp

import com.eygraber.uri.Uri
import io.ktor.http.Url
import org.openeel.demo.demolaunchableappserver.DemoConstants
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsFeedMetadata
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.ReadiumContributorStringValue
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ReadiumMetadata
import world.respect.libutil.ext.resolve
import world.respect.server.domain.school.demoapp.MakeDemoAppCollectionUseCase.Companion.DEMO_GRADE_TITLE_FN
import world.respect.server.domain.school.demoapp.MakeDemoAppLearningUnitManifestUseCase.Companion.LEARNING_UNIT_TITLE_FN
import world.respect.server.domain.school.demoapp.MakeDemoAppLearningUnitManifestUseCase.Companion.LESSON_MANIFEST_FILENAME

class MakeDemoAppGradeCollectionsUseCase(
    private val demoStrings: DemoStringMaps,
) {

    operator fun invoke(
        baseUrl: Url,
        gradeNum: Int,
        langCode: String,
        titleFn: (Int) -> String = DEMO_GRADE_TITLE_FN,
        publicationTitle: (gradeNum: Int, lessonNum: Int) -> String = LEARNING_UNIT_TITLE_FN,
    ) : OpdsFeed {
        return OpdsFeed(
            metadata = OpdsFeedMetadata(
                title = demoStrings.requireString(
                    lang = langCode,
                    key = titleFn(gradeNum)
                ).replacePlaceholders(gradeNum, -1)
            ),
            links = listOf(
                ReadiumLink(
                    href = baseUrl.resolve("$langCode/$GRADES_DIR_NAME/$gradeNum/$COLLECTION_FILE_NAME").toString(),
                    rel = listOf("self"),
                    type = "application/json"
                ),
                ReadiumLink(
                    rel = listOf("icon"),
                    href = baseUrl.resolve("static/grade.jpg").toString(),
                    type = "image/png",
                ),
            ),
            publications = (1..DemoConstants.NUM_LESSONS).map { lessonNum ->
                val lessonBase = baseUrl.resolve(
                    "$langCode/$GRADES_DIR_NAME/$gradeNum/$LEARNING_UNITS_DIR_NAME/$lessonNum/"
                )
                Publication(
                    metadata = ReadiumMetadata(
                        title = LangMapStringValue(
                            demoStrings.requireString(
                                lang = langCode,
                                key = publicationTitle(gradeNum, lessonNum)
                            ).replacePlaceholders(gradeNum, lessonNum)
                        ),
                        type = Uri.parse("http://schema.org/Game"),
                        author = listOf(
                            ReadiumContributorStringValue("Mullah Nasruddin")
                        ),
                        identifier = Uri.parse(lessonBase.toString())
                    ),
                    links = listOf(
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
                            href = baseUrl.resolve(MakeDemoAppManifestUseCase.APP_MANIFEST_FILENAME).toString(),
                            type = "application/opds-publication+json"
                        )
                    ),
                    images = listOf(
                        ReadiumLink(
                            href = baseUrl.resolve("static/$LEARNING_UNIT_ICON_NAME").toString(),
                            type = "image/png"
                        )
                    ),
                )
            }
        )
    }

    companion object {

        const val GRADES_DIR_NAME = "grade"

        const val LEARNING_UNITS_DIR_NAME = "learningunits"

        const val COLLECTION_FILE_NAME = "index.json"

        const val LEARNING_UNIT_ICON_NAME = "books.png"
    }

}