package org.openeel.datalayer.db.shared.ext

import org.openeel.datalayer.db.shared.entities.LangMapEntity

val LangMapEntity.langMapKey: String
    get() = if(lmeRegion != null) {
        "$lmeLang-$lmeRegion"
    }else {
        lmeLang
    }