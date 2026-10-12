package org.openeel.datalayer.db.school.xapi.ext

import org.openeel.datalayer.db.school.xapi.entities.XapiActivityLangMapEntry

fun List<XapiActivityLangMapEntry>.toLangMap(
    predicate: (XapiActivityLangMapEntry) -> Boolean
): Map<String, String> {
    return this.filter(predicate).associate {
        it.almeLangCode to it.almeValue
    }
}