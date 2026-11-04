package org.openeel.shared.ext

import io.ktor.http.Url
import org.openeel.datalayer.school.model.Clazz
import org.openeel.lib.xapi.model.XapiAccount
import org.openeel.lib.xapi.model.XapiGroup
import org.openeel.lib.xapi.model.XapiObjectType
import org.openeel.libutil.ext.appendEndpointSegments

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
