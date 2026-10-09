package world.respect.shared.ext

import io.ktor.http.Url
import world.respect.datalayer.school.model.Clazz
import world.respect.lib.xapi.model.XapiAccount
import world.respect.lib.xapi.model.XapiGroup
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.libutil.ext.appendEndpointSegments

fun Clazz.activityId(
    schoolUrl: Url
): String {
    return schoolUrl.appendEndpointSegments("classes", guid).toString()
}

fun Clazz.studentsXapiGroup(
    schoolUrl: Url
): XapiGroup {
    return XapiGroup(
        //This needs localized: however we don't currently have localization wrappers for formatted strings

        name = "$title students",
        account = XapiAccount(
            homePage = activityId(schoolUrl),
            name = "students"
        ),
        objectType = XapiObjectType.Group,
    )
}
