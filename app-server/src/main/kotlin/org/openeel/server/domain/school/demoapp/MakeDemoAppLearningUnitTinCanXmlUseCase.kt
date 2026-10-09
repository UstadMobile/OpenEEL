package world.respect.server.domain.school.demoapp

import io.ktor.http.Url
import org.openeel.demo.demolaunchableappserver.DemoConstants
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlActivities
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlActivity
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlDocument
import world.respect.lib.xapi.rusticilaunch.model.TinCanXmlLaunch
import world.respect.libutil.ext.resolve
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.GRADES_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase.Companion.LEARNING_UNITS_DIR_NAME
import world.respect.server.domain.school.demoapp.MakeDemoAppLearningUnitHtmlUseCase.Companion.LEARNING_UNIT_HTML_FILENAME

class MakeDemoAppLearningUnitTinCanXmlUseCase(
    private val demoStrings: DemoStringMaps,
) {

    operator fun invoke(
        baseUrl: Url,
        gradeNum: Int,
        lessonNum: Int,
        langCode: String,
        demoAppPackage: String = DEMO_APP_PACKAGE,
        useIntentUrl: Boolean = gradeNum == DemoConstants.APP_ONLY_GRADE,
        titleFn: (Int, Int) -> String = MakeDemoAppLearningUnitManifestUseCase.LEARNING_UNIT_TITLE_FN,
    ): TinCanXmlDocument {
        val lessonPath = "$langCode/$GRADES_DIR_NAME/$gradeNum/$LEARNING_UNITS_DIR_NAME/$lessonNum/"

        return TinCanXmlDocument(
            activities = TinCanXmlActivities(
                listOf(
                    TinCanXmlActivity(
                        id = baseUrl.resolve(lessonPath).toString(),
                        type = "http://activitystrea.ms/schema/1.0/game",
                        name = demoStrings.requireString(
                            lang = langCode,
                            key = titleFn(gradeNum, lessonNum),
                        ).replacePlaceholders(gradeNum, lessonNum),
                        launch = TinCanXmlLaunch(
                            lang = langCode,
                            value = if(useIntentUrl) {
                                "intent://demo.openeel.org/grade/$gradeNum/learningunits/$lessonNum/learningunit.html#Intent;scheme=https;category=android.intent.category.BROWSABLE;package=$DEMO_APP_PACKAGE;end"
                            }else {
                                LEARNING_UNIT_HTML_FILENAME
                            }
                        )
                    )
                )
            )
        )
    }

    companion object {

        const val DEMO_APP_PACKAGE = "org.openeel.demolaunchableapp"
    }


}