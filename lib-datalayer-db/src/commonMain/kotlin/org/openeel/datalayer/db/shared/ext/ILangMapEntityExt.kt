package org.openeel.datalayer.db.shared.ext

import org.openeel.datalayer.db.shared.entities.ILangMapEntity

val ILangMapEntity.langMapKey: String
    get() = if(region != null) {
        "$lang-$region"
    }else {
        lang
    }